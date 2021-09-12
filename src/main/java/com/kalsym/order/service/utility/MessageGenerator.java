package com.kalsym.order.service.utility;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.StoreWithDetails;

import java.util.List;

public class MessageGenerator {

    public static String generateEmailContent(String emailContent,
            Order order,
            StoreWithDetails storeWithDetails,
            List<OrderItem> orderItems,
            OrderShipmentDetail orderShipmentDetail) {
        if (emailContent != null) {
            emailContent = emailContent.replace("{{store-name}}", storeWithDetails.getName());
            emailContent = emailContent.replace("{{store-address}}", storeWithDetails.getStoreAsset().getLogoUrl());
            emailContent = emailContent.replace("{{invoice-number}}", order.getInvoiceId());
            emailContent = emailContent.replace("{{item-list}}", getOrderItemsEmailContent(orderItems));

            if (order.getOrderShipmentDetail().getStorePickup()) {
                emailContent = emailContent.replace("{{delivery-charges}}", "N/A");
                emailContent = emailContent.replace("{{delivery-address}}", "N/A");
                emailContent = emailContent.replace("{{delivery-city}}", "N/A");

            } else {
                emailContent = emailContent.replace("{{delivery-charges}}", order.getOrderPaymentDetail().getDeliveryQuotationAmount() + "");
            }

            if (null != order.getStoreServiceCharges()) {
                emailContent = emailContent.replace("{{service-charges}}", order.getStoreServiceCharges() + "");
            } else {
                emailContent = emailContent.replace("{{service-charges}}", "N/A");
            }

            if (null != order.getAppliedDiscount()) {
                emailContent = emailContent.replace("{{applied-discount}}", order.getAppliedDiscount() + "");
            } else {
                emailContent = emailContent.replace("{{applied-discount}}", "N/A");
            }
            
            
            if(null!=order.getDeliveryDiscount()){
                 emailContent = emailContent.replace("{{delivery-discount}}", order.getDeliveryDiscount() + "");
            } else {
                emailContent = emailContent.replace("{{delivery-discount}}", "N/A");
            }
            
            emailContent = emailContent.replace("{{sub-total}}", order.getSubTotal() + "");
            emailContent = emailContent.replace("{{store-contact}}", storeWithDetails.getPhoneNumber());
            emailContent = emailContent.replace("{{store-contact}}", storeWithDetails.getPhoneNumber());

            if (orderShipmentDetail != null) {
                if (orderShipmentDetail.getCustomerTrackingUrl() != null) {
                    emailContent = emailContent.replace("customer-tracking-url}}", orderShipmentDetail.getCustomerTrackingUrl());
                }
            }

        }

        return emailContent;
    }

    public static String getOrderItemsEmailContent(List<OrderItem> orderItems) {
        String orderItem = "                <tr>\n"
                + "                    <td>{{item-name}}</td>\n"
                + "                    <td>{{item-price}}</td>\n"
                + "                    <td>{{item-quantity}}</td>\n"
                + "                    <td>{{item-total}}</td>\n"
                + "                </tr>";

        String itemList = "";
        for (OrderItem oi : orderItems) {
            String item = orderItem;
            item = item.replace("{{item-name}}", oi.getProductName());
            item = item.replace("{{item-price}}", oi.getProductPrice() + "");
            item = item.replace("{{item-quantity}}", oi.getQuantity() + "");
            item = item.replace("{{item-total}}", oi.getPrice() + "");

            itemList = itemList + item;
        }

        return itemList;
    }

    public static String generateRocketChatMessageContent(String messageContent, Order order, List<OrderItem> orderItems, String onboardingOrderLink) {
        messageContent = messageContent.replace("{{order-items}}", getOrderItemsRcContent(orderItems));
        String orderLink = onboardingOrderLink + order.getId();
        messageContent = messageContent.replace("{{order-link}}", orderLink);
        return messageContent;
    }

    public static String getOrderItemsRcContent(List<OrderItem> orderItems) {
        String orderItemDetails = "";
        for (int i = 0; i < orderItems.size(); i++) {
            orderItemDetails += orderItems.get(i).getSKU() + ", QTY: " + orderItems.get(i).getQuantity() + "\n";
        }
        return orderItemDetails;
    }
}
