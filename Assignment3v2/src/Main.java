import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class Main {

    /**
     * Keep track of all the active processes through the lists.
     */
    private static ArrayList<String> ipList;
    private static ArrayList<String> portList;
    private static ArrayList<RBA_Process> processList;
    private static ArrayList<String> rmiList;

    /**
     * Variables which keep track of the process independent variables.
     */
    private static int n;
    private static int f;
    private static String myUserIDString;
    private static int myUserIDint;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting...");
        if(args.length!=1){
            System.out.println("Arguments incorrectly set. Go to Run configurations and assign 0 or 1 to the program arguments");
            System.out.println("Choose 0 if your name is Alan and choose 1 if your name is Nick.");
            System.exit(1);
        }
        myUserIDString = args[0];
        myUserIDint = Integer.parseInt(myUserIDString);
        //Path to the testfile
        String testFilePath = "11processes.txt";

        BufferedReader bReader = new BufferedReader(new FileReader(new File("").getAbsolutePath()+"/src/"+testFilePath));
        addIpsAndPorts(bReader);
        LocateRegistry.createRegistry(Integer.parseInt(portList.get(myUserIDint)));
        addProcesses(bReader);
        for(int i=0;i<processList.size();i++){
            new Thread(processList.get(i)).start();
        }
        System.out.println("Finished.");
    }

    /**
     * Process the information regarding the processes
     * @param bReader BufferedReader of the input file
     * @throws IOException when something goes wrong with the reader
     */
    private static void addProcesses(BufferedReader bReader) throws IOException {
        processList = new ArrayList<RBA_Process>();
        rmiList = new ArrayList<String>();
        String[] nfLine = bReader.readLine().split(" ");
        n = Integer.parseInt(nfLine[0]);
        f = Integer.parseInt(nfLine[1]);
        String[] lines = new String[n];
        for(int i=0;i<n;i++){
            lines[i] = bReader.readLine();
            String[] splittedLine = lines[i].split(" ");
            String rmiString = "rmi://" + ipList.get(Integer.parseInt(splittedLine[1])) + ":" + portList.get(Integer.parseInt(splittedLine[1])) +"/"+ splittedLine[0];
            rmiList.add(rmiString);
        }
        for(int i=0;i<n;i++) {
            String[] splittedLine = lines[i].split(" ");
            if (splittedLine[1].equals(myUserIDString)) {
                bindProcess(splittedLine[0],splittedLine[2],splittedLine[3],rmiList,n,f,rmiList.get(i));
            }
        }
    }

    /**
     * Auxilliary method which creates the Process objects and binds them.
     * @param index ID of the process.
     * @param initialValue The initial binary value it starts with.
     * @param failureType Indicates if the process is faulty, and if so what type.
     * @param rmiList List of all RMI strings.
     * @param n Number of processes.
     * @param f Number of faulty processes.
     * @param rmiString The process' own RMI string.
     */
    private static void bindProcess(String index, String initialValue, String failureType, ArrayList<String> rmiList, int n, int f, String rmiString) {
        try {
            //RBA_Process proc = new RBA_Process(Integer.parseInt(index),Integer.parseInt(initialValue), RBA_Process.FailureType.valueOf(failureType),rmiList,n,f);
            RBA_Process proc = new RBA_Process(Integer.parseInt(index),Integer.parseInt(initialValue),rmiList, n,f, Fault.valueOf(failureType));
            Naming.rebind(rmiString, proc);
            processList.add(proc);
            System.out.println("Started process with id = "+index + " at "+rmiString);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the host information
     * @param bReader BufferedReader of the input file
     * @throws IOException when something goes wrong with the reader
     */
    private static void addIpsAndPorts(BufferedReader bReader) throws IOException {
        ipList = new ArrayList<String>();
        portList = new ArrayList<String>();
        int amountOfIpAddresses = Integer.parseInt(bReader.readLine());
        for(int i=0;i<amountOfIpAddresses;i++){
            String[] splittedLine = bReader.readLine().split(" ");
            ipList.add(splittedLine[1]);
            portList.add(splittedLine[2]);
        }
    }
}
