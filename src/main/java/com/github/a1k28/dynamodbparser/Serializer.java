package com.github.a1k28.dynamodbparser;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Serializer extends AbstractProcessor {
    private Serializer() {
    }
    static <T> Map serialize(T object) throws IllegalAccessException, InvocationTargetException, JsonProcessingException {
        return serialize(object, false);
    }

    private static <T> Map serialize(T object, boolean wrapObject) throws IllegalAccessException, InvocationTargetException, JsonProcessingException {
        if (object == null) return null;
        Class<T> clazz = (Class<T>) object.getClass();

        Object simpleValue = simpleValue(object, clazz);
        if (simpleValue != null)
            return Type.getProperty(clazz).createObject(simpleValue);

        Map<String, List<Method>> declaredMethods = getDeclaredMethods(clazz);
        Map<String, Object> instance = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Type type = Type.getProperty(field.getType());
            if (type == null) continue;

            Method method = null;
            for (String name : getOrderedGetterMethodNames(field.getName(), type)) {
                method = getMatchingGetterMethod(
                        field.getType(),
                        declaredMethods.getOrDefault(name, null));
                if (method != null && method.canAccess(object)) break;
                else method = null;
            }
            if (method == null) continue;

            String name = getNames(field).getFirst();
            Object res = method.invoke(object);

            if (type.shouldEnd()) {
                instance.put(name, type.createObject(res));
            } else {
                if (type == Type.LIST) {
                    List<Map> list = new ArrayList();
                    List L = (List) res;
                    for (Object o : L) {
                        list.add(serialize(o, true));
                    }
                    instance.put(name, type.createObject(list));
                } else {
                    Object r = serialize(res, true);
                    instance.put(name, type.createObject(r));
                }
            }
        }

        if (wrapObject) {
            Type type = Type.getProperty(clazz);
            return type.createObject(instance);
        }
        return instance;
    }

    private static Object simpleValue(Object object, Class clazz) {
        if (clazz.isPrimitive())
            clazz = Array.get(Array.newInstance(clazz,1),0).getClass();

        if (Number.class.isAssignableFrom(clazz)) return parseNum(object, clazz);
        if (String.class.isAssignableFrom(clazz)) return String.valueOf(object);
        if (Boolean.class.isAssignableFrom(clazz)) return Boolean.parseBoolean(String.valueOf(object));
        if (Character.class.isAssignableFrom(clazz)) return object;

        return null;
    }
}
