import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class MultiLevelScheduler {

    public static final class PendingJob {
        public final String name;
        public final String owner;
        public final int arrival;
        public final int burst;
        public final int memoryKb;
        public final int priority;
        public final Integer existingPid;

        public PendingJob(String name, String owner, int arrival, int burst, int memoryKb, int priority) {
            this(name, owner, arrival, burst, memoryKb, priority, null);
        }

        public PendingJob(String name, String owner, int arrival, int burst, int memoryKb,
                int priority, Integer existingPid) {
            this.name = name;
            this.owner = owner;
            this.arrival = arrival;
            this.burst = burst;
            this.memoryKb = memoryKb;
            this.priority = priority;
            this.existingPid = existingPid;
        }

        public static PendingJob fromPcb(ProcessControlBlock pcb) {
            return new PendingJob(
                    pcb.getProcessName(),
                    pcb.getOwner(),
                    pcb.getArrivalTime(),
                    pcb.getBurstTime(),
                    pcb.getMemoryRequirementKb(),
                    pcb.getPriority(),
                    pcb.getProcessId());
        }

        @Override
        public String toString() {
            if (existingPid != null) {
                return "P" + existingPid + " - " + name;
            }
            return name + " (" + owner + ", A=" + arrival + ", B=" + burst + ", " + memoryKb + " KB)";
        }
    }

    public static class QueueState {
        public final String jobQueue;
        public final String readyQueue;
        public final String suspendQueue;
        public final String runningQueue;

        public QueueState(String job, String ready, String suspend, String running) {
            this.jobQueue = job;
            this.readyQueue = ready;
            this.suspendQueue = suspend;
            this.runningQueue = running;
        }
    }

    private static final Queue<PendingJob> jobQueue = new LinkedList<>();

    private MultiLevelScheduler() {
    }

    public static void enqueueJob(String name, String owner, int arrival, int burst, int memoryKb) {
        enqueueJob(name, owner, arrival, burst, memoryKb, 2);
    }

    public static void enqueueJob(String name, String owner, int arrival, int burst, int memoryKb, int priority) {
        jobQueue.add(new PendingJob(name, owner, arrival, burst, memoryKb, priority));
    }

    public static boolean enqueueFromRegistry(int pid) {
        return ProcessRegistry.getInstance().find(pid).map(pcb -> {
            for (PendingJob job : jobQueue) {
                if (pid == job.existingPid) {
                    return false;
                }
            }
            pcb.setState(ProcessControlBlock.ProcessState.NEW);
            jobQueue.add(PendingJob.fromPcb(pcb));
            ProcessRegistry.syncViews();
            return true;
        }).orElse(false);
    }

    public static boolean admitNextJob() {
        PendingJob job = jobQueue.poll();
        if (job == null) {
            return false;
        }
        if (job.existingPid != null) {
            ProcessRegistry.getInstance().find(job.existingPid).ifPresent(pcb -> {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
            });
            ProcessRegistry.syncViews();
            return true;
        }
        ProcessRegistry.getInstance().create(
                job.name, job.owner, job.arrival, job.burst, job.memoryKb, job.priority, null);
        return true;
    }

    public static boolean suspendProcess(int processId) {
        boolean changed = ProcessRegistry.getInstance().find(processId).map(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.READY
                    || pcb.getState() == ProcessControlBlock.ProcessState.RUNNING) {
                pcb.setState(ProcessControlBlock.ProcessState.SUSPENDED);
                return true;
            }
            return false;
        }).orElse(false);
        if (changed) {
            ProcessRegistry.syncViews();
        }
        return changed;
    }

    public static boolean resumeProcess(int processId) {
        boolean changed = ProcessRegistry.getInstance().find(processId).map(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.SUSPENDED) {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
                return true;
            }
            return false;
        }).orElse(false);
        if (changed) {
            ProcessRegistry.syncViews();
        }
        return changed;
    }

    public static boolean toggleSuspendResume(int processId) {
        return ProcessRegistry.getInstance().find(processId).map(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.SUSPENDED) {
                return resumeProcess(processId);
            }
            return suspendProcess(processId);
        }).orElse(false);
    }

    public static boolean dispatchNext(String algorithm) {
        ProcessRegistry reg = ProcessRegistry.getInstance();
        for (ProcessControlBlock running : new ArrayList<>(reg.getByState(ProcessControlBlock.ProcessState.RUNNING))) {
            running.setState(ProcessControlBlock.ProcessState.READY);
        }
        List<ProcessControlBlock> ready = new ArrayList<>(reg.getByState(ProcessControlBlock.ProcessState.READY));
        if (ready.isEmpty()) {
            return false;
        }
        ready.sort(dispatchComparator(algorithm));

        ProcessControlBlock next;
        if (isRoundRobinAlgorithm(algorithm)) {
            next = ready.get(roundRobinCursor % ready.size());
            roundRobinCursor = (roundRobinCursor + 1) % ready.size();
        } else {
            next = ready.get(0);
        }
        next.captureCpuContext();
        next.setState(ProcessControlBlock.ProcessState.RUNNING);
        ProcessRegistry.syncViews();
        return true;
    }

    private static int roundRobinCursor;

    private static boolean isRoundRobinAlgorithm(String algorithm) {
        return algorithm != null && algorithm.trim().equalsIgnoreCase("ROUND ROBIN");
    }

    private static Comparator<ProcessControlBlock> dispatchComparator(String algorithm) {
        String algo = algorithm == null ? "FCFS" : algorithm.trim().toUpperCase();
        if (algo.contains("SJF")) {
            return Comparator.comparingInt(ProcessControlBlock::getBurstTime)
                    .thenComparingInt(ProcessControlBlock::getArrivalTime)
                    .thenComparingInt(ProcessControlBlock::getProcessId);
        }
        if (algo.contains("PRIORITY")) {
            return Comparator.comparingInt(ProcessControlBlock::getPriority)
                    .thenComparingInt(ProcessControlBlock::getArrivalTime)
                    .thenComparingInt(ProcessControlBlock::getProcessId);
        }
        return Comparator.comparingInt(ProcessControlBlock::getArrivalTime)
                .thenComparingInt(ProcessControlBlock::getProcessId);
    }

    public static QueueState getQueueState() {
        ProcessRegistry reg = ProcessRegistry.getInstance();
        return new QueueState(
                formatJobQueue(),
                formatPcbQueue(reg.getByState(ProcessControlBlock.ProcessState.READY)),
                formatPcbQueue(reg.getByState(ProcessControlBlock.ProcessState.SUSPENDED)),
                formatPcbQueue(reg.getByState(ProcessControlBlock.ProcessState.RUNNING)));
    }

    private static String formatJobQueue() {
        if (jobQueue.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (PendingJob job : jobQueue) {
            if (i++ > 0) {
                sb.append(" → ");
            }
            sb.append(job.existingPid != null ? "P" + job.existingPid + "(" + job.name + ")" : job.name);
        }
        return sb.toString();
    }

    private static String formatPcbQueue(List<ProcessControlBlock> queue) {
        if (queue.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < queue.size(); i++) {
            ProcessControlBlock p = queue.get(i);
            if (i > 0) {
                sb.append(" → ");
            }
            sb.append("P").append(p.getProcessId()).append("(").append(p.getProcessName()).append(")");
        }
        return sb.toString();
    }

    public static void clearJobQueue() {
        jobQueue.clear();
    }
}
