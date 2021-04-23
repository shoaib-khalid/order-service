package com.kalsym.order.service.utility;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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

/**
 *
 * @author 7cu
 */
@Service
public class OrderPostService {

    private static Logger logger = LoggerFactory.getLogger("application");

    //@Autowired
    @Value("${liveChat.sendMessage.URL:http://209.58.160.20:3000/api/v1/chat.postMessage}")
    String liveChatMessageURL;

    @Value("${liveChat.token:kkJ4G-gEqu5nL-VY9YWBo25otESh_zlQu8ckpic49ne}") //kkJ4G-gEqu5nL-VY9YWBo25otESh_zlQu8ckpic49ne
    private String liveChatToken;

    @Value("${liveChat.userId:nubj4bBZHctboNnXt}") // nubj4bBZHctboNnXt
    private String liveChatUserId;

    @Autowired
    StoreNameService storeNameService;

    public String postOrderLink(String orderId, String storeId) {

        String logprefix = "createSubDomain";

        String storeName = storeNameService.getStoreName(storeId);
        String groupName = "#" + storeName + "-orders";

        OrderPostRequestBody orderPostBody = new OrderPostRequestBody();
        orderPostBody.setAlias("SYMplified order");
        orderPostBody.setAvatar("");
//        orderPostBody.setChannel("#kfood-orders");
        orderPostBody.setChannel(groupName);
        orderPostBody.setText("You have a new order, please visit the merchant portal to process, orderId: " + orderId);

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Auth-Token", liveChatToken);
            headers.add("X-User-Id", liveChatUserId);

            HttpEntity<OrderPostRequestBody> httpEntity;
            httpEntity = new HttpEntity(orderPostBody, headers);

            logger.debug("Sending request to live service, on group: {}", groupName);
            ResponseEntity res = restTemplate.exchange(liveChatMessageURL, HttpMethod.POST, httpEntity, String.class);

            logger.debug("Request sent to live service, responseCode: {}, responseBody: {}", res.getStatusCode(), res.getBody());
        } catch (RestClientException e) {
            logger.error("Error creating domain {}", liveChatMessageURL, e);
            return null;
        }
        return "";
    }

    @Getter
    @Setter
    @ToString
    public class OrderPostRequestBody {

        private String alias;
        private String avatar;
        private String channel;
        private String text;
    }
}
