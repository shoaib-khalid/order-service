package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.utility.Logger;
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
 * @author saros
 */
@Service
public class CustomerService {

    @Value("${user.service.customer.registration.URL:https://api.symplified.it/user-service/v1/customers/register}")
    String userServiceCustomerRegisterationURL;

    public String addCustomer(OrderShipmentDetail orderShipmentDetail, String storeId) {
        String logprefix = "addCustomer";

        class Customer {

            String id;
            String username;
            String email;
            String roleId;
            String storeId;
            String name;

            public Customer() {
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getRoleId() {
                return roleId;
            }

            public void setRoleId(String roleId) {
                this.roleId = roleId;
            }

            public String getStoreId() {
                return storeId;
            }

            public void setStoreId(String storeId) {
                this.storeId = storeId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

        }

        Customer customer = new Customer();

        customer.setUsername(orderShipmentDetail.getEmail());
        customer.setEmail(orderShipmentDetail.getEmail());
        customer.setName(orderShipmentDetail.getReceiverName());
        customer.setRoleId("CUSTOMER");
        customer.setStoreId(storeId);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");

        HttpEntity<Customer> entity;
        entity = new HttpEntity<>(customer, headers);

        String id = null;
        try {
            ResponseEntity<HttpResponse> res = restTemplate.exchange(userServiceCustomerRegisterationURL, HttpMethod.POST, entity, HttpResponse.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " created customer " + res);

            if (res.getBody().getData() != null) {
                Customer newCustomer = (Customer) res.getBody().getData();
                id = newCustomer.getId();
            }

        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " could not create customer", e);

        }

        return id;
    }
}
