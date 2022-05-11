##################################################
# order-service-3.7.20-SNAPSHOT |11-May-2022
##################################################
1. New API : postClaimNewUserVoucher()
2. check voucher allowDoubleDiscount during cartDiscount & placeOrder()
3. check voucher minimumSpend during cartDiscount & placeOrder()
4. Add voucher discount in email to customer
5. Multiple verticalCode for voucher

##DB changes:
ALTER TABLE voucher ADD isNewUserVoucher TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE voucher ADD checkTotalRedeem TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE voucher ADD minimumSpend DECIMAL(10,2);
ALTER TABLE voucher ADD allowDoubleDiscount TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `order` ADD voucherDiscount DECIMAL(10,2);


CREATE TABLE voucher_vertical (
id VARCHAR(50),
voucherId VARCHAR(50),
verticalCode VARCHAR(50)
);

Update email template in order_completion_config table : add voucher discount


##################################################
# order-service-3.7.19-SNAPSHOT |28-Apr-2022
##################################################
New API : mergeCart()


##################################################
# order-service-3.7.18-SNAPSHOT |26-Apr-2022
##################################################
Add store details in getCart() api


##################################################
# order-service-3.7.17-SNAPSHOT |22-Apr-2022
##################################################
Bug fix for voucher claim


##################################################
# order-service-3.7.16-SNAPSHOT |20-Apr-2022
##################################################
Bug fix for email to customer during receive callback from delivery-service for ASSIGNING_DRIVER
Bug fix for getOrdersWithDetails()
Bug for voucher feature in placeOrder
Bug fix for get claimed voucher

##################################################
# order-service-3.7.15-SNAPSHOT |14-Apr-2022
##################################################
1. Bug fix for send tracking url in email to customer
2. Bug fix for delivery-service response : ignore missing & unmatch field
2. Send activation link when customer not activate yet. Status whether customer activated or not saved in customer table. 
   Activate notice content with activation link saved in region_vertical, this will append to email content from order_completion_status_config 

ALTER TABLE customer ADD isActivated TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE region_vertical ADD customerActivationNotice varchar(255);


##################################################
# order-service-3.7.14-SNAPSHOT |13-Apr-2022
##################################################
1. Bug fix for get vehicle type in getWeight, check store max item for bike
2. Bug fix for COD store during placeOrder
3. Bug fix for email during edit order. fix sender address & name to read from region_vertical
4. Send Whatsapp Alert for order with paymentType=COD
5. New API for voucher
6. New parameter in getDiscountOfCart() : customerId & voucherCode`
7. New parameter in placeOrder() : voucherCode

##DB Changes:
CREATE TABLE `voucher` (
  `id` varchar(50) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','DELETED','EXPIRED') DEFAULT NULL,
  `startDate` date DEFAULT NULL,
  `endDate` date DEFAULT NULL,
  `voucherType` enum('PLATFORM','STORE') DEFAULT NULL,
  `verticalCode` varchra(50),
  `storeId` varchar(50) DEFAULT NULL,
  `discountType` enum('TOTALSALES','SHIPPING') DEFAULT NULL,
  `calculationType` enum('PERCENT','FIX','SHIPAMT') DEFAULT NULL,
  `discountValue` decimal(10,2) DEFAULT NULL,
  `maxDiscountAmount` decimal(10,2) DEFAULT NULL,
  `voucherCode` varchar(50) DEFAULT NULL,
  `totalQuantity` int DEFAULT NULL,
  `totalRedeem` int DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `deleteReason` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (verticalCode) REFERENCES region_vertical(code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE voucher_terms (
`id` VARCHAR(50) NOT NULL,
voucherId VARCHAR(50),
terms VARCHAR(255),
FOREIGN KEY (voucherId) REFERENCES voucher(id)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb3;


CREATE TABLE customer_voucher (
id VARCHAR(50) PRIMARY KEY,
customerId VARCHAR(50) CHARACTER SET latin1,
voucherId VARCHAR(50),
created DATETIME,
isUsed TINYINT(1) NOT NULL DEFAULT 0,
FOREIGN KEY (voucherId) REFERENCES voucher(id),
FOREIGN KEY (customerId) REFERENCES customer(id)
)  ENGINE=INNODB DEFAULT CHARSET=utf8mb3;


Insert autority for new api. Execute after order-service patched:

INSERT INTO role_authority VALUES ('STORE_OWNER','voucher-post','order-service');


##################################################
# order-service-3.7.13-SNAPSHOT |08-Apr-2022
##################################################
Add store asset in orderWithDetails


##################################################
# order-service-3.7.12-SNAPSHOT |05-Apr-2022
##################################################
Put finance's email sender name in config 
finance.email.sender.name=Deliver In Orders


##################################################
# order-service-3.7.11-SNAPSHOT |04-Apr-2022
##################################################
Bug fix for discount calculation : deduct discounted item if discount is only for normal item price
Differentiate FCM token for deliverin & easydukan

##Config Changes:
fcm.token.deliverin=
fcm.token.easydukan=


##################################################
# order-service-3.7.10-SNAPSHOT |01-Apr-2022
##################################################
Add sender email address, name, defaultLogo inside regionVertical table.

##DB Changes:
ALTER TABLE region_vertical ADD senderEmailAdress VARCHAR(255);
ALTER TABLE region_vertical ADD senderEmailName VARCHAR(255);

UPDATE region_vertical SET senderEmailAdress='no-reply@easydukan.co' WHERE domain LIKE '%easydukan%';
UPDATE region_vertical SET senderEmailName='Easy Dukan' WHERE domain LIKE '%easydukan%';
UPDATE region_vertical SET defaultLogoUrl='https://symplified.biz/store-assets/easydukan-logo-small.png' WHERE domain LIKE '%easydukan%';

UPDATE region_vertical SET senderEmailAdress='orders@deliverin.my' WHERE domain LIKE '%symplified%';
UPDATE region_vertical SET senderEmailName='Deliver In Orders' WHERE domain LIKE '%symplified%';
UPDATE region_vertical SET defaultLogoUrl='https://symplified.biz/store-assets/deliverin-logo-small.png' WHERE domain LIKE '%symplified%';

UPDATE region_vertical SET senderEmailAdress='orders@deliverin.my' WHERE domain LIKE '%deliverin%';
UPDATE region_vertical SET senderEmailName='Deliver In Orders' WHERE domain LIKE '%deliverin%';
UPDATE region_vertical SET defaultLogoUrl='https://symplified.biz/store-assets/deliverin-logo-small.png' WHERE domain LIKE '%deliverin%';


##################################################
# order-service-3.7.9-SNAPSHOT |31-Mar-2022
##################################################
New end point for get cart by customerId : getCartsByCustomerId

##################################################
# order-service-3.7.8-SNAPSHOT | 29-Mar-2022
##################################################
Change sender address based on regionCountry
MYS -> orders@deliverin.my
PAK -> no-reply@easydukan.co


##################################################
# order-service-3.7.7-SNAPSHOT | 28-Mar-2022
##################################################
Bug fix for cartItem that OUTOFSTOCK


##################################################
# order-service-3.7.6-SNAPSHOT | 25-Mar-2022
##################################################
New api order with item details : getOrdersWithDetails


##################################################
# order-service-3.7.5-SNAPSHOT | 15-Mar-2022
##################################################
Bug fix for WA alert for new order notification


##################################################
# order-service-3.7.4-SNAPSHOT | 10-Mar-2022
##################################################
Bug fix for revise quantity for COD


##################################################
# order-service-3.7.3-SNAPSHOT | 7-Mar-2022
##################################################
Change in kalsym commission : exclude delivery charges from calculation
example : 15% X (subtotal - subtotal_discount + service_charge)
Send order reminder only 1 time

##DB Changes
ALTER TABLE `order` ADD totalReminderSent TINYINT(1) DEFAULT 0;


##################################################
# order-service-3.7.2-SNAPSHOT | 3-Mar-2022
##################################################
Bug fix for PUT cartItem for discounted item


##################################################
# order-service-3.7.1-SNAPSHOT | 3-Mar-2022
##################################################
Bug fix for confirmOrderDelivery to delivery-service
Add delivery fulfilmentType in order_shipment_detail

##DB Changes
ALTER TABLE order_shipment_detail ADD fulfilmentType VARCHAR(20);


##################################################
# order-service-3.7.0-SNAPSHOT | 1-Mar-2022
##################################################
1. Handle calback in delivery-service if delivery-service return PENDING during requestDelivery
2. Set connect timeout & wait timeout for connection to delivery-service
3. bug fix for PUT cartItem that cause price become zero

##Compatibility
Only compatible with delivery-service 2.4.9 onward. Change in delivery-service response format for confirmOrderDelivery()


##################################################
# order-service-3.6.6-SNAPSHOT | 25-Feb-2022
##################################################
Bug fix for vehicleType in product


##################################################
# order-service-3.6.5-SNAPSHOT | 23-Feb-2022
##################################################
Add totalPcs in getWeightOfCart


##################################################
# order-service-3.6.4-SNAPSHOT | 21-Feb-2022
##################################################
Bug fix for placeOrder : insert 0.00 if delivery charges is null
Bug fix for reviseitem : check if delivery charges is null
Bug fix for process bulk order : handle if delivery-service return error/cannot connect


##################################################
# order-service-3.6.3-SNAPSHOT | 18-Feb-2022
##################################################
Bug fix for process bulk order


##################################################
# order-service-3.6.2-SNAPSHOT | 15-Feb-2022
##################################################
Bug fix for placeOrder


##################################################
# order-service-3.6.1-SNAPSHOT | 14-Feb-2022
##################################################
New response parameter in getWeightOfCart : Weight->VehicleType
Link cartItem & cartSubItem to ProductAsset to show image of product variant
Add vehicleType in OrderShipmentDetail
Save vehicleType during placeOrder in OrderShipmentDetail

##DB Changes:
ALTER TABLE `order_shipment_detail` ADD vehicleType ENUM('MOTORCYCLE','CAR','VAN','PICKUP','LARGEVAN','SMALLLORRY','MEDIUMLORRY','LARGELORRY');


##################################################
# order-service-3.6.0-SNAPSHOT | 11-Feb-2022
##################################################
New function to Revise Order Quantity
PUT /orders/reviseitem/{orderId}

##DB Changes:
ALTER TABLE `order` ADD isRevised TINYINT(1);
INSERT INTO (status, description) VALUES ('ITEM_REVISED', 'Order item is revised by merchant');
INSERT INTO order_completion_status_config for status = 'ITEM_REVISED'. only put verticalId & status, other field put default value


##################################################
# order-service-3.5.1-SNAPSHOT | 10-Feb-2022
##################################################
Bug fix for putOrderCompletionStatusUpdatesBulk 


##################################################
# order-service-3.5.0-SNAPSHOT | 28-Jan-2022
##################################################
Move logic of putOrderCompletionStatusUpdatesConfirm to separate class
New function to process order by bulk : 
	putOrderCompletionStatusUpdatesBulk()
	PUT /orders/completion-statuses/bulk
New function to revise order quantity :
	reviseOrderItems()
	PUT /orders/reviseitem/{orderId}
	
##Config change 
Add new config:
deliveryService.bulk.confirm.URL=https://api.symplified.it/delivery-service/v1/orders/bulkConfirm

##New role authority for STORE_OWNER
order-completion-status-updates-put-by-bulk


##################################################
# order-service-3.4.5-SNAPSHOT | 27-Jan-2022
##################################################
Bug fix for template for whatsapp-service


##################################################
# order-service-3.4.4-SNAPSHOT | 26-Jan-2022
##################################################
Change template for whatsapp-service
Bug fix for placeOrder : when error on discount/item return HTTP CONFLICT


##################################################
# order-service-3.4.3-SNAPSHOT | 24-Jan-2022
##################################################
Add field in response of getSubTotalDiscountOfCart() :
-storeServiceCharge
-storeServiceChargePercentage


##################################################
# order-service-3.4.2-SNAPSHOT | 20-Jan-2022
##################################################
Bug fix for placeOrder for store pickup with onlinepayment 


##################################################
# order-service-3.4.1-SNAPSHOT | 19-Jan-2022
##################################################
Add new field in DeliveryOrder :
-totalRequest
-deliveryQuotationId


##################################################
# order-service-3.4.0-SNAPSHOT | 17-Jan-2022
##################################################
Handle CANCELED_BY_MERCHANT by delivery-service in putOrderCompletionStatusUpdatesConfirm():
-revert back to previous status
-send alert to symplified support

##Database changes:
ALTER TABLE `order_completion_status_config` ADD pushWAToAdmin TINYINT(1);
UPDATE `order_completion_status_config` SET pushWAToAdmin=0 WHERE pushWAToAdmin IS NULL;

New order status : FAILED_FIND_DRIVER
INSERT INTO order_completion_status VALUES ('FAILED_FIND_DRIVER','Fail to find driver. Need to arrange manually');


New config in order_completion_status_config for FAILED_FIND_DRIVER
Need to insert into order_completion_status_config with values :
1. verticalId=store vertical, 
2. status=FAILED_FIND_DRIVER
3. pushWAToAdmin=1;
others field no need to actual value, just put default value

##Configuration changes:
whatsapp.service.admin.alert.templatename=welcome_to_symplified_7
whatsapp.service.admin.alert.refid=60133429331
whatsapp.service.admin.msisdn=60133429331


##################################################
# order-service-3.3.11-SNAPSHOT | 14-Jan-2022
##################################################
Format 2 decimal point for getDiscountOfCart & getSubTotalDiscountOfCart
New function getSubTotalDiscountOfCart


##################################################
# order-service-3.3.10-SNAPSHOT | 13-Jan-2022
##################################################
Add CustomRepository class to access entity manager refresh function to allow non-cache query
Buf fix for order payment details model


##################################################
# order-service-3.3.9-SNAPSHOT | 13-Jan-2022
##################################################
Add CustomRepository class to access entity manager refresh function to allow non-cache query


##################################################
# order-service-3.3.8-SNAPSHOT | 12-Jan-2022
##################################################
Get delivery charges from delivery-service (getQuotation) during calculate cart discount (getDiscountOfCart)

Get delivery charges from delivery-service (getQuotation) during place order

Add new request param in getDiscountOfCart() :
	deliveryType
Add new response in getDiscountOfCart:
	store service charges
	store service charge percentage
	delivery charges
	grand total
	
	
###New config:
deliveryService.get.quotation.URL=https://api.symplified.it/delivery-service/v1/orders/getQuotation

###Depedencies:
delivery-service-2.3.4


##################################################
# order-service-3.3.7-SNAPSHOT | 07-Jan-2022
##################################################
### Code Changes:
Add new request parameter in placeOrder() : orderShipmentDetails -> deliveryType
Add delivery charge into storeShare if deliveryType=SELF

###DB Changes
ALTER TABLE order_shipment_detail ADD deliveryType ENUM('SCHEDULED','ADHOC','SELF','PICKUP');


##################################################
# order-service-3.3.6-SNAPSHOT | 05-Jan-2022
##################################################
### Code Changes:
1) In order details:
Add new field deliveryDiscountMaxAmount

###DB Changes
ALTER TABLE `order` ADD deliveryDiscountMaxAmount decimal(10,2);


##################################################
# order-service-3.3.5-SNAPSHOT | 04-Jan-2022
##################################################
### Code Changes:
Bug fix for discount description in getOrderDetails
Set email from finance@symplified.biz for email to customer for order cancelled

### Dependecies:
email-3.0.7-SNAPSHOT and above

##################################################
# order-service-3.3.4-SNAPSHOT | 03-Jan-2022
##################################################
### Code Changes:
Bug fix order addToCart & placeOrder : check if merchant set outOfStockPurchases=false, then block add to cart & place order


##################################################
# order-service-3.3.3-SNAPSHOT | 31-December-2021
##################################################
### Code Changes:
Bug fix for count summmary & search order fitler by status


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

