package astava.tree;

import java.util.function.Consumer;

public abstract class AbstractStatementDom implements StatementDom {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof StatementDom &&
            equals((StatementDom)obj, new CodeDomComparison());
    }

    @Override
    public boolean equals(StatementDom other, CodeDomComparison context) {
        return Util.returnFrom(false, r -> other.accept(compare(context, (r))));
    }

    protected abstract StatementDomVisitor compare(CodeDomComparison context, Consumer<Boolean> r);
}
