package com.kalsym.order.service.utility;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;  

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
import com.kalsym.order.service.model.object.DeliveryServiceResponse;
import org.springframework.http.HttpStatus;

/**
 *
 * @author 7cu
 */
@Service
/**
 * Used to post the order in live.symplifed (rocket chat)
 */
public class DeliveryService {

    private static Logger logger = LoggerFactory.getLogger("application");

    //@Autowired
    @Value("${deliveryService.submitOrder.URL:https://api.symplified.biz/v1/delivery-service/orders/submitorder}")
    String deliveryServiceSubmitOrderURL;

    @Autowired
    StoreNameService storeNameService;

    public DeliveryServiceResponse submitDeliveryOrder(DeliveryServiceSubmitOrder orderPostBody) {
        
        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
            
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(orderPostBody, headers);

            logger.info("Sending request to delivery service : "+orderPostBody.toString());
            ResponseEntity<String> res = restTemplate.exchange(deliveryServiceSubmitOrderURL, HttpMethod.POST, httpEntity, String.class);

            logger.info("Request sent to delivery service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());
            
            if (res.getStatusCode()==HttpStatus.OK) {
                Gson gson = new Gson();
                DeliveryServiceResponse response = gson.fromJson(res.getBody(), DeliveryServiceResponse.class);
                logger.info("DeliveryServiceResponse:"+response.toString());
                return response;
            } 
        } catch (RestClientException e) {
            logger.error("Error creating domain {}", deliveryServiceSubmitOrderURL, e);
            return null;
        }
        return null;
    }

    
}
