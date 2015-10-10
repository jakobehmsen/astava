package astava.java.agent.sample.agent;

import astava.java.DomFactory;
import astava.java.agent.*;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Created by jakob on 09-10-15.
 */
public class Main {
    public static void premain(String agentArgument, Instrumentation instrumentation) {
        SequenceClassNodeExtender myClassNodeExtender = new SequenceClassNodeExtender();

        myClassNodeExtender.extend(ClassNodeExtenderFactory.addField(DomFactory.fieldDeclaration(Modifier.PUBLIC, "myField", "java/lang/String")));
        myClassNodeExtender.extend(ClassNodeExtenderFactory.addMethod(DomFactory.methodDeclaration(Modifier.PUBLIC, "toString", Arrays.asList(), "java/lang/String", DomFactory.ret(
            DomFactory.accessField(DomFactory.self(), "myField", "java/lang/String")
        ))));
        myClassNodeExtender.extend(MethodNodeExtenderFactory.setBody(DomFactory.block(Arrays.asList(
            DomFactory.assignField(DomFactory.self(), "myField", "java/lang/String", DomFactory.literal("Hello")),
            DomFactory.methodBody()
        ))).when((c, m) -> m.name.equals("<init>")));

        ConditionalClassNodeExtender extender = new ConditionalClassNodeExtender();

        extender.extend(x -> x.name.equals("astava/java/agent/sample/MyClass"), myClassNodeExtender);

        instrumentation.addTransformer(new ClassNodeTransformer(extender));
    }
}
