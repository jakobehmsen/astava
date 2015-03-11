package parse;
import astava.core.Node;

import java.util.ArrayList;
import java.util.List;

public class CommonMatcher implements Matcher {
    private CommonMatcher parent;
    private Collector collector;
    private ByteSource source;
    private int index;
    private boolean matched;
    private ArrayList<Node> output = new ArrayList<>();

    public CommonMatcher(ByteSource source, int index, CommonMatcher parent, Collector collector) {
        this.source = source;
        this.index = index;
        this.parent = parent;
        this.collector = collector;
    }

    @Override
    public Matcher beginMatch(Collector collector) {
        return new CommonMatcher(source, index, this, collector);
    }

    @Override
    public boolean matched() {
        return matched;
    }

    @Override
    public void match() {
        if(parent != null)
            parent.index = index;

        collector.putAll(output);
        matched = true;
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
}
