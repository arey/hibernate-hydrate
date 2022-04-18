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
import com.javaetmoi.core.persistence.hibernate.manyToOneList.SubSystem;
import com.javaetmoi.core.persistence.hibernate.manyToOneList.System;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
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

	@Test
	void listWithEmbeddableClass() {

		Plan dbContainer = transactionTemplate.execute(status -> {
			Plan plan = sessionFactory.getCurrentSession().get(Plan.class, 1);
			return LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), plan);
		});
		assertEquals(new Integer(1), dbContainer.getId());
		assertEquals(1, dbContainer.getTransfers().size());
		assertEquals(2, dbContainer.getTransfers().get(0).getSubPlan()
				.getEvents().size());
	}

	@Test
	void listWithMappedEntity() {
		Holder dbContainer = transactionTemplate.execute(status -> {
			Holder system = sessionFactory.getCurrentSession().get(Holder.class, 1);
			return LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), system);
		});
		assertEquals(new Integer(1), dbContainer.getId());
		assertNotNull(dbContainer.getSystem());
		assertEquals(new Integer(1), dbContainer.getSystem().getId());
		assertNotNull(dbContainer.getSystem().getSubSystems());
		assertEquals(2, dbContainer.getSystem().getSubSystems().size());
	}

//	@Test
//	public void listWithMappedEntityWithAdditionalSpecificCriteria() {
//		List<System> dbContainer = transactionTemplate.execute(status -> {
//			List<System> system = (List<System>) sessionFactory.getCurrentSession()
//					.createCriteria(System.class)
//					.addOrder(Order.asc("systemNumber")).list();
//			LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), system);
//			return system;
//		});
//		assertNotNull(dbContainer);
//		assertFalse(dbContainer.isEmpty());
//		assertEquals(2, dbContainer.size());
//		assertEquals(new Integer(1), dbContainer.get(0).getId());
//		assertNotNull(dbContainer.get(0).getSubSystems());
//		assertEquals(2, dbContainer.get(0).getSubSystems().size());
//
//	}

//	@Test
//	void retrieveEntityWhenAlreadyInsSessionOnAccountOfSave() {
//		List<System> dbContainer = transactionTemplate.execute(status -> {
//			IdentifierLoadAccess<Holder> loadAccess = sessionFactory.getCurrentSession().byId(Holder.class);
//			Holder holder = loadAccess.getReference(1);
//			System system = holder.getSystem();
//			system.setName("system1A");
//			system.setSystemNumber("1A");
//			SubSystem subSystem1 = system.getSubSystems().get(0);
//			subSystem1.setName("subsystem1A");
//			subSystem1.setSystemNumber("1-1A");
//			SubSystem subSystem2 = system.getSubSystems().get(1);
//			subSystem2.setName("subsystem21");
//			subSystem2.setSystemNumber("1-21");
//			sessionFactory.getCurrentSession().save(subSystem1);
//			sessionFactory.getCurrentSession().save(subSystem2);
//			sessionFactory.getCurrentSession().save(system);
//			sessionFactory.getCurrentSession().save(holder);
//
//			List<System> retrievedSystems = (List<System>) sessionFactory.getCurrentSession()
//					.createCriteria(System.class)
//					.addOrder(Order.asc("systemNumber")).list();
//			LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), system);
//			return retrievedSystems;
//		});
//	}
}
