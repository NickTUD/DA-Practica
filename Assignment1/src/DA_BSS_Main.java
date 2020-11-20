import java.net.MalformedURLException;
import java.rmi.*;
import java.sql.SQLOutput;
import java.util.ArrayList;

/**
 * Class that runs the Birman-Schiper-Stephenson algorithm
 */
public class DA_BSS_Main {

    public static void main(String[] args) {
        ArrayList<String> ipList = new ArrayList<>();

        String p0ip = "rmi://localhost:1099";
        String p1ip = "rmi://localhost:1100";
        String p2ip = "rmi://localhost:1101";

        ipList.add(p0ip);
        ipList.add(p1ip);
        ipList.add(p2ip);

        try {
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

            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        p0.broadcast("P0 - First message");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        p0.broadcast("P0 - Second message");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            t1.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t2.start();









        } catch (RemoteException | MalformedURLException e) {
            System.err.println("Main.main error: " +  e.toString());
            e.printStackTrace();
        }
    }

}