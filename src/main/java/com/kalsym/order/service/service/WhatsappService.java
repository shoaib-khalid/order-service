/*
 * Copyright (C) 2021 taufik
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.kalsym.order.service.utility.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author taufik
 */

@Service
public class WhatsappService {
        
    @Value("${whatsapp.service.push.url:https://waw.symplified.it/360dialog/callback/templatemessage/push}")
    private String whatsappServiceUrl;
    
    public boolean sendOrderReminder(String[] recipients, String storeName, String invoiceNo) throws Exception {
        String logprefix = "sendWhatsappMessage";
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();
        WhatsappMessage request = new WhatsappMessage();
        request.setGuest(false);
        request.setRecipientIds(recipients);
        request.setRefId(recipients[0]);
        request.setReferenceId("60133429331");
        Template template = new Template();
        template.setName("welcome_to_symplified_7");
        String[] message = {storeName, invoiceNo};
        template.setParameters(message);
        request.setTemplate(template);
        HttpEntity<WhatsappMessage> httpEntity = new HttpEntity<>(request, headers);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + whatsappServiceUrl, "");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity, "");

        ResponseEntity<String> res = restTemplate.postForEntity(whatsappServiceUrl, httpEntity, String.class);

        if (res.getStatusCode() == HttpStatus.ACCEPTED || res.getStatusCode() == HttpStatus.OK) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res.getBody(), "");
            return true;
        } else {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "could not send verification email res: " + res, "");
            return false;
        }

    }
}
