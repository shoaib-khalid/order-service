/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import org.springframework.http.HttpStatus;
import com.kalsym.order.service.model.Email;
import com.kalsym.order.service.model.StoreWithDetails;

/**
 *
 * @author taufik
 */
public class OrderProcessResult {
    public HttpStatus httpStatus;
    public String errorMsg;
    public Object data;
    public boolean pendingRequestDelivery;
    public OrderStatus previousStatus;
    public OrderCompletionStatusConfig orderCompletionStatusConfig;
    public Email email;
    public StoreWithDetails storeWithDetails;
}
