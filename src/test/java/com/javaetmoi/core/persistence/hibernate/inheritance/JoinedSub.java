package com.javaetmoi.core.persistence.hibernate.inheritance;

import com.javaetmoi.core.persistence.hibernate.mapping.Target;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import static jakarta.persistence.FetchType.LAZY;

@Entity
public class JoinedSub extends JoinedBase {
    @ManyToOne(fetch = LAZY)
    private Target target;

    protected JoinedSub() {}

    public JoinedSub(Integer id, Target target) {
        super(id);
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }
}
