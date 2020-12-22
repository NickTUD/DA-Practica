import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RBA_Process extends UnicastRemoteObject implements RBA_RMI,Runnable{
    private ArrayList<String> rmiList = new ArrayList<>();

    public RBA_Process() throws RemoteException {
    }

    public void broadCast(Message m) throws RemoteException, NotBoundException, MalformedURLException {
        for(int i=0;i<rmiList.size();i++){
            RBA_RMI otherProcess = (RBA_RMI) Naming.lookup(rmiList.get(i));
            otherProcess.receive(m);
        }
    }
    @Override
    public void receive(Message m) throws RemoteException {
        System.out.println(m.toString());
    }
    @Override
    public void run() {

    }


}
