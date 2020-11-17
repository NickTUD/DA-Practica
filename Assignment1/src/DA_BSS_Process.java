import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class DA_BSS_Process extends UnicastRemoteObject implements DA_BSS_RMI {

    private int[] clock;
    private Set<Message> buffer = new HashSet<Message>();
    private int ownIndex;
    private ArrayList<String> ipList;

    public DA_BSS_Process(int ownIndex, ArrayList<String> ipList) throws RemoteException {
        this.ownIndex = ownIndex;
        this.ipList = ipList;
        this.clock = new int[ipList.size()];
    }

    private void updateClock(int index){
        clock[index] += 1;
    }

    /**
     *
     * @param msg
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

    @Override
    public void broadcast(String text) throws RemoteException {

        //Update your own index
        updateClock(this.ownIndex);

        //TODO Add delays
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

    @Override
    public void receive(Message msg) throws RemoteException {

    }

    @Override
    public void deliver(Message msg) throws RemoteException {

        // "Deliver" results
        System.out.println("Process " + ownIndex + " delivered message: " + msg.getText());

        // Update clock
        updateClock(msg.getFromIndex());

        // Remove message from the buffer
        this.buffer.remove(msg);
    }
}