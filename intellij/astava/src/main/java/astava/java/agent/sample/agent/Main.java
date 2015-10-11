package astava.java.agent.sample.agent;

import astava.java.agent.*;
import astava.java.agent.Parser.ClassNodeExtenderParser;
import astava.java.parser.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.*;

public class Main {
    public static void premain(String agentArgument, Instrumentation instrumentation) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        ClassResolver classResolver = new DefaultClassResolver(classLoader, Arrays.asList(
            String.class,
            Modifier.class,
            Object.class
        ));

        ClassInspector classInspector = new DefaultClassInspector(classLoader);

        ClassNodeExtenderParser myClassNodeExtenderParser = new ClassNodeExtenderParser(classResolver, classInspector);

        myClassNodeExtenderParser.extend("public java.lang.String myField = \"Hello1\";");
        myClassNodeExtenderParser.extend("public int myField3 = 8;");
        myClassNodeExtenderParser.extend("public java.lang.String toString() {return myField;}");

        ConditionalClassNodeExtender extender = new ConditionalClassNodeExtender();

        extender.extend(x -> x.name.equals("astava/java/agent/sample/MyClass"), myClassNodeExtenderParser);

        instrumentation.addTransformer(new ClassNodeTransformer(extender));
    }
}
