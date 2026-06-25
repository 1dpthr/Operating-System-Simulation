import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteProcessService extends Remote {

    String createRemoteProcess(String name, int arrival, int burst, String owner) throws RemoteException;

    String getQueueSummary() throws RemoteException;
}
