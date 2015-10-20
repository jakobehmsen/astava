package astava.java.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

public class ClassLoaderExtender extends ClassLoader {
    private ClassNodeExtender extender;

    public ClassLoaderExtender(ClassNodeExtender extender) {
        this(ClassLoader.getSystemClassLoader(), extender);
    }

    public ClassLoaderExtender(ClassLoader parent, ClassNodeExtender extender) {
        super(parent);
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

            extender.transform(classNode);

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            PrintWriter ps = new PrintWriter(os);
            org.objectweb.asm.util.CheckClassAdapter.verify(new org.objectweb.asm.ClassReader(classWriter.toByteArray()), true, ps);
            //cr.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);

            byte[] bytes = classWriter.toByteArray();

            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {

        }

        return null;
    }
}
