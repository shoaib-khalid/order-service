package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.StoreResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Used to get the store name from the product service
 *
 * @author 7cu
 */
@Service
public class StoreNameService {

    //@Autowired
    @Value("${product-service.URL:https://api.symplified.biz/product-service/v1/}")
    String productServiceURL;

    @Value("${product-service.token:Bearer accessToken}")
    private String productServiceToken;

    public String getLiveChatOrdersGroupName(String storeId) {
        String url = productServiceURL + "stores/" + storeId;
        String logprefix = "getLiveChatOrdersGroupName";

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", productServiceToken);

            HttpEntity httpEntity = new HttpEntity(headers);

            //logger.debug("Sending request to product-service: {} to get store group name (liveChatCsrGroupName) against storeId: {} , httpEntity: {}", url, storeId, httpEntity);
            ResponseEntity res = restTemplate.exchange(url, HttpMethod.GET, httpEntity, StoreResponse.class);

            if (res != null) {
                StoreResponse storeResponse = (StoreResponse) res.getBody();
                String storeName = storeResponse.getData().getLiveChatOrdersGroupName();
                //logger.debug("Store orders group (liveChatOrdersGroupName) received: {}, against storeId: {}", storeName, storeId);
                return storeName;
            } else {
                Logger.application.warn(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Cannot get storename against storeId: " + storeId);
            }

            //logger.debug("Request sent to live service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getting storeName against url: " + productServiceURL, e);

            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error getting storeName against storeId: " + storeId, e);
            return null;
        }
        return "";
    }
}
