import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

public class DA_PSON_Main_2 {

    public static void main(String[] args){

        int[] otherIds = {3,4};
        int[] ownIds = {12,2};
        int defaultport = 1100;
        String ownIP = "192.168.0.171";
        String otherIP = "192.168.0.171";

        try {
            LocateRegistry.createRegistry(defaultport);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < ownIds.length; i++){

            String ownLookup = "rmi://" + ownIP + ":" + defaultport + "/" + Integer.toString(ownIds[i]);
            String nextLookup;

            if(i != ownIds.length-1){
                nextLookup = "rmi://" + ownIP + ":" + defaultport + "/" + Integer.toString(ownIds[i+1]);
            } else {
                nextLookup = "rmi://" + ownIP + ":1099/" + Integer.toString(otherIds[0]);
            }

            setupComponent(ownIds[i], ownLookup, nextLookup);
        }

//        ArrayList<DA_PSON_Component> ComponentList= new ArrayList<DA_PSON_Component>();
//        //hardcode the example from the slides
//        try {
//            ComponentList.add(new DA_PSON_Component(7));
//            ComponentList.add(new DA_PSON_Component(4));
//            ComponentList.add(new DA_PSON_Component(9));
//            ComponentList.add(new DA_PSON_Component(12));
//            ComponentList.add(new DA_PSON_Component(1));
//            ComponentList.add(new DA_PSON_Component(3));
//            ComponentList.add(new DA_PSON_Component(8));
//            ComponentList.add(new DA_PSON_Component(2));
//            ComponentList.add(new DA_PSON_Component(6));
//            ComponentList.add(new DA_PSON_Component(5));
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        for(int i=0;i<ComponentList.size();i++){
//            int nextIndex = i+1;
//            if(nextIndex==ComponentList.size()){
//                nextIndex=0;
//            }
//            ComponentList.get(i).setNext(ComponentList.get(nextIndex));
//        }
//        ComponentList.get(0).performElectionRound();
//        ComponentList.get(0).performElectionRound();
//        ComponentList.get(1).performElectionRound();



    }

    private static void setupComponent(int id, String own, String next){

        try {
            DA_PSON_Component component = new DA_PSON_Component(id, next);
            Naming.rebind(own, component);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
