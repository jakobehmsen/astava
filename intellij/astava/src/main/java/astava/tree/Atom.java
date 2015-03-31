package astava.tree;

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

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Atom && this.value.equals(((Atom)obj).value);
    }
}
