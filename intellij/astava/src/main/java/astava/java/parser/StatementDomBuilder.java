package astava.java.parser;

import astava.tree.CodeDom;
import astava.tree.DefaultCodeDomVisitor;
import astava.tree.StatementDom;
import astava.tree.Util;

import java.util.List;
import java.util.Map;

public interface StatementDomBuilder extends DomBuilder {
    default void accept(DomBuilderVisitor visitor) {
        visitor.visitStatementBuilder(this);
    }

    default void appendLocals(Map<String, String> locals) { }
    StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals, MethodDeclaration methodContext, Map<String, Object> captures);

    @Override
    default boolean test(CodeDom code, Map<String, Object> captures) {
        return Util.returnFrom(false, r -> code.accept(new DefaultCodeDomVisitor() {
            @Override
            public void visitStatement(StatementDom statementDom) {
                r.accept(test(statementDom, captures));
            }
        }));
    }

    default boolean test(StatementDom statement, Map<String, Object> captures) {
        return false;
    }
}
