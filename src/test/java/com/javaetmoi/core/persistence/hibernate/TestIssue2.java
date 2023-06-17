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

import com.javaetmoi.core.persistence.hibernate.domain.Parent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/2 fix
 */
class TestIssue2 extends AbstractTest {
    @Test
    void nestedListUsingMappedSuperclass() {
        var parent = getDeepHydratedEntity(Parent.class, 1);
        assertEquals(Long.valueOf(1), parent.getId());
        assertEquals("Parent 1", parent.getName());
        assertEquals(2, parent.getChildren().size());
        assertNotNull("Child 10", parent.getChildren().get(0).getName());
        assertNotNull("Parent 1", parent.getChildren().get(0).getParent().getName());
    }

}
