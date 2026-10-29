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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static jakarta.persistence.FetchType.LAZY;
import static java.util.Objects.hash;

@Entity
public class Address {
    @Id
    private Integer id;

    private String type;

    private String city;

    @ManyToOne(fetch = LAZY)
    private Employee employee;

    @ManyToOne
    private Country  country;

    public Address() {}

    public Address(Integer id, String type, String city, Employee employee) {
        this(id, type, city, employee, null);
    }

    public Address(Integer id, String type, String city, Employee employee, Country country) {
        this.id = id;
        this.type = type;
        this.city = city;
        this.employee = employee;
        this.country = country;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @Override
    public int hashCode() {
        return hash(id);
    }

    @Override
    public boolean equals(Object o) {
        // Do not compare the employees properties in order to avoid recursive equals stack call on
        // bi-directional relationship
        return o instanceof Address other && new EqualsBuilder()
                .append(id, other.id)
                .append(city, other.city)
                .append(country, other.country)
                .append(type, other.type)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(id)
                .append(city)
                .append(country)
                .append(type)
                .build();
    }
}
