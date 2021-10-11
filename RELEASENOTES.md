##################################################
# order-service-3.1.8-SNAPSHOT | 11-October-2021
##################################################
### Code Changes:
Remove pre-authorize token checking for getorder() function


##################################################
# order-service-3.1.7-SNAPSHOT | 11-October-2021
##################################################
### Code Changes:
Bug fix


##################################################
# order-service-3.1.6-SNAPSHOT | 11-October-2021
##################################################
### Code Changes:
Add custom pre-authorize function to check if session token is authorize to view/edit/delete spesific order


##################################################
# order-service-3.1.0-SNAPSHOT | 4-October-2021
##################################################
### Code Changes:
1. Change invoice number format : Store prefix + 5 digit sequence number
2. Add order created date time in email template, show in merchant time zone.

### DB changes:
1. ALTER TABLE store ADD invoiceSeqNo INT default 0;
2. ALTER TABLE `order_completion_status_config` ADD `statusSequence` TINYINT(1) AFTER `storeDeliveryType`;

3. create new function:

DELIMITER $$

USE `symplified`$$

DROP FUNCTION IF EXISTS `getInvoiceSeqNo`$$

CREATE FUNCTION `getInvoiceSeqNo`(storeId VARCHAR(50)) RETURNS INT
    DETERMINISTIC
BEGIN
UPDATE store SET invoiceSeqNo=LAST_INSERT_ID(invoiceSeqNo+1) WHERE id=storeId;
RETURN LAST_INSERT_ID();
END$$

DELIMITER ;


##################################################
# order-service-3.0.56-SNAPSHOT | 28-September-2021
##################################################
### Code Changes:
* Bug fix for FCM Notification

### DB changes:
ALTER TABLE order_completion_status_config ADD pushNotificationContent VARCHAR(500);
ALTER TABLE order_completion_status_config ADD storePushNotificationTitle VARCHAR(100);

##################################################
# order-service-3.0.53-SNAPSHOT | 24-September-2021
##################################################
### Code Changes:
* New features : Handle discount calculation
* Bug fix for email template

### DB changes:
ALTER TABLE order_item ADD productVariant VARCHAR(500);

##################################################
# order-service-3.0.41-SNAPSHOT | 15-September-2021
##################################################
### Code Changes:
* Added fcm notification on order status change.

### Properties Changes:
fcm.url=https://fcm.googleapis.com/fcm/send
fcm.token=key=AAAAj5hNRLI:APA91bEBW0gxueP0sjTtvixEb41IK7mZvDxyiSMDalS6ombzXoidlwGmvsagaF520jTxZxxLd1qsX4H-8iSs2qsgqY-rpdLvpTJFOYq0EGj7Mssjno0A7Xwd7nV8pt29HmewypxfaQ65
fcm.title=New Order
fcm.body=You have new order at $%storeName$%


##################################################
# order-service-3.0.35 | 3-September-2021
##################################################
### Code Changes:
* Handled status by adding order_completion_status_config.

### Database Changes
* Following table was added.

``CREATE TABLE `order_completion_status_config` (
`id` int(11) NOT NULL,
`verticalId` varchar(50) NOT NULL,
`status` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
`storePickup` tinyint(1) NOT NULL,
`storeDeliveryType` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
`emailToCustomer` tinyint(1) DEFAULT NULL COMMENT 'Whether to send email to customer or not (no=0)',
`emailToStore` tinyint(1) DEFAULT NULL COMMENT 'Whether to send email to store or not (no=0)',
`requestDelivery` tinyint(1) DEFAULT '0' COMMENT 'Whether to reqeust delivery or not (no=0)',
`rcMessage` tinyint(1) DEFAULT NULL COMMENT 'Whether to send rcMessage to store or not (no=0)',
`customerEmailContent` text,
`storeEmailContent` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
`rcMessageContent` text CHARACTER SET latin1 COLLATE latin1_swedish_ci,
`comments` varchar(300) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
`created` timestamp NULL DEFAULT NULL,
`updated` timestamp NULL DEFAULT NULL,
PRIMARY KEY (`id`,`verticalId`,`status`,`storePickup`,`storeDeliveryType`),
KEY `status` (`status`),
KEY `verticalId` (`verticalId`),
CONSTRAINT `order_completion_status_config_ibfk_1` FOREIGN KEY (`verticalId`) REFERENCES `region_vertical` (`code`) ON UPDATE CASCADE,
CONSTRAINT `order_completion_status_config_ibfk_2` FOREIGN KEY (`status`) REFERENCES `order_completion_status` (`status`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;``




##################################################
# order-service-3.0.1-SNAPSHOT(master)
##################################################
+ Changes:

1. Bug fixes 
=======
    1. Fixed issue where two messages being sent to rocket chat after order creation
    2. Fixed issue in order search, data type for klCommission was double need it to Double.




##################################################
# order-service-2.1.0-SNAPSHOT(master)
##################################################
+ Changes:

1. Modified 
=======
1. Updated Order model in post request
2. Added Enums OrderStatus for Order completion status and PaymentStatus for payment status.
3. Added new field 'specialInstruction' to cartItem and orderItem table and model and send to rocket chat as well
4. Added new model for email
5. Added generic endpoint for update completion status
6. Added search functionality to for orders
7. Updated text that's been sent to rocket chat SKU: value, QTY: value
8. Updated flow to send email to customer only in PAYMENT_CONFIRMED, DELIVER_TO_CUSTOMER, and REJECT_BY_STORE
9. Added specialInstruction in email if any
10. Modified order flow when user paid their payment there should be no call to delivery service. Merchant will call manually after order being ready for delivery
11. Added new endpoint that's been hitted by Merchant when order get ready for delivery
12. Restricted multiple callback issues for the above endpoint to just change the completion status if the previous state is PAYMENT_CONFIRMED only


##################################################
# order-service-1.3-SNAPSHOT
##################################################
+ Changes:
1. Modified the coloumn to be used to get live chat groupName, now getting from liveChatOrdersGroupName
2. Posted order to rocket chat in OrderController update endpoint






##################################################
# order-service-1.2.2-SNAPSHOT
##################################################
+ Changes:
1. Changed version from 1.2.1-SNAPSHOT to 1.2.2-SNAPSHOT
2. Changed OrderPostService.java, added: groupName += groupName.toLowerCase();
3. 
4. 






##################################################
# order-service-1.2-SNAPSHOT
##################################################
+ Changes:
1. Add new API to clear cart item
2. Add new API to increase /decrease quantity in cart item
3. bug fix for add new cart item
4. New API order-update:
	-receive order-update from payment-service after payment completed
	-trigger delivery-service for adhoc product
	-send email after receive response from delivery-service with tracking url


##################################################
# order-service-1.2-SNAPSHOT
##################################################
+ Changes:
1. Added order link posting to live chat service
2. Added StoreNameService to get the store name from product-service
3. Added properties for product-service in application-prod.properties file
        product-service.URL=https://api.symplified.biz/v1/product-service
        product-service.token=Bearer accessToken
4. Added properties for liveChat properties in application-prod.properties file
        liveChat.sendMessage.URL=http://209.58.160.20:3000/api/v1/chat.postMessage
        liveChat.token=kkJ4G-gEqu5nL-VY9YWBo25otESh_zlQu8ckpic49ne
        livechat.userid=JEdxZxgW4R5Z53xq2

##################################################
# order-service-1.1-SNAPSHOT
##################################################



##################################################
# order-service-1.0-FINAL
##################################################
Implemented the following end points:

Orders controller:
GET /orders
GET /orders/{id}
POST /orders
DELETE /orders/{id}
PUT /orders/{id}

OrderItems:
GET /orders/{orderId}/items
GET /orders/{orderId}/items/{orderItemId}
POST /orders/items
DELETE /orders/items
PUT /orders/{orderId}/items

OrderShipment:
GET /orders/{orderId}/shipment-details
POST /orders/{orderId}/shipment-details
DELETE /orders/{orderId}/shipment-details
PUT /orders/{orderId}/shipment-details

Carts controller:
GET /carts
GET /carts/{id}
POST /carts
DELETE /carts/{id}
PUT /carts/{id}

CartItems:
GET /{cartId}/items
GET /{cartId}/items/{cartItemId}
POST /items
DELETE /items
PUT /{cartId}/items

##################################################
# order-service-1.0-SNAPSHOT
##################################################
Not implementing the endpoint which converts the cart to order, since the price to be get from the backend could be different from what is to be shown at the front-end. So it is better to get it from the front-end, since front-end is already getting the latest prices from the product endpoint.
TODO:

delete cart, delete cart-items
delete order, delete order-items

Orders controller:
GET /orders
GET /orders/{id}
POST /orders
DELETE /orders/{id}
PUT /orders/{id}

OrderItems:
GET /orders/{orderId}/items
GET /orders/{orderId}/items/{orderItemId}
POST /orders/items
DELETE /orders/items
PUT /orders/{orderId}/items

OrderShipment:
GET /orders/{orderId}/shipment-details
POST /orders/{orderId}/shipment-details
DELETE /orders/{orderId}/shipment-details
PUT /orders/{orderId}/shipment-details


Carts controller:
GET /carts
GET /carts/{id}
POST /carts
DELETE /carts/{id}
PUT /carts/{id}

CartItems:
GET /{cartId}/items
GET /{cartId}/items/{cartItemId}
POST /items
DELETE /items
PUT /{cartId}/items

