package com.github.a1k28.dynamodbparser.model;

import java.util.List;
import java.util.Map;

public enum Type {
    STRING("S", true),
    BOOLEAN("Bool", true),
    NUMBER("N", true),
    LIST("L", false),
    MAP("M", false); // default

    private final String value;
    private final boolean end;

    Type(String value, boolean end) {
        this.value = value;
        this.end = end;
    }

    public String value() {
        return this.value;
    }

    public boolean shouldEnd() {
        return this.end;
    }

    public Object createObject(Object value) {
        return switch (this) {
            case STRING -> new TypeString(value == null ? null : (String) value);
            case BOOLEAN -> new TypeBool(value == null ? null : (Boolean) value);
            case NUMBER -> new TypeNumber(value == null ? null : (Number) value);
            case LIST -> new TypeList<>(value == null ? null : (List) value);
            case MAP -> new TypeObject<>(value == null ? null : (Map) value);
        };
    }

    public static Type getProperty(Map<?,?> map) {
        if (map.size() == 1)
            return getProperty((String) map.entrySet().iterator().next().getKey());
        return null;
    }

    private static Type getProperty(String str) {
//        if (clazz.isPrimitive())
//            clazz = Array.get(Array.newInstance(clazz,1),0).getClass();

        for (Type type : Type.values()) {
            if (type.value.equalsIgnoreCase(str)) return type;
        }
        return null;
    }
}
