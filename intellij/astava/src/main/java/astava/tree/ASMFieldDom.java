package astava.tree;

import org.objectweb.asm.tree.FieldNode;

public class ASMFieldDom implements FieldDom {
    private FieldNode fieldNode;

    public ASMFieldDom(FieldNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    @Override
    public String getName() {
        return fieldNode.name;
    }

    @Override
    public <T> T accept(FieldDomVisitor<T> visitor) {
        return visitor.visitASMField(fieldNode);
    }
}
