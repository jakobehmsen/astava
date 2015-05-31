package astava.java.gen;

import astava.java.Descriptor;
import astava.tree.ClassDom;
import astava.tree.MethodDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static astava.java.Factory.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FieldTest {
    private int modifiers;
    private String name = "myField";
    //private Class<?> type;
    private String descriptor;
    private LiteralTest.LiteralProvider typeInfo;

    public FieldTest(int modifiers, LiteralTest.LiteralProvider typeInfo) {
        this.modifiers = modifiers;
        this.descriptor = typeInfo.getDescriptor();
        this.typeInfo = typeInfo;
    }

    @Parameterized.Parameters
    public static Collection values() {
        List<Integer> accessModifiers = Arrays.asList(Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED);
        List<Integer> modifierCombos = accessModifiers.stream().flatMap(accessModifier -> Arrays.asList(
            accessModifier, // Access modifiers in instance context
            accessModifier | Modifier.STATIC // Access modifiers in static context
        ).stream()).collect(Collectors.toList());

        List<LiteralTest.LiteralProvider> types = LiteralTest.getProviders();

        // Combine modifiers combos with types
        return modifierCombos.stream().flatMap(m ->
            types.stream().map(t -> new Object[]{m, t})
        ).collect(Collectors.toList());
    }

    @Test
    public void testField() throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        String className = "MyClass";
        String setFieldMethodName = "set" + name;
        String getFieldMethodName = "get" + name;

        Object expectedValue = typeInfo.getValue();

        int memberModifiers = Modifier.PUBLIC;

        List<MethodDom> methods;

        if(Modifier.isStatic(modifiers)) {
            memberModifiers |= Modifier.STATIC;
            methods = Arrays.asList(
                methodDeclaration(memberModifiers, setFieldMethodName, Arrays.asList(), Descriptor.VOID, block(Arrays.asList(assignStaticField(Descriptor.get(className), name, typeInfo.createASTDom(expectedValue)), ret()))),
                methodDeclaration(memberModifiers, getFieldMethodName, Arrays.asList(), descriptor, ret(accessStaticField(Descriptor.get(className), name, descriptor)))
            );
        } else {
            methods = Arrays.asList(
                methodDeclaration(memberModifiers, "<init>", Arrays.asList(), Descriptor.VOID, block(Arrays.asList(
                    invokeSpecial(Descriptor.get("java/lang/Object"), "<init>", Descriptor.getMethodDescriptor(Arrays.asList(), Descriptor.VOID), self(), Arrays.asList()),
                    ret()
                ))),
                methodDeclaration(memberModifiers, setFieldMethodName, Arrays.asList(), Descriptor.VOID, block(Arrays.asList(assignField(self(), name, typeInfo.createASTDom(expectedValue)), ret()))),
                methodDeclaration(memberModifiers, getFieldMethodName, Arrays.asList(), descriptor, ret(accessField(self(), name, descriptor)))
            );
        }

        ClassDom classDeclaration = classDeclaration(Modifier.PUBLIC, className, "java/lang/Object",
            Arrays.asList(fieldDeclaration(modifiers, name, descriptor)),
            methods
        );
        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Class<?> c = generator.newClass();
        Field f = c.getDeclaredField(name);
        f.setAccessible(true);
        Method setFieldM = c.getDeclaredMethod(setFieldMethodName);
        setFieldM.setAccessible(true);
        Method getFieldM = c.getDeclaredMethod(getFieldMethodName);
        setFieldM.setAccessible(true);

        assertEquals(modifiers, f.getModifiers());
        assertEquals(name, f.getName());
        assertEquals(typeInfo.getType(), f.getType());

        Object instance = Modifier.isStatic(modifiers) ? null : c.newInstance();

        if(Modifier.isStatic(modifiers))
            setFieldM.invoke(null, null);
        else
            setFieldM.invoke(instance, null);

        Object value;
        if(Modifier.isStatic(modifiers))
            value = getFieldM.invoke(null, null);
        else
            value = getFieldM.invoke(instance, null);

        assertEquals(expectedValue, value);
    }
}
