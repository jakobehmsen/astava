package astava.tree;

import java.util.function.Consumer;

public abstract class AbstractExpressionDom implements ExpressionDom {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExpressionDom &&
            Util.returnFrom(false, r -> ((ExpressionDom) obj).accept(compare((r))));
    }

    protected abstract ExpressionDomVisitor compare(Consumer<Boolean> r);
}
