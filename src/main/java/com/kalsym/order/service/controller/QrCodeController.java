/*
 * Copyright (C) 2021 mohsi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.kalsym.order.service.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.OrderStatus;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.OrderGroup;
import com.kalsym.order.service.model.QrcodeSession;
import com.kalsym.order.service.model.repository.TagRepository;
import com.kalsym.order.service.model.repository.QrcodeSessionRepository;
import com.kalsym.order.service.model.repository.StoreDetailsRepository;
import com.kalsym.order.service.service.FCMService;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.TxIdUtil;
import java.util.List;
import java.util.Optional;
import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
 *
 * @author mohsin
 */
@RestController
@RequestMapping("/qrcode")
public class QrCodeController {

   
    @Autowired
    TagRepository tagRepository;
    
    @Autowired
    QrcodeSessionRepository qrcodeSessionRepository;
    
    @Autowired
    StoreDetailsRepository storeDetailsRepository;
    
    @Autowired
    FCMService fcmService;
    
    @Value("${qrcode.URL:https://dev-my2.symplified.ai/getting-started}")
    String qrCodeUrl;
    
    @PostMapping(path = {"/generate"}, name = "qrcode-generate")
    public ResponseEntity<HttpResponse> generate(HttpServletRequest request, @RequestBody String json) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "qrcode-generate-post, URL:  " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request Body:  " + json);
        
        JsonObject jsonRequest = new Gson().fromJson(json, JsonObject.class);
        String storeId = jsonRequest.get("storeId").getAsString();        
        List<Object[]> tagList = tagRepository.findByStoreId(storeId);
        BigInteger tagId=null;
        String tagKeyword="";
        if (tagList!=null && tagList.size()>0) {
            Object[] tag = tagList.get(0);            
            tagId = (BigInteger)(tag[0]);
            tagKeyword = String.valueOf(tag[1]);
        } else {
            //cannot find tag for this store
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Tag with storeId: " + storeId + " not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Tag not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
               
        //generate token
        String token = TxIdUtil.generateQrcodeToken(tagKeyword);
        QrcodeSession session = new QrcodeSession();
        session.setToken(token);
        session.setTagId(tagId);
        session.setStoreId(storeId);
        qrcodeSessionRepository.save(session);
        
        String urlGenerated = qrCodeUrl + "/" + tagKeyword + "?token=" + token;
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("url", urlGenerated);
        response.setData(jsonResponse);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    @GetMapping(path = {"/validate"}, name = "qrcode-validate")
    public ResponseEntity<HttpResponse> validate(HttpServletRequest request, 
            @RequestParam(required = true) String token
            ) throws Exception {
        String logprefix = request.getRequestURI() + " ";
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "qrcode-validate-get, URL:  " + request.getRequestURI());
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Request token:  " + token);
        
        //check token in db
        Optional<QrcodeSession> qrcodeSessionOpt = qrcodeSessionRepository.findById(token);
        if (!qrcodeSessionOpt.isPresent()) {
            //token not found
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "QrcodeSession with token: " + token + " not found");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setMessage("Token not found");
            return ResponseEntity.status(response.getStatus()).body(response);
        }
        
        QrcodeSession session = qrcodeSessionOpt.get();
        String storeId = session.getStoreId();
        
        Optional<StoreWithDetails> storeOpt = storeDetailsRepository.findById(storeId);
        StoreWithDetails store = storeOpt.get();        
        //remove token from db
        qrcodeSessionRepository.delete(session);
        
        //push FCM to mobile app
        //send push notification to DCM message
        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat to store: " + store.getName());
        try {
            fcmService.sendQrcodeNotification(storeId, store.getName(), store.getRegionVertical().getDomain());
        } catch (Exception e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "pushNotificationToMerchat error ", e);
        }
        
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("tokenValid", true);
        response.setData(jsonResponse);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
}
