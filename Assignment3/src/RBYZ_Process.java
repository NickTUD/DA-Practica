import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


//TODO IMPLEMENT BROADCASTING INVOCATION AND
public class RBYZ_Process extends UnicastRemoteObject implements RBYZ_RMI, Runnable {

    private ProcessState state = ProcessState.WAITING_N;
    private int round = 1;
    private boolean decided = false;
    private int bufferentries = 0;

    private final int index;
    private final ArrayList<String> ipList;
    private final FailureType failureType;
    private final int f;
    private final int n;

    private int value;
    private int[] buffer;


    public RBYZ_Process(int index, int value, FailureType failureType, ArrayList<String> ipList, int n, int f) throws RemoteException {
        this.index = index;
        this.value = value;
        this.failureType = failureType;
        this.ipList = ipList;
        this.f = f;
        this.n = n;

        this.buffer = new int[n-f];
    }

    /**
     * Method that broadcasts some content (in this case a Message). A broadcast implies that it is sent to all other
     * processes.
     * @param msg The message that needs to be sent.
     * @throws RemoteException Thrown when there is a fault concerning RMI.
     */
    private void broadcast(Message msg) throws RemoteException {
        switch(failureType) {

            case NONE:
                broadcastHelper(msg);

            case NO_SEND:
                break;

            case PROB_SEND:
                if(ThreadLocalRandom.current().nextBoolean()) { broadcastHelper(msg); }
                else { break; }

            case PROB_VALUE:
                int newvalue = ThreadLocalRandom.current().nextInt(2);
                Message newmsg = new Message(msg.getType(), msg.getRound(), newvalue);
                broadcastHelper(newmsg);
        }
    }

    private void broadcastHelper(Message msg) throws RemoteException {
        for(int i=0; i < ipList.size(); i++) {
            if(i != index) {
                try {
                    RBYZ_RMI otherProcess = (RBYZ_RMI) Naming.lookup(ipList.get(i));
                    otherProcess.receive(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Message that gets invoked when a message is received. Should only process messages from the current round.
     * @param msg Message containing the type of message, the round it was sent in and the value.
     * @throws RemoteException when something happens with RMI.
     */
    @Override
    public synchronized void receive(Message msg) throws RemoteException {
        if(round == msg.getRound()) {
            switch(state){

                case WAITING_N:
                    if(msg.getType() == Message.MessageType.NOTIF) {
                        buffer[bufferentries] = msg.getValue();
                        bufferentries++;
                        if(bufferentries == n-f) {
                            processNotifMsgs();
                        }
                    }


                case WAITING_P:
                    if(msg.getType() == Message.MessageType.PROP) {
                        buffer[bufferentries] = msg.getValue();
                        bufferentries++;
                        if(bufferentries == n-f) {
                            processPropMsgs();
                        }
                    }

            }
        }
    }

    /**
     * Handles the steps of Ben-or's algorithm after receiving n-f notification messages.
     * @throws RemoteException when something happens in RMI
     */
    private void processNotifMsgs() throws RemoteException {
        // Count the number of 1's received.
        int sum = 0;
        for (int entry : buffer) {
            sum += entry;
        }

        int w;

        //If the number of 1's is larger than (n+f)/2
        if(sum > (n+f)/2) {
            w = 1;

            // We have n-f messages. Our counts of 0's and 1's must thus equal to n-f.
            // Thus the count of 0's must be equal to n-f- the number of 1's.
        } else if (n - f - sum > (n+f)/2) {
            w = 0;
        } else {
            w = ThreadLocalRandom.current().nextInt(2);
        }

        bufferentries = 0;
        state = ProcessState.WAITING_P;

        if(decided) {
            System.exit(0);
        }
    }

    private void processPropMsgs() {
        int sum = 0;
        for (int entry : buffer) {
            sum += entry;
        }

        if(sum > f || n-f-sum > f) {
            if(sum > f) {
                value = 1;
                if(sum > 3f) {
                    decided = true;
                }

            } else {
                value = 0;
                if(n-f-sum > f) {
                    decided = true;
                }
            }
        } else {
            value = ThreadLocalRandom.current().nextInt(2);
        }

        bufferentries = 0;
        round++;
        state = ProcessState.WAITING_P;
    }

    @Override
    public void run() {
        while(true){
            //
        }
    }

    private enum FailureType {
        NONE, NO_SEND, PROB_SEND, PROB_VALUE
    }

    private enum ProcessState {
        WAITING_N, WAITING_P
    }
}
