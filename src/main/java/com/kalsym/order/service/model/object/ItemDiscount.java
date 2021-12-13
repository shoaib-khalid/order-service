/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import com.kalsym.order.service.enums.DiscountCalculationType;
import java.time.LocalDateTime;
import java.util.Date;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

/**
 *
 * @author taufik
 */
public class ItemDiscount {
    public double normalPrice;
    public double discountedPrice;
    public String discountLabel;
    public double discountAmount;    
    public boolean normalItemOnly;
    public String discountId;
    public DiscountCalculationType calculationType;
    
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDateTime discountStartTime;
    
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    public LocalDateTime discountEndTime;
    
    public Date lastUpdateTime;
}
