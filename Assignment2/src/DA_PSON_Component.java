import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class DA_PSON_Component extends UnicastRemoteObject implements DA_PSON_RMI {
    private boolean active;
    private int ownID;
    private int tid;
    private int ntid;
    private int nntid;
    boolean hasSentTid;
    private DA_PSON_Component next;

    public DA_PSON_Component(int ownID) throws RemoteException {
        active = true;
        this.ownID = ownID;
        tid = ownID;
        ntid = -1;
        nntid = -1;
        hasSentTid=false;
    }

    public void performElectionRound() {
        //send tid to downstream neighbor

        System.out.println("Component with id="+ownID+" is performing election round.");
        try {
            hasSentTid=true;
            next.receive(tid, true);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void receive(int receivedID, boolean singleN) throws RemoteException {
        //The process that received its own id has been elected
        if(receivedID==ownID){
            System.out.println("The Elected Leadder is me: Component with id = "+ownID);
            System.exit(0);
        }
        if (active) {
            if (singleN) {//so ntid is received
                if (hasSentTid==false) {//so hasnt started yet
                    performElectionRound();
                }
                //now tid is always defined if it was not already
                ntid = receivedID;
                //now ntid is defined
                next.receive(Integer.max(tid, ntid), false);
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
            next.receive(receivedID, singleN);
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

    public void setNext(DA_PSON_Component n) {
        next = n;
    }

    public boolean isActive() {
        return active;
    }

    public int getOwnID() {
        return ownID;
    }

    public DA_PSON_Component getNext() {
        return next;
    }


}
