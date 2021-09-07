/*
SQLyog Community v13.1.6 (64 bit)
MySQL - 8.0.13 : Database - symplified
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`symplified` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */;

USE `symplified`;

/*Table structure for table `order_completion_status_config` */

DROP TABLE IF EXISTS `order_completion_status_config`;

CREATE TABLE `order_completion_status_config` (
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
