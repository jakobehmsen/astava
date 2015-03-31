package astava.java.gen;

import astava.tree.Node;
import astava.tree.Tuple;
import astava.java.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static astava.java.Factory.*;
import static astava.java.Factory.compareRhs;
import static astava.java.Factory.logicalRhs;

public class CodeAnalyzer {
    private Tuple code;
    private AnalyseScope methodScope;

    public CodeAnalyzer(Tuple code) {
        this(code, new AnalyseScope());
    }

    public CodeAnalyzer(Tuple code, AnalyseScope methodScope) {
        this.code = code;
        this.methodScope = methodScope;
    }

    public String returnType() {
        return returnType(code, methodScope);
    }

    public String resultType() {
        return resultType(code, methodScope);
    }

    private String returnType(Tuple statement, AnalyseScope methodScope) {
        switch(astType(statement)) {
            case ASTType.VARIABLE_DECLARATION: {
                String type = declareVarType(statement);
                String name = declareVarName(statement);
                methodScope.declareVar(type, name);
                break;
            } case ASTType.VARIABLE_ASSIGNMENT: {
                String name = assignVarName(statement);
                methodScope.assignVar(name);
                break;
            } case ASTType.INCREMENT: {
                break;
            } case ASTType.RETURN_VALUE_STATEMENT: {
                Tuple expression = retExpression(statement);

                return resultType(expression, methodScope);
            } case ASTType.BLOCK: {
                List<Node> statements = blockStatements(statement);
                List<String> returnTypes = statements.stream().map(s -> returnType(statement, methodScope)).filter(s -> s != null).collect(Collectors.toList());
                return returnTypes.get(0); // Bad assumption!!!
            } case ASTType.IF_ELSE: {
                Tuple condition = ifElseCondition(statement);
                Tuple ifTrue = ifElseIfTrue(statement);
                Tuple ifFalse = ifElseIfFalse(statement);

                String ifTrueReturnType = returnType(ifTrue, methodScope);
                String ifFalseReturnType = returnType(ifFalse, methodScope);

                // What if they are different?
                return ifTrueReturnType;
            } case ASTType.BREAK_CASE: {
                break;
            } case ASTType.CONTINUE: {
                break;
            } default: {
                return null; // Not a statement
            }
        }

        return null;
    }

    private String resultType(Tuple expression, AnalyseScope methodScope) {
        switch(astType(expression)) {
            case ASTType.BOOLEAN_LITERAL: {
                return Descriptor.BOOLEAN;
            } case ASTType.BYTE_LITERAL: {

                return Descriptor.BYTE;
            } case ASTType.SHORT_LITERAL: {

                return Descriptor.SHORT;
            } case ASTType.INT_LITERAL: {

                return Descriptor.INT;
            } case ASTType.LONG_LITERAL: {

                return Descriptor.LONG;
            } case ASTType.FLOAT_LITERAL: {
                return Descriptor.FLOAT;
            } case ASTType.DOUBLE_LITERAL: {
                return Descriptor.DOUBLE;
            } case ASTType.CHAR_LITERAL: {
                return Descriptor.CHAR;
            } case ASTType.STRING_LITERAL: {
                return Descriptor.STRING;
            } case ASTType.ARITHMETIC: {
                Tuple lhs = arithmeticLhs(expression);
                Tuple rhs = arithmeticRhs(expression);

                String lhsResultType = resultType(lhs, methodScope);
                String rhsResultType = resultType(rhs, methodScope);

                String resultType = Factory.arithmeticResultType(lhsResultType, rhsResultType);

                return resultType;
            } case ASTType.SHIFT: {
                Tuple lhs = shiftLhs(expression);
                Tuple rhs = shiftRhs(expression);

                String lhsResultType = resultType(lhs, methodScope);
                String rhsResultType = resultType(rhs, methodScope);
                String resultType = Factory.shiftResultType(lhsResultType, rhsResultType);

                return resultType;
            } case ASTType.BITWISE: {
                Tuple lhs = bitwiseLhs(expression);
                Tuple rhs = bitwiseRhs(expression);

                String lhsResultType = resultType(lhs, methodScope);
                String rhsResultType = resultType(rhs, methodScope);
                String resultType = Factory.bitwiseResultType(lhsResultType, rhsResultType);

                return resultType;
            } case ASTType.COMPARE: {
                Tuple lhs = compareLhs(expression);
                Tuple rhs = compareRhs(expression);

                String lhsResultType = resultType(lhs, methodScope);
                String rhsResultType = resultType(rhs, methodScope);

                return Descriptor.BOOLEAN;
            } case ASTType.LOGICAL: {
                Tuple lhs = logicalLhs(expression);
                Tuple rhs = logicalRhs(expression);

                String resultType = null;

                String lhsResultType = resultType(lhs, methodScope);
                String rhsResultType = resultType(rhs, methodScope);

                resultType = Factory.logicalResultType(lhsResultType, rhsResultType);

                return resultType;
            } case ASTType.VARIABLE_ACCESS: {
                String name = accessVarName(expression);

                if(!methodScope.varIsSet(name))
                    throw new IllegalArgumentException("Variable '" + name + " hasn't been set yet.");

                return methodScope.getVarType(name);
            } case ASTType.NOT: {
                Tuple bExpression = notExpression(expression);

                return Descriptor.BOOLEAN;
            } case ASTType.INSTANCE_OF: {
                Tuple oExpression = instanceOfExpression(expression);
                String type = expression.getStringProperty(Property.KEY_TYPE);

                return Descriptor.BOOLEAN;
            } case ASTType.BLOCK: {
                // Exactly one expression should be contained with statements
                List<Node> statements = blockStatements(expression);
                List<String> expressionResultTypes = new ArrayList<>();

                statements.forEach(s -> {
                    // Try as expression
                    String resultType = resultType((Tuple) s, methodScope);

                    if(resultType != null) {
                        // Was an expression
                        expressionResultTypes.add(resultType);
                    } else {
                        // Try as statement
                        returnType((Tuple) s, methodScope);
                    }
                });

                if(expressionResultTypes.size() > 1)
                    throw new IllegalArgumentException("Expression block has multiple expressions.");
                else if(expressionResultTypes.isEmpty())
                    throw new IllegalArgumentException("Expression block has no expressions.");

                return expressionResultTypes.get(0);
            } case ASTType.IF_ELSE: {
                Tuple condition = ifElseCondition(expression);
                Tuple ifTrue = ifElseIfTrue(expression);
                Tuple ifFalse = ifElseIfFalse(expression);

                String resultType = resultType(condition, methodScope);
                String ifTrueResultType = resultType(ifTrue, methodScope);
                String ifFalseResultType = resultType(ifFalse, methodScope);

                if(!ifTrueResultType.equals(ifFalseResultType))
                    throw new IllegalArgumentException("Inconsistent result types in test: ifTrue => " + ifTrueResultType + ", ifFalse => " + ifFalseResultType);

                return ifTrueResultType;
            }
        }

        return null;
    }
}
