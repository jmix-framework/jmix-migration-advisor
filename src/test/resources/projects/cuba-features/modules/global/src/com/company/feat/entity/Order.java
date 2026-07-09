package com.company.feat.entity;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.Listeners;

@Listeners({"com.company.feat.listener.OrderListener1", "com.company.feat.listener.OrderListener2"})
public class Order extends StandardEntity {
    private String number;
}
