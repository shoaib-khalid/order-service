package com.kalsym.order.service.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author 7cu
 */
@Service
public class StoreNameService {

    private static Logger logger = LoggerFactory.getLogger("application");

    //@Autowired
    @Value("${product-service.URL:http://209.58.160.20:7071/}")
    String productServiceURL;

    @Value("${product-service.token:Bearer accessToken}")
    private String productServiceToken;

    public String getStoreName(String storeId) {
        String url = productServiceURL + "stores/" + storeId;
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", productServiceToken);

            HttpEntity httpEntity = new HttpEntity(headers);

            logger.debug("sending request to product service to get store name");
            ResponseEntity res = restTemplate.exchange(url, HttpMethod.GET, httpEntity, StoreResponse.class);

            if (res != null) {
                StoreResponse storeResponse = (StoreResponse) res.getBody();
                String storeName = storeResponse.getData().getName();
                logger.debug("Store name received: {}, against storeId: {}", storeName, storeId);
                return storeName;
            } else {
                logger.warn("Cannot get storename against storeId: {}", storeId);
            }

            logger.debug("Request sent to live service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());
        } catch (RestClientException e) {
            logger.error("Error creating domain {}", productServiceURL, e);
            return null;
        }
        return "";
    }
}
