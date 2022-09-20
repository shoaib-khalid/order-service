/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.Logger;

@Controller
public class WebSocketController {
        
    
    
    ///----------------------------------------
    //not use, for testing only
    ///----------------------------------------
    
    
        final String logprefix="WebSocketController";
    
	@Autowired
	private SimpMessageSendingOperations simpMessagingTemplate;

	@MessageMapping("/app/websocket")
	@SendToUser("/topic/cart")
	public String processMessageFromClient(@Payload String message, Principal principal) throws Exception {
		Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Received -> Client:"+principal.getName()+" message:"+message);
                String name = new Gson().fromJson(message, Map.class).get("name").toString();
                //messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/reply", name);
		return "Reply to : "+name;
	}
	
	@MessageExceptionHandler
        @SendToUser("/queue/errors")
        public String handleException(Throwable exception) {
            return exception.getMessage();
        }
       

}