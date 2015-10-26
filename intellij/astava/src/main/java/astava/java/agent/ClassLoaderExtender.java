package astava.java.agent;

import astava.java.parser.ClassInspector;
import astava.java.parser.ClassResolver;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class ClassLoaderExtender extends ClassLoader {
    private ClassResolver classResolver;
    private ClassInspector classInspector;
    private ClassNodeExtender extender;

    public ClassLoaderExtender(ClassNodeExtender extender, ClassResolver classResolver, ClassInspector classInspector) {
        this(ClassLoader.getSystemClassLoader(), classResolver, extender, classInspector);
    }

    public ClassLoaderExtender(ClassLoader parent, ClassResolver classResolver, ClassNodeExtender extender, ClassInspector classInspector) {
        super(parent);
        this.classResolver = classResolver;
        this.classInspector = classInspector;
        this.extender = extender;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("Loading class " + name);

        Class<?> c = findLoadedClass(name);

        if(c != null)
            return c;

        c = findClass(name);

        if(c != null)
            return c;

        return getParent() != null ? getParent().loadClass(name) : findSystemClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        InputStream resource = Class.forName(name).getResourceAsStream(name.substring(name.lastIndexOf('.') + 1) + ".class");

        try {
            ClassReader cr = new ClassReader(resource);

            ClassNode classNode = new ClassNode(Opcodes.ASM5);
            cr.accept(classNode, org.objectweb.asm.ClassReader.EXPAND_FRAMES);

            System.out.println(classNode.name);

            extender.transform(classNode, classResolver, classInspector);

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            PrintWriter ps = new PrintWriter(os);
            //cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
            org.objectweb.asm.util.CheckClassAdapter.verify(new org.objectweb.asm.ClassReader(classWriter.toByteArray()), true, ps);

            byte[] bytes = classWriter.toByteArray();

            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {

        }

        return null;
    }
}
