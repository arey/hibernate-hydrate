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
 * Unit test for the <a href="https://github.com/arey/hibernate-hydrate/issues/3">Issue 3</a> fix.
 */
class TestIssue3 extends AbstractTest {
	@Test
	void listWithEmbeddableClass() {
		var plan = transactional(session ->
				LazyLoadingUtil.deepHydrate(session,
						session.get(Plan.class, 1)));
		assertEquals(1, plan.getId());
		assertEquals(1, plan.getTransfers().size());
		assertEquals(2, plan.getTransfers().get(0).getSubPlan()
				.getEvents().size());
	}

	@Test
	void listWithMappedEntity() {
		var holder = transactional(session ->
				LazyLoadingUtil.deepHydrate(session,
						session.get(Holder.class, 1)));
		assertEquals(1, holder.getId());
		assertNotNull(holder.getSystem());
		assertEquals(1, holder.getSystem().getId());
		assertNotNull(holder.getSystem().getSubSystems());
		assertEquals(2, holder.getSystem().getSubSystems().size());
	}

	@Test
	void listWithMappedEntityWithAdditionalSpecificCriteria() {
		var systems = transactional(session ->
				LazyLoadingUtil.deepHydrate(session,
						selectAllSystemsOrderedByNumber(session)));
		assertNotNull(systems);
		assertFalse(systems.isEmpty());
		assertEquals(2, systems.size());
		assertEquals(1, systems.get(0).getId());
		assertNotNull(systems.get(0).getSubSystems());
		assertEquals(2, systems.get(0).getSubSystems().size());
	}

	@Test
	void retrieveEntityWhenAlreadyInsSessionOnAccountOfSave() {
		var dbSystem = transactional(session -> {
			var loadAccess = session.byId(Holder.class);
			var holder = loadAccess.getReference(1);
			var system = holder.getSystem();
			system.setName("system1A");
			system.setSystemNumber("1A");
			var subSystem1 = system.getSubSystems().get(0);
			subSystem1.setName("subsystem1A");
			subSystem1.setSystemNumber("1-1A");
			var subSystem2 = system.getSubSystems().get(1);
			subSystem2.setName("subsystem21");
			subSystem2.setSystemNumber("1-21");
			session.persist(subSystem1);
			session.persist(subSystem2);
			session.persist(system);
			session.persist(holder);

			selectAllSystemsOrderedByNumber(session);
			return LazyLoadingUtil.deepHydrate(session, system);
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
