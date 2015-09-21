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
import static org.mockito.Mockito.when;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.model.process.spi.MetadataBuildingProcess;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.tuple.component.ComponentMetamodel;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EmbeddedComponentType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

@Ignore
public class TestReflectionUtil {

    private ComponentMetamodel metadata;

    @Before
    public void setUp() {
        System.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder().build();
        MetadataBuildingOptions options = new MetadataBuilderImpl.MetadataBuildingOptionsImpl(standardServiceRegistry);
        MetadataImplementor metadataImplementor =MetadataBuildingProcess.build(Mockito.mock(MetadataSources.class), options);
        Component component = new Component(metadataImplementor, new RootClass(Mockito.mock(MetadataBuildingContext.class)));
        Property p1 = new Property();
        p1.setName("client");
        SimpleValue p1val = new SimpleValue(metadataImplementor);
        p1val.setTypeName("java.lang.Integer");
        p1.setValue(p1val);
        component.addProperty(p1);
        Property p2 = new Property();
        p2.setName("user");
        SimpleValue p2val = new SimpleValue(metadataImplementor);
        p2val.setTypeName("java.lang.String");
        p2.setValue(p2val);
        component.addProperty(p2);
        MetadataBuildingOptions metadataBuildingOptions = Mockito.mock(MetadataBuildingOptions.class);
        when(metadataBuildingOptions.getServiceRegistry()).thenReturn(Mockito.mock(StandardServiceRegistry.class));
        this.metadata = new ComponentMetamodel(component, metadataBuildingOptions);
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
