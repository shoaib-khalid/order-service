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
public class Row {
    private String id;
    private String title;
    private String description;
    
    public Row(String id, String title, String description) {
        this.id = id;
        if (title!=null && title.length()>24) {
            title = title.substring(0, 24);
        }
        this.title = title;
        if (description!=null && description.length()>72) {
            description = description.substring(0, 72);
        }
        this.description = description;
    }
    
    public Row(String id, String title, String description, Double price) {
        this.id = id;
        if (title!=null && title.length()>15) {
            title = title.substring(0, 15);
        }
        this.title = title + " RM"+price;
        if (description!=null && description.length()>72) {
            description = description.substring(0, 72);
        }
        this.description = description;
    }
}
