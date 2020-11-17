/**
 * Class which represents the messages that can be sent between processes.
 */
public class Message {

    private final String text;
    private final int[] clock;
    private final int fromIndex;

    /**
     * Constructor for the special message class.
     * @param text String representing the message content.
     * @param clock Clock of the process. Represents the clock at the moment of sending the message.
     * @param fromIndex The id/index of the process.
     */
    Message(String text, int[] clock, int fromIndex){
        this.text = text;
        this.clock = clock;
        this.fromIndex = fromIndex;
    }

    public String getText() {
        return this.text;
    }

    public int[] getClock() {
        return this.clock;
    }

    public int getFromIndex() {
        return this.fromIndex;
    }

}
