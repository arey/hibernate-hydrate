package com.javaetmoi.core.persistence.hibernate.joinInheritance;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class SubClass extends ParentClass {

    @OneToMany
    @JoinColumn(name = "DATA_ID")
    List<Data> datas;


    public List<Data> getDatas() {
        return datas;
    }

    public void setDatas(List<Data> datas) {
        this.datas = datas;
    }
}
