import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class DA_PSON_Component extends UnicastRemoteObject implements DA_PSON_RMI {
    private boolean active;
    private int ownID;
    private int tid;
    private int ntid;
    private int nntid;

    private DA_PSON_Component next;
    public DA_PSON_Component(int ownID) throws RemoteException {
        active = true;
        this.ownID = ownID;
        tid = ownID;
    }
    public void performElectionRound(){
        //send tid to downstream neighbor
        try {
            next.receive(tid, true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    @Override
    public void receive(int receivedID, boolean singleN) throws RemoteException{
        if(active){
            if(singleN){
                ntid=receivedID;
                next.receive(Integer.max(tid,ntid),false);
            } else {
                nntid=receivedID;
            }
            if(ntid>=tid && ntid>=nntid){
                tid=ntid;
            } else {
                active = false;
                System.out.println("Component " + ownID + " has turned passive. My tid = " + tid);
            }
        } else {
            //if passive:
            next.receive(receivedID,singleN);
        }

    }
    public void setNext(DA_PSON_Component n){
        next = n;
    }
    public boolean isActive(){
        return active;
    }
    public int getOwnID(){
        return ownID;
    }
    public DA_PSON_Component getNext(){
        return next;
    }


}
