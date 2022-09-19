/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;
/**
 *
 * @author taufik
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
        
        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
          config.enableSimpleBroker("/topic");
          config.setApplicationDestinationPrefixes("/app");
        }

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
                registry.addEndpoint("/chat")
                        .setAllowedOrigins("*");
		registry.addEndpoint("/chat")
                        .setAllowedOrigins("*")
                        .withSockJS();
	}  
        
        @Override
        public void configureClientInboundChannel(ChannelRegistration registration) {
            registration.interceptors(new WsChannelInterceptor());
        }
}
