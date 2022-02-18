/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;
        
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.StoreWithDetails;

/**
 *
 * @author taufik
 */
public class DeliveryServiceBulkConfirmRequest {
    public String deliveryQuotationId;
    public String orderId; 
    public OrderStatus previousStatus; 
    public OrderCompletionStatusConfig orderCompletionStatusConfig;
    public Email email;
    public StoreWithDetails storeWithDetails;
}
