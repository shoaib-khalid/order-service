/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;
        
/**
 *
 * @author taufik
 */
public class DeliveryServiceBulkConfirmResponse {
    public String id;
    public String orderId;  
    public String systemTransactionId; 
    public String spTransactionId; 
    public String status; 
    public String customerTrackingUrl;
    public int deliveryProviderId;
    public String message;
    public boolean success;
 
}
