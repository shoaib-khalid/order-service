package com.kalsym.order.service.controller;

import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderCompletionStatusConfig;
import com.kalsym.order.service.model.object.SMSRequestBody;
import com.kalsym.order.service.model.repository.OrderCompletionStatusConfigRepository;
import com.kalsym.order.service.model.repository.OrderRepository;
import com.kalsym.order.service.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.kalsym.order.service.utility.Logger;
import com.kalsym.order.service.utility.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    SmsService smsService;

    @Autowired
    OrderCompletionStatusConfigRepository orderCompletionStatusConfigRepository;

    @Autowired
    OrderRepository orderRepository;

    @Value("${order.tracking.url:https://api.e-kedai.my/order-service/orders/track/}")
    private String orderTrackingUrl;

    @PostMapping("/send")
    public ResponseEntity<HttpResponse> sendRequest(HttpServletRequest request,
                                                    @RequestBody SMSRequestBody requestBody) {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String destAddr = requestBody.getPhoneNumber();
        String logprefix = "sendRequestOTP";

        try {

            if (requestBody.getPhoneNumber() != null && requestBody.getPhoneNumber().startsWith("0")) {
                requestBody.setPhoneNumber("6" + requestBody.getPhoneNumber());
            }

            Optional<Order> optOrder = orderRepository.findById(requestBody.getOrderId());

            if (!optOrder.isPresent()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order not found");
                response.setErrorStatus(HttpStatus.NOT_FOUND);
                return ResponseEntity.status(response.getStatus()).body(response);
            }

            Order order = optOrder.get();

            List<OrderCompletionStatusConfig> orderCompletionStatusConfigs = orderCompletionStatusConfigRepository.findByVerticalIdAndStatusAndStorePickupAndStoreDeliveryTypeAndPaymentType(order.getStore().getVerticalCode(), order.getCompletionStatus().toString(), false, order.getDeliveryType(), order.getPaymentType());

            if (orderCompletionStatusConfigs.isEmpty()) {
                Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "order completion status config not found");
                response.setErrorStatus(HttpStatus.NOT_FOUND);
                return ResponseEntity.status(response.getStatus()).body(response);
            }
            OrderCompletionStatusConfig orderCompletionStatusConfig = orderCompletionStatusConfigs.get(0);

            String template = orderCompletionStatusConfig.getSmsToCustomerMessage();
            String trackingUrl = orderTrackingUrl + order.getId();

            String message = smsService.generateMessage(template, order.getInvoiceId(), order.getStore().getName(), trackingUrl);

            assert destAddr != null;
            String smsResponse = smsService.sendSms(destAddr, message);
            response.setData(smsResponse);
            response.setSuccessStatus(HttpStatus.OK);
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "OTP SENT For : ",
                    destAddr);

        } catch (Exception e) {
            response.setErrorStatus(HttpStatus.EXPECTATION_FAILED, e.getMessage());
            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix,
                    "Exception " + e.getMessage());
        }

        return ResponseEntity.status(response.getStatus()).body(response);

    }
}
