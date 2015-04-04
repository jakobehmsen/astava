package astava.samples.virela.parser;

public abstract class ExpressionReducer<T> implements ExpressionVisitor {
    private T reduction;

    public T getReduction() {
        return reduction;
    }

    protected void reduceTo(T reduction) {
        this.reduction = reduction;
    }
}
