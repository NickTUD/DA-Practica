import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class RBA_Process extends UnicastRemoteObject implements RBA_RMI, Runnable {
    private ArrayList<String> rmiList = new ArrayList<>();
    private int ownIndex;
    private int value;
    private int n, f;

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

    public RBA_Process(int index, int initialValue, ArrayList<String> rmiList, int n, int f) throws RemoteException {
        this.ownIndex = index;
        this.value = initialValue;
        this.rmiList = rmiList;
        this.n = n;
        this.f = f;
        resetRound(1);
        decided = false;
    }

    private void resetRound(int i) {
        currentRound = i;
        notificationMessagesReceivedThisRound = 0;
        proposalMessagesReceivedThisRound = 0;
        readyToBroadCastNotificationMessage = true;
        readyToBroadCastProposalMessage = false;
        amountOfOnesReceivedInNotificationPhaseThisRound = 0;
        amountOfOnesReceivedInProposalPhaseThisRound = 0;
    }

    public void broadCast(Message m) throws RemoteException, NotBoundException, MalformedURLException {
        for (int i = 0; i < rmiList.size(); i++) {
            RBA_RMI otherProcess = (RBA_RMI) Naming.lookup(rmiList.get(i));
            otherProcess.receive(m);
        }
        if (m.getType().equals(MessageType.P) && decided) {
            //STOP
            stopped = true;
        }
    }

    @Override
    public synchronized void receive(Message m) throws RemoteException {
        if(stopped){
            return;
        }
        if (m.getRound() != currentRound) {
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

    private void moveToDecisionPhase() {
        readyToPerformDecisionPhase = true;
    }

    private void moveToProposalPhase() {
        readyToBroadCastProposalMessage = true;
    }

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



    private void tryToBroadcastNotificationMessage() {
        Message m = new Message("N", currentRound, value, ownIndex);
        try {
            broadCast(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void tryToBroadcastProposalMessage() {
        System.out.println("Process "+ownIndex+"is trying to broadcast a proposal");
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
        Message m = new Message("P", currentRound, valueToSend, ownIndex);

        try {
            broadCast(m);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

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

    @Override
    public void run() {
        //initial waiting time before starting
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!stopped) {
            //add possible delay here
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tryToSendMessages();

        }
        System.out.println("Process "+ownIndex+"has stopped, and has decided on value: "+value);
    }


}
