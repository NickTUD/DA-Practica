import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class DA_PSON_Component extends UnicastRemoteObject implements DA_PSON_RMI {
    private ArrayList<String> ipList;
    private boolean active;
    private int ownID;
    private int tid;
    private int ntid;
    private int nntid;
    boolean hasSentTid;
    private String nextString;

    public DA_PSON_Component(int ownID, String nnextString) throws RemoteException {
        active = true;
        this.ownID = ownID;
        tid = ownID;
        ntid = -1;
        nntid = -1;
        hasSentTid=false;
        nextString=nnextString;
    }

    public void performElectionRound() {
        //send tid to downstream neighbor

        System.out.println("Component with id="+ownID+" is performing election round.");

        hasSentTid = true;
        sendToNext(tid, true);


    }

    @Override
    public void receive(int receivedID, boolean singleN) throws RemoteException {
        //The process that received its own id has been elected
        if(receivedID==ownID){
            System.out.println("The Elected Leadder is me: Component with id = "+ownID);
            callQuit();
        }
        if (active) {
            if (singleN) {//so ntid is received
                if (hasSentTid==false) {//so hasnt started yet
                    performElectionRound();
                }
                //now tid is always defined if it was not already
                ntid = receivedID;
                //now ntid is defined
                sendToNext(Integer.max(tid, ntid), false);
                if (nntid != -1) {//so we have all 3 tid ntid and nntid defined
                    performCheckToTurnPassive(tid, ntid, nntid);
                }
            } else {//so nntid is received
                if (hasSentTid==false) {//so hasnt started yet
                    performElectionRound();
                }
                //now tid is always defined if it was not already
                if (ntid == -1) {//if has not received ntid
                    nntid = receivedID;//we store it but dont do the final computation yet to turn passive because we have not received ntid
                } else {//so we have already received ntid and sent that one through
                    nntid = receivedID;
                    performCheckToTurnPassive(tid, ntid, nntid);
                }
            }
        } else {
            //if passive:
            sendToNext(receivedID, singleN);
        }

    }
    @Override
    public void callQuit() {
        try {
            DA_PSON_RMI nextComponent = (DA_PSON_RMI) Naming.lookup(nextString);
            nextComponent.callQuit();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void sendToNext(int receivedID, boolean singleN) {
        try {
            DA_PSON_RMI nextComponent = (DA_PSON_RMI) Naming.lookup(nextString);
            nextComponent.receive(receivedID,singleN);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void performCheckToTurnPassive(int tid, int ntid, int nntid) {
        if(active){
            if(ntid>=tid && ntid>=nntid){
                //remain active
                this.tid=ntid;//take on the value of upstream node
                System.out.println("Component with original id " + ownID + " has remained active and taken on ntid: " + ntid);
            } else {
                //turn passive
                active=false;
                System.out.println("Component with original id " + ownID + " has turned passive");
            }
        }
        //either case, the round is over.
        this.ntid=-1;
        this.nntid=-1;
        hasSentTid=false;
    }

    public void setNextString(String n) {
        nextString = n;
    }

    public boolean isActive() {
        return active;
    }

    public int getOwnID() {
        return ownID;
    }

    public String getNextString() {
        return nextString;
    }


}
