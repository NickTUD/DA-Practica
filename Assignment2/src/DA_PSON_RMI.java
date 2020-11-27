import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface which represents the methods that can be invoked by other objects
 */
public interface DA_PSON_RMI extends Remote {
    void receive(int id, boolean singleN) throws RemoteException;

    void performElectionRound() throws RemoteException;

    /**
     * Method that broadcasts a message to other methods
     * Content of the message can be of any type, we assume text for now.
     */
    //void broadcast(String text) throws RemoteException;

    /**
     * Method that receives a message, consisting of content, logical clock and receiver information.
     */
    //void receive(Message msg) throws RemoteException;

    /**
     * Method that delivers a message if it is able to deliver it.
     */
    //void deliver(Message msg) throws RemoteException;
}
