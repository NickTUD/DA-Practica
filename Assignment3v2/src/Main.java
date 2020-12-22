public class Main {
    public static void main(String[] args){
        if(args.length!=1){
            System.out.println("Arguments incorrectly set. Go to Run configurations and assign 0 or 1 to the program arguments");
            System.out.println("Choose 0 if your name is Alan and choose 1 if your name is Nick.");
            System.exit(1);
        }
        String myUserIDString = args[0];
        int myUserID = Integer.parseInt(myUserIDString);


        System.out.println();
        System.out.println("hello world");
    }
}
