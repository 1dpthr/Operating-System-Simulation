import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class QueueServiceImpl extends UnicastRemoteObject implements QueueService {

    public static final int RMI_PORT = 1099;
    public static final String BIND_NAME = "QueueService";

    protected QueueServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String getQueues() throws RemoteException {
        ProcessRegistry reg = ProcessRegistry.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("Ready Queue: ").append(formatList(
                reg.getByState(ProcessControlBlock.ProcessState.READY))).append('\n');
        sb.append("Blocked Queue: ").append(formatList(
                reg.getByState(ProcessControlBlock.ProcessState.BLOCKED))).append('\n');
        sb.append("Suspended Queue: ").append(formatList(
                reg.getByState(ProcessControlBlock.ProcessState.SUSPENDED))).append('\n');
        sb.append("Running: ").append(formatList(
                reg.getByState(ProcessControlBlock.ProcessState.RUNNING)));
        return sb.toString();
    }

    private static String formatList(List<ProcessControlBlock> list) {
        if (list.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            ProcessControlBlock p = list.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("P").append(p.getProcessId()).append("-").append(p.getProcessName());
        }
        return sb.toString();
    }
}
