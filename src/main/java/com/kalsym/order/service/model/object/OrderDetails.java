/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model.object;

import com.kalsym.order.service.model.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author taufik
 */
@Getter
@Setter
@ToString
public class OrderDetails {    
    private Order order;
    private String currentCompletionStatus;
    private String nextCompletionStatus;
    private String nextActionText;           
}
