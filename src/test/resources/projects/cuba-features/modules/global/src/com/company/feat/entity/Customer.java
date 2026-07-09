package com.company.feat.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.Listeners;

@Listeners("com.company.feat.listener.CustomerListener")
public class Customer extends StandardEntity {
    private String name;
}
