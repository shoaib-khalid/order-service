/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import com.kalsym.order.service.enums.DiscountCalculationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;


/**
 *
 * @author taufik
 */
public class ItemDiscount {
    public double normalPrice;
    public double dineInNormalPrice;
    public double discountedPrice;
    public double dineInDiscountedPrice;
    public String discountLabel;
    public double discountAmount;    
    public boolean normalItemOnly;
    public String discountId;
    public DiscountCalculationType calculationType;
    public double dineInDiscountAmount;  
    public DiscountCalculationType dineInCalculationType;
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    public Date discountStartTime;    
    
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    public Date discountEndTime;
    
    public Date lastUpdateTime;
}
