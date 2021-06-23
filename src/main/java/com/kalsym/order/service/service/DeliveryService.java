package com.kalsym.order.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

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
import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
import com.kalsym.order.service.model.object.DeliveryServiceResponse;
import org.springframework.http.HttpStatus;
import com.kalsym.order.service.model.DeliveryOrder;
import java.util.logging.Level;
import org.json.JSONObject;

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
    @Value("${deliveryService.submitOrder.URL:https://api.symplified.biz/delivery-service/v1/orders/submitorder}")
    String deliveryServiceSubmitOrderURL;

    @Value("${deliveryService.confirmation.URL:https://api.symplified.biz/delivery-service/v1/orders/confirmDelivery/}")
    String orderDeliveryConfirmationURL;

    @Autowired
    StoreNameService storeNameService;

    public DeliveryServiceResponse submitDeliveryOrder(DeliveryServiceSubmitOrder orderPostBody) {

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");

            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(orderPostBody, headers);

            logger.info("Sending request to delivery service : " + orderPostBody.toString());
            ResponseEntity<String> res = restTemplate.exchange(deliveryServiceSubmitOrderURL, HttpMethod.POST, httpEntity, String.class);

            logger.info("Request sent to delivery service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Gson gson = new Gson();
                DeliveryServiceResponse response = gson.fromJson(res.getBody(), DeliveryServiceResponse.class);
                logger.info("DeliveryServiceResponse:" + response.toString());
                return response;
            }
        } catch (RestClientException e) {
            logger.error("Error creating domain {}", deliveryServiceSubmitOrderURL, e);
            return null;
        }
        return null;
    }

    public DeliveryOrder confirmOrderDelivery(String refId, String orderId) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
        HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
        httpEntity = new HttpEntity(headers);
        String url = orderDeliveryConfirmationURL + refId + "/" + orderId;
        logger.info("orderDeliveryConfirmationURL : " + url);
        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        logger.info("res : " + res);
        JSONObject jsonObject = new JSONObject(res.getBody());
//        logger.info("got json Object " + jsonObject.toString());
//        System.exit(0);
        //create ObjectMapper instance
        JSONObject deliveryOrderObject = jsonObject.getJSONObject("data").getJSONObject("orderCreated");
        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        //convert json string to object
        DeliveryOrder deliveryOrder = null;
        
        deliveryOrder = objectMapper.readValue(deliveryOrderObject.toString(), DeliveryOrder.class);
        
        logger.info("got delivery object : " + deliveryOrder.toString());
        return deliveryOrder;
    }

}
