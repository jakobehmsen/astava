package astava.tree;

import java.util.List;

public interface MethodDom {
    int getModifiers();

    String getName();

    List<ParameterInfo> getParameterTypes();

    String getReturnTypeName();

    StatementDom getBody();
}
