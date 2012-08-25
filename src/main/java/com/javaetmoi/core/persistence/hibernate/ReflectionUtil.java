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

abstract class ReflectionUtil {

    @SuppressWarnings("unchecked")
    static <T> T getValue(String fieldName, Object object) {
        Class<? extends Object> clazz = object.getClass();
        T value = null;
        try {
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            value = (T) field.get(object);
        } catch (SecurityException ex) {
            handleReflectionException(ex, clazz, fieldName);
        } catch (IllegalArgumentException ex) {
            handleReflectionException(ex, clazz, fieldName);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex, clazz, fieldName);
        }
        return value;
    }

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

    private static void handleReflectionException(Exception ex, Class<? extends Object> clazz,
            String fieldName) {
        throw new IllegalStateException("Unexpected reflection exception while getting "
                + fieldName + " field of class " + clazz.getSimpleName() + ": " + ex.getMessage(),
                ex);

    }

}
