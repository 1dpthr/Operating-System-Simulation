import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueueService extends Remote {

    String getQueues() throws RemoteException;
}
