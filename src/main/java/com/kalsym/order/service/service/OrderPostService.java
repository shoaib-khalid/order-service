package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.livechat.LiveChatLoginReponse;
import com.kalsym.order.service.utility.Logger;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author 7cu
 */
@Service
/**
 * Used to post the order in live.symplifed (rocket chat)
 */
public class OrderPostService {

    //@Autowired
    @Value("${liveChat.sendMessage.URL:http://209.58.160.20:3000/api/v1/chat.postMessage}")
    String liveChatMessageURL;

    //@Value("${liveChat.token:kkJ4G-gEqu5nL-VY9YWBo25otESh_zlQu8ckpic49ne}") //kkJ4G-gEqu5nL-VY9YWBo25otESh_zlQu8ckpic49ne
    private String liveChatToken;

    //@Value("${liveChat.userId:nubj4bBZHctboNnXt}") // nubj4bBZHctboNnXt
    private String liveChatUserId;

    @Value("${onboarding.order.URL:https://symplified.biz/orders/order-details?orderId=}")
    private String onboardingOrderLink;

    @Value("${liveChatlogin.username:order}")
    private String liveChatLoginUsername;

    @Value("${liveChat.login.password:sarosh@1234}")
    private String liveChatLoginPassword;

    @Value("${liveChat.login.url:http://209.58.160.20:3000/api/v1/login}")
    private String liveChatLoginUrl;

    @Autowired
    StoreNameService storeNameService;

    public String postOrderLink(String orderId, String storeId, List<OrderItem> orderItems) {

        String logprefix = "postOrderLink";

        if (!loginLiveChat()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "live chat not logged in");
            return "";
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "live chat logged in");

        String storeLiveChatOrdersGroupName = storeNameService.getLiveChatOrdersGroupName(storeId);
//        String groupName = "#" + storeLiveChatOrdersGroupName + "-orders";
        String orderItemDetails = "";
        for(int i = 0; i< orderItems.size(); i++){
            orderItemDetails += orderItems.get(i).getSKU() + ", QTY: " + orderItems.get(i).getQuantity() + "\n";
        }
        OrderPostRequestBody orderPostBody = new OrderPostRequestBody();
        orderPostBody.setAlias("SYMplified order");
        orderPostBody.setAvatar("");
        // Rocket chat accepts groupName in lowerCase
//        groupName += groupName.toLowerCase();
        orderPostBody.setChannel(storeLiveChatOrdersGroupName);
        orderPostBody.setText("You have a new order, \n " + orderItemDetails + " please click [here](" + onboardingOrderLink + orderId + ") for details"  );

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Auth-Token", liveChatToken);
            headers.add("X-User-Id", liveChatUserId);

            HttpEntity<OrderPostRequestBody> httpEntity;
            httpEntity = new HttpEntity(orderPostBody, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "liveChatMessageURL: " + liveChatMessageURL);
            ResponseEntity res = restTemplate.exchange(liveChatMessageURL, HttpMethod.POST, httpEntity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res);
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error posting order on liveChat URL: {}", liveChatMessageURL, e);
            return null;
        }
        return "";
    }
    
    
    /**
     * 
     * @param orderId
     * @param storeId
     * @param orderItem
     * @return 
     */
    public String sendMinimumQuantityAlarm(String orderId, String storeId, OrderItem orderItem, int remainingQuantity) {

        String logprefix = "sendMinimumQuantityAlarm";

        if (!loginLiveChat()) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "live chat not logged in");
            return "";
        }
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "live chat logged in");

        String storeLiveChatOrdersGroupName = storeNameService.getLiveChatOrdersGroupName(storeId);
//        String groupName = "#" + storeLiveChatOrdersGroupName + "-orders";
        String orderItemDetails = "";
//        for(int i = 0; i< orderItem.size(); i++){
            orderItemDetails = "SKU: " + orderItem.getSKU() + ",\nName: " + orderItem.getProductName() + ",\nREMAINING QTY: " + remainingQuantity + "\n";
//        }
        OrderPostRequestBody orderPostBody = new OrderPostRequestBody();
        orderPostBody.setAlias("SYMplified Out of stock intimation");
        orderPostBody.setAvatar("");
        // Rocket chat accepts groupName in lowerCase
//        groupName += groupName.toLowerCase();
        orderPostBody.setChannel(storeLiveChatOrdersGroupName);
        orderPostBody.setText("Product with below details is going out of stock, \n " + orderItemDetails );

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Auth-Token", liveChatToken);
            headers.add("X-User-Id", liveChatUserId);

            HttpEntity<OrderPostRequestBody> httpEntity;
            httpEntity = new HttpEntity(orderPostBody, headers);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "liveChatMessageURL: " + liveChatMessageURL);
            ResponseEntity res = restTemplate.exchange(liveChatMessageURL, HttpMethod.POST, httpEntity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res);
        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error intimating out of stock prdouct on liveChat URL: {}", liveChatMessageURL, e);
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

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "liveChatLoginUrl: " + liveChatLoginUrl);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "httpEntity: " + httpEntity);

            ResponseEntity<LiveChatLoginReponse> res = restTemplate.exchange(liveChatLoginUrl, HttpMethod.POST, httpEntity, LiveChatLoginReponse.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "res: " + res);

            LiveChatLoginReponse liveChatLoginReponse = res.getBody();

            liveChatUserId = liveChatLoginReponse.getData().userId;
            liveChatToken = liveChatLoginReponse.getData().authToken;
            return true;
        } catch (Exception e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Error loging in livechat ", e);
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
