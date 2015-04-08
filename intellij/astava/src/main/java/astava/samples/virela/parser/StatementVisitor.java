package astava.samples.virela.parser;

public interface StatementVisitor {
    void visitAssign(String id, Expression value);
    void visitEdit(String id);
    void visitShow(String id);
}
