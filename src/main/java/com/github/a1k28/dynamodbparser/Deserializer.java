package com.github.a1k28.dynamodbparser;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import com.github.a1k28.dynamodbparser.model.Type;

public final class Deserializer {
    private Deserializer() {}

    public static <T> T deserialize(Map<?,?> object, Class<T> clazz)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Type property = Type.getProperty(object);
        if (property != null) {
            Object value = object.entrySet().iterator().next().getValue();
            if (Type.MAP == property) {
                object = (Map<?, ?>) value;
            } else {
                return (T) parseObject(value, clazz);
            }
        }

        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            for (String name : getNames(field)) {
                fieldMap.put(name, field);
            }
        }

        Map<String, List<Method>> declaredMethods = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (!declaredMethods.containsKey(method.getName()))
                declaredMethods.put(method.getName(), new ArrayList<>());
            declaredMethods.get(method.getName()).add(method);
        }

        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Map.Entry<?,?> entry : object.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            if (!fieldMap.containsKey(entry.getKey())) continue;
            Field field = fieldMap.get(entry.getKey());

            if (!(entry.getValue() instanceof Map<?,?> map)) continue;
            Type objType = Type.getProperty(map);
            if (objType == null) continue;

            Method method = null;
            List<String> setterMethodNames = getOrderedSetterMethodNames(
                    field.getName(), objType);
            for (String setterMethodName : setterMethodNames) {
                method = getMatchingMethod(
                        field.getType(),
                        declaredMethods.getOrDefault(setterMethodName, null));
                if (method != null) break;
            }

            if (method == null || !method.canAccess(instance)) continue;

            Object valObj = parseObject(map, field.getType());
            if (valObj == null) continue;
            if (objType.shouldEnd()) {
                // set value
                method.invoke(instance, valObj);
            } else {
                if (objType == Type.LIST) {
                    // recursively set value for list
                    List list = new ArrayList();
                    List L = (List) valObj;
                    ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
                    Class<?> listElementType = (Class<?>) fieldType.getActualTypeArguments()[0];
                    for (Object item : L) {
                        Object res = deserialize((Map<?, ?>) item, listElementType);
                        list.add(res);
                    }
                    method.invoke(instance, list);
                } else {
                    // recursively set value
                    Object res = deserialize((Map<?, ?>) entry.getValue(), field.getType());
                    method.invoke(instance, res);
                }
            }
        }
        return instance;
    }

    private static Object parseObject(Map<?,?> map, Class clazz) {
        Object obj = map.entrySet().iterator().next().getValue();
        return parseObject(obj, clazz);
    }

    private static Object parseObject(Object obj, Class clazz) {
        if (obj == null) return null;
        if (!Number.class.isAssignableFrom(clazz)) return obj;
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

    private static Method getMatchingMethod(Class<?> type, List<Method> declaredMethods) {
        if (declaredMethods == null) return null;
        for (Method method : declaredMethods) {
            if (!method.getReturnType().equals(Void.TYPE)) continue;
            if (method.getParameterCount() != 1) continue;
            if (!type.isAssignableFrom(method.getParameterTypes()[0])) continue;
            return method;
        }
        return null;
    }

    private static List<String> getNames(Field field) {
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

    private static List<String> getOrderedSetterMethodNames(String name, Type type) {
        name = capitalize(name);
        if (Type.BOOLEAN == type) {
            return List.of("is"+name, "set"+name, name);
        }
        return List.of("set"+name, name);
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
