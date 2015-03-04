package astava.java;

public class Descriptor {
    public static final String BOOLEAN = "Z";
    public static final String BYTE = "B";
    public static final String SHORT = "S";
    public static final String INT = "I";
    public static final String LONG = "J";
    public static final String FLOAT = "F";
    public static final String DOUBLE = "D";
    public static final String CHAR = "C";
    public static final String VOID = "V";
    public static final String STRING = get(String.class);

    public static String get(Class<?> numberClass) {
        switch(numberClass.getTypeName()) {
            case "boolean": return Descriptor.BOOLEAN;
            case "byte": return Descriptor.BYTE;
            case "short": return Descriptor.SHORT;
            case "int": return Descriptor.INT;
            case "double": return Descriptor.DOUBLE;
            case "char": return Descriptor.CHAR;
            case "void": return Descriptor.VOID;
        }

        return numberClass.getName().replace(".", "/");
    }
}
