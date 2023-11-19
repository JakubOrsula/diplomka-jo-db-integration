package com.example.services.utils;

import java.lang.reflect.Field;

public class JavaUtils {
    public static void validateNonNullProperties(Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) { // Access all declared fields
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) { // Check if the field is static
                field.setAccessible(true); // Ensure accessibility
                if (field.get(null) == null) { // Since it's static, use null for the object parameter
                    throw new IllegalStateException("Static field " + field.getName() + " in class " + clazz.getName() + " is null!");
                }
            }
        }
    }
}
