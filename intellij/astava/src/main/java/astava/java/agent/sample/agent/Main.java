package astava.java.agent.sample.agent;

import astava.java.Descriptor;
import astava.java.agent.*;
import astava.java.agent.Parser.ClassNodeExtenderParser;
import astava.java.agent.Parser.ClassNodePredicateParser;
import astava.java.parser.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

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

        /*classNodeModifier.extend(
            "public boolean equals(Object other) {" +
            "   if(other instanceof {0}) {" +
            "       {0} otherAsThis = ({0})other;" +
            "       return {1};" +
            "   }" +
            "   return false;" +
            "}",
            cd -> cd.getName(),
            cd -> cd.getFields().stream().map(x -> classNodeModifier.parse("this.{0} = other.{0}", x))
        );*/

        ClassNodePredicateParser classNodePredicate = new ClassNodePredicateParser(classInspector);

        //classNodePredicate.add("class astava.java.agent.sample.MyClass extends astava.java.agent.sample.MyOtherClass implements java.io.Serializable");
        classNodePredicate.add("class astava.java.agent.sample.MyClass");
        //classNodePredicate.add("extends astava.java.agent.sample.MyOtherClass");
        classNodePredicate.add("extends java.lang.Object");
        classNodePredicate.add("implements java.io.Serializable");

        // Support parsed filters
        //instrumentation.addTransformer(classNodeModifier.when(classNodePredicate));
        instrumentation.addTransformer(((ClassNodeExtender) classNode -> {
            ClassNodeExtenderParser m = new ClassNodeExtenderParser(classResolver, classInspector);
            ClassDeclaration classDeclaration = new ASMClassDeclaration(classNode);
            m.extend(String.format(
                "public boolean equals(Object other) {\n" +
                "   if(other instanceof %1$s) {\n" +
                "       %1$s otherAsThis = (%1$s)other;\n" +
                "       return %2$s;\n" +
                "   }\n" +
                "   return false;\n" +
                "}",
                Descriptor.getName(classNode.name),
                classDeclaration.getFields().stream().map(x -> String.format("this.%1$s.equals(otherAsThis.%1$s)", x.getName())).collect(Collectors.joining(" && "))
            ));
            m.transform(classNode);
        }).when(classNodePredicate));
    }
}
