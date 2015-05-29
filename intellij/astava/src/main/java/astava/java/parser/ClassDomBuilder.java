package astava.java.parser;

import astava.tree.ClassDom;

public interface ClassDomBuilder {
    ClassDom build(ClassResolver classResolver);
}
