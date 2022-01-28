/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import org.springframework.http.HttpStatus;

/**
 *
 * @author taufik
 */
public class OrderProcessResult {
    public HttpStatus httpStatus;
    public String errorMsg;
    public Object data;
    public boolean pendingRequestDelivery;
}
