import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProcessControlBlock {

    public enum ProcessState {
        NEW, READY, RUNNING, BLOCKED, SUSPENDED, TERMINATED
    }

    private static final Random ID_GENERATOR = new Random();

    private final int processId;
    private ProcessState state;
    private String owner;
    private int priority;
    private Integer parentId;
    private final List<Integer> childProcessIds;
    private int memoryRequirementKb;
    private String allocatedMemoryPointer;
    private final int[] cpuRegisters;
    private int processorId;
    private String ioStateInfo;
    private String processName;
    private int arrivalTime;
    private int burstTime;

    public ProcessControlBlock(String processName, String owner, int arrivalTime, int burstTime, int memoryRequirementKb) {
        this(0, processName, owner, arrivalTime, burstTime, memoryRequirementKb, 2, ProcessState.NEW);
    }

    /** Restore a process from file storage (explicit PID). */
    public ProcessControlBlock(int processId, String processName, String owner, int arrivalTime,
            int burstTime, int memoryRequirementKb, int priority, ProcessState state) {
        this.processId = processId > 0 ? processId : ID_GENERATOR.nextInt(9000) + 1000;
        this.processName = processName;
        this.owner = owner;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.state = ProcessState.NEW;
        this.priority = priority;
        this.parentId = null;
        this.childProcessIds = new ArrayList<>();
        this.memoryRequirementKb = Math.max(1, memoryRequirementKb);
        this.allocatedMemoryPointer = "NULL";
        this.cpuRegisters = new int[8];
        this.processorId = -1;
        this.ioStateInfo = "Idle";
        setState(state);
    }

    public int getProcessId() {
        return processId;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
        switch (state) {
            case RUNNING -> processorId = 0;
            case READY -> processorId = -1;
            case BLOCKED, SUSPENDED -> {
                processorId = -1;
                ioStateInfo = state == ProcessState.BLOCKED ? "Waiting I/O" : "Swapped Out";
            }
            case TERMINATED -> {
                processorId = -1;
                allocatedMemoryPointer = "FREED";
            }
            default -> {
            }
        }
    }

    public String getStateLabel() {
        return state.name().charAt(0) + state.name().substring(1).toLowerCase();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getPriorityLabel() {
        return switch (priority) {
            case 1 -> "High";
            case 2 -> "Medium";
            default -> "Low";
        };
    }

    public static int priorityFromLabel(String label) {
        if (label == null) {
            return 2;
        }
        return switch (label.trim().toLowerCase()) {
            case "high" -> 1;
            case "low" -> 3;
            default -> 2;
        };
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public List<Integer> getChildProcessIds() {
        return childProcessIds;
    }

    public void addChild(int childId) {
        if (!childProcessIds.contains(childId)) {
            childProcessIds.add(childId);
        }
    }

    public int getMemoryRequirementKb() {
        return memoryRequirementKb;
    }

    public void setMemoryRequirementKb(int memoryRequirementKb) {
        this.memoryRequirementKb = memoryRequirementKb;
    }

    public String getAllocatedMemoryPointer() {
        return allocatedMemoryPointer;
    }

    public void setAllocatedMemoryPointer(String allocatedMemoryPointer) {
        this.allocatedMemoryPointer = allocatedMemoryPointer;
    }

    public int[] getCpuRegisters() {
        return cpuRegisters;
    }

    public int getProcessorId() {
        return processorId;
    }

    public void setProcessorId(int processorId) {
        this.processorId = processorId;
    }

    public String getIoStateInfo() {
        return ioStateInfo;
    }

    public void setIoStateInfo(String ioStateInfo) {
        this.ioStateInfo = ioStateInfo;
    }

    
    public void captureCpuContext() {
        java.util.Random rng = new java.util.Random(processId * 31L + System.nanoTime());
        for (int i = 0; i < cpuRegisters.length; i++) {
            cpuRegisters[i] = rng.nextInt(999);
        }
    }

    public String getProcessName() {
        return processName;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public Object[] toTableRow() {
        return new Object[]{
            processId,
            processName,
            arrivalTime,
            burstTime,
            getStateLabel(),
            getPriorityLabel()
        };
    }

    public Object[] toPcbDetailRow() {
        return new Object[]{
            processId,
            getStateLabel(),
            owner,
            getPriorityLabel(),
            parentId == null ? "—" : parentId,
            childProcessIds.isEmpty() ? "—" : childProcessIds.toString(),
            memoryRequirementKb + " KB (" + PageTableManager.getInstance().getPageTableSummary(processId) + ")",
            allocatedMemoryPointer,
            registerSummary(),
            processorId < 0 ? "—" : "CPU-" + processorId,
            ioStateInfo
        };
    }

    private String registerSummary() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < cpuRegisters.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("R").append(i).append("=").append(cpuRegisters[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
