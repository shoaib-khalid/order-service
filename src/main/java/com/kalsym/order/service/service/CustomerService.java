package com.kalsym.order.service.service;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.utility.HttpResponse;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.utility.Logger;
import java.io.Serializable;
import java.util.LinkedHashMap;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;
import org.json.JSONObject;
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

    @Value("${user.service.customer.registration.URL:https://api.symplified.biz/user-service/v1/stores/$%storeId$%/customers/register}")
    String userServiceCustomerRegisterationURL;

    @Value("${user.service.customer.udate.URL:https://api.symplified.biz/user-service/v1/stores/$%storeId$%/customers/$%customerId$%}")
    String userServiceCustomerUpdateURL;

    @Value("${user.service.customer.address.URL:https://api.symplified.biz/user-service/v1/customer/$%customerId$%/address}")
    String userServiceCustomerAddressURL;
    
    @Value("${user.service.temp.token.URL:https://api.symplified.biz/user-service/v1/clients/generateTempToken}")
    String userServiceTempTokenURL;

    public String addCustomer(OrderShipmentDetail orderShipmentDetail, String storeId) {
        String logprefix = "addCustomer";

        class Customer {

            String id;
            String username;
            String email;
            String roleId;
            String storeId;
            String phoneNumber;
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

            public String getPhoneNumber() {
                return phoneNumber;
            }

            public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
            }

            @Override
            public String toString() {
                return "Customer{" + "id=" + id + ", username=" + username + ", email=" + email + ", roleId=" + roleId + ", storeId=" + storeId + ", name=" + name + '}';
            }

        }

        Customer customer = new Customer();

        customer.setUsername(orderShipmentDetail.getEmail());
        customer.setEmail(orderShipmentDetail.getEmail());
        customer.setName(orderShipmentDetail.getReceiverName());
        customer.setRoleId("CUSTOMER");
        customer.setStoreId(storeId);
        customer.setPhoneNumber(orderShipmentDetail.getPhoneNumber());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");

        HttpEntity<Customer> entity;
        entity = new HttpEntity<>(customer, headers);

        String id = null;
        try {
            String url = userServiceCustomerRegisterationURL.replace("$%storeId$%", storeId);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " url: " + url);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " entity: " + entity);

            ResponseEntity<HttpResponse> res = restTemplate.exchange(url, HttpMethod.POST, entity, HttpResponse.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " created customer " + res);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " data: " + res.getBody().getData());

            if (res.getBody().getData() != null) {
                JSONObject newCustomer = new JSONObject((LinkedHashMap) res.getBody().getData());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " newCustomer: " + newCustomer);

                id = newCustomer.getString("id");

                addCustomerAddress(id, customer.getName(), orderShipmentDetail);
            }

        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " could not create customer", e);

        }

        return id;
    }

    void addCustomerAddress(String id, String name, OrderShipmentDetail osd) {

        String logprefix = "addCustomerAddress";

        try {
            class CustomerAddress implements Serializable {

                private String name;

                private String address;

                private String email;

                private String phoneNumber;

                private String postCode;

                private String city;

                private String state;

                private String country;

                private String customerId;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getAddress() {
                    return address;
                }

                public void setAddress(String address) {
                    this.address = address;
                }

                public String getEmail() {
                    return email;
                }

                public void setEmail(String email) {
                    this.email = email;
                }

                public String getPhoneNumber() {
                    return phoneNumber;
                }

                public void setPhoneNumber(String phoneNumber) {
                    this.phoneNumber = phoneNumber;
                }

                public String getPostCode() {
                    return postCode;
                }

                public void setPostCode(String postCode) {
                    this.postCode = postCode;
                }

                public String getCity() {
                    return city;
                }

                public void setCity(String city) {
                    this.city = city;
                }

                public String getState() {
                    return state;
                }

                public void setState(String state) {
                    this.state = state;
                }

                public String getCountry() {
                    return country;
                }

                public void setCountry(String country) {
                    this.country = country;
                }

                public String getCustomerId() {
                    return customerId;
                }

                public void setCustomerId(String customerId) {
                    this.customerId = customerId;
                }

                @Override
                public String toString() {
                    return "CustomerAddress{" + "name=" + name + ", address=" + address + ", email=" + email + ", phoneNumber=" + phoneNumber + ", postCode=" + postCode + ", city=" + city + ", state=" + state + ", country=" + country + ", customerId=" + customerId + '}';
                }

            }

            CustomerAddress customerAddress = new CustomerAddress();

            customerAddress.setCity(osd.getCity());
            customerAddress.setCountry(osd.getCountry());
            customerAddress.setCustomerId(id);
            customerAddress.setAddress(osd.getAddress());
            customerAddress.setName(name);
            customerAddress.setState(osd.getState());
            customerAddress.setPhoneNumber(osd.getPhoneNumber());
            customerAddress.setPostCode(osd.getZipcode());
            customerAddress.setEmail(osd.getEmail());

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer accessToken");

            HttpEntity<CustomerAddress> entity;
            entity = new HttpEntity<>(customerAddress, headers);

            String url = userServiceCustomerAddressURL.replace("$%customerId$%", id);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "url: " + url);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "entity: " + entity);

            ResponseEntity<HttpResponse> res = restTemplate.exchange(url, HttpMethod.POST, entity, HttpResponse.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " created customer " + res);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " data: " + res.getBody().getData());

            if (res.getBody().getData() != null) {
                JSONObject newCustomer = new JSONObject((LinkedHashMap) res.getBody().getData());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " newCustomer: " + newCustomer);
            }

        } catch (Exception e) {
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " could not add customer address" + e.getMessage());

        }
    }

    public String updateCustomer(OrderShipmentDetail orderShipmentDetail, String storeId, String customerId) {
        String logprefix = "updateCustomer";

        class Customer {

            String id;
            String username;
            String email;
            String roleId;
            String storeId;
            String phoneNumber;
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

            public String getPhoneNumber() {
                return phoneNumber;
            }

            public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
            }

            @Override
            public String toString() {
                return "Customer{" + "id=" + id + ", username=" + username + ", email=" + email + ", roleId=" + roleId + ", storeId=" + storeId + ", name=" + name + '}';
            }

        }

        Customer customer = new Customer();

        customer.setUsername(orderShipmentDetail.getEmail());
        customer.setEmail(orderShipmentDetail.getEmail());
        customer.setName(orderShipmentDetail.getReceiverName());
        customer.setRoleId("CUSTOMER");
        customer.setStoreId(storeId);
        customer.setPhoneNumber(orderShipmentDetail.getPhoneNumber());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");

        HttpEntity<Customer> entity;
        entity = new HttpEntity<>(customer, headers);

        String id = null;
        try {
            String url = userServiceCustomerUpdateURL.replace("$%storeId$%", storeId);
            url = url.replace("$%customerId$%", customerId);
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " url: " + url);
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " entity: " + entity);

            ResponseEntity<HttpResponse> res = restTemplate.exchange(url, HttpMethod.PUT, entity, HttpResponse.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " updated customer " + res);

            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " data: " + res.getBody().getData());

            if (res.getBody().getData() != null) {
                JSONObject updatedCustomer = new JSONObject((LinkedHashMap) res.getBody().getData());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " updatedCustomer: " + updatedCustomer);

                id = updatedCustomer.getString("id");

                addCustomerAddress(id, customer.getName(), orderShipmentDetail);
            }

        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " could not create customer", e);

        }

        return id;
    }
    
    
    public String GenerateTempToken(String clientId, String username, String password) {
        String logprefix = "GenerateTempToken";

        class TempTokenBody {

            String clientId;
            String username;
            String password;
            
            public TempTokenBody() {
            }

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
            
            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            @Override
            public String toString() {
                return "tempTokenBody{" + "clientId=" + clientId + ", username=" + username + ", password=" + password + "}";
            }

        }

        TempTokenBody tempTokenBody = new TempTokenBody();

        tempTokenBody.setUsername(username);
        tempTokenBody.setPassword(password);
        tempTokenBody.setClientId(clientId);
        
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer accessToken");

        HttpEntity<TempTokenBody> entity;
        entity = new HttpEntity<>(tempTokenBody, headers);

        String token = null;
        try {
            String url = userServiceTempTokenURL;
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " url: " + url);
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " entity: " + entity);

            ResponseEntity<HttpResponse> res = restTemplate.exchange(url, HttpMethod.POST, entity, HttpResponse.class);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " request temp token response: " + res);

            if (res.getBody().getData() != null) {
                JSONObject sessionDetails = new JSONObject((LinkedHashMap) res.getBody().getData());
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " data: " + sessionDetails);
                token = sessionDetails.getJSONObject("session").getString("accessToken");
            }

        } catch (RestClientException e) {
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, " could not request temp token", e);

        }

        return token;
    }

}
