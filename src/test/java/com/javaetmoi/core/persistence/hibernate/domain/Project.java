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

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.util.Objects.hash;

@Entity
public class Project {
    @Id
    private Integer id;

    private String name;

    @ManyToMany(mappedBy = "projects")
    private Collection<Employee> members = new ArrayList<>();

    public Project() {}

    public Project(Integer id, String name) {
        this.id = id;
        this.name = name;
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

    public Collection<Employee> getMembers() {
        return members;
    }

    public void setMembers(Collection<Employee> members) {
        this.members = members;
    }

    @Override
    public int hashCode() {
        return hash(id);
    }

    @Override
    public boolean equals(Object o) {
        // Do not compare the members properties in order to avoid recursive equals stack call on
        // bi-directional relationship
        return o instanceof Project other && new EqualsBuilder()
                .append(id, other.id)
                .append(name, other.name)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(id)
                .append(name)
                .build();
    }
}
