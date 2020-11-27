import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface which represents the methods that can be invoked by other objects
 */
public interface DA_PSON_RMI extends Remote {

    /**
     * Method that is invoked on an object when a message is received.
     * When an object X wants to send to object Y, object X should invoke this message on object Y
     *
     * @throws RemoteException when there is something wrong with RMI
     */
    void receive(int id, boolean singleN) throws RemoteException;

    /**
     * Helper method that is invoked on another object when the current object is passive.
     *
     * @throws RemoteException when there is something wrong with RMI
     */
    void performElectionRound() throws RemoteException;

}
