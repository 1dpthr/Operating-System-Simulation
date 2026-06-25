import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SharedMemoryIPC {

    private static final SharedMemoryIPC INSTANCE = new SharedMemoryIPC();
    private final ConcurrentHashMap<String, StringBuilder> segments = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Integer>> attachedProcesses = new ConcurrentHashMap<>();

    private SharedMemoryIPC() {
    }

    public static SharedMemoryIPC getInstance() {
        return INSTANCE;
    }

    public boolean attach(String segmentName, int processId) {
        if (!ProcessRegistry.getInstance().find(processId).isPresent()) {
            return false;
        }
        MemorySharingManager.getInstance().shmat(segmentName, processId);
        String key = normalize(segmentName);
        segments.computeIfAbsent(key, k -> new StringBuilder());
        attachedProcesses.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(processId);
        return true;
    }

    public boolean isAttached(String segmentName, int processId) {
        Set<Integer> attached = attachedProcesses.get(normalize(segmentName));
        return attached != null && attached.contains(processId);
    }

    public String getAttachedSummary(String segmentName) {
        Set<Integer> attached = attachedProcesses.get(normalize(segmentName));
        if (attached == null || attached.isEmpty()) {
            return "(no processes attached)";
        }
        return attached.toString();
    }

    public boolean write(String segmentName, int processId, String data) {
        String key = normalize(segmentName);
        if (!isAttached(key, processId)) {
            return false;
        }
        MemorySharingManager.getInstance().shmWrite(key, processId, data);
        segments.computeIfAbsent(key, k -> new StringBuilder())
                .append("[P").append(processId).append("] ").append(data).append("\n");
        ProcessRegistry.getInstance().find(processId).ifPresent(
                pcb -> pcb.setIoStateInfo("SHM write: " + key));
        return true;
    }

    public String read(String segmentName, int processId) {
        String key = normalize(segmentName);
        if (!isAttached(key, processId)) {
            return null;
        }
        String fromManager = MemorySharingManager.getInstance().shmRead(key, processId);
        if (fromManager != null && !fromManager.startsWith("(empty")) {
            return fromManager;
        }
        StringBuilder sb = segments.get(key);
        return sb == null || sb.length() == 0 ? "(empty)" : sb.toString().trim();
    }

    public void clearSegment(String segmentName) {
        String key = normalize(segmentName);
        segments.remove(key);
        attachedProcesses.remove(key);
    }

    public void clearAll() {
        segments.clear();
        attachedProcesses.clear();
    }

    public Set<Integer> getAttachedPids(String segmentName) {
        Set<Integer> attached = attachedProcesses.get(normalize(segmentName));
        return attached == null ? Collections.emptySet() : Set.copyOf(attached);
    }

    private static String normalize(String segmentName) {
        return segmentName == null ? "" : segmentName.trim();
    }
}
