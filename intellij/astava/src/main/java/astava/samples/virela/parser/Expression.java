package astava.samples.virela.parser;

public interface Expression {
    void accept(ExpressionVisitor visitor);
    default <T> T reduce(ExpressionReducer<T> reducer) {
        accept(reducer);
        return reducer.getReduction();
    }
}
