package astava.java.ijava;

import astava.debug.Debug;

public class DebugClassLoader extends ClassLoader {
    public DebugClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return getParent().loadClass(name);
    }

    public void println(String message) {
        Debug.getPrintStream(Debug.LEVEL_HIGH).println(message);
    }
}
