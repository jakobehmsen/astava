package astava.java;

import astava.debug.Debug;
import jdk.internal.org.objectweb.asm.Type;

import java.util.List;
import java.util.stream.Collectors;

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

    public static String get(Class<?> c) {
        /*switch(numberClass.getTypeName()) {
            case "boolean": return Descriptor.BOOLEAN;
            case "byte": return Descriptor.BYTE;
            case "short": return Descriptor.SHORT;
            case "int": return Descriptor.INT;
            case "long": return Descriptor.LONG;
            case "float": return Descriptor.FLOAT;
            case "double": return Descriptor.DOUBLE;
            case "char": return Descriptor.CHAR;
            case "void": return Descriptor.VOID;
        }

        return numberClass.getName().replace(".", "/");*/

        return get(c.getName());
    }

    public static String get(String typeName) {
        switch(typeName) {
            case "boolean": return Descriptor.BOOLEAN;
            case "byte": return Descriptor.BYTE;
            case "short": return Descriptor.SHORT;
            case "int": return Descriptor.INT;
            case "long": return Descriptor.LONG;
            case "float": return Descriptor.FLOAT;
            case "double": return Descriptor.DOUBLE;
            case "char": return Descriptor.CHAR;
            case "void": return Descriptor.VOID;
        }

        if(typeName.endsWith("[]"))
            return "[L" + typeName.substring(0, typeName.length() - 2).replace(".", "/") + ";";

        /*if(typeName.startsWith("[L"))
            return typeName;*/

        return typeName.replace(".", "/");
    }

    public static boolean isPrimitiveName(String typeName) {
        switch(typeName) {
            case "boolean":
            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
            case "char":
            case "void": return true;
        }

        return false;
    }

    public static String getMethodDescriptor(List<Class<?>> parameterTypes, Class<?> returnTypes) {
        return getMethodDescriptor(parameterTypes.stream().map(c -> get(c)).collect(Collectors.toList()), get(returnTypes));
    }

    public static String getMethodDescriptor(List<String> parameterTypeNames, String returnTypeName) {
        StringBuilder mdBuilder = new StringBuilder();

        mdBuilder.append("(");
        for(int i = 0; i < parameterTypeNames.size(); i++) {
            String ptn = parameterTypeNames.get(i);
            mdBuilder.append(getMethodDescriptorPart(ptn));
        }
        mdBuilder.append(")");
        mdBuilder.append(getMethodDescriptorPart(returnTypeName));

        return mdBuilder.toString();
    }

    private static String getMethodDescriptorPart(String typeName) {
        switch(typeName) {
            case "V":
            case "Z":
            case "C":
            case "B":
            case "S":
            case "I":
            case "F":
            case "J":
            case "D":
                return typeName;
        }

        if(typeName.startsWith("[L"))
            return typeName;

        return "L" + typeName + ";";
    }

    public static String getTypeDescriptor(String typeName) {
        return getMethodDescriptorPart(typeName);
    }

    public static String getDescriptorName(String descriptor) {
        return Type.getType(descriptor).getClassName();
    }

    public static String getFieldDescriptor(String typeName) {
        return getMethodDescriptorPart(typeName);
    }

    public static String getName(String descriptor) {
        switch(descriptor) {
            case Descriptor.VOID: return "void";
            case Descriptor.BOOLEAN: return "boolean";
            case Descriptor.CHAR: return "char";
            case Descriptor.BYTE: return "byte";
            case Descriptor.SHORT: return "short";
            case Descriptor.INT: return "int";
            case Descriptor.FLOAT: return "float";
            case Descriptor.LONG: return "long";
            case Descriptor.DOUBLE: return "double";
        }

        return descriptor.replace('/', '.');
    }

    public static Object getDefaultValue(String descriptor) {
        switch(descriptor) {
            case Descriptor.BOOLEAN: return false;
            case Descriptor.CHAR: return '0';
            case Descriptor.BYTE: return (byte)0;
            case Descriptor.SHORT: return (short)0;
            case Descriptor.INT: return 0;
            case Descriptor.FLOAT: return 0.0f;
            case Descriptor.LONG: return 0L;
            case Descriptor.DOUBLE: return 0.0;
        }

        return null;
    }

    public static String getReturnType(String methodDescriptor) {
        String typeName = Type.getReturnType(methodDescriptor).getClassName();
        return get(typeName);
    }
}
