package astava.tree;

import java.util.List;

public interface MethodDom {
    int getModifier();

    String getName();

    List<String> getParameterTypes();

    String getReturnTypeName();

    StatementDom getBody();
}
