/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.service.whatsapp;

import com.kalsym.order.service.service.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author saros
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class WhatsappInteractiveMessage {    
    private String[] recipientIds;
    private String title;
    private String subTitle;
    private String url;
    private String urlType;
    private String menuItems;
    private String refId;
    private String referenceId;
    private Boolean guest;
    private String orderId;
    private String merchantToken;
    private Interactive interactive;
}
