import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Logger_RMI extends Remote {

    /**
     * Method that is invoked when an object wants to log something.
     * @throws RemoteException when there is something wrong with RMI
     */
    void log(String text) throws RemoteException;


}