import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RBYZ_RMI extends Remote {

    /**
     * Method that is invoked on an object when a message is received.
     * When an object X wants to send to object Y, object X should invoke this message on object Y
     *
     * @throws RemoteException when there is something wrong with RMI
     */
    void receive(Message msg) throws RemoteException;


}
