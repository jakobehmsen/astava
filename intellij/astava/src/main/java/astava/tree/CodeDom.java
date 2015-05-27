package astava.tree;

public interface CodeDom {
    void accept(CodeDomVisitor visitor);
}
