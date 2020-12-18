import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class RBYZ_Main {
    //USERID 0 = ALAN
    //USERID 1 = NICK
    private static ArrayList<RBYZ_Process> processList = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        //Make sure the program arguments are correctly set. args[0]=0 for Alan, args[0]=1 for Nick.
        if(args.length!=1){
            System.out.println("Arguments incorrectly set. Go to Run configurations and assign 0 or 1 to the program arguments");
            System.out.println("Choose 0 if your name is Alan and choose 1 if your name is Nick.");
            System.exit(1);
        }
        ArrayList<String> ipList = new ArrayList<>();
        ArrayList<String> rmiList = new ArrayList<>();
        int defaultport = 1099;
        String myUserID = args[0];
        int myUserIDint = Integer.parseInt(myUserID);
        LocateRegistry.createRegistry(defaultport);

        //read the test file
        String testFilePath = "5processes.txt";
        BufferedReader bReader = new BufferedReader(new FileReader(new File("").getAbsolutePath()+"/src/"+testFilePath));

        //find ip addresses
        int amountOfIpAddresses = Integer.parseInt(bReader.readLine());
        for(int i=0;i<amountOfIpAddresses;i++){
            //These lines have the format: <userID> <ipAdress>
            String[] splittedLine = bReader.readLine().split(" ");
            ipList.add(splittedLine[1]);
        }

        //create processes that match your own id
        //line is of the format: <n> <f>
        String[] nfLine = bReader.readLine().split(" ");
        int n = Integer.parseInt(nfLine[0]);
        int f = Integer.parseInt(nfLine[1]);
        //These lines have the format <processID> <userID> <Initial Value><fault type>
        String[] lines = new String[n];
        for(int i=0;i<n;i++){
            lines[i]=bReader.readLine();
            String[] splittedLine = lines[i].split(" ");

            //This means we have to make this process and host it on this machine
            String rmiString = "rmi://" + ipList.get(Integer.parseInt(splittedLine[1])) + ":" + defaultport +"/"+ splittedLine[0];
            rmiList.add(rmiString);

        }
        for(int i=0;i<n;i++) {
            String[] splittedLine = lines[i].split(" ");
            if (splittedLine[1].equals(myUserID)) {
                startProcess(splittedLine[0],splittedLine[2],splittedLine[3],rmiList,n,f,rmiList.get(i));
            }
        }
        for(int i=0;i<processList.size();i++){
            new Thread(processList.get(i)).start();
        }
        System.out.println("All done");
    }

    private static void startProcess(String index, String initialValue, String failureType, ArrayList<String> rmiList, int n, int f, String rmiString) {
        try {
            RBYZ_Process proc = new RBYZ_Process(Integer.parseInt(index),Integer.parseInt(initialValue), RBYZ_Process.FailureType.valueOf(failureType),rmiList,n,f);
            Naming.rebind(rmiString, proc);
            processList.add(proc);
            System.out.println("Started process with id = "+index + " at "+rmiString);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
