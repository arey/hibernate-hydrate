package com.javaetmoi.core.persistence.hibernate.inheritance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

/**
 * Holds entities by associations declared with the root types of their hierarchies.
 */
@Entity
public class InheritanceHolder {
    @Id
    private Integer id;

    @ManyToOne(fetch = LAZY)
    private JoinedBase joined;

    @ManyToOne(fetch = LAZY)
    private TablePerClassBase tablePerClass;

    protected InheritanceHolder() {}

    public InheritanceHolder(Integer id, JoinedBase joined, TablePerClassBase tablePerClass) {
        this.id = id;
        this.joined = joined;
        this.tablePerClass = tablePerClass;
    }

    public JoinedBase getJoined() {
        return joined;
    }

    public TablePerClassBase getTablePerClass() {
        return tablePerClass;
    }
}
