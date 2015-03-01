package astava.java.gen;

import astava.core.Node;

public class Message {
    private String message;
    private Node target;

    public Message(String message, Node target) {
        this.message = message;
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public Node getTarget() {
        return target;
    }
}
