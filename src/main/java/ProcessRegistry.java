import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.File;

public final class ProcessRegistry {

    private static ProcessRegistry instance;
    private static Runnable pcbRefreshCallback;
    private final Map<Integer, ProcessControlBlock> processes = new LinkedHashMap<>();
    private boolean loadingFromFile;

    private ProcessRegistry() {
        loadingFromFile = true;
        ProcessStorage.loadInto(this);
        loadingFromFile = false;
    }

    public static synchronized ProcessRegistry getInstance() {
        if (instance == null) {
            instance = new ProcessRegistry();
            if (instance.count() > 0) {
                SharedTableModel.getInstance().reloadFrom(instance);
                ProcessPicker.notifyRefresh();
            }
        }
        return instance;
    }

    public static void setPcbRefreshCallback(Runnable callback) {
        pcbRefreshCallback = callback;
    }

    public static void syncViews() {
        ProcessRegistry registry = getInstance();
        SharedTableModel.getInstance().reloadFrom(registry);
        ProcessPicker.notifyRefresh();
        if (pcbRefreshCallback != null) {
            pcbRefreshCallback.run();
        }
        registry.persistIfNeeded();
    }

    private void persistIfNeeded() {
        if (!loadingFromFile) {
            ProcessStorage.save(this);
        }
    }

    void clearForReload() {
        processes.clear();
    }

    void registerLoaded(ProcessControlBlock pcb) {
        processes.put(pcb.getProcessId(), pcb);
        if (pcb.getState() != ProcessControlBlock.ProcessState.TERMINATED) {
            PageTableManager.getInstance().allocatePagesForProcess(
                    pcb.getProcessId(), pcb.getMemoryRequirementKb());
            pcb.setAllocatedMemoryPointer(
                    PageTableManager.getInstance().getMemoryPointer(pcb.getProcessId()));
        }
    }

    public boolean reloadFromFile() {
        loadingFromFile = true;
        int count = ProcessStorage.loadInto(this);
        loadingFromFile = false;
        syncViews();
        return count > 0;
    }

    public boolean saveToFile() {
        return ProcessStorage.save(this);
    }

    public static File getStorageFile() {
        return ProcessStorage.resolveFile();
    }

    public List<ProcessControlBlock> getActiveProcesses() {
        List<ProcessControlBlock> active = new ArrayList<>();
        for (ProcessControlBlock pcb : processes.values()) {
            if (pcb.getState() != ProcessControlBlock.ProcessState.TERMINATED) {
                active.add(pcb);
            }
        }
        return active;
    }

    public ProcessControlBlock create(String name, String owner, int arrival, int burst) {
        return create(name, owner, arrival, burst, burst * 64, null);
    }

    public ProcessControlBlock create(String name, String owner, int arrival, int burst, Integer parentId) {
        return create(name, owner, arrival, burst, burst * 64, parentId);
    }

    public ProcessControlBlock create(String name, String owner, int arrival, int burst, int memoryKb, Integer parentId) {
        return create(name, owner, arrival, burst, memoryKb, 2, parentId);
    }

    public ProcessControlBlock create(String name, String owner, int arrival, int burst, int memoryKb,
            int priority, Integer parentId) {
        ProcessControlBlock pcb = new ProcessControlBlock(name, owner, arrival, burst, memoryKb);
        pcb.setState(ProcessControlBlock.ProcessState.READY);
        pcb.setPriority(priority);
        if (parentId != null) {
            ProcessControlBlock parent = processes.get(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("Parent process P" + parentId + " not found.");
            }
            pcb.setParentId(parentId);
            parent.addChild(pcb.getProcessId());
        }
        processes.put(pcb.getProcessId(), pcb);
        PageTableManager.getInstance().allocatePagesForProcess(
                pcb.getProcessId(), pcb.getMemoryRequirementKb());
        pcb.setAllocatedMemoryPointer(PageTableManager.getInstance().getMemoryPointer(pcb.getProcessId()));
        syncViews();
        return pcb;
    }

    public Optional<ProcessControlBlock> find(int processId) {
        return Optional.ofNullable(processes.get(processId));
    }

    public boolean remove(int processId) {
        ProcessControlBlock pcb = processes.remove(processId);
        if (pcb == null) {
            return false;
        }
        if (pcb.getParentId() != null) {
            ProcessControlBlock parent = processes.get(pcb.getParentId());
            if (parent != null) {
                parent.getChildProcessIds().remove(Integer.valueOf(processId));
            }
        }
        for (Integer childId : new ArrayList<>(pcb.getChildProcessIds())) {
            processes.computeIfPresent(childId, (id, child) -> {
                child.setParentId(null);
                return child;
            });
        }
        PageTableManager.getInstance().freeProcess(processId);
        syncViews();
        return true;
    }

    public List<ProcessControlBlock> getAll() {
        return new ArrayList<>(processes.values());
    }

    public List<ProcessControlBlock> getByState(ProcessControlBlock.ProcessState state) {
        List<ProcessControlBlock> result = new ArrayList<>();
        for (ProcessControlBlock pcb : processes.values()) {
            if (pcb.getState() == state) {
                result.add(pcb);
            }
        }
        return result;
    }

    public int count() {
        return processes.size();
    }

    public void clear() {
        processes.clear();
    }

    public List<String> getQueueSummary() {
        List<String> lines = new ArrayList<>();
        appendQueue(lines, "Ready Queue", ProcessControlBlock.ProcessState.READY);
        appendQueue(lines, "Blocked Queue", ProcessControlBlock.ProcessState.BLOCKED);
        appendQueue(lines, "Suspended Queue", ProcessControlBlock.ProcessState.SUSPENDED);
        appendQueue(lines, "Running", ProcessControlBlock.ProcessState.RUNNING);
        return lines;
    }

    private void appendQueue(List<String> lines, String label, ProcessControlBlock.ProcessState state) {
        List<ProcessControlBlock> queue = getByState(state);
        if (queue.isEmpty()) {
            lines.add(label + ": (empty)");
        } else {
            StringBuilder sb = new StringBuilder(label).append(": ");
            for (int i = 0; i < queue.size(); i++) {
                ProcessControlBlock p = queue.get(i);
                if (i > 0) {
                    sb.append(" → ");
                }
                sb.append("P").append(p.getProcessId()).append("(").append(p.getProcessName()).append(")");
            }
            lines.add(sb.toString());
        }
    }

    public List<ProcessControlBlock> getUnmodifiableList() {
        return Collections.unmodifiableList(getAll());
    }
}
