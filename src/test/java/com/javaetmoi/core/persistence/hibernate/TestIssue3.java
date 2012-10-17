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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.javaetmoi.core.persistence.hibernate.listWithEmbeddable.Plan;
import com.javaetmoi.core.persistence.hibernate.manyToOneList.Holder;
import com.javaetmoi.core.persistence.hibernate.manyToOneList.SubSystem;
import com.javaetmoi.core.persistence.hibernate.manyToOneList.System;

/**
 * Unit test for the https://github.com/arey/hibernate-hydrate/issues/3 fix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("TestLazyLoadingUtil-context.xml")
public class TestIssue3 {

	@Autowired
    private SessionFactory   sessionFactory;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private DBUnitLoader dbUnitLoader;

	/**
	 * Populate entities graph and embbeded database
	 */
	@Before
	@Transactional
	public void setUp() {
		dbUnitLoader.loadDatabase(getClass());
	}

	@Test
	public void listWithEmbeddableClass() {

		Plan dbContainer = transactionTemplate
				.execute(new TransactionCallback<Plan>() {

					public Plan doInTransaction(TransactionStatus status) {
						Plan plan = (Plan) sessionFactory.getCurrentSession().get(Plan.class, 1);
						LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), plan);
						return plan;
					}
				});
		assertEquals(new Integer(1), dbContainer.getId());
		assertEquals(1, dbContainer.getTransfers().size());
		assertEquals(2, dbContainer.getTransfers().get(0).getSubPlan()
				.getEvents().size());
	}

	@Test
	public void listWithMappedEntity() {
		Holder dbContainer = transactionTemplate
				.execute(new TransactionCallback<Holder>() {

					public Holder doInTransaction(TransactionStatus status) {
						Holder system = (Holder) sessionFactory.getCurrentSession().get(Holder.class, 1);
						LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(),
								system);
						return system;
					}
				});
		assertEquals(new Integer(1), dbContainer.getId());
		assertNotNull(dbContainer.getSystem());
		assertEquals(new Integer(1), dbContainer.getSystem().getId());
		assertNotNull(dbContainer.getSystem().getSubSystems());
		assertEquals(2, dbContainer.getSystem().getSubSystems().size());
	}

	@Test
	public void listWithMappedEntityWithAdditionalSpecificCriteria() {
		List<System> dbContainer = transactionTemplate
				.execute(new TransactionCallback<List<System>>() {
					public List<System> doInTransaction(TransactionStatus status) {
						List<System> system = (List<System>) sessionFactory.getCurrentSession()
								.createCriteria(System.class)
								.addOrder(Order.asc("systemNumber")).list();
						LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), system);
						return system;
					}
				});
		assertNotNull(dbContainer);
		assertFalse(dbContainer.isEmpty());
		assertEquals(2, dbContainer.size());
		assertEquals(new Integer(1), dbContainer.get(0).getId());
		assertNotNull(dbContainer.get(0).getSubSystems());
		assertEquals(2, dbContainer.get(0).getSubSystems().size());

	}

	@Test
	public void retrieveEntityWhenAlreadyInsSessionOnAccountOfSave() {
		List<System> dbContainer = transactionTemplate
				.execute(new TransactionCallback<List<System>>() {
					public List<System> doInTransaction(TransactionStatus status) {
						Holder holder = new Holder();
						System system = new System();
						system.setName("system1");
						system.setSystemNumber("1");
						SubSystem subSystem1 = new SubSystem();
						subSystem1.setName("subsystem1");
						subSystem1.setParent(system);
						subSystem1.setSystemNumber("1-1");
						SubSystem subSystem2 = new SubSystem();
						subSystem2.setName("subsystem2");
						subSystem2.setParent(system);
						subSystem2.setSystemNumber("1-2");
						holder.setSystem(system);
						List<SubSystem> subSystems = new ArrayList<SubSystem>();
						subSystems.add(subSystem1);
						subSystems.add(subSystem2);
						system.setSubSystems(subSystems);
						sessionFactory.getCurrentSession().save(subSystem1);
						sessionFactory.getCurrentSession().save(subSystem2);
						sessionFactory.getCurrentSession().save(system);
						sessionFactory.getCurrentSession().save(holder);

						List<System> retrievedSystems = (List<System>) sessionFactory.getCurrentSession()
								.createCriteria(System.class)
								.addOrder(Order.asc("systemNumber")).list();
						LazyLoadingUtil.deepHydrate(sessionFactory.getCurrentSession(), system);
						return retrievedSystems;
					}
				});
	}
}
