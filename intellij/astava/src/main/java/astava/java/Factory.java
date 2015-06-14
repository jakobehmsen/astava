package astava.java;

import astava.tree.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Factory {
    public static ClassDom classDeclaration(int modifiers, String name, String superName, List<String> interfaces, List<FieldDom> fields, List<MethodDom> methods) {
        return new ClassDom() {
            @Override
            public int getModifiers() {
                return modifiers;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getSuperName() {
                return superName;
            }

            @Override
            public List<String> getInterfaces() {
                return interfaces;
            }

            @Override
            public List<FieldDom> getFields() {
                return fields;
            }

            @Override
            public List<MethodDom> getMethods() {
                return methods;
            }
        };
    }

    public static FieldDom fieldDeclaration(int modifiers, String name, String typeName) {
        return new FieldDom() {
            @Override
            public int getModifiers() {
                return modifiers;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getTypeName() {
                return typeName;
            }
        };
    }

    public static MethodDom methodDeclaration(int modifiers, String name, List<ParameterInfo> parameters, String returnType, StatementDom body) {
        return new MethodDom() {
            @Override
            public int getModifier() {
                return modifiers;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<ParameterInfo> getParameterTypes() {
                return parameters;
            }

            @Override
            public String getReturnTypeName() {
                return returnType;
            }

            @Override
            public StatementDom getBody() {
                return body;
            }
        };
    }

    public static StatementDom ret() {
        return v -> v.visitReturn();
    }

    public static StatementDom ret(ExpressionDom expression) {
        return v -> v.visitReturnValue(expression);
    }

    public static ExpressionDom literal(boolean value) {
        return v -> v.visitBooleanLiteral(value);
    }

    public static ExpressionDom literal(byte value) {
        return v -> v.visitByteLiteral(value);
    }

    public static ExpressionDom literal(short value) {
        return v -> v.visitShortLiteral(value);
    }

    public static ExpressionDom literal(int value) {
        return v -> v.visitIntLiteral(value);
    }

    public static ExpressionDom literal(long value) {
        return v -> v.visitLongLiteral(value);
    }

    public static ExpressionDom literal(float value) {
        return v -> v.visitFloatLiteral(value);
    }

    public static ExpressionDom literal(double value) {
        return v -> v.visitDoubleLiteral(value);
    }

    public static ExpressionDom literal(char value) {
        return v -> v.visitCharLiteral(value);
    }

    public static ExpressionDom literal(String value) {
        return v -> v.visitStringLiteral(value);
    }

    public static ExpressionDom nil() {
        return v -> v.visitNull();
    }

    public static ExpressionDom add(ExpressionDom lhs, ExpressionDom rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.ADD);
    }

    public static ExpressionDom rem(ExpressionDom lhs, ExpressionDom rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.REM);
    }

    public static ExpressionDom sub(ExpressionDom lhs, ExpressionDom rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.SUB);
    }

    public static ExpressionDom mul(ExpressionDom lhs, ExpressionDom rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.MUL);
    }

    public static ExpressionDom div(ExpressionDom lhs, ExpressionDom rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.DIV);
    }

    public static ExpressionDom arithmetic(ExpressionDom lhs, ExpressionDom rhs, int operator) {
        return v -> v.visitArithmetic(operator, lhs, rhs);
    }

    public static ExpressionDom shl(ExpressionDom lhs, ExpressionDom rhs) {
        return shift(lhs, rhs, ShiftOperator.SHL);
    }

    public static ExpressionDom shr(ExpressionDom lhs, ExpressionDom rhs) {
        return shift(lhs, rhs, ShiftOperator.SHR);
    }

    public static ExpressionDom ushr(ExpressionDom lhs, ExpressionDom rhs) {
        return shift(lhs, rhs, ShiftOperator.USHR);
    }

    public static ExpressionDom shift(ExpressionDom lhs, ExpressionDom rhs, int operator) {
        return v -> v.visitShift(operator, lhs, rhs);
    }

    public static ExpressionDom band(ExpressionDom lhs, ExpressionDom rhs) {
        return bitwise(lhs, rhs, BitwiseOperator.AND);
    }

    public static ExpressionDom bor(ExpressionDom lhs, ExpressionDom rhs) {
        return bitwise(lhs, rhs, BitwiseOperator.OR);
    }

    public static ExpressionDom bxor(ExpressionDom lhs, ExpressionDom rhs) {
        return bitwise(lhs, rhs, BitwiseOperator.XOR);
    }

    public static ExpressionDom bitwise(ExpressionDom lhs, ExpressionDom rhs, int operator) {
        return v -> v.visitBitwise(operator, lhs, rhs);
    }

    public static ExpressionDom and(ExpressionDom lhs, ExpressionDom rhs) {
        return logical(lhs, rhs, LogicalOperator.AND);
    }

    public static ExpressionDom or(ExpressionDom lhs, ExpressionDom rhs) {
        return logical(lhs, rhs, LogicalOperator.OR);
    }

    public static ExpressionDom logical(ExpressionDom lhs, ExpressionDom rhs, int operator) {
        return v -> v.visitLogical(operator, lhs, rhs);
    }

    public static ExpressionDom lt(ExpressionDom lhs, ExpressionDom rhs) {
        return compare(lhs, rhs, RelationalOperator.LT);
    }

    public static ExpressionDom le(ExpressionDom lhs, ExpressionDom rhs) {
        return compare(lhs, rhs, RelationalOperator.LE);
    }

    public static ExpressionDom gt(ExpressionDom lhs, ExpressionDom rhs) {
        return compare(lhs, rhs, RelationalOperator.GT);
    }

    public static ExpressionDom ge(ExpressionDom lhs, ExpressionDom rhs) {
        return compare(lhs, rhs, RelationalOperator.GE);
    }

    public static ExpressionDom eq(ExpressionDom lhs, ExpressionDom rhs) {
        return compare(lhs, rhs, RelationalOperator.EQ);
    }

    public static ExpressionDom ne(ExpressionDom lhs, ExpressionDom rhs) {
        return compare(lhs, rhs, RelationalOperator.NE);
    }

    public static ExpressionDom compare(ExpressionDom lhs, ExpressionDom rhs, int operator) {
        return v -> v.visitCompare(operator, lhs, rhs);
    }

    public static StatementDom declareVar(String type, String name) {
        return v -> v.visitVariableDeclaration(type, name);
    }

    // Assign variable as statement (not expression)
    public static StatementDom assignVar(String name, ExpressionDom expression) {
        return v -> v.visitVariableAssignment(name, expression);
    }

    public static StatementDom assignField(ExpressionDom target, String name, String type, ExpressionDom expression) {
        return v -> v.visitFieldAssignment(target, name, type, expression);
    }

    public static StatementDom assignStaticField(String targetTypeName, String name, String type, ExpressionDom expression) {
        return v -> v.visitStaticFieldAssignment(targetTypeName, name, type, expression);
    }

    public static ExpressionDom accessVar(String name) {
        return v -> v.visitVariableAccess(name);
    }

    public static ExpressionDom accessField(ExpressionDom target, String name, String fieldTypeName) {
        return v -> v.visitFieldAccess(target, name, fieldTypeName);
    }

    public static ExpressionDom accessStaticField(String targetTypeName, String name, String fieldTypeName) {
        return v -> v.visitStaticFieldAccess(targetTypeName, name, fieldTypeName);
    }

    public static ExpressionDom self() {
        return v -> v.visitThis();
    }

    public static StatementDom block(List<StatementDom> statements) {
        return v -> v.visitBlock(statements);
    }

    // At most one expression dom
    public static ExpressionDom blockExpr(List<CodeDom> codeList) {
        return v -> v.visitBlock(codeList);
    }

    public static ExpressionDom top(ExpressionDom expression, BiFunction<ExpressionDom, ExpressionDom, ExpressionDom> usage) {
        return v -> v.visitTop(expression, usage);
    }

    public static StatementDom intIncVar(String name, int amount) {
        return v -> v.visitIncrement(name, amount);
    }

    public static ExpressionDom not(ExpressionDom expression) {
        return v -> v.visitNot(expression);
    }

    // If-else-statement (not expression)
    public static StatementDom ifElse(ExpressionDom condition, StatementDom ifTrue, StatementDom ifFalse) {
        return v -> v.visitIfElse(condition, ifTrue, ifFalse);
    }

    // If-else-expression (not statement)
    public static ExpressionDom ifElseExpr(ExpressionDom condition, ExpressionDom ifTrue, ExpressionDom ifFalse) {
        return v -> v.visitIfElse(condition, ifTrue, ifFalse);
    }

    public static StatementDom breakOption() {
        return v -> v.visitBreakCase();
    }

    public static ExpressionDom instanceOf(ExpressionDom expression, String type) {
        return v -> v.visitInstanceOf(expression, type);
    }

    public static StatementDom invokeInterface(String type, String name, String methodDescriptor, ExpressionDom target, List<ExpressionDom> arguments) {
        return invoke(Invocation.INTERFACE, type, name, methodDescriptor, target, arguments);
    }

    public static StatementDom invokeStatic(String type, String name, String methodDescriptor, List<ExpressionDom> arguments) {
        return invoke(Invocation.STATIC, type, name, methodDescriptor, null, arguments);
    }

    public static StatementDom invokeVirtual(String type, String name, String methodDescriptor, ExpressionDom target, List<ExpressionDom> arguments) {
        return invoke(Invocation.VIRTUAL, type, name, methodDescriptor, target, arguments);
    }

    public static StatementDom invokeSpecial(String type, String name, String methodDescriptor, ExpressionDom target, List<ExpressionDom> arguments) {
        return invoke(Invocation.SPECIAL, type, name, methodDescriptor, target, arguments);
    }

    public static StatementDom invoke(int invocation, String type, String name, String methodDescriptor, ExpressionDom target /*null for static*/, List<ExpressionDom> arguments) {
        return v -> v.visitInvocation(invocation, target, type, name, methodDescriptor, arguments);
    }

    public static ExpressionDom invokeStaticExpr(String type, String name, String methodDescriptor, List<ExpressionDom> arguments) {
        return invokeExpr(Invocation.STATIC, type, name, methodDescriptor, null, arguments);
    }

    public static ExpressionDom invokeExpr(int invocation, String type, String name, String methodDescriptor, ExpressionDom target /*null for static*/, List<ExpressionDom> arguments) {
        return v -> v.visitInvocation(invocation, target, type, name, methodDescriptor, arguments);
    }

    public static StatementDom newInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
        return v -> v.visitNewInstance(type, parameterTypes, arguments);
    }

    public static ExpressionDom newInstanceExpr(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
        return v -> v.visitNewInstance(type, parameterTypes, arguments);
    }

    public static StatementDom label(String name) {
        return v -> v.visitLabel(name);
    }

    public static StatementDom goTo(String name) {
        return v -> v.visitGoTo(name);
    }

    // What about support for select expressions?
    public static StatementDom select(ExpressionDom expression, List<Map.Entry<Integer, StatementDom>> cases, StatementDom defaultBody) {
        Map<Integer, StatementDom> casesMap = cases.stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        return v -> v.visitSwitch(expression, casesMap, defaultBody);
    }

    public static Map.Entry<Integer, StatementDom> option(int key, StatementDom body) {
        return new AbstractMap.SimpleImmutableEntry<Integer, StatementDom>(key, body);
    }

    public static String arithmeticResultType(String lhsType, String rhsType) {
        switch(lhsType) {
            case Descriptor.BYTE:
                switch (rhsType) {
                    case Descriptor.BYTE: return lhsType;
                    case Descriptor.SHORT: return rhsType;
                    case Descriptor.INT: return rhsType;
                }
                break;
            case Descriptor.SHORT:
                switch (rhsType) {
                    case Descriptor.BYTE: return lhsType;
                    case Descriptor.SHORT: return lhsType;
                    case Descriptor.INT: return rhsType;
                }
                break;
            case Descriptor.INT:
                switch (rhsType) {
                    case Descriptor.BYTE: return lhsType;
                    case Descriptor.SHORT: return lhsType;
                    case Descriptor.INT: return lhsType;
                }
                break;
            case Descriptor.LONG:
                switch (rhsType) {
                    case Descriptor.LONG: return lhsType;
                }
                break;
            case Descriptor.FLOAT:
                switch (rhsType) {
                    case Descriptor.FLOAT: return lhsType;
                }
                break;
            case Descriptor.DOUBLE:
                switch (rhsType) {
                    case Descriptor.DOUBLE: return rhsType;
                }
                break;
        }

        return null;
    }

    public static String shiftResultType(String lhsType, String rhsType) {
        if(rhsType.equals(Descriptor.INT)) {
            switch (lhsType) {
                case Descriptor.BYTE:
                case Descriptor.SHORT:
                case Descriptor.INT:
                    return Descriptor.INT;
                case Descriptor.LONG:
                    return Descriptor.LONG;
            }
        }

        return null;
    }

    public static String bitwiseResultType(String lhsType, String rhsType) {
        switch (lhsType) {
            case Descriptor.BYTE:
            case Descriptor.SHORT:
            case Descriptor.INT:
                switch (rhsType) {
                    case Descriptor.BYTE:
                    case Descriptor.SHORT:
                    case Descriptor.INT:
                        return Descriptor.INT;
                }
                break;
            case Descriptor.LONG:
                switch (rhsType) {
                    case Descriptor.LONG:
                        return Descriptor.LONG;
                }
                break;
        }

        return null;
    }

    public static String logicalResultType(String lhsType, String rhsType) {
        return lhsType.equals(Descriptor.BOOLEAN) && rhsType.equals(Descriptor.BOOLEAN) ? Descriptor.BOOLEAN : null;
    }

    public static String compareResultType(String lhsType, String rhsType) {
        switch(lhsType) {
            case Descriptor.BYTE:
            case Descriptor.SHORT:
            case Descriptor.INT:
                switch (rhsType) {
                    case Descriptor.BYTE:
                    case Descriptor.SHORT:
                    case Descriptor.INT:
                        return Descriptor.BOOLEAN;
                }
        }

        return lhsType.equals(rhsType) ? Descriptor.BOOLEAN : null;
    }
}
