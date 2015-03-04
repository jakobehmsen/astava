package astava.java;

import astava.core.Atom;
import astava.core.Node;
import astava.core.Tuple;

import java.util.Arrays;
import java.util.List;

public class Factory {
    public static Tuple classDeclaration(int modifier, String name, String superName, List<Node> members) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.CLASS)),
            Tuple.newProperty(Property.KEY_MODIFIER, new Atom(modifier)),
            Tuple.newProperty(Property.KEY_NAME, new Atom(name)),
            Tuple.newProperty(Property.KEY_SUPER_NAME, new Atom(superName)),
            Tuple.newProperty(Property.KEY_MEMBERS, new Tuple(members))
        );
    }

    public static Tuple methodDeclaration(int modifier, String name, List<String> parameterTypes, String returnType, Tuple body) {
        /*String bodyReturnType = getReturnType(body); // May result in multiple return types


        if(!areCompatible(returnType, bodyReturnType))
            throw new IllegalArgumentException("Declared return type '" + returnType + "' is incompatible with actual return type '" + bodyReturnType + "'.");
        */

        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.METHOD)),
            Tuple.newProperty(Property.KEY_MODIFIER, new Atom(modifier)),
            Tuple.newProperty(Property.KEY_NAME, new Atom(name)),
            Tuple.newProperty(Property.KEY_PARAMETER_TYPES, new Atom(parameterTypes)),
            Tuple.newProperty(Property.KEY_RETURN_TYPE, new Atom(returnType)),
            Tuple.newProperty(Property.KEY_BODY, body)
        );
    }

    public static boolean areCompatible(String declared, String actual) {
        switch(declared) {
            case Descriptor.BOOLEAN:
                switch(actual) {
                    case Descriptor.BOOLEAN:
                        return true;
                }
            case Descriptor.BYTE:
            case Descriptor.SHORT:
            case Descriptor.INT:
                switch(actual) {
                    case Descriptor.BYTE:
                    case Descriptor.SHORT:
                    case Descriptor.INT:
                        return true;
                }
            case Descriptor.LONG:
                switch(actual) {
                    case Descriptor.LONG:
                        return true;
                }
            case Descriptor.FLOAT:
                switch(actual) {
                    case Descriptor.FLOAT:
                        return true;
                }
            case Descriptor.DOUBLE:
                switch(actual) {
                    case Descriptor.DOUBLE:
                        return true;
                }
        }

        return false;
    }

    public static String getReturnType(Tuple statement) {
        switch(statement.getIntProperty(Property.KEY_AST_TYPE)) {
            case ASTType.RETURN_STATEMENT:
                Tuple expression = statement.getTupleProperty(Property.KEY_EXPRESSION);

                return expression.getStringProperty(Property.KEY_RESULT_TYPE);
        }

        return null;
    }

    public static Tuple ret(Node expression) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.RETURN_STATEMENT)),
            Tuple.newProperty(Property.KEY_EXPRESSION, expression)
        );
    }

    public static Tuple literal(boolean value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.BOOLEAN_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.BOOLEAN))*/
        );
    }

    public static Tuple literal(byte value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.BYTE_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.BYTE))*/
        );
    }

    public static Tuple literal(short value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.SHORT_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.SHORT))*/
        );
    }

    public static Tuple literal(int value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.INT_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.INT))*/
        );
    }

    public static Tuple literal(long value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.LONG_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.LONG))*/
        );
    }

    public static Tuple literal(float value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.FLOAT_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.FLOAT))*/
        );
    }

    public static Tuple literal(double value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.DOUBLE_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.DOUBLE))*/
        );
    }

    public static Tuple literal(char value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.CHAR_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(Descriptor.CHAR))*/
        );
    }

    public static Tuple literal(String value) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.STRING_LITERAL)),
            Tuple.newProperty(Property.KEY_VALUE, new Atom(value))/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom("java/lang/String"))*/
        );
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
        /*String lhsResultType = lhs.getStringProperty(Property.KEY_RESULT_TYPE);
        String rhsResultType = rhs.getStringProperty(Property.KEY_RESULT_TYPE);

        String arithmeticResultType = arithmeticResultType(lhsResultType, rhsResultType);


        if(arithmeticResultType == null)
            throw new IllegalArgumentException("Lhs arithmeticResultType '" + lhsResultType + " is incompatible with lhs result type '" + rhsResultType + "'.");
        */

        return new Tuple(
                Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.REDUCE)),
                Tuple.newProperty(Property.KEY_OPERATOR, new Atom(operator)),
                Tuple.newProperty(Property.KEY_LHS, lhs),
                Tuple.newProperty(Property.KEY_RHS, rhs)/*,
            Tuple.newProperty(Property.KEY_RESULT_TYPE, new Atom(arithmeticResultType))*/
        );
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
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.SHIFT)),
            Tuple.newProperty(Property.KEY_OPERATOR, new Atom(operator)),
            Tuple.newProperty(Property.KEY_LHS, lhs),
            Tuple.newProperty(Property.KEY_RHS, rhs)
        );
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
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.BITWISE)),
            Tuple.newProperty(Property.KEY_OPERATOR, new Atom(operator)),
            Tuple.newProperty(Property.KEY_LHS, lhs),
            Tuple.newProperty(Property.KEY_RHS, rhs)
        );
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

    public static Tuple and(Tuple lhs, Tuple rhs) {
        return logical(lhs, rhs, LogicalOperator.AND);
    }

    public static Tuple or(Tuple lhs, Tuple rhs) {
        return logical(lhs, rhs, LogicalOperator.OR);
    }

    public static Tuple logical(Tuple lhs, Tuple rhs, int operator) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.LOGICAL)),
            Tuple.newProperty(Property.KEY_OPERATOR, new Atom(operator)),
            Tuple.newProperty(Property.KEY_LHS, lhs),
            Tuple.newProperty(Property.KEY_RHS, rhs)
        );
    }

    public static String logicalResultType(String lhsType, String rhsType) {
        return lhsType.equals(Descriptor.BOOLEAN) && rhsType.equals(Descriptor.BOOLEAN) ? Descriptor.BOOLEAN : null;
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
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.COMPARE)),
            Tuple.newProperty(Property.KEY_OPERATOR, new Atom(operator)),
            Tuple.newProperty(Property.KEY_LHS, lhs),
            Tuple.newProperty(Property.KEY_RHS, rhs)
        );
    }

    public static Tuple declareVar(String type, String name) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.VARIABLE_DECLARATION)),
            Tuple.newProperty(Property.KEY_VAR_TYPE, new Atom(type)),
            Tuple.newProperty(Property.KEY_NAME, new Atom(name))
        );
    }

    public static Tuple assignVar(String name, Tuple expression) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.VARIABLE_ASSIGNMENT)),
            Tuple.newProperty(Property.KEY_NAME, new Atom(name)),
            Tuple.newProperty(Property.KEY_EXPRESSION, expression)
        );
    }

    public static Tuple accessVar(String name) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.VARIABLE_ACCESS)),
            Tuple.newProperty(Property.KEY_NAME, new Atom(name))
        );
    }

    public static Tuple block(List<Node> statements) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.BLOCK)),
            Tuple.newProperty(Property.KEY_STATEMENTS, new Atom(statements))
        );
    }

    public static Tuple intIncVar(String name, int timing, int amount) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.INCREMENT)),
            Tuple.newProperty(Property.KEY_NAME, new Atom(name)),
            Tuple.newProperty(Property.KEY_TIMING, new Atom(timing)),
            Tuple.newProperty(Property.KEY_AMOUNT, new Atom(amount))
        );
    }

    public static Tuple not(Tuple expression) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.NOT)),
            Tuple.newProperty(Property.KEY_EXPRESSION, expression)
        );
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

    public static Tuple ifElse(Tuple condition, Tuple ifTrue, Tuple ifFalse) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.IF_ELSE)),
            Tuple.newProperty(Property.KEY_CONDITION, condition),
            Tuple.newProperty(Property.KEY_IF_TRUE, ifTrue),
            Tuple.newProperty(Property.KEY_IF_FALSE, ifFalse)
        );
    }

    public static Tuple loop(Tuple condition, Tuple body) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.LOOP)),
            Tuple.newProperty(Property.KEY_CONDITION, condition),
            Tuple.newProperty(Property.KEY_BODY, body)
        );
    }

    public static Tuple brk() {
        return new Tuple(Arrays.asList(
                Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.BREAK))
        ));
    }

    public static Tuple cnt() {
        return new Tuple(Arrays.asList(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.CONTINUE))
        ));
    }

    public static Tuple instanceOf(Tuple expression, String type) {
        return new Tuple(
            Tuple.newProperty(Property.KEY_AST_TYPE, new Atom(ASTType.INSTANCE_OF)),
            Tuple.newProperty(Property.KEY_EXPRESSION, expression),
            Tuple.newProperty(Property.KEY_TYPE, new Atom(type))
        );
    }
}
