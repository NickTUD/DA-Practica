import java.rmi.RemoteException;
import java.util.ArrayList;

public class DA_PSON_Main {
    public static void main(String[] args){
        ArrayList<DA_PSON_Component> ComponentList= new ArrayList<DA_PSON_Component>();
        //hardcode the example from the slides
        try {
            ComponentList.add(new DA_PSON_Component(7));
            ComponentList.add(new DA_PSON_Component(4));
            ComponentList.add(new DA_PSON_Component(9));
            ComponentList.add(new DA_PSON_Component(12));
            ComponentList.add(new DA_PSON_Component(1));
            ComponentList.add(new DA_PSON_Component(3));
            ComponentList.add(new DA_PSON_Component(8));
            ComponentList.add(new DA_PSON_Component(2));
            ComponentList.add(new DA_PSON_Component(6));
            ComponentList.add(new DA_PSON_Component(5));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        for(int i=0;i<ComponentList.size();i++){
            int nextIndex = i+1;
            if(nextIndex==ComponentList.size()){
                nextIndex=0;
            }
            ComponentList.get(i).setNext(ComponentList.get(nextIndex));
        }
        for(int i=0;i<ComponentList.size();i++){
            ComponentList.get(i).performElectionRound();
        }

    }
}
