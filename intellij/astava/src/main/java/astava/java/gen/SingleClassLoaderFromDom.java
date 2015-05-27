package astava.java.gen;

public class SingleClassLoaderFromDom extends ClassLoader {
    private ClassGeneratorFromDom generator;

    public SingleClassLoaderFromDom(ClassGeneratorFromDom generator) {
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
