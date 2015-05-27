package astava.tree;

import java.util.List;

public interface ClassDom {
    int getModifier();
    String getName();
    String getSuperName();
    List<FieldDom> getFields();
    List<MethodDom> getMethods();
}
