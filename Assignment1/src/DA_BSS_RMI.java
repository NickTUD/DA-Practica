import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface which represents the methods that can be invoked by other objects
 */
public interface DA_BSS_RMI extends Remote {

    /**
     * Method that broadcasts a message to other methods
     * Content of the message can be of any type, we assume text for now.
     */
    void broadcast(Message m) throws RemoteException;

    /**
     * Method that receives a message, consisting of content, logical clock and receiver information.
     */
    void receive(Message m) throws RemoteException;

    /**
     * Method that delivers a message if it is able to deliver it.
     */
    void deliver(Message m) throws RemoteException;
}
