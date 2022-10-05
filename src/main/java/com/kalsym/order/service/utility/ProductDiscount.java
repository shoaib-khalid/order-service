/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.model.repository.StoreDiscountRepository;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.DiscountCalculationType;
import com.kalsym.order.service.model.object.ItemDiscount;
import com.kalsym.order.service.model.RegionCountry;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 *
 * @author taufik
 */
public class ProductDiscount {
    
    public static ItemDiscount getItemDiscount(StoreDiscountRepository storeDiscountRepository,
            String storeId, String itemCode) {
        
        ItemDiscount discountDetails = null;
        List<Object[]> t = storeDiscountRepository.getItemDiscount(itemCode, storeId);
        if (t!=null) {
            Object[] itemDiscount = t.get(0);
            String discountName = String.valueOf(itemDiscount[0]);
            if (!discountName.equalsIgnoreCase("NOTFOUND")) {
                discountDetails = new ItemDiscount();
                try {
                    //discountName, startDate, endDate, normalPriceItemOnly, discountAmount, calculationType, discountId
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "discountDetails:"+itemDiscount.toString());
                    discountDetails.discountLabel = String.valueOf(itemDiscount[0]);
                    discountDetails.discountAmount = Double.parseDouble(String.valueOf(itemDiscount[4]));
                    discountDetails.calculationType = DiscountCalculationType.valueOf(String.valueOf(itemDiscount[5]));
                    discountDetails.discountId =  String.valueOf(itemDiscount[6]);
                    discountDetails.dineInDiscountAmount = Double.parseDouble(String.valueOf(itemDiscount[7]));
                    discountDetails.dineInCalculationType = DiscountCalculationType.valueOf(String.valueOf(itemDiscount[8]));
                } catch (Exception ex){
                    Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, "getItemDiscount", "Error extracting discount details : ", ex);
                }
            }
        }
        
        return discountDetails;
    }
                    
}
