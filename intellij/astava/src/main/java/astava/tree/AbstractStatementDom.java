package astava.tree;

import java.util.function.Consumer;

public abstract class AbstractStatementDom implements StatementDom {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof StatementDom &&
            Util.returnFrom(false, r -> ((StatementDom) obj).accept(compare((r))));
    }

    protected abstract StatementDomVisitor compare(Consumer<Boolean> r);
}
