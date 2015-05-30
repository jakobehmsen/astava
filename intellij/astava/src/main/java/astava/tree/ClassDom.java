package astava.tree;

import java.util.List;

public interface ClassDom {
    int getModifiers();
    String getName();
    String getSuperName();
    List<FieldDom> getFields();
    List<MethodDom> getMethods();
}
