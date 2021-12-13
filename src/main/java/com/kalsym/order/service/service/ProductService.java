package com.kalsym.order.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.ProductStatus;
import com.kalsym.order.service.model.object.DeliveryServiceSubmitOrder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.kalsym.order.service.model.*;
import com.kalsym.order.service.utility.Logger;
import org.springframework.stereotype.Service;

/**
 *
 * @author FaisalHayatJadoon
 */
@Service
public class ProductService {

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

    @Value("${get.store.commission.URL:https://api.symplified.biz/product-service/v1/stores/%STOREID%//commission}")
    private String getStoreCommissionURL;
    
    /**
     *
     * @param storeId
     * @param productId
     * @return
     */
    public Product getProductById(String storeId, String productId) {
        String logprefix = "getProductById";

        try {
            String targetUrl = getProductByIdURL;
            targetUrl = targetUrl.replace("%STOREID%", storeId);
            targetUrl = targetUrl.replace("%PRODUCTID%", productId);
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getProductByIdURL created: " + getProductByIdURL);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, Integer> params = new HashMap<>();
//            params.put("quantity", quantity);
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sending request to product service to get product by id : " + productId + ", store id: " + storeId + ", URL: " + targetUrl);
            ResponseEntity<String> res = restTemplate.exchange(targetUrl, HttpMethod.GET, httpEntity, String.class);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseCode: " + res.getStatusCode() + ", responseBody:" + res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productObject = jsonObject.getJSONObject("data");
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "object of productObject: " + productObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                Product product = objectMapper.readValue(productObject.toString(), Product.class);

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got procduct object : " + product.toString());
                return product;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error product", e);
            return null;
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error product", ex);
        }
        return null;
    }

    /**
     *
     * @param storeId
     * @return
     */
    public StoreCommission getStoreCommissionByStoreId(String storeId) {
        String logprefix = "getStoreCommissionByStoreId";

        try {
            String targetUrl = getStoreCommissionURL;
            targetUrl = targetUrl.replace("%STOREID%", storeId);

//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getProductByIdURL created: " + getProductByIdURL);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, Integer> params = new HashMap<>();
//            params.put("quantity", quantity);
            HttpEntity<String> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sending request to product service to get store commission by store id: " + storeId + ", URL: " + targetUrl);
            ResponseEntity<String> res = restTemplate.exchange(targetUrl, HttpMethod.GET, httpEntity, String.class);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseCode: " + res.getStatusCode() + ", responseBody: " + res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject storeCommissionObject = jsonObject.getJSONObject("data");
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "object of storeCommissionObject: " + storeCommissionObject);
                if (storeCommissionObject != null) {
                    //create ObjectMapper instance
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                    objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                    //convert json string to object
                    StoreCommission storeCommission = objectMapper.readValue(storeCommissionObject.toString(), StoreCommission.class);

                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got storeCommission object : " + storeCommission.toString());
                    return storeCommission;
                }

            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error storeCommission", e);
            return null;
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error storeCommission", ex);
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
        String logprefix = "reduceProductInventory";

        try {
            String targetUrl = reduceProductInventoryURL;
            targetUrl = targetUrl.replace("%STOREID%", storeId);
            targetUrl = targetUrl.replace("%PRODUCTID%", productId);
            targetUrl = targetUrl.replace("%ITEMCODE%", itemcode);
            targetUrl = targetUrl + quantity;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "reduceProductInventoryURL created: " + targetUrl);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, Integer> params = new HashMap<>();
//            params.put("quantity", quantity);
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sending request to product service to reduce " + productId + " quantity:  " + quantity);
            ResponseEntity<String> res = restTemplate.exchange(targetUrl, HttpMethod.PUT, httpEntity, String.class);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseCode: " + res.getStatusCode());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseBody: " + res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "object of productInventory: " + productInventoryObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                ProductInventory productInventory = objectMapper.readValue(productInventoryObject.toString(), ProductInventory.class);

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got procduct Inventory object : " + productInventory.toString());
                return productInventory;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error product inventory reduction domain: " + reduceProductInventoryURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error product inventory reduction: " + reduceProductInventoryURL, ex);
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
        String logprefix = "changeProductStatus";

        try {
            String targetUrl = changeProductStatusURL;
            targetUrl = targetUrl.replace("%STOREID%", storeId);
            targetUrl = targetUrl.replace("%PRODUCTID%", productId);
            targetUrl = targetUrl.replace("%ITEMCODE%", itemcode);
            targetUrl = targetUrl + productStatus.toString();
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "changeProductStatusURL created: " + targetUrl);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, String> params = new HashMap<>();
//            params.put("status", productStatus.toString());
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sending request to product service to change status of product to: " + productStatus.toString());
            ResponseEntity<String> res = restTemplate.exchange(targetUrl, HttpMethod.PUT, httpEntity, String.class);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseCode: " + res.getStatusCode());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseBody: " + res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "object of productInventory: " + productInventoryObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                ProductInventory productInventory = objectMapper.readValue(productInventoryObject.toString(), ProductInventory.class);

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got procduct Inventory object : " + productInventory.toString());
                return productInventory;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error product inventory reduction domain: " + changeProductStatusURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error product inventory reduction:" + changeProductStatusURL, ex);
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
        String logprefix = "getStoreById";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
        HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
        httpEntity = new HttpEntity(headers);

        try {
            String targetUrl = getStoreByIdURL;
            targetUrl = targetUrl.replace("%STOREID%", storeId);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getStoreByIdURL : " + targetUrl);
            ResponseEntity<String> res = restTemplate.exchange(targetUrl, HttpMethod.GET, httpEntity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
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

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store object : " + storeWithDetails.toString());
                return storeWithDetails;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getStoreById domain: " + getStoreByIdURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getStoreById domain: " + getStoreByIdURL, ex);
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
        String logprefix = "getStoreDeliveryDetails";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");
        HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
        httpEntity = new HttpEntity(headers);

        try {
            String targetUrl = getStoreDeliveryDetailsURL;
            targetUrl = targetUrl.replace("%STOREID%", storeId);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getStoreDeliveryDetailsURL : " + targetUrl);
            ResponseEntity<String> res = restTemplate.exchange(targetUrl, HttpMethod.GET, httpEntity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
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

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got store delivery object : " + storeDeliveryDetail.toString());
                return storeDeliveryDetail;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getStoreDeliveryDetails domain: " + getStoreDeliveryDetailsURL, e);
            return null;
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getStoreDeliveryDetails domain: " + getStoreDeliveryDetailsURL, ex);
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
        String logprefix = "getProductInventoryById";

        try {
            String targetUrl = getProductInventoryURL;
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getProductInventoryById URL: " + getProductInventoryURL);
            targetUrl = targetUrl.replace("%STOREID%", storeId);
            targetUrl = targetUrl.replace("%PRODUCTID%", productId);
            targetUrl = targetUrl.replace("%ITEMCODE%", itemcode);
//            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "getProductByIdURL created: " + getProductByIdURL);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");
//            Map<String, Integer> params = new HashMap<>();
//            params.put("quantity", quantity);
            HttpEntity<DeliveryServiceSubmitOrder> httpEntity;
            httpEntity = new HttpEntity(null, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Sending request to product service to get product inventory by id : " + productId + ", store id: " + storeId + ", itemcode: " + itemcode + ", URL: " + targetUrl);
            ResponseEntity<String> res = restTemplate.exchange(targetUrl, HttpMethod.GET, httpEntity, String.class);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseCode: " + res.getStatusCode());
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request sent to product service, responseBody: " + res.getBody());

            if (res.getStatusCode() == HttpStatus.OK) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res : " + res);
                JSONObject jsonObject = new JSONObject(res.getBody());
//        
                //create ObjectMapper instance
                JSONObject productInventoryObject = jsonObject.getJSONObject("data");
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "object of productInventoryObject: " + productInventoryObject);
                //create ObjectMapper instance
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                objectMapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
                //convert json string to object
                ProductInventory productInventory = objectMapper.readValue(productInventoryObject.toString(), ProductInventory.class);

                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "got productInventoryObject object : " + productInventory.toString());
                return productInventory;
            }
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getProductInventoryById", e);
            return null;
        } catch (JsonProcessingException ex) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getProductInventoryById", ex);
        }
        return null;
    }
}
