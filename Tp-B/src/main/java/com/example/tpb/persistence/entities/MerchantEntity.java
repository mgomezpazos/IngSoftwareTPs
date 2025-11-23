package com.example.tpb.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "merchants")
@Getter @Setter
public class MerchantEntity {
    @Id
    private String code;
    public MerchantEntity() {}
    public MerchantEntity(String code) {this.code = code;}
}