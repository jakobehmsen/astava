package astava.java.gen;

public class SingleClassLoader extends ClassLoader {
    private ClassGenerator generator;

    public SingleClassLoader(ClassGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if(generator.getClassName().equals(name)) {
            byte[] classBytes = generator.toBytes();
            return defineClass(name, classBytes, 0, classBytes.length);
        }

        return getParent().loadClass(name);
    }
}
