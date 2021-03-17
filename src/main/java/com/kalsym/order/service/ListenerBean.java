package com.kalsym.order.service;

//import com.kalsym.product.service.utility.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import javax.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 *
 * @author Sarosh
 */
@Component
public class ListenerBean {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    RestTemplate restTemplate;

    @Value("${services.user-service.bulk_authorities.url:not-known}")
    String userServiceUrl;

    @EventListener
    public void handleEvent(ContextRefreshedEvent event) {

        if (event instanceof ContextRefreshedEvent) {
            ApplicationContext applicationContext = ((ContextRefreshedEvent) event).getApplicationContext();
            Map<RequestMappingInfo, HandlerMethod> map = applicationContext.getBean(RequestMappingHandlerMapping.class).getHandlerMethods();

            class Authority {

                private String id;
                private String serviceId;
                private String name;
                private String description;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getServiceId() {
                    return serviceId;
                }

                public void setServiceId(String serviceId) {
                    this.serviceId = serviceId;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getDescription() {
                    return description;
                }

                public void setDescription(String description) {
                    this.description = description;
                }

            }

            List<Authority> authorities = new ArrayList<>();

            map.forEach((RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) -> {
                try {
                    if (!handlerMethod.getMethod().getName().equalsIgnoreCase("error")
                            && !handlerMethod.getMethod().getName().equalsIgnoreCase("errorHtml")) {
                        logger.info(OrderServiceApplication.VERSION, "", "name: " + requestMappingInfo.getName());
                        logger.info(OrderServiceApplication.VERSION, "", "method: " + handlerMethod.getMethod().getName());
                        logger.info(OrderServiceApplication.VERSION, "", "description: " + requestMappingInfo.toString());

                        Authority authority = new Authority();
                        authority.setId(requestMappingInfo.getName());
                        authority.setName(handlerMethod.getMethod().getName());
                        authority.setDescription(requestMappingInfo.toString());
                        authority.setServiceId("order-service");

                        if (null != authority.getId()) {
                            authorities.add(authority);
                        }
                        logger.info(OrderServiceApplication.VERSION, "", "inserted authority", "");

                    }

                } catch (Exception e) {
                    logger.error(OrderServiceApplication.VERSION, "error inserting authority", e.getMessage());
                }

            });

            ResponseEntity<String> response = restTemplate.postForEntity(userServiceUrl, authorities, String.class);
            logger.info(OrderServiceApplication.VERSION, "response: " + response);

        }
    }
}
