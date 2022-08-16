package com.kalsym.order.service.service.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reply {
    private String id;
    private String title;
    
    public Reply(String id, String title) {
        this.id = id;
        this.title = title;
    }
}
