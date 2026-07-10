package com.company.feat2.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

@JmixEntity(name = "feat2_CustomerData")
public class CustomerData {

    private String displayName;

    private Integer ordersCount;
}
