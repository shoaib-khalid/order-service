package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
@Service
public class UserService {

    @Value("${hellosim.user.login:https://mvnouat.hellosim.com.my:9022/hellosim-api/auth/login}")
    String loginURL;

    public String getToken(String username, String password) {
        String logprefix = "getToken";

        class TokenBody {

            String deviceToken;
            String username;
            String password;

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public String getDeviceToken() {
                return deviceToken;
            }

            public void setDeviceToken(String deviceToken) {
                this.deviceToken = deviceToken;
            }

        }

        TokenBody tokenBody = new TokenBody();

        tokenBody.setUsername(username);
        tokenBody.setPassword(password);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer accessToken");

        HttpEntity<TokenBody> entity;
        entity = new HttpEntity<>(tokenBody);

        String token = null;
        try {
            String url = loginURL;
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " url: " + url);
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " entity: " + entity);

            ResponseEntity res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " request token response: " + res);

            if (res.getStatusCode() == HttpStatus.OK) {

                String responseJson = res.getBody().toString();
                JSONObject jsonResponse = new JSONObject(responseJson);

                String accessToken = jsonResponse.getString("accessToken");

                if (accessToken != null) {
                    token = accessToken;
                }
            }

        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " could not request temp token", e);

        }

        return token;
    }
}
