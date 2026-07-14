package com.company.feat2.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@JmixEntity
@Embeddable
public class Address {

    @Column(name = "CITY")
    private String city;
}
