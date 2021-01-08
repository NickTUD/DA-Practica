import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class which represents a single process in the algorithm.
 */
public class RBA_Process extends UnicastRemoteObject implements RBA_RMI, Runnable {
    private ArrayList<String> rmiList = new ArrayList<>();
    private ArrayList<Message> buffer = new ArrayList<>();

    private int ownIndex;
    private int value;
    private int n, f;
    private Fault type;

    private int currentRound;

    private boolean readyToBroadCastNotificationMessage;
    private boolean readyToBroadCastProposalMessage;
    private boolean readyToPerformDecisionPhase;

    private int notificationMessagesReceivedThisRound;
    private int proposalMessagesReceivedThisRound;

    private int amountOfOnesReceivedInNotificationPhaseThisRound;
    private int amountOfOnesReceivedInProposalPhaseThisRound;

    private boolean decided;
    private boolean stopped;

    /**
     * Constructor for a process.
     * @param index ID of the process
     * @param initialValue Its initial binary value
     * @param rmiList the list of all RMI strings, needed for remote invocation.
     * @param n The total amount of processes
     * @param f The total amount of faulty processes.
     * @param type Its own type, indicating faultiness or not.
     * @throws RemoteException when something goes wrong with RMI invocations.
     */
    public RBA_Process(int index, int initialValue, ArrayList<String> rmiList, int n, int f, Fault type) throws RemoteException {
        this.ownIndex = index;
        this.value = initialValue;
        this.rmiList = rmiList;
        this.n = n;
        this.f = f;
        this.type = type;
        resetRound(1);
        decided = false;
    }

    /**
     * Resets the round for the process, handling all things needed to start a new round such as handling messages received
     * for future rounds.
     * @param i The round the process should be set to.
     */
    private void resetRound(int i) {
        currentRound = i;
        notificationMessagesReceivedThisRound = 0;
        proposalMessagesReceivedThisRound = 0;
        readyToBroadCastNotificationMessage = true;
        readyToBroadCastProposalMessage = false;
        amountOfOnesReceivedInNotificationPhaseThisRound = 0;
        amountOfOnesReceivedInProposalPhaseThisRound = 0;

        try {
            for(Message msg : buffer) {
                if(msg.getRound() == currentRound) {
                    receive(msg);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        buffer.clear();
    }

    /**
     * Broadcast a message to all other processes.
     * @param m The message wanted to be broadcasted.
     * @throws RemoteException if issues with remote invocation occur
     * @throws NotBoundException if an object is not bound in naming
     * @throws MalformedURLException if a misformed URL is used
     */
    public void broadCast(Message m) throws RemoteException, NotBoundException, MalformedURLException {
        if(m.getValue() >= 0) {
            for (int i = 0; i < rmiList.size(); i++) {
                RBA_RMI otherProcess = (RBA_RMI) Naming.lookup(rmiList.get(i));
                otherProcess.receive(m);
            }
        }
        if (m.getType().equals(MessageType.P) && decided) {
            //STOP
            stopped = true;
        }
    }

    /**
     * Method that gets called when a message is received by a process
     * @param m The message received
     * @throws RemoteException when something funky happens with RMI.
     */
    @Override
    public synchronized void receive(Message m) throws RemoteException {
        if(stopped){
            return;
        }
        if (m.getRound() != currentRound) {
            if (m.getRound() > currentRound) {
                buffer.add(m);
            }
            System.out.println("Process " + ownIndex + " received msg: " + m.toString()+"but I am in round " + currentRound + "so I ignore the message");
            return;
        }
        System.out.println("Process " + ownIndex + " received msg: " + m.toString());
        if (m.getType().equals(MessageType.N)) {
            notificationMessagesReceivedThisRound++;
            amountOfOnesReceivedInNotificationPhaseThisRound += m.getValue();
            if (notificationMessagesReceivedThisRound == (n - f)) {
                //so this process has received at least n-f notif messages
                System.out.println("Process " + ownIndex + " has now received " + notificationMessagesReceivedThisRound + " Notification messages in round " + currentRound + " and is moving to proposal phase");
                moveToProposalPhase();
            }
        } else if (m.getType().equals(MessageType.P)) {
            proposalMessagesReceivedThisRound++;
            amountOfOnesReceivedInProposalPhaseThisRound += m.getValue();
            if (proposalMessagesReceivedThisRound == (n - f)) {
                System.out.println("Process " + ownIndex + " has now received " + proposalMessagesReceivedThisRound + " Proposal messages in round " + currentRound + " and is moving to decision phase");
                moveToDecisionPhase();
            }
        }


    }

    /**
     * Auxilliary method which gets called if enough P-messages are received.
     */
    private void moveToDecisionPhase() {
        readyToPerformDecisionPhase = true;
    }

    /**
     * Auxilliary method which gets called if enough N-messages are received.
     */
    private void moveToProposalPhase() {
        readyToBroadCastProposalMessage = true;
    }

    /**
     * Method that gets called on an interval.
     * Only actually sends messages if no messages are sent yet in any phase other than the decision phase.
     */
    private void tryToSendMessages() {

        //see if we still need to send N message
        if (readyToBroadCastNotificationMessage) {
            readyToBroadCastNotificationMessage = false;
            tryToBroadcastNotificationMessage();
        } else if (readyToBroadCastProposalMessage) {
            readyToBroadCastProposalMessage = false;
            tryToBroadcastProposalMessage();
        } else if (readyToPerformDecisionPhase){
            System.out.println("performing dec phase by index :" +ownIndex);
            readyToPerformDecisionPhase=false;
            performDecisionPhase();
        }
    }

    /**
     * Auxilliary method which creates a message according to its fault type.
     * @param msgType String indicating whether the message should be a P or N message.
     * @param valueToSend Value which the message wants to send if it was a correct process.
     * @return Message object containing all the necessary information.
     */
    private Message createMessage(String msgType, int valueToSend) {
        Message msg;
        switch(type) {
            case NONE:
                msg = new Message(msgType, currentRound, valueToSend, ownIndex);
                break;
            case NO_SEND:
                msg = new Message(msgType, currentRound, -1, ownIndex);
                break;
            case RANDOM_VAL:
                msg = new Message(msgType, currentRound, ThreadLocalRandom.current().nextInt(0,2), ownIndex);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        return msg;
    }

    /**
     * Attempts a broadcast of messages.
     */
    private void tryToBroadcastNotificationMessage() {
        try {
            Message m = createMessage("N", value);
            broadCast(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles logic of received notification message and consecutively broadcasts proposal messages
     */
    private void tryToBroadcastProposalMessage() {
        System.out.println("Process "+ownIndex+" is trying to broadcast a proposal");
        int amountOfZerosReceivedInNotificationPhaseThisRound = notificationMessagesReceivedThisRound - amountOfOnesReceivedInNotificationPhaseThisRound;
        int valueToSend;
        if (amountOfZerosReceivedInNotificationPhaseThisRound > ((n + f) / 2)) {
            valueToSend = 0;
        } else if (amountOfOnesReceivedInNotificationPhaseThisRound > ((n + f) / 2)) {
            valueToSend = 1;
        } else {
            //choose a random value
            valueToSend = ThreadLocalRandom.current().nextInt(0, 2);
        }


        try {
            Message m = createMessage("P", valueToSend);
            broadCast(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks after receiving enough proposal messages, whether it can decide on a value.
     */
    private void performDecisionPhase() {
        int amountOfZerosReceivedInProposalPhaseThisRound = proposalMessagesReceivedThisRound-amountOfOnesReceivedInProposalPhaseThisRound;
        if(amountOfZerosReceivedInProposalPhaseThisRound>f||amountOfOnesReceivedInProposalPhaseThisRound>f){
            if(amountOfZerosReceivedInProposalPhaseThisRound>amountOfOnesReceivedInProposalPhaseThisRound){
                value =0;
            } else {
                value =1;
            }
            if(amountOfZerosReceivedInProposalPhaseThisRound>3*f){
                //decide 0
                value =0;
                System.out.println("Process "+ownIndex+" has decided on value: "+value+" in round "+currentRound);
                decided=true;
            } else if(amountOfOnesReceivedInProposalPhaseThisRound>3*f){
                //decide 1
                value =1;
                System.out.println("Process "+ownIndex+" has decided on value: "+value+" in round "+currentRound);
                decided=true;
            }
        } else {
            //set random value
            value = ThreadLocalRandom.current().nextInt(0, 2);
        }
        resetRound(currentRound+1);
    }

    /**
     * Method that gets run by each process so each process can be assigned to a thread.
     */
    @Override
    public void run() {
        //initial waiting time before starting
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!stopped) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(1, 3) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tryToSendMessages();

        }
        System.out.println("Process "+ownIndex+" has stopped, and has decided on value: "+value);
    }


}
