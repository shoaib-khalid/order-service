/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

/**
 *
 * @author taufik
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeliveryResponse {
   Integer deliveryProviderId;
   Boolean isSuccess;
   String message;
   String status;
   String systemTransactionId;
   String orderId;
   Object orderCreated;
}
