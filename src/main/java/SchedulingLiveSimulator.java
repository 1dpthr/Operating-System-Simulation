import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.Timer;

/**
 * Step-by-step live CPU scheduling demo for all algorithms used in Algorithm Simulation.
 */
public final class SchedulingLiveSimulator {

    public interface Listener {
        void onTick(int time, String message);

        void onFinished(String summary);
    }

    private Timer timer;
    private final List<ProcessControlBlock> processes = new ArrayList<>();
    private final Map<Integer, Integer> remainingBurst = new HashMap<>();
    private final Queue<Integer> rrQueue = new LinkedList<>();
    private int currentTime;
    private int currentPid = -1;
    private int quantumUsed;
    private int timeQuantum;
    private String algorithm = "FCFS";
    private Listener listener;

    public void start(String algorithm, Listener listener) {
        stop();
        this.algorithm = algorithm == null ? "FCFS" : algorithm.trim();
        this.listener = listener;
        this.timeQuantum = KernelConfig.getInstance().getTimeQuantum();
        processes.clear();
        remainingBurst.clear();
        rrQueue.clear();
        currentTime = 0;
        currentPid = -1;
        quantumUsed = 0;

        ProcessRegistry reg = ProcessRegistry.getInstance();
        for (ProcessControlBlock pcb : reg.getAll()) {
            if (pcb.getState() == ProcessControlBlock.ProcessState.TERMINATED) {
                continue;
            }
            if (pcb.getState() == ProcessControlBlock.ProcessState.RUNNING) {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
            }
            processes.add(pcb);
            remainingBurst.put(pcb.getProcessId(), pcb.getBurstTime());
        }

        timer = new Timer(700, e -> tick());
        timer.start();
        notifyTick("Live demo started — " + this.algorithm + " (" + processes.size() + " process(es)).");
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void tick() {
        if (processes.isEmpty()) {
            finish("No processes to simulate.");
            return;
        }

        enqueueArrivals();

        if (isRoundRobin()) {
            tickRoundRobin();
            return;
        }

        if (isPreemptive()) {
            currentPid = selectPidPreemptive();
        } else if (currentPid < 0 || remainingBurst.getOrDefault(currentPid, 0) <= 0
                || !isRunnable(findPcb(currentPid))) {
            currentPid = selectPidNonPreemptive();
        }

        if (currentPid < 0) {
            currentTime++;
            notifyTick("CPU idle at t=" + currentTime);
            if (allDone()) {
                finish("Live " + algorithm + " complete. Total time: " + currentTime);
            }
            return;
        }

        runSlice(currentPid, 1, false);
    }

    private void tickRoundRobin() {
        enqueueArrivals();

        if (rrQueue.isEmpty() && currentPid < 0) {
            currentTime++;
            notifyTick("CPU idle at t=" + currentTime);
            if (allDone()) {
                finish("Live ROUND ROBIN complete. Total time: " + currentTime);
            }
            return;
        }

        if (currentPid < 0 || quantumUsed >= timeQuantum
                || remainingBurst.getOrDefault(currentPid, 0) <= 0) {
            if (currentPid >= 0 && remainingBurst.getOrDefault(currentPid, 0) > 0) {
                rrQueue.add(currentPid);
            }
            Integer next = rrQueue.poll();
            currentPid = next == null ? -1 : next;
            quantumUsed = 0;
        }

        if (currentPid < 0) {
            return;
        }

        int slice = Math.min(timeQuantum - quantumUsed, remainingBurst.getOrDefault(currentPid, 0));
        if (slice <= 0) {
            currentPid = -1;
            return;
        }
        quantumUsed += slice;
        runSlice(currentPid, slice, true);
    }

    private void runSlice(int pid, int slice, boolean roundRobin) {
        demoteAllRunning();
        ProcessControlBlock running = findPcb(pid);
        if (running == null || !isRunnable(running)) {
            currentPid = -1;
            return;
        }

        if (currentTime < running.getArrivalTime()) {
            currentTime++;
            notifyTick("CPU idle until t=" + currentTime);
            return;
        }

        running.captureCpuContext();
        running.setState(ProcessControlBlock.ProcessState.RUNNING);

        int left = remainingBurst.getOrDefault(pid, 0) - slice;
        remainingBurst.put(pid, left);
        currentTime += slice;

        if (left <= 0) {
            running.setState(ProcessControlBlock.ProcessState.TERMINATED);
            notifyTick("t=" + currentTime + ": P" + pid + " finished (" + algorithm + ").");
            currentPid = -1;
            quantumUsed = 0;
            rrQueue.removeIf(id -> id == pid);
            ProcessRegistry.syncViews();
            if (allDone()) {
                finish("Live " + algorithm + " complete. Total time: " + currentTime);
            }
        } else {
            notifyTick("t=" + currentTime + ": P" + pid + " running (" + left + " burst left).");
            if (roundRobin && quantumUsed >= timeQuantum) {
                rrQueue.add(pid);
                currentPid = -1;
                quantumUsed = 0;
            }
            ProcessRegistry.syncViews();
        }
    }

    private void enqueueArrivals() {
        if (!isRoundRobin()) {
            return;
        }
        for (ProcessControlBlock pcb : processes) {
            int pid = pcb.getProcessId();
            if (currentTime >= pcb.getArrivalTime()
                    && remainingBurst.getOrDefault(pid, 0) > 0
                    && isRunnable(pcb)
                    && !rrQueue.contains(pid)
                    && pid != currentPid) {
                rrQueue.add(pid);
            }
        }
    }

    private int selectPidNonPreemptive() {
        ProcessControlBlock best = null;
        for (ProcessControlBlock pcb : processes) {
            int pid = pcb.getProcessId();
            if (remainingBurst.getOrDefault(pid, 0) <= 0 || !isRunnable(pcb)) {
                continue;
            }
            if (currentTime < pcb.getArrivalTime()) {
                continue;
            }
            if (best == null || betterCandidate(pcb, best)) {
                best = pcb;
            }
        }
        return best == null ? -1 : best.getProcessId();
    }

    private int selectPidPreemptive() {
        return selectPidNonPreemptive();
    }

    private boolean betterCandidate(ProcessControlBlock a, ProcessControlBlock b) {
        String algo = algorithm.toUpperCase();
        if (algo.contains("SJF")) {
            int cmp = Integer.compare(remainingBurst.getOrDefault(a.getProcessId(), 0),
                    remainingBurst.getOrDefault(b.getProcessId(), 0));
            if (cmp != 0) {
                return cmp < 0;
            }
        } else if (algo.contains("PRIORITY")) {
            int cmp = Integer.compare(a.getPriority(), b.getPriority());
            if (cmp != 0) {
                return cmp < 0;
            }
        } else {
            int cmp = Integer.compare(a.getArrivalTime(), b.getArrivalTime());
            if (cmp != 0) {
                return cmp < 0;
            }
        }
        return a.getProcessId() < b.getProcessId();
    }

    private ProcessControlBlock findPcb(int pid) {
        return ProcessRegistry.getInstance().find(pid).orElse(null);
    }

    private boolean isRunnable(ProcessControlBlock pcb) {
        if (pcb == null) {
            return false;
        }
        return pcb.getState() != ProcessControlBlock.ProcessState.BLOCKED
                && pcb.getState() != ProcessControlBlock.ProcessState.SUSPENDED
                && pcb.getState() != ProcessControlBlock.ProcessState.TERMINATED;
    }

    private boolean isPreemptive() {
        String algo = algorithm.toUpperCase();
        return algo.contains("PREEMPTIVE") || algo.equals("ROUND ROBIN");
    }

    private boolean isRoundRobin() {
        return "ROUND ROBIN".equalsIgnoreCase(algorithm);
    }

    private void demoteAllRunning() {
        for (ProcessControlBlock pcb : ProcessRegistry.getInstance().getByState(
                ProcessControlBlock.ProcessState.RUNNING)) {
            if (pcb.getState() != ProcessControlBlock.ProcessState.TERMINATED) {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
            }
        }
    }

    private boolean allDone() {
        for (ProcessControlBlock pcb : processes) {
            int pid = pcb.getProcessId();
            if (remainingBurst.getOrDefault(pid, 0) > 0 && isRunnable(pcb)) {
                return false;
            }
        }
        return true;
    }

    private void notifyTick(String msg) {
        if (listener != null) {
            listener.onTick(currentTime, msg);
        }
    }

    private void finish(String summary) {
        stop();
        if (listener != null) {
            listener.onFinished(summary);
        }
    }
}
