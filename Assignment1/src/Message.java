import java.io.Serializable;
import java.util.Arrays;

/**
 * Class which represents the messages that can be sent between processes.
 */
public class Message implements Serializable {

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
    public String toString(){
        return "Text: " + text + ", Clock: "+ Arrays.toString(clock) + ", fromindex: "+ fromIndex;
    }
    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Message)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Message c = (Message) o;

        // Compare the data members and return accordingly
        return this.toString().equals(c.toString());
    }

}
