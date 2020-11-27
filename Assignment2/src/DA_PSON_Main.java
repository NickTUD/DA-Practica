import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class DA_PSON_Main {
    public static void main(String[] args){
        ArrayList<String> ipList = new ArrayList<>();
        ArrayList<DA_PSON_Component> ComponentList= new ArrayList<DA_PSON_Component>();

        for(int i=0;i<10;i++){
            String currentIP = "rmi://localhost:110"+i;
            ipList.add(currentIP);
        }
        int[] exampleFromSlides = {7,4,9,12,1,3,8,2,6,5};
        for(int i=0;i<ipList.size();i++){
            try {
                java.rmi.registry.LocateRegistry.createRegistry(1100+i);
                DA_PSON_Component currentComponent = new DA_PSON_Component(exampleFromSlides[i], "EMPTY");
                Naming.rebind(ipList.get(i) +"//DA_PSON_Component", currentComponent);
                ComponentList.add(currentComponent);
            } catch (RemoteException | MalformedURLException e) {
                e.printStackTrace();
            }
        }
        for(int i=0;i<ipList.size();i++){
            //setNext for each node
            int nextint = i+1;
            if(nextint==ipList.size()){
                nextint=0;
            }
            ComponentList.get(i).setNextString(ipList.get(nextint)+"//DA_PSON_Component");
        }

        ComponentList.get(0).performElectionRound();
        ComponentList.get(0).performElectionRound();


        //hardcode the example from the slides
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
//        ComponentList.get(1).performElectionRound();



    }
}
