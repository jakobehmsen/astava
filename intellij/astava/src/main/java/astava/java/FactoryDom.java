package astava.java;

import astava.tree.*;

import java.util.Arrays;
import java.util.List;

public class FactoryDom {
    public static int astType(Tuple tuple) {
        return (int)((Atom)tuple.get(0)).getValue();
    }

    public static ClassDom classDeclaration(int modifier, String name, String superName, List<FieldDom> fields, List<MethodDom> methods) {
        return new ClassDom() {
            @Override
            public int getModifier() {
                return modifier;
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
            public List<FieldDom> getFields() {
                return fields;
            }

            @Override
            public List<MethodDom> getMethods() {
                return methods;
            }
        };
    }

    public static int classDeclarationModifier(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static String classDeclarationName(Tuple tuple) {
        return (String)((Atom)tuple.get(2)).getValue();
    }

    public static String classDeclarationSuperName(Tuple tuple) {
        return (String)((Atom)tuple.get(3)).getValue();
    }

    public static List<Node> classDeclarationMembers(Tuple tuple) {
        return (Tuple)tuple.get(4);
    }

    public static MethodDom methodDeclaration(int modifier, String name, List<String> parameterTypes, String returnType, StatementDom body) {
        return new MethodDom() {
            @Override
            public int getModifier() {
                return modifier;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<String> getParameterTypes() {
                return parameterTypes;
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

    public static int methodDeclarationModifier(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static String methodDeclarationName(Tuple tuple) {
        return (String)((Atom)tuple.get(2)).getValue();
    }

    public static List<String> methodDeclarationParameterTypes(Tuple tuple) {
        return (List<String>)((Atom)tuple.get(3)).getValue();
    }

    public static String methodDeclarationReturnType(Tuple tuple) {
        return (String)((Atom)tuple.get(4)).getValue();
    }

    public static Tuple methodDeclarationBody(Tuple tuple) {
        return (Tuple)tuple.get(5);
    }

    public static Tuple ret() {
        return new Tuple(Arrays.asList(
            new Atom(ASTType.RETURN_STATEMENT)
        ));
    }

    public static StatementDom ret(ExpressionDom expression) {
        return new StatementDom() {
            @Override
            public void accept(StatementDomVisitor visitor) {
                visitor.visitReturnValue(expression);
            }
        };
    }

    public static Tuple retExpression(Tuple tuple) {
        return (Tuple)tuple.get(1);
    }

    public static ExpressionDom literal(boolean value) {
        return v -> v.visitBooleanLiteral(value);
    }

    public static boolean literalBoolean(Tuple tuple) {
        return (boolean)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(byte value) {
        return v -> v.visitByteLiteral(value);
    }

    public static byte literalByte(Tuple tuple) {
        return (byte)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(short value) {
        return v -> v.visitShortLiteral(value);
    }

    public static short literalShort(Tuple tuple) {
        return (short)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(int value) {
        return v -> v.visitIntLiteral(value);
    }

    public static int literalInt(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(long value) {
        return v -> v.visitLongLiteral(value);
    }

    public static long literalLong(Tuple tuple) {
        return (long)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(float value) {
        return v -> v.visitFloatLiteral(value);
    }

    public static float literalFloat(Tuple tuple) {
        return (float)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(double value) {
        return v -> v.visitDoubleLiteral(value);
    }

    public static double literalDouble(Tuple tuple) {
        return (double)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(char value) {
        return v -> v.visitCharLiteral(value);
    }

    public static char literalChar(Tuple tuple) {
        return (char)((Atom)tuple.get(1)).getValue();
    }

    public static ExpressionDom literal(String value) {
        return v -> v.visitStringLiteral(value);
    }

    public static String literalString(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple add(Tuple lhs, Tuple rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.ADD);
    }

    public static Tuple rem(Tuple lhs, Tuple rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.REM);
    }

    public static Tuple sub(Tuple lhs, Tuple rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.SUB);
    }

    public static Tuple mul(Tuple lhs, Tuple rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.MUL);
    }

    public static Tuple div(Tuple lhs, Tuple rhs) {
        return arithmetic(lhs, rhs, ArithmeticOperator.DIV);
    }

    public static Tuple arithmetic(Tuple lhs, Tuple rhs, int operator) {
        return new Tuple(
            new Atom(ASTType.ARITHMETIC),
            new Atom(operator),
            lhs,
            rhs
        );
    }

    public static int arithmeticOperator(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple arithmeticLhs(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple arithmeticRhs(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static Tuple shl(Tuple lhs, Tuple rhs) {
        return shift(lhs, rhs, ShiftOperator.SHL);
    }

    public static Tuple shr(Tuple lhs, Tuple rhs) {
        return shift(lhs, rhs, ShiftOperator.SHR);
    }

    public static Tuple ushr(Tuple lhs, Tuple rhs) {
        return shift(lhs, rhs, ShiftOperator.USHR);
    }

    public static Tuple shift(Tuple lhs, Tuple rhs, int operator) {
        return new Tuple(
            new Atom(ASTType.SHIFT),
            new Atom(operator),
            lhs,
            rhs
        );
    }

    public static int shiftOperator(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple shiftLhs(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple shiftRhs(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static Tuple band(Tuple lhs, Tuple rhs) {
        return bitwise(lhs, rhs, BitwiseOperator.AND);
    }

    public static Tuple bor(Tuple lhs, Tuple rhs) {
        return bitwise(lhs, rhs, BitwiseOperator.OR);
    }

    public static Tuple bxor(Tuple lhs, Tuple rhs) {
        return bitwise(lhs, rhs, BitwiseOperator.XOR);
    }

    public static Tuple bitwise(Tuple lhs, Tuple rhs, int operator) {
        return new Tuple(
            new Atom(ASTType.BITWISE),
            new Atom(operator),
            lhs,
            rhs
        );
    }

    public static int bitwiseOperator(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple bitwiseLhs(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple bitwiseRhs(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static Tuple and(Tuple lhs, Tuple rhs) {
        return logical(lhs, rhs, LogicalOperator.AND);
    }

    public static Tuple or(Tuple lhs, Tuple rhs) {
        return logical(lhs, rhs, LogicalOperator.OR);
    }

    public static Tuple logical(Tuple lhs, Tuple rhs, int operator) {
        return new Tuple(
            new Atom(ASTType.LOGICAL),
            new Atom(operator),
            lhs,
            rhs
        );
    }

    public static int logicalOperator(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple logicalLhs(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple logicalRhs(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static Tuple lt(Tuple lhs, Tuple rhs) {
        return compare(lhs, rhs, RelationalOperator.LT);
    }

    public static Tuple le(Tuple lhs, Tuple rhs) {
        return compare(lhs, rhs, RelationalOperator.LE);
    }

    public static Tuple gt(Tuple lhs, Tuple rhs) {
        return compare(lhs, rhs, RelationalOperator.GT);
    }

    public static Tuple ge(Tuple lhs, Tuple rhs) {
        return compare(lhs, rhs, RelationalOperator.GE);
    }

    public static Tuple eq(Tuple lhs, Tuple rhs) {
        return compare(lhs, rhs, RelationalOperator.EQ);
    }

    public static Tuple ne(Tuple lhs, Tuple rhs) {
        return compare(lhs, rhs, RelationalOperator.NE);
    }

    public static Tuple compare(Tuple lhs, Tuple rhs, int operator) {
        return new Tuple(
            new Atom(ASTType.COMPARE),
            new Atom(operator),
            lhs,
            rhs
        );
    }

    public static int compareOperator(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple compareLhs(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple compareRhs(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static Tuple declareVar(String type, String name) {
        return new Tuple(
            new Atom(ASTType.VARIABLE_DECLARATION),
            new Atom(type),
            new Atom(name)
        );
    }

    public static String declareVarType(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static String declareVarName(Tuple tuple) {
        return (String)((Atom)tuple.get(2)).getValue();
    }

    public static Tuple assignVar(String name, Tuple expression) {
        return new Tuple(
            new Atom(ASTType.VARIABLE_ASSIGNMENT),
            new Atom(name),
            expression
        );
    }

    public static String assignVarName(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple assignVarExpression(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple accessVar(String name) {
        return new Tuple(
            new Atom(ASTType.VARIABLE_ACCESS),
            new Atom(name)
        );
    }

    public static String accessVarName(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static StatementDom block(List<StatementDom> statements) {
        return v -> v.visitBlock(statements);
    }

    public static List<Node> blockStatements(Tuple tuple) {
        return (Tuple)tuple.get(1);
    }

    public static Tuple intIncVar(String name, int amount) {
        return new Tuple(
            new Atom(ASTType.INCREMENT),
            new Atom(name),
            new Atom(amount)
        );
    }

    public static String intIncVarName(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static int intIncVarAmount(Tuple tuple) {
        return (int)((Atom)tuple.get(2)).getValue();
    }

    public static ExpressionDom not(ExpressionDom expression) {
        return v -> v.visitNot(expression);
    }

    public static Tuple notExpression(Tuple tuple) {
        return (Tuple)tuple.get(1);
    }

    // If-else-statement (not expression)
    public static StatementDom ifElse(ExpressionDom condition, StatementDom ifTrue, StatementDom ifFalse) {
        return v -> v.visitIfElse(condition, ifTrue, ifFalse);
    }

    public static Tuple ifElseCondition(Tuple tuple) {
        return (Tuple)tuple.get(1);
    }

    public static Tuple ifElseIfTrue(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple ifElseIfFalse(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static Tuple breakOption() {
        return new Tuple(Arrays.asList(
            new Atom(ASTType.BREAK_CASE)
        ));
    }

    public static Tuple instanceOf(Tuple expression, String type) {
        return new Tuple(
            new Atom(ASTType.INSTANCE_OF),
            expression,
            new Atom(type)
        );
    }

    public static Tuple instanceOfExpression(Tuple tuple) {
        return (Tuple)tuple.get(1);
    }

    public static String instanceOfType(Tuple tuple) {
        return (String)((Atom)tuple.get(2)).getValue();
    }

    public static Tuple invokeInterface(String type, String name, String methodDescriptor, Node target, List<Node> arguments) {
        return invoke(Invocation.INTERFACE, type, name, methodDescriptor, target, arguments);
    }

    public static Tuple invokeStatic(String type, String name, String methodDescriptor, List<Node> arguments) {
        return invoke(Invocation.STATIC, type, name, methodDescriptor, null, arguments);
    }

    public static Tuple invokeVirtual(String type, String name, String methodDescriptor, Node target, List<Node> arguments) {
        return invoke(Invocation.VIRTUAL, type, name, methodDescriptor, target, arguments);
    }

    public static Tuple invoke(int invocation, String type, String name, String methodDescriptor, Node target /*null for static*/, List<Node> arguments) {
        return new Tuple(
            new Atom(ASTType.INVOCATION),
            new Atom(invocation),
            new Atom(type),
            new Atom(name),
            new Atom(methodDescriptor),
            target,
            new Tuple(arguments)
        );
    }

    public static int invokeInvocation(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static String invokeType(Tuple tuple) {
        return (String)((Atom)tuple.get(2)).getValue();
    }

    public static String invokeName(Tuple tuple) {
        return (String)((Atom)tuple.get(3)).getValue();
    }

    public static String invokeMethodDescriptor(Tuple tuple) {
        return (String)((Atom)tuple.get(4)).getValue();
    }

    public static Tuple invokeTarget(Tuple tuple) {
        return (Tuple)tuple.get(5);
    }

    public static List<Node> invokeMethodArguments(Tuple tuple) {
        return (Tuple)tuple.get(6);
    }

    public static Tuple newInstance(String type, List<String> parameterTypes, List<Node> arguments) {
        return new Tuple(
            new Atom(ASTType.NEW_INSTANCE),
            new Atom(type),
            new Atom(parameterTypes),
            new Tuple(arguments)
        );
    }

    public static String newInstanceType(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static List<String> newInstanceParameterTypes(Tuple tuple) {
        return (List<String>)((Atom)tuple.get(2)).getValue();
    }

    public static List<Node> newInstanceArguments(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static StatementDom label(String name) {
        return v -> v.visitLabel(name);
    }

    public static String labelName(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static StatementDom goTo(String name) {
        return v -> v.visitGoTo(name);
    }

    public static String goToName(Tuple tuple) {
        return (String)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple select(Tuple expression, List<Node> cases, Tuple defaultBody) {
        return new Tuple(
            new Atom(ASTType.SWITCH),
            expression,
            new Tuple(cases),
            defaultBody
        );
    }

    public static Tuple selectExpression(Tuple tuple) {
        return (Tuple)tuple.get(1);
    }

    public static List<Node> selectOptions(Tuple tuple) {
        return (Tuple)tuple.get(2);
    }

    public static Tuple selectDefault(Tuple tuple) {
        return (Tuple)tuple.get(3);
    }

    public static Tuple option(int key, Tuple body) {
        return new Tuple(
            new Atom(ASTType.CASE),
            new Atom(key),
            body
        );
    }

    public static int optionKey(Tuple tuple) {
        return (int)((Atom)tuple.get(1)).getValue();
    }

    public static Tuple optionBody(Tuple tuple) {
        return (Tuple)tuple.get(2);
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
