package com.github.a1k28.dynamodbparser;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class DynamoDB {
    public static <T> Map serialize(T object) throws IllegalAccessException, InvocationTargetException, JsonProcessingException {
        return Serializer.serialize(object);
    }

    public static <T> T deserialize(Map<?,?> object, Class<T> clazz)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return Deserializer.deserialize(object, clazz);
    }
}
