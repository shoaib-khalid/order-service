/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.kalsym.order.service.OrderServiceApplication;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 *
 * @author taufik
 */
public class HttpClient {
    
    //Override timeouts in request factory
    public static HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory(int connectTimeout, int readTimeout) 
    {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                          = new HttpComponentsClientHttpRequestFactory();
        //Connect timeout
        clientHttpRequestFactory.setConnectTimeout(connectTimeout);

        //Read timeout
        clientHttpRequestFactory.setReadTimeout(readTimeout);
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "Delivery-Service", "Set Connect Timeout:"+connectTimeout+" WaitTimeout:"+readTimeout);
        return clientHttpRequestFactory;
    }
    
}
