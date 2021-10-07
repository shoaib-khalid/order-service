/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.service;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author saros
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public class FCMNotification implements Serializable {

    private String to;
    private String priority;
    private FCMNotificationData data;
}
