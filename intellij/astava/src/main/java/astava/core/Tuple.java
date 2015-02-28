package astava.core;

import java.util.Arrays;
import java.util.List;
import java.util.AbstractList;
import java.util.stream.Stream;

public class Tuple extends AbstractList<Node> implements Node {
    private List<Node> children;

    public Tuple(List<Node> children) {
        this.children = children;
    }

    public Tuple(Node... children) {
        this.children = Arrays.asList(children);
    }

    @Override
    public Node get(int index) {
        return children.get(index);
    }

    @Override
    public int size() {
        return children.size();
    }

    public boolean isPair() {
        return size() == 2;
    }

    public boolean isProperty(Object key) {
        return isPair() && get(0) instanceof Atom && ((Atom)get(0)).getValue().equals(key);
    }

    public Stream<Tuple> getTuples() {
        return children.stream()
            .filter(c -> c instanceof Tuple).map(c -> (Tuple)c);
    }

    public Tuple getProperty(Object key) {
        return getTuples()
            .filter(t -> t.isProperty(key))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No such property '" + key + "'."));
    }

    public Node getPropertyValue(Object key) {
        return getProperty(key).getValue();
    }

    public Object getKey() {
        ensureIsProperty();

        return get(0);
    }

    public Node getValue() {
        ensureIsProperty();

        return get(1);
    }

    private void ensureIsProperty() {
        if(!isPair())
            throw new UnsupportedOperationException("Tuple isn't property.");
    }

    public <T> T getValueAs(Class<T> c) {
        Node value = getValue();

        if(!(value instanceof Atom))
            throw new UnsupportedOperationException("Value isnt't Atom.");

        Atom valueAsAtom = (Atom)value;
        if(!c.isInstance(valueAsAtom.getValue()))
            throw new UnsupportedOperationException("Value isnt't " + c.getSimpleName() + "'.");

        return c.cast(valueAsAtom.getValue());
    }

    public <T> T getPropertyValueAs(Object key, Class<T> c) {
        return getProperty(key).getValueAs(c);
    }

    public boolean getBooleanValue() {
        return getValueAs(Boolean.class);
    }

    public byte getByteValue() {
        return getValueAs(Byte.class);
    }

    public short getShortValue() {
        return getValueAs(Short.class);
    }

    public int getIntValue() {
        return getValueAs(Integer.class);
    }

    public long getLongValue() {
        return getValueAs(Long.class);
    }

    public float getFloatValue() {
        return getValueAs(Float.class);
    }

    public double getDoubleValue() {
        return getValueAs(Double.class);
    }

    public char getCharValue() {
        return getValueAs(Character.class);
    }

    public String getStringValue() {
        return getValueAs(String.class);
    }

    public Tuple getTupleProperty(Object key) {
        return (Tuple)getProperty(key).getValue();
    }

    public boolean getBooleanProperty(String key) {
        return getProperty(key).getBooleanValue();
    }

    public byte getByteProperty(String key) {
        return getProperty(key).getByteValue();
    }

    public short getShortProperty(Object key) {
        return getProperty(key).getShortValue();
    }

    public int getIntProperty(Object key) {
        return getProperty(key).getIntValue();
    }

    public long getLongProperty(String key) {
        return getProperty(key).getLongValue();
    }

    public float getFloatProperty(String key) {
        return getProperty(key).getFloatValue();
    }

    public double getDoubleProperty(String key) {
        return getProperty(key).getDoubleValue();
    }

    public char getCharProperty(String key) {
        return getProperty(key).getCharValue();
    }

    public String getStringProperty(String keyName) {
        return getProperty(keyName).getStringValue();
    }

    public static Tuple newProperty(Object key, Node value) {
        return new Tuple(Arrays.asList(new Atom(key), value));
    }
}
