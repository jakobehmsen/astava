package astava.tree;

import java.util.function.Consumer;

public abstract class AbstractExpressionDom implements ExpressionDom {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ExpressionDom &&
            equals((ExpressionDom)obj, new CodeDomComparison());
    }

    @Override
    public boolean equals(ExpressionDom other, CodeDomComparison context) {
        return Util.returnFrom(false, r -> other.accept(compare(context, (r))));
    }

    protected abstract ExpressionDomVisitor compare(CodeDomComparison context, Consumer<Boolean> r);
}
