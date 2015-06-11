package astava.java.parser;

import astava.tree.ExpressionDom;
import astava.tree.StatementDom;

import java.util.Map;

public class Factory {
    public static StatementDomBuilder ret(ExpressionDomBuilder expression) {
        return new StatementDomBuilder() {
            @Override
            public StatementDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.ret(expression.build(classResolver, classDeclaration, classInspector, locals));
            }
        };
    }

    public static ExpressionDomBuilder literal(int value) {
        return new ExpressionDomBuilder() {
            @Override
            public ExpressionDom build(ClassResolver classResolver, ClassDeclaration classDeclaration, ClassInspector classInspector, Map<String, String> locals) {
                return astava.java.Factory.literal(value);
            }
        };
    }
}
