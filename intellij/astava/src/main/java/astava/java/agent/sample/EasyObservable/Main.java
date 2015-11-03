package astava.java.agent.sample.EasyObservable;

import astava.java.Descriptor;
import astava.java.agent.ClassLoaderExtender;
import astava.java.agent.Parser.ParserFactory;
import astava.java.agent.Parser.SourceCode;
import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.DefaultClassInspector;
import astava.java.parser.DefaultClassResolver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        ClassResolver classResolver = new DefaultClassResolver(classLoader, Arrays.asList(
            Support.class, Observable.class
        ));

        ClassInspector classInspector = new DefaultClassInspector(classLoader);

        ParserFactory factory = new ParserFactory(classResolver, classInspector);

        ClassLoaderExtender loader = new ClassLoaderExtender(factory
            .whenClass("@Support(Observable.class)")
            .then(factory
                .whenMethod("")
                .then(factory
                    .whenBody("this.? = ?;")
                    .then(factory.modBody(captures -> new SourceCode(
                        "observer.changingField(this, \"" + captures.get(0) + "\");\n" +
                        "this." + captures.get(0) + " = ?;\n" +
                        "observer.changedField(this, \"" + captures.get(0) + "\");\n"
                        , captures.get(1)
                    )))
                )
                .andThen(factory.modClass(
                    "implements astava.java.agent.sample.EasyObservable.Observable\n" +
                    "private astava.java.agent.sample.EasyObservable.Observer observer;\n" +
                    "public void setObserver(astava.java.agent.sample.EasyObservable.Observer observer) {this.observer = observer;}"
                )
            )),
            classResolver, classInspector
        );

        Object person = Class.forName("astava.java.agent.sample.EasyObservable.Person", false, loader).newInstance();
        ((Observable)person).setObserver(new Observer() {
            @Override
            public void changingField(Object target, String name) {
                System.out.println("Assigning field " + name);
            }

            @Override
            public void changedField(Object target, String name) {
                System.out.println("Assigned field " + name);
            }
        });

        person.getClass().getMethod("setName").invoke(person);
    }
}
