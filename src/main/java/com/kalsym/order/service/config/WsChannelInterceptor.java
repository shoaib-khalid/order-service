/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.config;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 *
 * @author taufik
 */
public class WsChannelInterceptor implements ChannelInterceptor {
    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        StompCommand command = accessor.getCommand();
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "WsChannelInterceptor", "Received Command:"+command.name()+"  Channel:"+channel.toString());
    }
    
}
