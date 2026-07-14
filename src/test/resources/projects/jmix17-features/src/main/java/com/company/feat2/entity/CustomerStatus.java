package com.company.feat2.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;

public enum CustomerStatus implements EnumClass<Integer> {

    ACTIVE(10),
    BLOCKED(20);

    private final Integer id;

    CustomerStatus(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }
}
