package com.kalsym.order.service.service.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interactive {
    private String type;
    private Header header;
    private Body body;
    private Footer footer;
    private Action action;     
}
