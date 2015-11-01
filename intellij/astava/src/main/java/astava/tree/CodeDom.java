package astava.tree;

public interface CodeDom extends Dom {
    void accept(CodeDomVisitor visitor);
}
