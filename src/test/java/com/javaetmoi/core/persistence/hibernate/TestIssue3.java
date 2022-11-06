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
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/3 fix
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestLazyLoadingUtilConfiguration.class)
class TestIssue3 {

	@Autowired
    private SessionFactory      sessionFactory;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private DBUnitLoader        dbUnitLoader;

	/**
	 * Populate entities graph and embedded database
	 */
	@BeforeEach
	@Transactional
	void setUp() {
		dbUnitLoader.loadDatabase(getClass());
	}

	// TODO markus 2022-11-06: Fix this or report a bug to Hibernate.
	@Disabled("Hibernate 6 seems to not support this or is buggy.")
	@Test
	void listWithEmbeddableClass() {
		var dbContainer = transactionTemplate.execute(status -> {
			var entityManager = sessionFactory.getCurrentSession();
			var plan = entityManager.get(Plan.class, 1);
			return LazyLoadingUtil.deepHydrate(entityManager, plan);
		});
		assertEquals(1, dbContainer.getId());
		assertEquals(1, dbContainer.getTransfers().size());
		assertEquals(2, dbContainer.getTransfers().get(0).getSubPlan()
				.getEvents().size());
	}

	@Test
	void listWithMappedEntity() {
		var dbContainer = transactionTemplate.execute(status -> {
			var entityManager = sessionFactory.getCurrentSession();
			var holder = entityManager.get(Holder.class, 1);
			return LazyLoadingUtil.deepHydrate(entityManager, holder);
		});
		assertEquals(1, dbContainer.getId());
		assertNotNull(dbContainer.getSystem());
		assertEquals(1, dbContainer.getSystem().getId());
		assertNotNull(dbContainer.getSystem().getSubSystems());
		assertEquals(2, dbContainer.getSystem().getSubSystems().size());
	}

	@Test
	void listWithMappedEntityWithAdditionalSpecificCriteria() {
		var dbContainer = transactionTemplate.execute(status -> {
			var entityManager = sessionFactory.getCurrentSession();
			var systems = selectAllSystemsOrderedByNumber(entityManager);
			return LazyLoadingUtil.deepHydrate(entityManager, systems);
		});
		assertNotNull(dbContainer);
		assertFalse(dbContainer.isEmpty());
		assertEquals(2, dbContainer.size());
		assertEquals(1, dbContainer.get(0).getId());
		assertNotNull(dbContainer.get(0).getSubSystems());
		assertEquals(2, dbContainer.get(0).getSubSystems().size());
	}

	@Test
	void retrieveEntityWhenAlreadyInsSessionOnAccountOfSave() {
		var dbContainer = transactionTemplate.execute(status -> {
			var entityManager = sessionFactory.getCurrentSession();
			var loadAccess = entityManager.byId(Holder.class);
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
			entityManager.persist(subSystem1);
			entityManager.persist(subSystem2);
			entityManager.persist(system);
			entityManager.persist(holder);

			selectAllSystemsOrderedByNumber(entityManager);
			return LazyLoadingUtil.deepHydrate(entityManager, system);
		});
		// TODO markus 2022-11-06: Don't we need some assertions here?
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
