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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.tuple.component.ComponentMetamodel;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EmbeddedComponentType;
import org.junit.Before;
import org.junit.Test;

public class TestReflectionUtil {

    private ComponentMetamodel metadata;

    @Before
    public void setUp() {
        Configuration configuration = new Configuration();
        Mappings mappings = configuration.createMappings();
        Component component = new Component(mappings, new RootClass());
        Property p1 = new Property();
        p1.setName("client");
        SimpleValue p1val = new SimpleValue(mappings);
        p1val.setTypeName("java.lang.Integer");
        p1.setValue(p1val);
        component.addProperty(p1);
        Property p2 = new Property();
        p2.setName("user");
        SimpleValue p2val = new SimpleValue(mappings);
        p2val.setTypeName("java.lang.String");
        p2.setValue(p2val);
        component.addProperty(p2);
        metadata = new ComponentMetamodel(component);
    }

    @Test
    public void getPropertyNamesFromComponentType() {
        ComponentType componentType = new ComponentType(null, metadata);
        String[] propertyNames = ReflectionUtil.getValue("propertyNames", componentType);
        assertNotNull(propertyNames);
        assertEquals(2, propertyNames.length);
        assertEquals("client", propertyNames[0]);
    }

    @Test
    public void getPropertyNamesFromEmbeddedComponentType() {
        ComponentType componentType = new EmbeddedComponentType(null, metadata);
        String[] propertyNames = ReflectionUtil.getValue("propertyNames", componentType);
        assertNotNull(propertyNames);
        assertEquals(2, propertyNames.length);
        assertEquals("client", propertyNames[0]);
    }
}
