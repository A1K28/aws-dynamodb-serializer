package com.github.a1k28.dynamodbparser;

import com.github.a1k28.dynamodbparser.model.Type;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Deserializer extends AbstractProcessor {
    private static final Logger log = Logger.getInstance(Deserializer.class);

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

        Map<String, Field> fieldMap = getFieldMap(clazz);
        Map<String, List<Method>> declaredMethods = getDeclaredMethods(clazz);
        T instance = clazz.getDeclaredConstructor().newInstance();

        for (Map.Entry<?,?> entry : object.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            if (!fieldMap.containsKey(entry.getKey())) continue;
            Field field = fieldMap.get(entry.getKey());

            if (!(entry.getValue() instanceof Map<?,?> map)) continue;
            Type objType = Type.getProperty(map);
            if (objType == null) continue;

            Method method = null;
            for (String name : getOrderedSetterMethodNames(field.getName(), objType)) {
                method = getMatchingSetterMethod(
                        field.getType(),
                        declaredMethods.getOrDefault(name, null));
                if (method != null && method.canAccess(instance)) break;
                else method = null;
            }
            if (method == null) continue;

            Object valObj = parseObject(map, field.getType());
            if (valObj == null) continue;

            if (objType.shouldEnd()) {
                // set value
                if (!field.getType().isAssignableFrom(valObj.getClass())) {
                    log.error("Could not match value: " + valObj + " of class: "
                            + valObj.getClass() + " with field class: " + field.getType());
                    continue;
                }
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
        if (Number.class.isAssignableFrom(clazz)) return parseNum(obj, clazz);
        if (clazz.isEnum()) return parseEnum(obj, clazz);
        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
            return Boolean.parseBoolean(String.valueOf(obj));
        return obj;
    }
}
