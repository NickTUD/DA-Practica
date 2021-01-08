import java.io.Serializable;

/**
 * Class that represents a message
 */
public class Message implements Serializable {

    /**
     * Each message has a type: either notification or proposing
     */
    private MessageType type;

    /**
     * Each message has a round in which it is sent.
     */
    private int round;

    /**
     * Each message has a binary value it wants to send to other processes.
     */
    private int value;

    /**
     * Index which serves as the processID of the sender.
     */
    private int senderIndex;

    public Message(String typeString, int r, int w,int senderIndex){
        type = MessageType.valueOf(typeString);
        this.round = r;
        this.value = w;
        this.senderIndex=senderIndex;
    }
    public String toString(){
        return "Type: "+type + ", Round: "+round + ", " +"Value: "+value+", sent by Process: "+senderIndex;
    }
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
