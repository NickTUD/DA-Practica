import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for all the remote callable methods.
 */
public interface RBA_RMI extends Remote {
    void receive(Message m) throws RemoteException;
}
