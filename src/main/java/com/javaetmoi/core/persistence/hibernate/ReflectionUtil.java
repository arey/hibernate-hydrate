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
        T value = null;
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            value = (T) field.get(object);
        } catch (SecurityException ex) {
            handleReflectionException(ex);
        } catch (NoSuchFieldException ex) {
            handleReflectionException(ex);
        } catch (IllegalArgumentException ex) {
            handleReflectionException(ex);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        }
        return value;
    }

    private static void handleReflectionException(Exception ex) {
        throw new IllegalStateException("Unexpected reflection exception: " + ex.getMessage(), ex);

    }

}
