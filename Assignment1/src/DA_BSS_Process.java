import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

/**
 *
 */
public class DA_BSS_Process extends UnicastRemoteObject implements DA_BSS_RMI {

    private int[] clock;
    private ArrayList<Message> buffer = new ArrayList<>();
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

        System.out.println("Trying to broadcast message");

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
        if (canDeliver(msg)) {

            try {

                DA_BSS_RMI ownProcess = (DA_BSS_RMI) Naming.lookup(ipList.get(ownIndex)+ "//DA_BSS_Process");
                ownProcess.receive(msg);

                boolean loop = true;
                for(int i=0; i < buffer.size(); i++) {
                        Message bufferMsg = buffer.get(i);
                        if(canDeliver(bufferMsg)) {
                            ownProcess.receive(msg);
                        }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            this.buffer.add(msg);
        }
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