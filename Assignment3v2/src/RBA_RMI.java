import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RBA_RMI extends Remote {
    void receive(Message m) throws RemoteException;
}
