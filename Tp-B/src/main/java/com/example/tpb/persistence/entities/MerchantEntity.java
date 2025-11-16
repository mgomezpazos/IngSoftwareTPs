package com.example.tpb.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "merchants")
public class MerchantEntity {

    @Id
    private String code;

    public MerchantEntity() {}
    public MerchantEntity(String code) { this.code = code; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

