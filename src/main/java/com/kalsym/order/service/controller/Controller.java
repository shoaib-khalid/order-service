package com.kalsym.order.service.controller;

import com.kalsym.order.service.Main;
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
        logger.debug("[" + Main.VERSION + "] order-service up!");
        JSONObject json = new JSONObject();
        json.put("name","order-service");
        json.put("version", Main.VERSION);

        return json.toString();
    }

    /**
     * Get the next step in the conversation
     * @param refId
     * @param msisdn
     * @param isOnNet
     * @return 
     */
    @RequestMapping(value = "/step", method = RequestMethod.GET, produces = "application/json", params = {"refId", "msisdn", "isOnNet"})
    public String getMessage(@RequestParam("refId") String refId, @RequestParam("msisdn") long msisdn, @RequestParam("isOnNet") boolean isOnNet) {
        logger.debug("[" + Main.VERSION + "][" + refId + "] New HTTP Request received getMessage");
        return "";
    }
    
    @RequestMapping(value = "/getProducts", method = RequestMethod.GET, produces = "application/json", params = {"refId", "msisdn", "isOnNet"})
    public String getProducts(@RequestParam("refId") String refId, @RequestParam("msisdn") long msisdn, @RequestParam("isOnNet") boolean isOnNet) {
        logger.debug("[" + Main.VERSION + "][" + refId + "] New HTTP Request received getMessage");
        return "";
    }
    
    @RequestMapping(value = "/product", method = RequestMethod.GET, produces = "application/json", params = {"refId", "msisdn", "isOnNet"})
    public String getProduct(@RequestParam("refId") String refId, @RequestParam("msisdn") long msisdn, @RequestParam("isOnNet") boolean isOnNet) {
        logger.debug("[" + Main.VERSION + "][" + refId + "] New HTTP Request received getMessage");
        return "";
    }
    
    @RequestMapping(value = "/selectProduct", method = RequestMethod.PUT, produces = "application/json")
    public void selectProduct(){
        
    }
}
