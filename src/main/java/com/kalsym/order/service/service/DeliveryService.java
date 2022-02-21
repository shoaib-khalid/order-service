package com.kalsym.order.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.kalsym.order.service.OrderServiceApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;

import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
import com.kalsym.order.service.model.object.DeliveryServiceResponse;
import com.kalsym.order.service.model.object.DeliveryPickup;
import com.kalsym.order.service.model.object.DeliveryServiceBulkConfirmResponse;
import org.springframework.http.HttpStatus;
import com.kalsym.order.service.model.DeliveryOrder;
import com.kalsym.order.service.model.DeliveryQuotation;
import com.kalsym.order.service.model.Product;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.model.object.DeliveryServiceBulkConfirmRequest;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Date;
import java.sql.Time;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author 7cu
 */
@Service
/**
 * Used to post the order in live.symplifed (rocket chat)
 */
public class DeliveryService {

    //@Autowired
    @Value("${deliveryService.submitOrder.URL:not-set}")
    String deliveryServiceSubmitOrderURL;

    @Value("${deliveryService.confirmation.URL:not-set}")
    String orderDeliveryConfirmationURL;
    
    @Value("${deliveryService.get.quotation.URL:not-set}")
    String getQuotationURL;
    
    @Value("${deliveryService.bulk.confirm.URL:not-set}")
    String orderBulkConfirmURL;

    @Autowired
    StoreNameService storeNameService;

    public DeliveryServiceResponse submitDeliveryOrder(DeliveryServiceSubmitOrder orderPostBody) {
        String logprefix = "submitDeliveryOrder";

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
            
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(orderPostBody, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sending request to delivery service : " + orderPostBody.toString());
            ResponseEntity<String> res = restTemplate.exchange(deliveryServiceSubmitOrderURL, HttpMethod.POST, httpEntity, String.class);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to delivery service, responseCode: " + res.getStatusCode() + ", responseBody: " + res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Gson gson = new Gson();
                DeliveryServiceResponse response = gson.fromJson(res.getBody(), DeliveryServiceResponse.class);
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "DeliveryServiceResponse:" + response.toString());
                return response;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error creating domain: " + deliveryServiceSubmitOrderURL, e);
            return null;
        }
        return null;
    }

    public DeliveryOrder confirmOrderDelivery(String refId, String orderId, String pickupDate, String pickupTime)  {
        String logprefix = "confirmOrderDelivery";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
        
        DeliveryPickup deliveryPickup = new DeliveryPickup();
        deliveryPickup.startPickScheduleDate = pickupDate;
        deliveryPickup.startPickScheduleTime = pickupTime;
        HttpEntity<DeliveryPickup> httpEntity;
        httpEntity = new HttpEntity<>(deliveryPickup, headers);
        
        try {
            String url = orderDeliveryConfirmationURL + refId + "/" + orderId;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderDeliveryConfirmationURL : " + url);
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject deliveryObject = jsonObject.getJSONObject("data").getJSONObject("orderCreated");
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                //convert json string to object
                DeliveryOrder deliveryOrder = objectMapper.readValue(deliveryObject.toString(), DeliveryOrder.class);

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got delivery order object : " + deliveryOrder.toString());
                return deliveryOrder;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error delivery order domain: " + orderDeliveryConfirmationURL, e);
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error delivery order domain: " + orderDeliveryConfirmationURL, ex);
        } catch (Exception ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error delivery order domain: " + orderDeliveryConfirmationURL, ex);
        }
        return null;
    }
    
    
    public DeliveryQuotation getDeliveryQuotation(String deliveryQuotationId)  {
        String logprefix = "getDeliveryQuotation";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
        
        HttpEntity<DeliveryPickup> httpEntity;
        httpEntity = new HttpEntity<>(null, headers);
        
        try {
            String url = getQuotationURL + "/" + deliveryQuotationId;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getDeliveryQuotation URL : " + url);
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject deliveryObject = jsonObject.getJSONObject("data");
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                DeliveryQuotation deliveryQuotation = objectMapper.readValue(deliveryObject.toString(), DeliveryQuotation.class);
                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got delivery quotation object : " + deliveryQuotation.toString());
                return deliveryQuotation;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error delivery quotation domain: " + getQuotationURL, e);
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error delivery quotation domain: " + getQuotationURL, ex);
        } catch (Exception ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error delivery quotation domain: " + getQuotationURL, ex);
        }
        return null;
    }
    
    
    public List<DeliveryServiceBulkConfirmResponse> bulkConfirmOrderDelivery(List bulkConfirmOrderList)  {
        String logprefix = "bulkConfirmOrderDelivery";
        try {
            
            String url = orderBulkConfirmURL;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "orderBulkConfirmURL : " + url);
            
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");

            HttpEntity<List<DeliveryServiceBulkConfirmRequest>> httpEntity;
            httpEntity = new HttpEntity<>(bulkConfirmOrderList, headers);
        
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);

            if (res.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonObject = new JSONObject(res.getBody());

                List<DeliveryServiceBulkConfirmResponse> deliveryResponseList = new ArrayList();
                //create ObjectMapper instance
                JSONArray deliveryList= jsonObject.getJSONArray("data");
                for (int i=0;i<deliveryList.length();i++) {
                    
                    JSONObject deliveryObject = deliveryList.getJSONObject(i);
                    //create ObjectMapper instance
                    ObjectMapper objectMapper = new ObjectMapper();
                    //convert json string to object
                    DeliveryServiceBulkConfirmResponse deliveryResponse = objectMapper.readValue(deliveryObject.toString(), DeliveryServiceBulkConfirmResponse.class);
                    deliveryResponseList.add(deliveryResponse);
                }
                
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got delivery response list object : " + deliveryResponseList.toString());
                return deliveryResponseList;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "RestClientException rror delivery order domain: " + orderBulkConfirmURL, e);
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "JsonProcessingException Error delivery order domain: " + orderBulkConfirmURL, ex);
        } catch (Exception ex2) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception Error delivery order domain: " + orderBulkConfirmURL, ex2);
        }
        
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Finish process bulkConfirmOrderDelivery");
        return null;
    }
      

}
