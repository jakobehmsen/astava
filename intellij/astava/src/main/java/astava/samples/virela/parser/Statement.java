package astava.samples.virela.parser;

public interface Statement {
    void accept(StatementVisitor visitor);
}
