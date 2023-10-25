package com.kalsym.order.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EkedaiTransaction {
    private String transactionId;
    private Double transactionAmount;
}
