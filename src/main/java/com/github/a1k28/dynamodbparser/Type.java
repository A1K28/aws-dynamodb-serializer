package com.github.a1k28.dynamodbparser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

enum Type {
    STRING("S", true),
    BOOLEAN("BOOL", true),
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

    public Map createObject(Object value) {
        Map map = new LinkedHashMap();
        map.put(this.value, value);
        return map;
    }

    public static Type getProperty(Class clazz) {
        if (String.class.isAssignableFrom(clazz)) return STRING;
        if (clazz.isEnum()) return STRING;
        if (Boolean.class.isAssignableFrom(clazz)) return BOOLEAN;
        if (Number.class.isAssignableFrom(clazz)) return NUMBER;
        if (List.class.isAssignableFrom(clazz)) return LIST;
        return MAP;
    }

    public static Type getProperty(Map<?,?> map) {
        if (map.size() == 1)
            return getProperty((String) map.entrySet().iterator().next().getKey());
        return null;
    }

    private static Type getProperty(String str) {
        for (Type type : Type.values()) {
            if (type.value.equalsIgnoreCase(str)) return type;
        }
        return null;
    }
}
