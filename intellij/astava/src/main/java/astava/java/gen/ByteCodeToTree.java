package astava.java.gen;

import astava.java.DomFactory;
import astava.java.parser.DefaultExpressionDomVisitor;
import astava.java.parser.MethodDeclaration;
import astava.tree.*;
import com.sun.tools.corba.se.idl.constExpr.Expression;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ByteCodeToTree extends InstructionAdapter {
    // Multiple stacks for each branch?
    private Type returnType;
    private Stack<ExpressionDom> stack = new Stack<>();
    private ArrayList<StatementDom> statements = new ArrayList<>();

    public ByteCodeToTree(Type returnType) {
        super(Opcodes.ASM5, new MethodVisitor(Opcodes.ASM5, null) {
        });
        this.returnType = returnType;
    }

    @Override
    public void iconst(int cst) {
        stack.push(DomFactory.literal(cst));
    }

    @Override
    public void invokevirtual(String owner, String name, String desc, boolean itf) {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        List<ExpressionDom> arguments =
            Arrays.asList(argumentTypes).stream().map(x -> stack.pop()).collect(Collectors.toList());
        ExpressionDom target = stack.pop();
        stack.push(DomFactory.invokeVirtualExpr(owner, name, desc, target, arguments));
    }

    @Override
    public void aconst(Object cst) {
        if(cst instanceof String) {
            stack.push(DomFactory.literal((String)cst));
        } else {
            // What?
        }
    }

    @Override
    public void add(Type type) {
        ExpressionDom rhs = stack.pop();
        ExpressionDom lhs = stack.pop();
        stack.push(DomFactory.add(lhs, rhs));
    }

    @Override
    public void pop() {
        // An expression as statement? Always? When not?
        ExpressionDom expression = stack.pop();
        statements.add(expressionToStatement(expression));
    }

    @Override
    public void getstatic(String owner, String name, String desc) {
        stack.push(DomFactory.accessStaticField(owner, name, desc));
    }

    @Override
    public void putfield(String owner, String name, String desc) {

    }

    @Override
    public void putstatic(String owner, String name, String desc) {

    }

    @Override
    public void store(int var, Type type) {

    }

    @Override
    public void astore(Type type) {

    }

    private StatementDom expressionToStatement(ExpressionDom expression) {
        return Util.returnFrom(null, r -> expression.accept(new DefaultExpressionDomVisitor() {
            @Override
            public void visitInvocation(int invocation, ExpressionDom target, String type, String name, String descriptor, List<ExpressionDom> arguments) {
                r.accept(DomFactory.invoke(invocation, type, name, descriptor, target, arguments));
            }

            @Override
            public void visitNewInstance(String type, List<String> parameterTypes, List<ExpressionDom> arguments) {
                r.accept(DomFactory.newInstance(type, parameterTypes, arguments));
            }
        }));
    }

    @Override
    public void areturn(Type type) {
        ExpressionDom expression = stack.pop();

        expression = ensureType(returnType, expression);

        statements.add(DomFactory.ret(expression));
    }

    private ExpressionDom ensureType(Type type, ExpressionDom expression) {
        if(type.equals(Type.BOOLEAN_TYPE)) {
            return Util.returnFrom(expression, r -> expression.accept(new DefaultExpressionDomVisitor() {
                @Override
                public void visitIntLiteral(int value) {
                    r.accept(DomFactory.literal(value == 1 ? true : false));
                }
            }));
        } else if(type.equals(Type.BYTE_TYPE)) {
            return Util.returnFrom(expression, r -> expression.accept(new DefaultExpressionDomVisitor() {
                @Override
                public void visitIntLiteral(int value) {
                    r.accept(DomFactory.literal((byte)value));
                }
            }));
        } else if(type.equals(Type.SHORT_TYPE)) {
            return Util.returnFrom(expression, r -> expression.accept(new DefaultExpressionDomVisitor() {
                @Override
                public void visitIntLiteral(int value) {
                    r.accept(DomFactory.literal((short)value));
                }
            }));
        }

        return expression;
    }

    public StatementDom getBlock() {
        return DomFactory.block(statements);
    }
}
