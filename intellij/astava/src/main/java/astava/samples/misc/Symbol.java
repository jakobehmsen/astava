package astava.samples.misc;

public class Symbol {
    public final String str;

    public Symbol(String str) {
        this.str = str;
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Symbol && str.equals(((Symbol)obj).str);
    }

    @Override
    public String toString() {
        return str;
    }
}
