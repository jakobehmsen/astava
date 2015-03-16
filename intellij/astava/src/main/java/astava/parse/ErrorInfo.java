package astava.parse;

public class ErrorInfo {
    private int index;
    private String message;

    public ErrorInfo(int index, String message) {
        this.index = index;
        this.message = message;
    }

    public int getIndex() {
        return index;
    }

    public String getMessage() {
        return message;
    }
}
