import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DA_BSS_Process extends UnicastRemoteObject implements DA_BSS_RMI {

    private int[] clock;
    private Set<Message> buffer = new HashSet<Message>();
    private int index;
    private ArrayList<String> ipList;

    public DA_BSS_Process(int index, ArrayList<String> newIpList) throws RemoteException {
        this.index = index;
        ipList = newIpList;
    }

    private void updateClock(int index){
        clock[index] += 1;
    }

    @Override
    public void broadcast(Message m) throws RemoteException {
        System.out.println(index);

    }

    @Override
    public void receive(Message m) throws RemoteException {

    }

    @Override
    public void deliver(Message m) throws RemoteException {

    }
}
