package astava.java.parser;

import astava.tree.*;

import java.util.Map;

public interface ExpressionDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitExpressionBuilder(this);
    }
    ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext, Map<String, Object> captures);

    @Override
    default boolean test(CodeDom code, Map<String, Object> captures) {
        return Util.returnFrom(false, r -> code.accept(new DefaultCodeDomVisitor() {
            @Override
            public void visitExpression(ExpressionDom expressionDom) {
                r.accept(test(expressionDom, captures));
            }
        }));
    }

    default boolean test(ExpressionDom expression, Map<String, Object> captures) {
        return false;
    }
}
