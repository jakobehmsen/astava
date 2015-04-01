package astava.samples.virela;

public interface Expression {
    void accept(ExpressionVisitor visitor);
}
