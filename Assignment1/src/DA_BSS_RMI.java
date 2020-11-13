import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 */
public interface DA_BSS_RMI extends Remote {

    /**
     */
    void broadcast(Message m) throws RemoteException;

    /**
     */
    void receive(Message m) throws RemoteException;

    /**
     */
    void deliver(Message m) throws RemoteException;
}
