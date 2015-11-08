package astava.java.agent.sample.EasyObservable;

import astava.java.Descriptor;
import astava.java.agent.ClassLoaderExtender;
import astava.java.agent.Parser.ParserFactory;
import astava.java.agent.Parser.SourceCode;
import astava.java.agent.sample.MyClass;
import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import astava.java.parser.DefaultClassInspector;
import astava.java.parser.DefaultClassResolver;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.ClassFileReader;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.languages.java.JavaLanguage;
import com.strobel.decompiler.languages.java.ast.CompilationUnit;
import com.strobel.decompiler.languages.java.ast.IAstVisitor;

import java.io.IOException;
import java.io.StringWriter;
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
                    "implements Observable\n" +
                    "private Observer observer;\n" +
                    "public void setObserver(Observer observer) {this.observer = observer;}"
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
