package parse;
import astava.core.Node;

import java.util.ArrayList;
import java.util.List;

public class CommonMatcher implements Matcher, ErrorHandler {
    private CommonMatcher parent;
    private Collector collector;
    private ErrorHandler errorHandler;
    private ByteSource source;
    private int index;
    private int state = UNDETERMINED;
    private ArrayList<Node> output = new ArrayList<>();
    private ArrayList<ErrorInfo> errors = new ArrayList<>();

    public CommonMatcher(ByteSource source, Collector collector, ErrorHandler errorHandler) {
        this(source, 0, null, collector, errorHandler);
    }

    public CommonMatcher(ByteSource source, int index, CommonMatcher parent, Collector collector, ErrorHandler errorHandler) {
        this.source = source;
        this.index = index;
        this.parent = parent;
        this.collector = collector;
        this.errorHandler = errorHandler;
    }

    @Override
    public Matcher beginMatch(Collector collector) {
        return new CommonMatcher(source, index, this, collector, this);
    }

    @Override
    public int state() {
        return state;
    }

    @Override
    public void accept() {
        if(parent != null)
            parent.index = index;

        collector.putAll(output);
        state = ACCEPTED;
    }

    @Override
    public void reject() {
        errors.forEach(e -> errorHandler.reportError(e.getIndex(), e.getMessage()));
        state = REJECTED;
    }

    @Override
    public void error(String message) {
        reportError(index, message);
    }

    @Override
    public int peekByte() {
        return source.get(index);
    }

    @Override
    public void consume() {
        index++;
    }

    @Override
    public void put(Node node) {
        output.add(node);
    }

    @Override
    public void putAll(List<Node> nodes) {
        output.addAll(nodes);
    }

    @Override
    public void reportError(int index, String message) {
        errors.add(new ErrorInfo(index, message));
    }
}
