package astava.java.agent.sample.EasyObservable;

import astava.java.agent.ClassLoaderExtender;
import astava.java.agent.Parser.ParserFactory;
import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.DefaultClassInspector;
import astava.java.parser.DefaultClassResolver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        /*StringWriter writer = new StringWriter();
        Decompiler.decompile(MyClass.class.getName(), new PlainTextOutput(writer));
        System.out.println(writer);

        DecompilerSettings settings = new DecompilerSettings();
        Object typeLoader = settings.getTypeLoader() != null?settings.getTypeLoader():new InputTypeLoader();
        MetadataSystem metadataSystem = new MetadataSystem((ITypeLoader)typeLoader);
        TypeDefinition type = (TypeDefinition)metadataSystem.lookupType(MyClass.class.getName());;

        JavaLanguage javaLanguage = new JavaLanguage();
        DecompilationOptions decompilationOptions = new DecompilationOptions();
        decompilationOptions.setFullDecompilation(true);
        CompilationUnit unit = javaLanguage.decompileTypeToAst(type, decompilationOptions);*/

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        ClassResolver classResolver = new DefaultClassResolver(classLoader, Arrays.asList(
            Support.class, Observable.class, Observer.class
        ));

        ClassInspector classInspector = new DefaultClassInspector(classLoader);

        ParserFactory factory = new ParserFactory(classResolver, classInspector);

        /*ClassLoaderExtender loader = new ClassLoaderExtender(factory
            .whenClass("@Support(Observable.class)")
            .then(factory
                .whenMethodName(x -> !x.equals("<init>"))
                .then(factory
                    .whenBody("this.?name = ?value;")
                    .then(factory.modBody(captures -> new SourceCode(
                        "observer.changingField(this, \"" + captures.get("name") + "\");\n" +
                        "this." + captures.get("name") + " = ?value;\n" +
                        "observer.changedField(this, \"" + captures.get("name") + "\");\n"
                        , captures
                    )))
                )
                .andThen(factory.modClass(
                    "implements Observable\n" +
                    "private Observer observer;\n" +
                    "public void setObserver(Observer observer) {this.observer = observer;}"
                ))),
            classResolver, classInspector
        );*/


        ClassLoaderExtender loader = new ClassLoaderExtender(factory
            .whenClass("@Support(Observable.class)")
            .then(factory
                .whenMethodName(x -> !x.equals("<init>"))
                .then(factory
                    .whenBody("this.?name = ?value;")
                    .then(factory.modBody(
                        "observer.changingField(this, \"?name\");\n" +
                        "this.?name = ?value;\n" +
                        "observer.changedField(this, \"?name\");\n"
                    ))
                    .andThen(factory.modMethod(m ->
                        "observer.enter(this, \"" + m.name + "\");\n" + // Method could be an implicit argument with a shortcut pseudo variable therefore?
                        "...\n" +
                        "observer.leave(this, \"" + m.name + "\");\n" + // Method could be an implicit argument with a shortcut pseudo variable therefore?
                        "return;"
                    ))
                ).andThen(factory.modClass(
                    "implements Observable\n" +
                    "private Observer observer;\n" +
                    "public void setObserver(Observer observer) {this.observer = observer;}"
                ))),
            classResolver, classInspector
        );


        /*
        .whenBody("this.?name = ?value;")
        .then(factory.modBody(
            "observer.changingField(this, \"?name\");\n" +
            "this.?name = ?value;\n" +
            "observer.changedField(this, \"?name\");\n"
        ))
        */

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

            @Override
            public void enter(Object target, String methodName) {
                System.out.println("Enter " + methodName);
            }

            @Override
            public void leave(Object target, String methodName) {
                System.out.println("Leave " + methodName);
            }
        });

        person.getClass().getMethod("setName", String.class, String.class).invoke(person, "John", "Johnson");
    }
}
