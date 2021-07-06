package com.kalsym.order.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.kalsym.order.service.model.*;

/**
 *
 * @author FaisalHayatJadoon
 */
public class ProductService {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Value("${product.reduce.quantity.URL:https://api.symplified.biz/stores/%STOREID%/products/%PRODUCTID%/inventory/%ITEMCODE%}")
    String reduceProductInventoryURL;
    
    @Value("${get.product.by.id.URL:https://api.symplified.biz/stores/%STOREID%/products/%PRODUCTID%}")
    String getProductByIdURL;
    
    @Value("${change.product.status.URL:https://api.symplified.biz/stores/%STOREID%/products/%PRODUCTID%/inventory/%ITEMCODE%/productStatus}")
    String changeProductStatusURL;
    
    public Product getProductById(String storeId, String productId) {

        try {
            getProductByIdURL = getProductByIdURL.replace("%STOREID%", storeId);
            getProductByIdURL = getProductByIdURL.replace("%PRODUCTID%", productId);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, Integer> params = new HashMap<>();
//            params.put("quantity", quantity);
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            logger.info("Sending request to product service to get product by id : " + productId +  ", store id: "  + storeId + ", URL: " + getProductByIdURL);
            ResponseEntity<String> res = restTemplate.exchange(getProductByIdURL, HttpMethod.GET, httpEntity, String.class);

            logger.info("Request sent to product service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productObject = jsonObject.getJSONObject("data");
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                //convert json string to object
                Product product = objectMapper.readValue(productObject.toString(), Product.class);

                logger.info("got procduct Inventory object : " + product.toString());
                return product;
            }
        } catch (RestClientException e) {
            logger.error("Error product inventory reduction domain {}", reduceProductInventoryURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            logger.error("Error product inventory reduction {}", reduceProductInventoryURL, ex);
        }
        return null;
    }

    /**
     * 
     * @param storeId
     * @param productId
     * @param itemcode
     * @param quantity
     * @return 
     */
    public ProductInventory reduceProductInventory(String storeId, String productId, String itemcode, int quantity) {

        try {
            reduceProductInventoryURL = reduceProductInventoryURL.replace("%STOREID%", storeId);
            reduceProductInventoryURL = reduceProductInventoryURL.replace("%PRODUCTID%", productId);
            reduceProductInventoryURL = reduceProductInventoryURL.replace("%ITEMCODE%", itemcode);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
            Map<String, Integer> params = new HashMap<>();
            params.put("quantity", quantity);
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            logger.info("Sending request to product service to reduce " + quantity + " quantity:  " + reduceProductInventoryURL);
            ResponseEntity<String> res = restTemplate.exchange(reduceProductInventoryURL, HttpMethod.PUT, httpEntity, String.class, params);

            logger.info("Request sent to product service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                //convert json string to object
                ProductInventory productInventory = objectMapper.readValue(productInventoryObject.toString(), ProductInventory.class);

                logger.info("got procduct Inventory object : " + productInventory.toString());
                return productInventory;
            }
        } catch (RestClientException e) {
            logger.error("Error product inventory reduction domain {}", reduceProductInventoryURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            logger.error("Error product inventory reduction {}", reduceProductInventoryURL, ex);
        }
        return null;
    }
    
    /**
     * 
     * @param storeId
     * @param productId
     * @param itemcode
     * @param productStatus
     * @return 
     */
    public ProductInventory changeProductStatus(String storeId, String productId, String itemcode, ProductStatus productStatus) {

        try {
            changeProductStatusURL = changeProductStatusURL.replace("%STOREID%", storeId);
            changeProductStatusURL = changeProductStatusURL.replace("%PRODUCTID%", productId);
            changeProductStatusURL = changeProductStatusURL.replace("%ITEMCODE%", itemcode);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
            Map<String, String> params = new HashMap<>();
            params.put("status", productStatus.toString());
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            logger.info("Sending request to product service to change status of product to: " + productStatus.toString() + changeProductStatusURL);
            ResponseEntity<String> res = restTemplate.exchange(changeProductStatusURL, HttpMethod.PUT, httpEntity, String.class, params);

            logger.info("Request sent to product service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                //convert json string to object
                ProductInventory productInventory = objectMapper.readValue(productInventoryObject.toString(), ProductInventory.class);

                logger.info("got procduct Inventory object : " + productInventory.toString());
                return productInventory;
            }
        } catch (RestClientException e) {
            logger.error("Error product inventory reduction domain {}", changeProductStatusURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            logger.error("Error product inventory reduction {}", changeProductStatusURL, ex);
        }
        return null;
    }
}
