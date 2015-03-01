package astava.core;

public class Atom implements Node {
    private Object value;

    public Atom(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean subsumes(Node other) {
        return other instanceof Atom && this.getValue().equals(((Atom)other).getValue());
    }
}
