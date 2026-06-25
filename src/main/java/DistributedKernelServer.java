import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DistributedKernelServer extends UnicastRemoteObject implements RemoteProcessService {

    public static final int RMI_PORT = 1099;
    public static final String BIND_NAME = "DistributedKernel";

    private static DistributedKernelServer instance;

    protected DistributedKernelServer() throws RemoteException {
        super();
    }

    public static synchronized String startServer() {
        try {
            if (instance == null) {
                instance = new DistributedKernelServer();
            }
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(RMI_PORT);
            } catch (RemoteException ex) {
                registry = LocateRegistry.getRegistry(RMI_PORT);
            }
            registry.rebind(BIND_NAME, instance);
            return "RMI server listening on port " + RMI_PORT + " as '" + BIND_NAME + "'";
        } catch (Exception ex) {
            return "RMI server failed: " + ex.getMessage();
        }
    }

    @Override
    public String createRemoteProcess(String name, int arrival, int burst, String owner) throws RemoteException {
        ProcessControlBlock pcb = ProcessRegistry.getInstance().create(name, owner, arrival, burst);
        pcb.captureCpuContext();
        PageTableManager.getInstance().allocatePagesForProcess(pcb.getProcessId(), pcb.getMemoryRequirementKb());
        pcb.setAllocatedMemoryPointer(PageTableManager.getInstance().getMemoryPointer(pcb.getProcessId()));
        return "Created remote process P" + pcb.getProcessId() + " (" + name + ")";
    }

    @Override
    public String getQueueSummary() throws RemoteException {
        return String.join("\n", ProcessRegistry.getInstance().getQueueSummary());
    }
}
