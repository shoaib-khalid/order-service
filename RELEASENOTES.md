##################################################
# order-service-3.3.2-SNAPSHOT | 28-December-2021
##################################################
### Code Changes:
Add sortByCol & sortingOrder in search order


##################################################
# order-service-3.3.1-SNAPSHOT | 27-December-2021
##################################################
### Code Changes:
New field for discount capped amount in order details : discountMaxAmount
Block placeOrder if cart is empty

###DB Changes
ALTER TABLE `order` ADD discountMaxAmount decimal(10,2);


##################################################
# order-service-3.3.0-SNAPSHOT | 22-December-2021
##################################################
### Code Changes:
New function cancelOrder()


###DB Changes
INSERT INTO order_completion_status VALUES ('CANCELED_BY_MERCHANT','Merchant cancel the order');

ALTER TABLE order_refund
 ADD refundType ENUM('ORDER_CANCELLED','ITEM_CANCELLED','ITEM_REVISED'),
 ADD paymentChannel VARCHAR(20),
 ADD refundAmount DECIMAL(10,2),
 ADD remarks VARCHAR(255),
 ADD created TIMESTAMP,
 ADD updated TIMESTAMP
;

ALTER TABLE cart_item ADD discountCalculationType VARCHAR(20) ;
ALTER TABLE cart_item ADD discountCalculationValue DECIMAL(10,2);

ALTER TABLE order_item ADD discountCalculationType VARCHAR(20) ;
ALTER TABLE order_item ADD discountCalculationValue DECIMAL(10,2);
ALTER TABLE order_item ADD originalQuantity INT;
ALTER TABLE order_item ADD status VARCHAR(20);

ALTER TABLE `order` ADD discountId VARCHAR(50) ;
ALTER TABLE `order` ADD discountCalculationType VARCHAR(20) ;
ALTER TABLE `order` ADD discountCalculationValue DECIMAL(10,2);

ALTER TABLE `order_completion_status_config` ADD emailToFinance TINYINT(1) DEFAULT 0;
ALTER TABLE `order_completion_status_config` ADD financeEmailContent TEXT;

Need to insert into order_completion_status_config with values :
1. verticalId=store vertical, 
2. status=CANCELED_BY_MERCHANT
3. paymentType=ONLINEPAYMENT or COD, 
4. emailToFinance=1 for ONLINEPAYMENT, 0 for COD
5. financeEmailContent=email content sent to finance for refund process
others field no need to actual value, just put default value

##New config:
finance.email.address=taufik@kalsym.com
//email to sent for refund notification


##################################################
# order-service-3.2.29-SNAPSHOT | 21-December-2021
##################################################
### Code Changes:
Allow search by multiple completion_status in searchOrderDetails (GET /orders/search)


##################################################
# order-service-3.2.28-SNAPSHOT | 15-December-2021
##################################################
### Code Changes:
Bug fix for order count summary


##################################################
# order-service-3.2.27-SNAPSHOT | 15-December-2021
##################################################
### Code Changes:
Bug fix for completion-status-updates for failed request for delivery
Add discount details in order item details

###DB Changes:
1. ALTER TABLE `order_item` ADD discountId varchar(50);
2. ALTER TABLE `order_item` ADD normalPrice decimal(10,2);
3. ALTER TABLE `order_item` ADD discountLabel varchar(100);


##################################################
# order-service-3.2.26-SNAPSHOT | 14-December-2021
##################################################
### Code Changes:
Bug fix for placeOrder with payment type COD : clear item cart
Add new delivery type in order table : PICKUP

###DB Changes
ALTER TABLE
    `order`
MODIFY COLUMN
    `deliveryType` enum(
        'ADHOC',
        'SCHEDULED',
        'SELF',
		'PICKUP'
    );

##################################################
# order-service-3.2.25-SNAPSHOT | 13-December-2021
##################################################
### Code Changes:
1. Generate temp token and send to whatsapp service for Order Reminder
2. Clear cart item only for COD in placeOrder, removed for ONLINEPAYMENT
3. Query product-service during addToCart to get discount details
	
###Config Changes:
Add new config to ask user-service generate temp token: 
user.service.temp.token.URL=https://api.symplified.it/user-service/v1/clients/generateTempToken

###DB Changes:
1. ALTER TABLE `cart_item` ADD discountId varchar(50);
2. ALTER TABLE `cart_item` ADD normalPrice decimal(10,2);
3. ALTER TABLE `cart_item` ADD discountLabel varchar(100);

###Depedencies:
product-service-3.3.2 
user-service-3.1.1-SNAPSHOT


##################################################
# order-service-3.2.24-SNAPSHOT | 9-December-2021
##################################################
### Code Changes:
Bug fix for max discount amount


##################################################
# order-service-3.2.23-SNAPSHOT | 6-December-2021
##################################################
### Code Changes:
Handle order status update for PAYMENT_FAILED


##################################################
# order-service-3.2.22-SNAPSHOT | 1-December-2021
##################################################
### Code Changes:
Set max discount amount for store discount calculation (retrieve from store_discount table) 
Use product inventory price when add item to cart


##################################################
# order-service-3.2.21-SNAPSHOT | 30-November-2021
##################################################
### Code Changes:
Block request if order being process for putOrderCompletionStatusUpdatesConfirm()

### DB Changes:
ALTER TABLE `order` ADD beingProcess TINYINT(1) DEFAULT 0;


##################################################
# order-service-3.2.20-SNAPSHOT | 26-November-2021
##################################################
### Code Changes:
Add new field in order table : deliveryType
new function searchOrderDetails() -> return order list, current status and next status

### DB Changes:
ALTER TABLE `order` ADD `deliveryType` enum('ADHOC','SCHEDULED','SELF') DEFAULT NULL;


##################################################
# order-service-3.2.19-SNAPSHOT | 25-November-2021
##################################################
### Code Changes:
Bug fix for order history


##################################################
# order-service-3.2.18-SNAPSHOT | 22-November-2021
##################################################
### Code Changes:
Add whatsapp alert referenceId & templateName in config

### New Config:
whatsapp.service.push.url=https://waw.symplified.it/360dialog/callback/templatemessage/push
whatsapp.service.order.reminder.templatename=welcome_to_symplified_7
whatsapp.service.order.reminder.refid=60133429331


##################################################
# order-service-3.2.17-SNAPSHOT | 19-November-2021
##################################################
### Code Changes:
1. New feature for add item to cart :
	-allow to add subitem (for combo product)
2. New response parameter in get order item	:
	-orderSubItem[] (for combo product, it have subitem list)
3. Remove checking for paymentType=COD for function placeOrder

### DB Changes:	

CREATE TABLE `cart_subitem` (
  `id` varchar(50) NOT NULL,
  `quantity` int DEFAULT NULL COMMENT 'Once the cart is order the quantity is subtracted from option.',
  `cartItemId` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `productId` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'A cart can have same product listed multiple times with different options. ',
  `itemCode` varchar(50) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `productPrice` decimal(10,2) DEFAULT NULL,
  `weight` decimal(6,2) DEFAULT NULL,
  `SKU` varchar(500) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `productName` varchar(500) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `specialInstruction` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `productId` (`productId`,`itemCode`),
  KEY `itemCode` (`itemCode`),
  KEY `cartId` (`cartItemId`),
  CONSTRAINT `cart_subitem_ibfk_1` FOREIGN KEY (`cartItemId`) REFERENCES `cart_item` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `order_subitem` (
  `id` varchar(50) NOT NULL,
  `orderItemId` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `productId` varchar(50) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL COMMENT '	Price of ordered item in the cart',
  `productPrice` decimal(10,2) DEFAULT NULL COMMENT 'Basic product price without options markups, wholesale discounts etc.',
  `weight` decimal(6,2) DEFAULT NULL,
  `SKU` varchar(500) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `itemCode` varchar(50) DEFAULT NULL,
  `productName` varchar(500) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  `specialInstruction` varchar(1000) DEFAULT NULL,
  `productVariant` varchar(500) CHARACTER SET latin1 COLLATE latin1_swedish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `productId` (`productId`),
  KEY `orderId` (`orderItemId`),
  CONSTRAINT `order_subitem_ibfk_1` FOREIGN KEY (`orderItemId`) REFERENCES `order_item` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



##################################################
# order-service-3.2.16-SNAPSHOT | 12-November-2021
##################################################
### Code Changes:
Bug fix for confirm order with delivery-service, new request format :
{
    "startPickScheduleDate": "2021-11-11",
    "endPickScheduleDate": "2021-11-11",
    "startPickScheduleTime": "10:15:00",
    "endPickScheduleTime": "10:20:00"
}

##################################################
# order-service-3.2.15-SNAPSHOT | 11-November-2021
##################################################
### Code Changes:
New parameter for putOrderCompletionStatusUpdatesConfirm :
-pickupDate
-pickupTime

### Database Changes:
ALTER TABLE order_completion_status_update ADD pickupDate VARCHAR(20);
ALTER TABLE order_completion_status_update ADD pickupTime VARCHAR(20);


##################################################
# order-service-3.2.14-SNAPSHOT | 1-November-2021
##################################################
### Code Changes:
Bug fix for getOrders
Order Reminder scheduler to send alert through WA if order not process more than 5 minutes. Only for FnB

### New Config:
order.reminder.enabled=false


##################################################
# order-service-3.2.13-SNAPSHOT | 27-October-2021
##################################################
### Code Changes:
Bug fix for placeOrder


##################################################
# order-service-3.2.12-SNAPSHOT | 27-October-2021
##################################################
### Code Changes:
To cater COD processing, when front-end query order with completionStatus=PAYMENT_CONFIRMED, order-service will query from db based on criteria:
1. payment=ONLINE_PAYMENT and completionStatus=PAYMENT_CONFIRMED
2. payment=COD and completionStatus=ORDER_RECEIVED (because COD only receive payment after item delivered) 


##################################################
# order-service-3.2.7-SNAPSHOT | 26-October-2021
##################################################
### Code Changes:
Add new criteria for order completion status config : payment type

### DB Changes:

ALTER table order_completion_status_config ADD `paymentType` enum('ONLINEPAYMENT','COD') NOT NULL after storeDeliveryType;
ALTER table order_completion_status_config ADD `nextActionText` varchar(100) DEFAULT NULL COMMENT 'text to show in button for ext action';

##################################################
# order-service-3.2.6-SNAPSHOT | 25-October-2021
##################################################
### Code Changes:
Bug fix for pushCODOrder()


##################################################
# order-service-3.2.5-SNAPSHOT | 18-October-2021
##################################################
### Code Changes:
Bug fix


##################################################
# order-service-3.1.8-SNAPSHOT | 15-October-2021
##################################################
### Code Changes:
Add new request parameter 'clientId' in getOrder to retrieve all order by clientId


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

