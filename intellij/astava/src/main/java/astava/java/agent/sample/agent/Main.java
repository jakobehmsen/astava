package astava.java.agent.sample.agent;

import astava.java.agent.*;
import astava.java.agent.Parser.ClassNodeExtenderParser;
import astava.java.parser.*;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.*;

public class Main {
    public static void premain(String agentArgument, Instrumentation instrumentation) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        ClassResolver classResolver = new DefaultClassResolver(classLoader, Arrays.asList(
            String.class,
            Modifier.class,
            Object.class
        ));

        ClassInspector classInspector = new DefaultClassInspector(classLoader);

        ClassNodeExtenderParser classNodeModifier = new ClassNodeExtenderParser(classResolver, classInspector);

        //myClassNodeExtenderParser.extend("public java.lang.String myField = \"Hello1\";");
        //myClassNodeExtenderParser.extend("@astava.java.agent.sample.MyAnnotation()");
        classNodeModifier.extend("@astava.java.agent.sample.MyAnnotation(567, extra = \"abc\")");
        classNodeModifier.extend("public java.lang.String myField = myMethod();");
        classNodeModifier.extend("public java.lang.String myMethod() {return \"Hello there!!!\";}");
        classNodeModifier.extend("public int myField3 = 8;");
        classNodeModifier.extend("public java.lang.String toString() {return myField;}");

        // Support parsed filters
        instrumentation.addTransformer(new ClassNodeTransformer(classNodeModifier.when(
            "class astava.java.agent.sample.MyClass extends astava.java.agent.sample.MyOtherClass implements java.io.Serializable"
        )));
    }
}
