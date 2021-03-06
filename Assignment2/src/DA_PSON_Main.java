import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class DA_PSON_Main {

    private static ArrayList<DA_PSON_Component> componentList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        //Creating the unidirectional ring as presented in the slides
        //          7
        //      5       4
        //  6               9
        //  2               12
        //      8       1
        //          3
        //Each machine will create half of the ring
        int[] ownIds = {3, 8, 2, 6, 5};
        int[] otherIds = {7, 4, 9, 12, 1};

        int defaultport = 1099;
        //my ip address
        String ownIP = "192.168.0.171";
        //the ip address of the other host
        String otherIP = "192.168.0.174";
        try {
            LocateRegistry.createRegistry(defaultport);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //create x process, where x is ids length
        for (int i = 0; i < ownIds.length; i++) {
            //lookup is of the shape: RMI+IP+PORT+ID
            String ownLookup = "rmi://" + ownIP + ":" + defaultport + "/" + Integer.toString(ownIds[i]);
            String nextLookup;
            //For the last one in this half of the circle, we have to set the nextLookup to the first id of the otherIDs
            if (i != ownIds.length - 1) {
                nextLookup = "rmi://" + ownIP + ":" + defaultport + "/" + Integer.toString(ownIds[i + 1]);
            } else {
                nextLookup = "rmi://" + otherIP + ":1099/" + Integer.toString(otherIds[0]);
            }
            //Call the setup component function, which assigns the Strings of the next and own component
            setupComponent(ownIds[i], ownLookup, nextLookup);
        }


        int numrounds = 1;

        // Do the rounds forever
        while(true) {
            System.out.println("Starting round " + numrounds);
            componentList.get(0).performElectionRound();
            System.out.println();
            numrounds++;
            Thread.sleep(1000);
        }
    }

    private static void setupComponent(int id, String own, String next) {
        try {
            //Create the component, with id equalling your own id and next equalling the lookup-String of the next component in line
            DA_PSON_Component component = new DA_PSON_Component(id, next);
            //Bind the component to its string
            Naming.rebind(own, component);
            //Add component to componentlist
            componentList.add(component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
