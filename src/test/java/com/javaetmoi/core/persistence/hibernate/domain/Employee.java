/**
 * Copyright 2012-2026 the original author or authors.
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
package com.javaetmoi.core.persistence.hibernate.domain;

import java.util.*;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static java.util.Objects.hash;

@Entity
public class Employee {
    @Id
    private Integer id;

    private String name;

    @ManyToMany(cascade = ALL)
    private List<Project> projects = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = ALL)
    @MapKey(name = "type")
    private Map<String, Address> addresses = new HashMap<>();

    @Basic(fetch = LAZY)
    private String job;

    public Employee() {}

    public Employee(Integer id, String name, String job) {
        this.id = id;
        this.name = name;
        this.job = job;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public Map<String, Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<String, Address> adresses) {
        this.addresses = adresses;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public int hashCode() {
        return hash(id);
    }

    @Override
    public boolean equals(Object o) {
        // PersistentBag does not respect the collection API so we compare arrays
        return o instanceof Employee other && new EqualsBuilder()
                .append(id, other.id)
                .append(job, other.job)
                .append(name, other.name)
                .append(addresses, other.addresses)
                .append(projects.toArray(), other.projects.toArray())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(id)
                .append(job)
                .append(name)
                .append(addresses)
                .append(projects)
                .build();
    }
}
