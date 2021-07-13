package com.kalsym.order.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
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
import org.springframework.stereotype.Service;

/**
 *
 * @author FaisalHayatJadoon
 */

@Service
public class ProductService {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Value("${product.reduce.quantity.URL:https://api.symplified.biz/product-service/v1/stores/%STOREID%/products/%PRODUCTID%/inventory/%ITEMCODE%?quantity=}")
    private String reduceProductInventoryURL;
    
    @Value("${get.product.by.id.URL:https://api.symplified.biz/product-service/v1/stores/%STOREID%/products/%PRODUCTID%}")
    private String getProductByIdURL;
    
    @Value("${change.product.status.URL:https://api.symplified.biz/product-service/v1/stores/%STOREID%/products/%PRODUCTID%/inventory/%ITEMCODE%/productStatus?status=}")
    private String changeProductStatusURL;
    
    @Value("${get.store.by.id.URL:https://api.symplified.biz/product-service/v1/stores/%STOREID%}")
    private String getStoreByIdURL;
    
    @Value("${get.store.delivery.details.URL:https://api.symplified.biz/product-service/v1/stores/%STOREID%/deliverydetails}")
    private String getStoreDeliveryDetailsURL;
    
    @Value("${get.product.inventory.URL:https://api.symplified.biz/product-service/v1/stores/%STOREID%/products/%PRODUCTID%/inventory/%ITEMCODE%}")
    private String getProductInventoryURL;
    
    /**
     * 
     * @param storeId
     * @param productId
     * @return 
     */
    public Product getProductById(String storeId, String productId) {

        try {
            getProductByIdURL = getProductByIdURL.replace("%STOREID%", storeId);
            getProductByIdURL = getProductByIdURL.replace("%PRODUCTID%", productId);
//            logger.info("getProductByIdURL created: " + getProductByIdURL);
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
                logger.info("object of productObject: " + productObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                Product product = objectMapper.readValue(productObject.toString(), Product.class);

                logger.info("got procduct object : " + product.toString());
                return product;
            }
        } catch (RestClientException e) {
            logger.error("Error product {}", e);
            return null;
        } catch (JsonProcessingException ex) {
            logger.error("Error product {}", ex);
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
            reduceProductInventoryURL = reduceProductInventoryURL + quantity;
            logger.info("reduceProductInventoryURL created: " + reduceProductInventoryURL);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, Integer> params = new HashMap<>();
//            params.put("quantity", quantity);
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            logger.info("Sending request to product service to reduce " + quantity + " quantity:  " + reduceProductInventoryURL);
            ResponseEntity<String> res = restTemplate.exchange(reduceProductInventoryURL, HttpMethod.PUT, httpEntity, String.class);

            logger.info("Request sent to product service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                logger.info("object of productInventory: " + productInventoryObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
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
            changeProductStatusURL = changeProductStatusURL + productStatus.toString();
            logger.info("changeProductStatusURL created: " + changeProductStatusURL);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, String> params = new HashMap<>();
//            params.put("status", productStatus.toString());
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            logger.info("Sending request to product service to change status of product to: " + productStatus.toString() + changeProductStatusURL);
            ResponseEntity<String> res = restTemplate.exchange(changeProductStatusURL, HttpMethod.PUT, httpEntity, String.class);

            logger.info("Request sent to product service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                logger.info("object of productInventory: " + productInventoryObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
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
    
    /**
     * 
     * @param storeId
     * @return
     * @throws JsonProcessingException 
     */
    public StoreWithDetails getStoreById(String storeId) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
        HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
        httpEntity = new HttpEntity(headers);

        try {
            getStoreByIdURL = getStoreByIdURL.replace("%STOREID%", storeId);
            logger.info("getStoreByIdURL : " + getStoreByIdURL);
            ResponseEntity<String> res = restTemplate.exchange(getStoreByIdURL, HttpMethod.GET, httpEntity, String.class);
            logger.info("res : " + res);

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject storeObject = jsonObject.getJSONObject("data");
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                StoreWithDetails storeWithDetails = objectMapper.readValue(storeObject.toString(), StoreWithDetails.class);

                logger.info("got store object : " + storeWithDetails.toString());
                return storeWithDetails;
            }
        } catch (RestClientException e) {
            logger.error("Error getStoreById domain {}", getStoreByIdURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            logger.error("Error getStoreById domain {}", getStoreByIdURL, ex);
        }
        return null;
    }
    
    
    /**
     * 
     * @param storeId
     * @return
     * @throws JsonProcessingException 
     */
    public StoreDeliveryDetail getStoreDeliveryDetails(String storeId) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
        HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
        httpEntity = new HttpEntity(headers);

        try {
            getStoreDeliveryDetailsURL = getStoreDeliveryDetailsURL.replace("%STOREID%", storeId);
            logger.info("getStoreDeliveryDetailsURL : " + getStoreDeliveryDetailsURL);
            ResponseEntity<String> res = restTemplate.exchange(getStoreDeliveryDetailsURL, HttpMethod.GET, httpEntity, String.class);
            logger.info("res : " + res);

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject storeDeliveryDetailsObject = jsonObject.getJSONObject("data");
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                StoreDeliveryDetail storeDeliveryDetail = objectMapper.readValue(storeDeliveryDetailsObject.toString(), StoreDeliveryDetail.class);

                logger.info("got store delivery object : " + storeDeliveryDetail.toString());
                return storeDeliveryDetail;
            }
        } catch (RestClientException e) {
            logger.error("Error getStoreDeliveryDetails domain {}", getStoreDeliveryDetailsURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            logger.error("Error getStoreDeliveryDetails domain {}", getStoreDeliveryDetailsURL, ex);
        }
        return null;
    }
    
    
    /**
     * 
     * @param storeId
     * @param productId
     * @param itemcode
     * @return 
     */
    public ProductInventory getProductInventoryById(String storeId, String productId, String itemcode) {

        try {
            getProductInventoryURL = getProductInventoryURL.replace("%STOREID%", storeId);
            getProductInventoryURL = getProductInventoryURL.replace("%PRODUCTID%", productId);
            getProductInventoryURL = getProductInventoryURL.replace("%ITEMCODE%", itemcode);
//            logger.info("getProductByIdURL created: " + getProductByIdURL);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, Integer> params = new HashMap<>();
//            params.put("quantity", quantity);
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            logger.info("Sending request to product service to get product inventory by id : " + productId +  ", store id: "  + storeId + ", itemcode: " + itemcode  + ", URL: " + getProductInventoryURL);
            ResponseEntity<String> res = restTemplate.exchange(getProductInventoryURL, HttpMethod.GET, httpEntity, String.class);

            logger.info("Request sent to product service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                logger.info("res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                logger.info("object of productInventoryObject: " + productInventoryObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                ProductInventory productInventory = objectMapper.readValue(productInventoryObject.toString(), ProductInventory.class);

                logger.info("got productInventoryObject object : " + productInventory.toString());
                return productInventory;
            }
        } catch (RestClientException e) {
            logger.error("Error getProductInventoryById {}", e);
            return null;
        } catch (JsonProcessingException ex) {
            logger.error("Error getProductInventoryById {}", ex);
        }
        return null;
    }
}
