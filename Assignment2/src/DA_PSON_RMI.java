import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface which represents the methods that can be invoked by other objects
 */
public interface DA_PSON_RMI extends Remote {

    /**
     *
     * @param id
     * @param singleN
     * @throws RemoteException
     */
    void receive(int id, boolean singleN) throws RemoteException;

    void performElectionRound() throws RemoteException;

}
