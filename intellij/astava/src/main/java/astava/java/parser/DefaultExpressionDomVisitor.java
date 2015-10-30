package astava.java.parser;

import astava.tree.CodeDom;
import astava.tree.ExpressionDom;
import astava.tree.ExpressionDomVisitor;

import java.util.List;
import java.util.function.BiFunction;

public abstract class DefaultExpressionDomVisitor implements ExpressionDomVisitor {

    @Override
    public void visitBooleanLiteral(boolean value) {

    }

    @Override
    public void visitByteLiteral(byte value) {

    }

    @Override
    public void visitShortLiteral(short value) {

    }

    @Override
    public void visitIntLiteral(int value) {

    }

    @Override
    public void visitLongLiteral(long value) {

    }

    @Override
    public void visitFloatLiteral(float value) {

    }

    @Override
    public void visitDoubleLiteral(double value) {

    }

    @Override
    public void visitCharLiteral(char value) {

    }

    @Override
    public void visitStringLiteral(String value) {

    }

    @Override
    public void visitArithmetic(int operator, ExpressionDom lhs, ExpressionDom rhs) {

    }

    @Override
    public void visitShift(int operator, ExpressionDom lhs, ExpressionDom rhs) {

    }

    @Override
    public void visitBitwise(int operator, ExpressionDom lhs, ExpressionDom rhs) {

    }

    @Override
    public void visitCompare(int operator, ExpressionDom lhs, ExpressionDom rhs) {

    }

    @Override
    public void visitLogical(int operator, ExpressionDom lhs, ExpressionDom rhs) {

    }

    @Override
    public void visitVariableAccess(String name) {

    }

    @Override
    public void visitFieldAccess(ExpressionDom target, String name, String fieldTypeName) {

    }

    @Override
    public void visitStaticFieldAccess(String typeName, String name, String fieldTypeName) {

    }

    @Override
    public void visitNot(ExpressionDom expression) {

    }

    @Override
    public void visitInstanceOf(ExpressionDom expression, String type) {

    }

    @Override
    public void visitBlock(List<CodeDom> codeList) {

    }

    @Override
    public void visitIfElse(ExpressionDom condition, ExpressionDom ifTrue, ExpressionDom ifFalse) {

    }

    @Override
    public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {

    }

    @Override
    public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {

    }

    @Override
    public void visitThis() {

    }

    @Override
    public void visitNull() {

    }

    @Override
    public void visitTop(ExpressionDom expression, BiFunction<ExpressionDom, ExpressionDom, ExpressionDom> usage) {

    }

    @Override
    public void visitDup(String type) {

    }

    @Override
    public void visitLetBe(String type) {

    }

    @Override
    public void visitTypeCast(ExpressionDom expression, String targetType) {

    }

    @Override
    public void visitMethodBody() {

    }
}
