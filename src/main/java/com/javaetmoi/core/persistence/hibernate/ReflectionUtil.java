/**
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.javaetmoi.core.persistence.hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class ReflectionUtil {

    public static Field getField(Class<?> clazz, String name) {
        Class<?> currentClazz = clazz;
        while (!Object.class.equals(currentClazz) && currentClazz != null) {
            Field[] fields = currentClazz.getDeclaredFields();
            for (Field field : fields) {
                if ((name == null) || name.equals(field.getName())) {
                    return field;
                }
            }
            currentClazz = currentClazz.getSuperclass();
        }
        throw new IllegalStateException("The " + clazz.getSimpleName()
              + " class does not have any " + name + " field");
    }

    @SuppressWarnings("unchecked")
    static <T> T getValue(String fieldName, Object object) {
        Class<?> clazz = object.getClass();
        try {
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (SecurityException ex) {
            throw fieldReflectionException(ex, clazz, fieldName);
        } catch (IllegalArgumentException ex) {
            throw fieldReflectionException(ex, clazz, fieldName);
        } catch (IllegalAccessException ex) {
            throw fieldReflectionException(ex, clazz, fieldName);
        }
    }

    private static IllegalStateException fieldReflectionException(Exception ex, Class<?> clazz, String fieldName) {
        throw new IllegalStateException(
              "Unexpected reflection exception while getting field " + fieldName +
                    " of class " + clazz.getSimpleName() + ": " + ex.getMessage(), ex);

    }

    /**
     * Call getter method.
     *
     * @param object
     *            target object
     * @param propertyName
     *            name of property
     */
    protected static Object getProperty(Object object, String propertyName) {
        Class<?> clazz = object.getClass();
        try {
            String methodName = "get" + Character.toTitleCase(propertyName.charAt(0)) + propertyName.substring(1);
            Method getter = clazz.getMethod(methodName);
            getter.setAccessible(true);
            return getter.invoke(object);
        } catch (NoSuchMethodException ex) {
            throw getterReflectionException(ex, clazz, propertyName);
        } catch (IllegalAccessException ex) {
            throw getterReflectionException(ex, clazz, propertyName);
        } catch (InvocationTargetException ex) {
            throw getterReflectionException(ex, clazz, propertyName);
        }
    }

    private static IllegalStateException getterReflectionException(Exception ex, Class<?> clazz, String propertyName) {
        return new IllegalStateException(
              "Unexpected reflection exception while getting property " + propertyName +
                    " of class " + clazz.getSimpleName() + ": " + ex.getMessage(), ex);

    }
}
