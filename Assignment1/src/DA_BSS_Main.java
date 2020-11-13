import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 *
 */
public class DA_BSS_Main {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        ArrayList<String> ipList = new ArrayList<String>();

        String p0ip = "rmi://localhost:1099";
        String p1ip = "rmi://localhost:1100";
        String p2ip = "rmi://localhost:1101";

        ipList.add(p0ip);
        ipList.add(p1ip);
        ipList.add(p2ip);

        try {
            //Registry registry = LocateRegistry.getRegistry(host);
            //Create process 0
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            DA_BSS_Process p0 = new DA_BSS_Process(0, ipList);
            Naming.rebind(p0ip +"//DA_BSS_Process", p0);
            //Create process1
            java.rmi.registry.LocateRegistry.createRegistry(1100);
            DA_BSS_Process p1 = new DA_BSS_Process(1,ipList);
            Naming.rebind(p1ip +"//DA_BSS_Process", p1);
            //Create process2
            java.rmi.registry.LocateRegistry.createRegistry(1101);
            DA_BSS_Process p2 = new DA_BSS_Process(2,ipList);
            Naming.rebind(p2ip +"//DA_BSS_Process", p2);

            p0.broadcast(null);


        } catch (RemoteException | MalformedURLException e) {
            System.err.println("Main.main error: " +  e.toString());
            e.printStackTrace();
        }
    }

}