package astava.java.agent.sample.agent;

import astava.java.DomFactory;
import astava.java.agent.ClassNodeExtenderFactory;
import astava.java.agent.ClassNodeTransformer;
import astava.java.agent.ConditionalClassNodeExtender;
import astava.java.agent.SequenceClassNodeExtender;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Created by jakob on 09-10-15.
 */
public class Main {
    public static void premain(String agentArgument, Instrumentation instrumentation) {
        SequenceClassNodeExtender myFieldExtender = new SequenceClassNodeExtender();

        myFieldExtender.extend(ClassNodeExtenderFactory.addField(DomFactory.fieldDeclaration(Modifier.PUBLIC, "myField", "java/lang/String")));
        myFieldExtender.extend(ClassNodeExtenderFactory.addMethod(DomFactory.methodDeclaration(Modifier.PUBLIC, "toString", Arrays.asList(), "java/lang/String", DomFactory.ret(
            DomFactory.accessField(DomFactory.self(), "myField", "java/lang/String")
        ))));

        ConditionalClassNodeExtender extender = new ConditionalClassNodeExtender();

        extender.extend(x -> x.name.equals("astava/java/agent/sample/MyClass"), myFieldExtender);

        instrumentation.addTransformer(new ClassNodeTransformer(extender));
    }
}
