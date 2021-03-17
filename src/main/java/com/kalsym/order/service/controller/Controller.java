package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author 7cu
 */
@RestController
public class Controller {
    @Autowired
    private Environment env;

    private static org.slf4j.Logger logger = LoggerFactory.getLogger("application");

    @RequestMapping(value = "/liveness", method = RequestMethod.GET, produces = "application/json", params = {})
    public String liveness() {
        logger.debug("[" + OrderServiceApplication.VERSION + "] order-service up!");
        JSONObject json = new JSONObject();
        json.put("name","order-service");
        json.put("version", OrderServiceApplication.VERSION);

        return json.toString();
    }
}
