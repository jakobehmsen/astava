package astava.tree;

import org.objectweb.asm.tree.FieldNode;

public interface FieldDomVisitor<T> {
    T visitCustomField(CustomFieldDom field);
    T visitASMField(FieldNode fieldNode);
}
