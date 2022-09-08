/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.config;

import com.kalsym.order.service.OrderServiceApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.kalsym.order.service.utility.Logger;

/**
 *
 * @author taufik
 */
@Component
public class WebSocketEventListener {
    
    final String logprefix="WebSocketEventListener";
    
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        //String username = (String) headerAccessor.getSessionAttributes().get("username");
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Received disconnect event");
        /*
        if(username != null) {

            WebSocketChatMessage chatMessage = new WebSocketChatMessage();
            chatMessage.setType("Leave");
            chatMessage.setSender(username);

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }*/
    }
}
