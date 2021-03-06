import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class RBYZ_Process extends UnicastRemoteObject implements RBYZ_RMI, Runnable {

    private ProcessState state = ProcessState.WAITING_N;
    private int round = 1;
    private boolean decided = false;
    private boolean broadcasted = false;
    private int bufferentries = 0;

    private final int index;
    private final ArrayList<String> rmiList;
    private final FailureType failureType;
    private final int f;
    private final int n;


    private int value;
    private int[] buffer;
    private int decidedInRound;
    private Message msgToBroadcast;


    public RBYZ_Process(int index, int value, FailureType failureType, ArrayList<String> rmiList, int n, int f) throws RemoteException {
        this.index = index;
        this.value = value;
        this.failureType = failureType;
        this.rmiList = rmiList;
        this.f = f;
        this.n = n;

        this.buffer = new int[n-f];
        this.msgToBroadcast = new Message(Message.MessageType.NOTIF, round, value);
    }

    /**
     * Method that broadcasts some content (in this case a Message). A broadcast implies that it is sent to all other
     * processes.
     * @param msg The message that needs to be sent.
     * @throws RemoteException Thrown when there is a fault concerning RMI.
     */
    private void broadcast(Message msg) throws RemoteException, InterruptedException {
        //Thread.sleep(ThreadLocalRandom.current().nextInt(11) * 1000);
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

        if(decided && decidedInRound + 1 == msg.getRound() && msg.getType() == Message.MessageType.PROP) {
            System.exit(0);
        }
    }

    private void broadcastHelper(Message msg) throws RemoteException {
        for(int i=0; i < rmiList.size(); i++) {
            try {
                RBYZ_RMI otherProcess = (RBYZ_RMI) Naming.lookup(rmiList.get(i));
                otherProcess.receive(msg);
            } catch (Exception e) {
                e.printStackTrace();
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
        //System.out.println("Process" + index + "is in state "+state+" and is receiving " +msg.toString());
        if(round == msg.getRound()) {
            switch(state){

                case WAITING_N:
                    if(msg.getType() == Message.MessageType.NOTIF) {
                        buffer[bufferentries] = msg.getValue();
                        bufferentries++;
                        if(bufferentries == n-f) {
                            //System.out.println("Calling processNotifMsgs");
                            processNotifMsgs();
                        }
                    }


                case WAITING_P:
                    if(msg.getType() == Message.MessageType.PROP) {
                        buffer[bufferentries] = msg.getValue();
                        bufferentries++;
                        if(bufferentries == n-f) {
                            //System.out.println("Calling processpropmsgs");
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
            if(failureType==FailureType.NONE){
                System.out.println("Correct Process " + index + "has selected random value " + w);
            }
        }

        // Reset the amount of messages received of the correct type.
        bufferentries = 0;
        state = ProcessState.WAITING_P;
        msgToBroadcast = new Message(Message.MessageType.PROP, round, w);
        broadcasted = false;
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
                    if(!decided) {
                        System.out.println("Process " + index + " decided 1 in round " +round);
                        decidedInRound = round;
                    }
                    decided = true;

                }

            } else {
                value = 0;
                if(n-f-sum > f) {
                    if(!decided) {
                        System.out.println("Process " + index + " decided 0 in round " + round);
                        decidedInRound = round;
                    }
                    decided = true;
                }
            }
        } else {
            value = ThreadLocalRandom.current().nextInt(2);
        }

        bufferentries = 0;
        round++;
        state = ProcessState.WAITING_N;
        msgToBroadcast = new Message(Message.MessageType.NOTIF, round, value);
        broadcasted = false;
    }

    @Override
    public void run() {
        while(true) {
            if(!broadcasted){
                broadcasted = true;
                try {
                    System.out.println("Process " + index + " did broadcast"+ msgToBroadcast.toString()+" in round " + msgToBroadcast.getRound());
                    broadcast(msgToBroadcast);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public enum FailureType {
        NONE, NO_SEND, PROB_SEND, PROB_VALUE
    }

    public enum ProcessState {
        WAITING_N, WAITING_P
    }
}
