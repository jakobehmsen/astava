package astava.samples.virela.parser;

public interface Expression {
    void accept(ExpressionVisitor visitor);
}
