package astava.java.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Hashtable;

public class ClassLoaderExtender extends ClassLoader {
    private ClassNodeExtender extender;
    private Hashtable<String, Class<?>> cache = new Hashtable<>();

    public ClassLoaderExtender(ClassNodeExtender extender) {
        this.extender = extender;
    }

    public ClassLoaderExtender(ClassLoader parent, ClassNodeExtender extender) {
        super(parent);
        this.extender = extender;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("Finding class " + name);

        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        byte[] classData;

        try {
            classData = loadClassData(name);
        } catch (IOException e) {
            throw new ClassNotFoundException("Class [" + name
                + "] could not be found", e);
        }

        Class<?> c = defineClass(name, classData, 0, classData.length);
        resolveClass(c);
        cache.put(name, c);

        return c;
    }

    private byte[] loadClassData(String name) throws IOException {

        InputStream resource = null;//getResourceAsStream(name);
        try {
            resource = Class.forName(name).getResourceAsStream(name.substring(name.lastIndexOf('.') + 1) + ".class");
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

            return classWriter.toByteArray();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;

        /*try {
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

            return classWriter.toByteArray();
        } catch (SecurityException e) {
            return super.findClass(name);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(NoClassDefFoundError e) {
            return super.findClass(name);
        }
        return super.findClass(name);*/
    }

    /*@Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("Finding class " + name);

        Class<?> c = cache.get(name);

        if(c != null)
            return c;

        InputStream resource = Class.forName(name).getResourceAsStream(name.substring(name.lastIndexOf('.') + 1) + ".class");//getResourceAsStream(name);

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
            c = defineClass(name, bytes, 0, bytes.length);
            cache.put(name, c);
            return c;
        } catch (SecurityException e) {
            return super.findClass(name);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(NoClassDefFoundError e) {
            return super.findClass(name);
        }
        return super.findClass(name);
    }*/

    /*@Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return findClass(name);
    }*/

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("Loading class " + name);

        InputStream resource = Class.forName(name).getResourceAsStream(name.substring(name.lastIndexOf('.') + 1) + ".class");//getResourceAsStream(name);

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
        } catch (SecurityException e) {
            //throw new ClassNotFoundException(name);
            //return super.loadClass(name);
            return getParent().loadClass(name);
        } catch (IOException e) {
            e.printStackTrace();
        } catch(NoClassDefFoundError e) {
            return super.loadClass(name);
        }
        return super.loadClass(name);
    }
}
