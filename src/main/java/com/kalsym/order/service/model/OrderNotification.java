package com.kalsym.order.service.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrderNotification implements Serializable {

    private String body;
    private String title;
    private String to;
}
