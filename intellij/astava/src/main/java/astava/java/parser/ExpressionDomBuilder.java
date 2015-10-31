package astava.java.parser;

import astava.tree.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExpressionDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitExpressionBuilder(this);
    }
    ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext);

    @Override
    default boolean test(CodeDom code, List<Object> captures) {
        return Util.returnFrom(false, r -> code.accept(new DefaultCodeDomVisitor() {
            @Override
            public void visitExpression(ExpressionDom expressionDom) {
                r.accept(test(expressionDom, captures));
            }
        }));
    }

    default boolean test(ExpressionDom expression, List<Object> captures) {
        return false;
    }
}
