import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class RBA_Process extends UnicastRemoteObject implements RBA_RMI,Runnable{
    private ArrayList<String> rmiList = new ArrayList<>();
    private int ownIndex;

    public RBA_Process(int index,ArrayList<String> rmiList) throws RemoteException {
        this.ownIndex = index;
        this.rmiList = rmiList;
    }

    public void broadCast(Message m) throws RemoteException, NotBoundException, MalformedURLException {
        for(int i=0;i<rmiList.size();i++){
            RBA_RMI otherProcess = (RBA_RMI) Naming.lookup(rmiList.get(i));
            otherProcess.receive(m);
        }
    }
    @Override
    public void receive(Message m) throws RemoteException {
        System.out.println("Process "+ownIndex+" received msg: "+m.toString());
    }
    @Override
    public void run() {
        boolean readytoSend = true;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(true){
            if(readytoSend){
                readytoSend=false;
                try {
                    broadCast(new Message("N",1,1));
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
