package astava.java.gen;

import astava.java.Descriptor;
import astava.tree.ClassDom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static astava.java.Factory.classDeclaration;
import static astava.java.Factory.fieldDeclaration;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FieldTest {
    private int modifier;
    private String name = "myField";
    private Class<?> type;
    private String typeName;

    public FieldTest(int modifier, Class<?> type) {
        this.modifier = modifier;
        this.type = type;
        this.typeName = Descriptor.get(type);
    }

    @Parameterized.Parameters
    public static Collection values() {
        List<Integer> accessModifiers = Arrays.asList(Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED);
        List<Integer> modifierCombos = accessModifiers.stream().flatMap(accessModifier -> Arrays.asList(
            accessModifier, // Access modifier in instance context
            accessModifier & Modifier.STATIC // Access modifier in static context
        ).stream()).collect(Collectors.toList());
        List<Class<?>> types = Arrays.asList(
            Object.class, String.class,
            boolean.class, byte.class, short.class, int.class, long.class, float.class, double.class, char.class
        );

        // Combine modifier combos with types
        return modifierCombos.stream().flatMap(m ->
            types.stream().map(t -> new Object[]{m, t})
        ).collect(Collectors.toList());
    }

    @Test
    public void testField() throws ClassNotFoundException, NoSuchFieldException {
        ClassDom classDeclaration = classDeclaration(Modifier.PUBLIC, "MyClass", "java/lang/Object", Arrays.asList(fieldDeclaration(modifier, name, typeName)), Arrays.asList());
        ClassGenerator generator = new ClassGenerator(classDeclaration);
        Class<?> c = generator.newClass();
        Field f = c.getDeclaredField(name);

        assertEquals(modifier, f.getModifiers());
        assertEquals(name, f.getName());
        assertEquals(type, f.getType());
    }
}
