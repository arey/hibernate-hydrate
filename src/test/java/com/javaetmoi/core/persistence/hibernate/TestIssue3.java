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

import com.javaetmoi.core.persistence.hibernate.listWithEmbeddable.Plan;
import com.javaetmoi.core.persistence.hibernate.manyToOneList.Holder;
import com.javaetmoi.core.persistence.hibernate.manyToOneList.System;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for issue 3.
 *
 * @see <a href="https://github.com/arey/hibernate-hydrate/issues/3">Issue 3</a>
 */
class TestIssue3 extends AbstractTest {
	@Test
	void listWithEmbeddableClass() {
		var dbPlan = findDeepHydratedEntity(Plan.class, 1);

		assertEquals(1, dbPlan.getId());
		assertEquals(1, dbPlan.getTransfers().size());
		assertEquals(2, dbPlan.getTransfers().get(0).getSubPlan()
				.getEvents().size());
	}

	@Test
	void listWithMappedEntity() {
		var dbHolder = findDeepHydratedEntity(Holder.class, 1);

		assertEquals(1, dbHolder.getId());
		assertNotNull(dbHolder.getSystem());
		assertEquals(1, dbHolder.getSystem().getId());
		assertNotNull(dbHolder.getSystem().getSubSystems());
		assertEquals(2, dbHolder.getSystem().getSubSystems().size());
	}

	@Test
	void listWithMappedEntityWithAdditionalSpecificCriteria() {
		var dbSystems = doInJPA(entityManager ->
				hydrator.deepHydrateCollection(selectAllSystemsOrderedByNumber(entityManager)));

		assertEquals(2, dbSystems.size());
		assertEquals(1, dbSystems.get(0).getId());
		assertNotNull(dbSystems.get(0).getSubSystems());
		assertEquals(2, dbSystems.get(0).getSubSystems().size());
	}

	@Test
	void retrieveEntityWhenAlreadyInsSessionOnAccountOfSave() {
		var dbSystem = doInJPA(entityManager -> {
			var holder = entityManager.find(Holder.class, 1L);
			var system = holder.getSystem();
			system.setName("system1A");
			system.setSystemNumber("1A");
			var subSystem1 = system.getSubSystems().get(0);
			subSystem1.setName("subsystem1A");
			subSystem1.setSystemNumber("1-1A");
			var subSystem2 = system.getSubSystems().get(1);
			subSystem2.setName("subsystem21");
			subSystem2.setSystemNumber("1-21");
			entityManager.persist(subSystem1);
			entityManager.persist(subSystem2);
			entityManager.persist(system);
			entityManager.persist(holder);

			selectAllSystemsOrderedByNumber(entityManager);
			return hydrator.deepHydrate(system);
		});

		assertEquals(1, dbSystem.getId());
		assertNotNull(dbSystem.getSubSystems());
		assertEquals(2, dbSystem.getSubSystems().size());
		assertEquals(1, dbSystem.getSubSystems().get(0).getId());
		assertEquals(2, dbSystem.getSubSystems().get(1).getId());
	}

	private List<System> selectAllSystemsOrderedByNumber(EntityManager entityManager) {
		var builder = entityManager.getCriteriaBuilder();
		var query = builder.createQuery(System.class);
		var from = query.from(System.class);
		query.orderBy(builder.asc(from.get("systemNumber")));
		var select = query.select(from);
		return entityManager.createQuery(select).getResultList();
	}
}
