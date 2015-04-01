package astava.parse;

public class Pair<TFirst, TSecond> {
    private TFirst first;
    private TSecond second;

    public Pair(TFirst first, TSecond second) {
        this.first = first;
        this.second = second;
    }

    public TFirst getFirst() {
        return first;
    }

    public TSecond getSecond() {
        return second;
    }
}
