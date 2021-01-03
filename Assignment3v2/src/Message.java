import java.io.Serializable;

public class Message implements Serializable {

    private MessageType type;
    private int round;
    private int value;
    public Message(MessageType type, int r, int w){
        this.type=type;
        this.round =r;
        this.value = w;
    }
    public Message(String typeString, int r, int w){
        type = MessageType.valueOf(typeString);
        this.round = r;
        this.value = w;
    }
    public String toString(){
        return "Type: "+type + ", Round: "+round + ", " +"Value: "+value;
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
