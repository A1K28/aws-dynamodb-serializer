package com.github.a1k28.dynamodbparser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.a1k28.dynamodbparser.model.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractProcessor {
    protected AbstractProcessor() {}

    protected static <T> Map<String, Field> getFieldMap(Class<T> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            for (String name : getNames(field)) {
                fieldMap.put(name, field);
            }
        }
        return fieldMap;
    }

    protected static <T> Map<String, List<Method>> getDeclaredMethods(Class<T> clazz) {
        Map<String, List<Method>> declaredMethods = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!declaredMethods.containsKey(method.getName()))
                declaredMethods.put(method.getName(), new ArrayList<>());
            declaredMethods.get(method.getName()).add(method);
        }
        return declaredMethods;
    }

    protected static Method getMatchingGetterMethod(Class<?> type, List<Method> declaredMethods) {
        if (declaredMethods == null) return null;
        for (Method method : declaredMethods) {
            if (!method.getReturnType().equals(type)) continue;
            if (method.getParameterCount() != 0) continue;
            if (!method.getReturnType().isAssignableFrom(type)) continue;
            return method;
        }
        return null;
    }

    protected static Method getMatchingSetterMethod(Class<?> type, List<Method> declaredMethods) {
        if (declaredMethods == null) return null;
        for (Method method : declaredMethods) {
            if (!method.getReturnType().equals(Void.TYPE)) continue;
            if (method.getParameterCount() != 1) continue;
            if (!method.getParameterTypes()[0].isAssignableFrom(type)) continue;
            return method;
        }
        return null;
    }

    protected static List<String> getOrderedGetterMethodNames(String name, Type type) {
        String cap = capitalize(name);
        if (Type.BOOLEAN == type) {
            return List.of("is"+cap, "get"+cap, name);
        }
        return List.of("get"+cap, name);
    }

    protected static List<String> getOrderedSetterMethodNames(String name, Type type) {
        String cap = capitalize(name);
        if (Type.BOOLEAN == type) {
            return List.of("is"+cap, "set"+cap, name);
        }
        return List.of("set"+cap, name);
    }

    protected static List<String> getNames(Field field) {
        List<String> names = new ArrayList<>();
        for (Annotation annotation : field.getDeclaredAnnotations()) {
            if (JsonProperty.class.isAssignableFrom(annotation.annotationType())) {
                String name = ((JsonProperty) annotation).value();
                if (name == null || name.isEmpty()) continue;
                names.add(name);
            }
        }
        names.add(field.getName());
        return names;
    }

    protected static Object parseEnum(Object obj, Class clazz) {
        Object[] enumConstants = clazz.getEnumConstants();
        for (Object constant : enumConstants) {
            if (String.valueOf(constant).equalsIgnoreCase(String.valueOf(obj))) return constant;
        }
        return null;
    }

    protected static Object parseNum(Object obj, Class clazz) {
        if (obj == null) return null;
        if (!Number.class.isAssignableFrom(clazz)) return null;
        String number = String.valueOf(obj);
        if (number.isBlank()) return null;
        Number res = null;
        number = number.strip();
        try {
            res = Double.parseDouble(number);
            if (Byte.class.isAssignableFrom(clazz)) res = res.byteValue();
            if (Short.class.isAssignableFrom(clazz)) res = res.shortValue();
            if (Integer.class.isAssignableFrom(clazz)) res = res.intValue();
            if (Long.class.isAssignableFrom(clazz)) res = res.longValue();
            if (Float.class.isAssignableFrom(clazz)) res = res.floatValue();
        } catch (NumberFormatException ignored) {
        }
        return res;
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
