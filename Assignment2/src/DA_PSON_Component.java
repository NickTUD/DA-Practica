import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class that represents a single process in Peterson's algorithm for election in a unidirectional ring
 */
public class DA_PSON_Component extends UnicastRemoteObject implements DA_PSON_RMI {

    private boolean active;
    private int ownID;
    private int tid;
    private int ntid;
    private int nntid;
    boolean hasSentTid;
    private String nextString;

    /**
     * Constructor for a process in Peterson's algorithm
     *
     * @param ownID       the ID of a process, should be unique amongst all processes.
     * @param nnextString the URL for the neighbour
     * @throws RemoteException when something happens with RMI.
     */
    public DA_PSON_Component(int ownID, String nnextString) throws RemoteException {
        active = true;
        this.ownID = ownID;
        tid = ownID;
        ntid = -1;
        nntid = -1;
        hasSentTid = false;
        nextString = nnextString;
    }

    /**
     * Method that gets invoked for a process at the start of each round in Peterson's algorithm
     */
    public void performElectionRound() {

        // If the process is active, let it start the round.
        if (active) {
            System.out.println("Component with id=" + ownID + " is performing election round.");

            hasSentTid = true;
            sendToNext(tid, true);


            // If the process is passive, let another process start the round.
        } else {

            try {
                DA_PSON_RMI nextComponent = (DA_PSON_RMI) Naming.lookup(nextString);
                nextComponent.performElectionRound();
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * Method that handles what a process should do upon receiving a message.
     *
     * @param receivedID the ID it received from it's neighbour
     * @param singleN    indicator for which type of ID is received, ntid or nntid.
     * @throws RemoteException when something happens with RMI.
     */
    @Override
    public void receive(int receivedID, boolean singleN) throws RemoteException {
        //The process that received its own id has been elected
        if (receivedID == ownID) {
            System.out.println("The Elected Leadder is me: Component with id = " + ownID);
            System.exit(0);
        }
        if (active) {
            if (singleN) {//so ntid is received
                if (hasSentTid == false) {//so hasnt started yet
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
                if (hasSentTid == false) {//so hasnt started yet
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

    /**
     * Helper method for invoking a receive on another object.
     *
     * @param receivedID ID that needs to be sent
     * @param singleN    indicator for which type of ID it is, ntid or nntid.
     */
    private void sendToNext(int receivedID, boolean singleN) {
        try {
            DA_PSON_RMI nextComponent = (DA_PSON_RMI) Naming.lookup(nextString);
            nextComponent.receive(receivedID, singleN);
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method that switches processes from active to passive if needed
     *
     * @param tid   temporary ID of the current process
     * @param ntid  temporary ID of the previous neighbor
     * @param nntid max of the ID's of the previous 2 neighbors.
     */
    private void performCheckToTurnPassive(int tid, int ntid, int nntid) {
        if (active) {
            if (ntid >= tid && ntid >= nntid) {
                //remain active
                this.tid = ntid;//take on the value of upstream node
                System.out.println("Component with original id " + ownID + " has remained active and taken on ntid: " + ntid);
            } else {
                //turn passive
                active = false;
                System.out.println("Component with original id " + ownID + " has turned passive");
            }
        }
        //either case, the round is over.
        this.ntid = -1;
        this.nntid = -1;
        hasSentTid = false;
    }
}
