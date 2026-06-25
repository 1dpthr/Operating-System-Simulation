import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Timer;

public final class FcfsLiveSimulator {

    public interface Listener {
        void onTick(int time, String message);

        void onFinished(String summary);
    }

    private Timer timer;
    private final List<ProcessControlBlock> order = new ArrayList<>();
    private final Map<Integer, Integer> remainingBurst = new HashMap<>();
    private int currentIndex = -1;
    private int currentTime;
    private Listener listener;

    public void start(Listener listener) {
        stop();
        this.listener = listener;
        ProcessRegistry reg = ProcessRegistry.getInstance();
        order.clear();
        remainingBurst.clear();
        currentTime = 0;
        currentIndex = -1;

        for (ProcessControlBlock pcb : reg.getAll()) {
            if (pcb.getState() == ProcessControlBlock.ProcessState.TERMINATED) {
                continue;
            }
            if (pcb.getState() == ProcessControlBlock.ProcessState.RUNNING) {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
            }
            order.add(pcb);
            remainingBurst.put(pcb.getProcessId(), pcb.getBurstTime());
        }
        order.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));

        timer = new Timer(700, e -> tick());
        timer.start();
        notifyTick("Live FCFS started — " + order.size() + " process(es) in simulation.");
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void tick() {
        if (order.isEmpty()) {
            finish("No processes to simulate.");
            return;
        }

        if (currentIndex < 0) {
            currentIndex = findNextReadyIndex();
            if (currentIndex < 0) {
                currentTime++;
                notifyTick("CPU idle at t=" + currentTime);
                if (allDone()) {
                    finish("Live FCFS complete. Total time: " + currentTime);
                }
                return;
            }
        }

        ProcessControlBlock running = order.get(currentIndex);
        if (currentTime < running.getArrivalTime()) {
            demoteAllRunning();
            currentTime++;
            notifyTick("CPU idle until t=" + currentTime);
            return;
        }

        demoteAllRunning();
        running.captureCpuContext();
        running.setState(ProcessControlBlock.ProcessState.RUNNING);

        int pid = running.getProcessId();
        int left = remainingBurst.getOrDefault(pid, 0) - 1;
        remainingBurst.put(pid, left);
        currentTime++;

        if (left <= 0) {
            running.setState(ProcessControlBlock.ProcessState.TERMINATED);
            notifyTick("t=" + currentTime + ": P" + pid + " finished (FCFS).");
            currentIndex = -1;
            ProcessRegistry.syncViews();
            if (allDone()) {
                finish("Live FCFS complete. Total time: " + currentTime);
            }
        } else {
            notifyTick("t=" + currentTime + ": P" + pid + " running (" + left + " burst left).");
            ProcessRegistry.syncViews();
        }
    }

    private void demoteAllRunning() {
        for (ProcessControlBlock pcb : ProcessRegistry.getInstance().getByState(ProcessControlBlock.ProcessState.RUNNING)) {
            if (pcb.getState() != ProcessControlBlock.ProcessState.TERMINATED) {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
            }
        }
    }

    private int findNextReadyIndex() {
        for (int i = 0; i < order.size(); i++) {
            ProcessControlBlock pcb = order.get(i);
            int pid = pcb.getProcessId();
            if (remainingBurst.getOrDefault(pid, 0) > 0
                    && pcb.getState() != ProcessControlBlock.ProcessState.BLOCKED
                    && pcb.getState() != ProcessControlBlock.ProcessState.SUSPENDED
                    && currentTime >= pcb.getArrivalTime()) {
                return i;
            }
        }
        return -1;
    }

    private boolean allDone() {
        for (ProcessControlBlock pcb : order) {
            if (remainingBurst.getOrDefault(pcb.getProcessId(), 0) > 0
                    && pcb.getState() != ProcessControlBlock.ProcessState.BLOCKED
                    && pcb.getState() != ProcessControlBlock.ProcessState.SUSPENDED) {
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
