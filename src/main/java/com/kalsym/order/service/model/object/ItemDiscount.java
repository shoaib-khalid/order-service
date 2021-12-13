/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import com.kalsym.order.service.enums.DiscountCalculationType;
import java.util.Date;


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
    
    public Date discountStartTime;    
    public Date discountEndTime;
    
    public Date lastUpdateTime;
}
