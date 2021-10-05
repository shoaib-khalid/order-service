package com.kalsym.order.service.utility;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.StoreWithDetails;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

import java.util.List;
import java.util.Optional;

public class MessageGenerator {

    public static String generateEmailContent(String emailContent,
            Order order,
            StoreWithDetails storeWithDetails,
            List<OrderItem> orderItems,
            OrderShipmentDetail orderShipmentDetail,
            RegionCountry regionCountry) {
        if (emailContent != null) {
            if ( storeWithDetails.getStoreAsset()!=null) {
                if ( storeWithDetails.getStoreAsset().getLogoUrl()!=null) {
                    emailContent = emailContent.replace("{{store-logo}}", storeWithDetails.getStoreAsset().getLogoUrl());            
                }
            }
            emailContent = emailContent.replace("{{store-name}}", storeWithDetails.getName());
            emailContent = emailContent.replace("{{store-address}}", storeWithDetails.getAddress());
            emailContent = emailContent.replace("{{invoice-number}}", order.getInvoiceId());
            emailContent = emailContent.replace("{{item-list}}", getOrderItemsEmailContent(orderItems));
            
            //convert time to merchant timezone
            if (regionCountry!=null) {
                LocalDateTime startLocalTime = DateTimeUtil.convertToLocalDateTimeViaInstant(order.getCreated(), ZoneId.of(regionCountry.getTimezone()) );                
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss, zzzz");
                emailContent = emailContent.replace("{{order-created-date-time}}", formatter1.format(startLocalTime));                
            }            
            
            if (order.getOrderShipmentDetail().getStorePickup()) {
                emailContent = emailContent.replace("{{delivery-charges}}", "N/A");
                emailContent = emailContent.replace("{{delivery-address}}", "N/A");
                emailContent = emailContent.replace("{{delivery-city}}", "N/A");
            } else {
                emailContent = emailContent.replace("{{delivery-charges}}", order.getOrderPaymentDetail().getDeliveryQuotationAmount() + "");
                emailContent = emailContent.replace("{{delivery-address}}", orderShipmentDetail.getAddress());
                emailContent = emailContent.replace("{{delivery-city}}", orderShipmentDetail.getCity());
            }

            if (null != order.getStoreServiceCharges()) {
                emailContent = emailContent.replace("{{service-charges}}", String.format("%.2f",order.getStoreServiceCharges()) + "");
            } else {
                emailContent = emailContent.replace("{{service-charges}}", "N/A");
            }

            if (null != order.getAppliedDiscount()) {
                emailContent = emailContent.replace("{{applied-discount}}", String.format("%.2f",order.getAppliedDiscount()) + "");
            } else {
                emailContent = emailContent.replace("{{applied-discount}}", "N/A");
            }
            
            
            if(null!=order.getDeliveryDiscount()){
                 emailContent = emailContent.replace("{{delivery-discount}}", String.format("%.2f",order.getDeliveryDiscount()) + "");
            } else {
                emailContent = emailContent.replace("{{delivery-discount}}", "N/A");
            }
            
            emailContent = emailContent.replace("{{sub-total}}", String.format("%.2f",order.getSubTotal()) + "");
            emailContent = emailContent.replace("{{grand-total}}", String.format("%.2f",order.getTotal()) + "");
            emailContent = emailContent.replace("{{store-contact}}", storeWithDetails.getPhoneNumber());
           
            if (orderShipmentDetail != null) {
                if (orderShipmentDetail.getCustomerTrackingUrl() != null) {
                    emailContent = emailContent.replace("{{customer-tracking-url}}", orderShipmentDetail.getCustomerTrackingUrl());
                }
            }

        }

        return emailContent;
    }

    public static String getOrderItemsEmailContent(List<OrderItem> orderItems) {
        String orderItem = "                <tr>\n"
                + "                    <td>{{item-name}}</td>\n"
                + "                    <td  style=\"text-align: center;\">{{item-price}}</td>\n"
                + "                    <td  style=\"text-align: center;\">{{item-quantity}}</td>\n"
                + "                    <td  style=\"text-align: right;\">{{item-total}}</td>\n"
                + "                </tr>";

        String itemList = "";
        for (OrderItem oi : orderItems) {
            String item = orderItem;
            if (oi.getProductVariant()!=null && !"".equals(oi.getProductVariant()) && !"null".equals(oi.getProductVariant())) {
                item = item.replace("{{item-name}}", oi.getProductName()+" | "+oi.getProductVariant());
            } else{
                item = item.replace("{{item-name}}", oi.getProductName());
            }
            item = item.replace("{{item-price}}", String.format("%.2f",oi.getProductPrice()) + "");
            item = item.replace("{{item-quantity}}", oi.getQuantity() + "");
            item = item.replace("{{item-total}}", String.format("%.2f",oi.getPrice()) + "");

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
