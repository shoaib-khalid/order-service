package com.kalsym.order.service.service;

import com.kalsym.order.service.model.livechat.LiveChatLoginReponse;
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
/**
 * Used to post the order in live.symplifed (rocket chat)
 */
public class OrderPostService {

    private static Logger logger = LoggerFactory.getLogger("application");

    //@Autowired
    @Value("${liveChat.sendMessage.URL:http://209.58.160.20:3000/api/v1/chat.postMessage}")
    String liveChatMessageURL;

    //@Value("${liveChat.token:kkJ4G-gEqu5nL-VY9YWBo25otESh_zlQu8ckpic49ne}") //kkJ4G-gEqu5nL-VY9YWBo25otESh_zlQu8ckpic49ne
    private String liveChatToken;

    //@Value("${liveChat.userId:nubj4bBZHctboNnXt}") // nubj4bBZHctboNnXt
    private String liveChatUserId;

    @Value("${onboarding.order.URL:https://symplified.biz/orders/order-details?orderId=}")
    private String onboardingOrderLink;

    @Value("${liveChatlogin.username:sarosh}")
    private String liveChatLoginUsername;

    @Value("${liveChat.login.password:sarosh@1234}")
    private String liveChatLoginPassword;

    @Value("${liveChat.login.url:http://209.58.160.20:3000/api/v1/login}")
    private String liveChatLoginUrl;

    @Autowired
    StoreNameService storeNameService;

    public String postOrderLink(String orderId, String storeId) {

        String logprefix = "postOrderLink";

        if (!loginLiveChat()) {
            logger.info("live chat not logged in");
            return "";
        }
        logger.info("live chat logged in");

        String storeLiveChatOrdersGroupName = storeNameService.getLiveChatOrdersGroupName(storeId);
//        String groupName = "#" + storeLiveChatOrdersGroupName + "-orders";

        OrderPostRequestBody orderPostBody = new OrderPostRequestBody();
        orderPostBody.setAlias("SYMplified order");
        orderPostBody.setAvatar("");
        // Rocket chat accepts groupName in lowerCase
//        groupName += groupName.toLowerCase();
        orderPostBody.setChannel(storeLiveChatOrdersGroupName);
        orderPostBody.setText("You have a new order, please visit the merchant portal to process, orderId: " + orderId + " " + onboardingOrderLink + orderId);

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Auth-Token", liveChatToken);
            headers.add("X-User-Id", liveChatUserId);

            HttpEntity<OrderPostRequestBody> httpEntity;
            httpEntity = new HttpEntity(orderPostBody, headers);

            logger.info("httpEntity: " + httpEntity);
            logger.info("liveChatMessageURL: " + liveChatMessageURL);
            ResponseEntity res = restTemplate.exchange(liveChatMessageURL, HttpMethod.POST, httpEntity, String.class);
            logger.info("res: " + res);
        } catch (RestClientException e) {
            logger.error("Error posting order on liveChat URL: {}", liveChatMessageURL, e);
            return null;
        }
        return "";
    }

    public boolean loginLiveChat() {
        String logprefix = "loginLiveChat";
        if (null != liveChatToken) {
            return true;
        }
        class LoginRequest {

            public String user;
            public String password;

            public LoginRequest() {
            }

            public LoginRequest(String user, String password, String code) {
                this.user = user;
                this.password = password;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            @Override
            public String toString() {
                return "LoginRequest{" + "user=" + user + ", password=" + password + '}';
            }

        }

        RestTemplate restTemplate = new RestTemplate();

        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setUser(liveChatLoginUsername);
        loginRequest.setPassword(liveChatLoginPassword);

        HttpEntity<LoginRequest> httpEntity = new HttpEntity<>(loginRequest);

        try {

            logger.info("liveChatLoginUrl: " + liveChatLoginUrl);
            logger.info("httpEntity: " + httpEntity);

            ResponseEntity<LiveChatLoginReponse> res = restTemplate.exchange(liveChatLoginUrl, HttpMethod.POST, httpEntity, LiveChatLoginReponse.class);
            logger.info("res: " + res);

            LiveChatLoginReponse liveChatLoginReponse = res.getBody();

            liveChatUserId = liveChatLoginReponse.getData().userId;
            liveChatToken = liveChatLoginReponse.getData().authToken;
            return true;
        } catch (Exception e) {
            logger.error("Error loging in livechat ", e);
            return false;
        }
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
