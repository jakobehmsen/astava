package astava.samples.virela.parser;

public interface StatementVisitor {
    void visitAssignLazy(String id, Expression value);
    void visitAssignEager(String id, Expression value);
}
