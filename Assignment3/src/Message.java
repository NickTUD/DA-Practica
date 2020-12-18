import java.io.Serializable;

public class Message implements Serializable {

    private final MessageType type;
    private final int round;
    private int value;

    public Message(MessageType type, int round, int value) {
        this.type = type;
        this.round = round;
        this.value = value;
    }

    public MessageType getType() {
        return type;
    }

    public int getRound() {
        return round;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value){
        this.value = value;
    }

    public enum MessageType {
        NOTIF, PROP
    }
}

