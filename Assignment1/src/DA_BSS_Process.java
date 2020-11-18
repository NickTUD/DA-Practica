import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 * Class that represents one process in the Birman-Schiper-Stephenson algorithm
 */
public class DA_BSS_Process extends UnicastRemoteObject implements DA_BSS_RMI {

    private int[] clock;
    private ArrayList<Message> buffer = new ArrayList<>();
    private int ownIndex;
    private ArrayList<String> ipList;
    private ArrayList<String> deliveredMessages = new ArrayList<>();

    /**
     * Constructor for a process.
     * @param ownIndex The id/index of the process
     * @param ipList List of IPs for all processes
     * @throws RemoteException
     */
    public DA_BSS_Process(int ownIndex, ArrayList<String> ipList) throws RemoteException {
        this.ownIndex = ownIndex;
        this.ipList = ipList;
        this.clock = new int[ipList.size()];
    }

    /**
     * Updates the clock at the position index
     * @param index Index of the clock that needs to be updated
     */
    private void updateClock(int index){
        clock[index] += 1;
    }

    /**
     * Method that checks whether a message can be delivered upon receiving a message
     * @param msg The message (content, clock and sender info) that needs to be checked for delivering
     * @return true if the message can deliver according to the process' clock, false if not
     */
    public boolean canDeliver(Message msg) {
        int[] msgClock = msg.getClock();
        int[] localClock = this.clock.clone();
        localClock[msg.getFromIndex()] += 1;
        for(int i = 0; i < localClock.length; i++) {
            if(localClock[i] < msgClock[i]){
                return false;
            }
        }
        return true;
    }


    /**
     * Method that broadcasts some content (in this case a string). A broadcast implies that it is sent to all other
     * processes.
     * @param text The content of the message that needs to be sent.
     * @throws RemoteException Thrown when there is a fault concerning RMI.
     */
    @Override
    public void broadcast(String text) throws RemoteException {

        System.out.println("Process " + ownIndex + " broadcasting message: " + text);

        //Update your own index
        updateClock(this.ownIndex);

        //TODO Add random delays
        for(int i=0; i < ipList.size(); i++) {
            if(i != ownIndex) {
                try {
                    DA_BSS_RMI otherProcess = (DA_BSS_RMI) Naming.lookup(ipList.get(i)+ "//DA_BSS_Process");
                    otherProcess.receive(new Message(text, this.clock, this.ownIndex));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Method that gets invoked when a message is received.
     * @param msg Message instance containing the content, sender information and clock.
     * @throws RemoteException Thrown when there is a fault concerning RMI.
     */
    @Override
    public void receive(Message msg) throws RemoteException {

        System.out.println("Process " + ownIndex + " received message: " + msg.getText());

        if (canDeliver(msg)) {

            try {

                // If a message can be delivered according to the process' clock, then do it.
                DA_BSS_RMI ownProcess = (DA_BSS_RMI) Naming.lookup(ipList.get(ownIndex)+ "//DA_BSS_Process");
                ownProcess.deliver(msg);


                // Loop over the buffer, checking whether we can deliver a message
                boolean deliveredNonZero = true;
                while(deliveredNonZero){
                    deliveredNonZero = false;
                    for (Message bufferMsg : buffer) {
                        if (canDeliver(bufferMsg)) {
                            ownProcess.deliver(bufferMsg);
                            deliveredNonZero = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            // If a message can't be delivered, add it to the buffer.
            this.buffer.add(msg);
        }
    }

    /**
     * Method that gets invoked when a message can be delivered.
     * @param msg Message instance containing the content, sender information and clock.
     * @throws RemoteException Thrown when there is a fault concerning RMI.
     */
    @Override
    public void deliver(Message msg) throws RemoteException {

        // "Deliver" results
        System.out.println("Process " + ownIndex + " delivered message: " + msg.getText());
        deliveredMessages.add(msg.getText());

        // Update clock
        updateClock(msg.getFromIndex());

        // Remove message from the buffer
        this.buffer.remove(msg);
    }
}