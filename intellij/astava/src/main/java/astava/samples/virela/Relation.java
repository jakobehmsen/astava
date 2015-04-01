package astava.samples.virela;

public class Relation {
    private String id;
    private Expression value;

    public Relation(String id, Expression value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public Expression getValue() {
        return value;
    }
}
