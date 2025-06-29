/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.8.2-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: shopinv
-- ------------------------------------------------------
-- Server version	11.8.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `archived_order_items`
--

DROP TABLE IF EXISTS `archived_order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `archived_order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `product_ref` varchar(255) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `is_custom` bit(1) DEFAULT NULL,
  `personalized_message` varchar(255) DEFAULT NULL,
  `custom_size` varchar(255) DEFAULT NULL,
  `tarpaulin_finish` varchar(255) DEFAULT NULL,
  `tarpaulin_thickness` varchar(255) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1ea244u4rx3d27nmsjlw2bvvt` (`order_id`),
  CONSTRAINT `FK1ea244u4rx3d27nmsjlw2bvvt` FOREIGN KEY (`order_id`) REFERENCES `archived_orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archived_order_items`
--

LOCK TABLES `archived_order_items` WRITE;
/*!40000 ALTER TABLE `archived_order_items` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `archived_order_items` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `archived_orders`
--

DROP TABLE IF EXISTS `archived_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `archived_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `tracking_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `order_date` datetime(6) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `shipping_address` varchar(255) DEFAULT NULL,
  `shipping_option` varchar(255) DEFAULT NULL,
  `original_order_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archived_orders`
--

LOCK TABLES `archived_orders` WRITE;
/*!40000 ALTER TABLE `archived_orders` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `archived_orders` VALUES
(57,86,'A2EB124A-6D2','TO_SHIP','2025-06-22 16:22:09.000000','creditcard',25038.64,'San Pedro','overnight',86),
(58,87,'479921E6-6B0','COMPLETED','2025-06-23 02:24:02.000000','creditcard',486.00,'San Pedro','overnight',87),
(59,88,'C530E54D-8F3','COMPLETED','2025-06-23 02:24:28.000000','banktransfer',467.00,'San Pedro','express',88),
(60,89,'FADD44BE-D31','CANCELLED','2025-06-23 02:25:02.000000','banktransfer',411.00,'San Pedro','express',89),
(61,92,'8B0F29B6-B2F','PENDING','2025-06-23 07:48:38.000000','paypal',1163.88,'San Pedro','standard',92),
(65,93,'E631B606-79E','PENDING','2025-06-23 08:34:32.000000','banktransfer',598.00,'San Pedro','overnight',93);
/*!40000 ALTER TABLE `archived_orders` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `archived_payments`
--

DROP TABLE IF EXISTS `archived_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `archived_payments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `amount` decimal(38,2) NOT NULL,
  `quantity` int(11) DEFAULT NULL,
  `days_left` int(11) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `is_custom` bit(1) DEFAULT NULL,
  `custom_product_ref` varchar(255) DEFAULT NULL,
  `payment_method_details` text DEFAULT NULL,
  `shipping_option` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKt8bweou8eaw7gty9a26bgi9qu` (`user_id`),
  KEY `FKnd43e624grchennx5s7i5muxl` (`order_id`),
  CONSTRAINT `FKnd43e624grchennx5s7i5muxl` FOREIGN KEY (`order_id`) REFERENCES `archived_orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archived_payments`
--

LOCK TABLES `archived_payments` WRITE;
/*!40000 ALTER TABLE `archived_payments` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `archived_payments` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `archived_products`
--

DROP TABLE IF EXISTS `archived_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `archived_products` (
  `id` bigint(20) NOT NULL,
  `archived_at` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `created_at` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `image_loc` varchar(255) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `stock` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archived_products`
--

LOCK TABLES `archived_products` WRITE;
/*!40000 ALTER TABLE `archived_products` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `archived_products` VALUES
(19,'2025-06-24T12:49:56.307694986','Other','2025-06-09T02:06:56.037669600','tralalelo tralala','/uploads/0ad10595-4d98-4204-b243-e48a4855fdc5_images (4).jpg','Tralalelo Tralala',350,498),
(21,'2025-06-24T12:49:53.332969128','fatass teto','2025-06-09T16:11:42.606054300','HER ass is not hiding!','/uploads/06ce3e12-a1d6-4b43-878c-68e72d928eb0_fatass-teto-vs-fatass-miku-who-would-win-v0-o7zqmbj87bge1.webp','teto fatass',85,95),
(36,'2025-06-24T12:49:42.545522716','Other','2025-06-19T21:02:12.931155212','jdent pascual','/uploads/66b757a1-ed62-4359-b0b1-34ea77adf4f8_Screenshot_20250619_210141.png','Jdent Pascual',1,1),
(38,'2025-06-24T12:49:38.954797752','Other','2025-06-20T02:39:18.896934959','Huge.','/uploads/da77d90c-c04d-4ee9-b1a9-1aab2f0624f8_6EWDT4Ys_400x400.jpg','Trigger\'s Cake',999,499),
(42,'2025-06-23T18:32:39.937882727','Other','2025-06-22T14:23:37.925433459','Nothing changed','/uploads/a412da7b-6e2b-4c5d-a21e-d3da7990a92e_506862830_1682701822409732_3782583227379716267_n.jpg','Jdent Pascual2',22,2),
(43,'2025-06-23T18:32:43.148803237','Balloon','2025-06-22T14:24:12.100989740','hwrgwgeqetwqf','/uploads/e2120516-af66-44cc-b4b5-864131369bcc_506862830_1682701822409732_3782583227379716267_n.jpg','testafaf',22222,0),
(44,'2025-06-24T10:55:37.902275299','Other','2025-06-24T10:54:54.265262965','adasd21q34134','/uploads/342cd6f8-2f1b-4e4d-bf7e-e13068484ee1_images.jpeg','testafaf22',22,2222),
(45,'2025-06-24T10:55:33.057468024','Other','2025-06-24T10:55:27.596473999','asdasdasd','/uploads/9f86696c-758b-403a-b240-078c70cb10c8_511126332_685714117772921_4530927667720312098_n.jpg','asdasdas',222,22),
(48,'2025-06-24T12:49:35.480604330','Other','2025-06-24T11:20:38.659601287','haiyaaa ','/uploads/dbb84d83-85ce-41a1-a3bc-018a38e57837_Screenshot_20250613_055003.png','buling the phase 2',222,222),
(49,'2025-06-29T12:35:00.312586248','Other','2025-06-26T00:07:43.140982546','restocked!','/uploads/2114f0a2-7e5e-4f7e-8749-22ef212e988b_506862830_1682701822409732_3782583227379716267_n.jpg','Jdent Pascual - the reckoning',1,1),
(50,'2025-06-29T12:37:42.333789770','Other','2025-06-24T10:58:14.500356283','haiyaaa','/uploads/b4a78925-6fa2-4003-b657-515b656fcd3e_511126332_685714117772921_4530927667720312098_n.jpg','buling',222,1111);
/*!40000 ALTER TABLE `archived_products` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `archived_user`
--

DROP TABLE IF EXISTS `archived_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `archived_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `prof_img_loc` varchar(255) DEFAULT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `original_user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archived_user`
--

LOCK TABLES `archived_user` WRITE;
/*!40000 ALTER TABLE `archived_user` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `archived_user` VALUES
(27,'san pedro','2018-07-27','pauloneil3334@mail.com','MALE','','2025-06-02 09:45:24.000000','paulo','$2a$10$4o.u69fkeHcKB2.jlQDpYepFljzaAjuihZZ4kcHI5/LExCKIP/rde','09550367575','USER','usertest',26),
(28,'San Pedro','2008-09-24','junkemail3@gmail.com','FEMALE','/img/default-profile.png','2025-06-21 01:34:35.000000','Evelyn','$2a$10$iNXzGoIOk8x0Isk0yvWQl.PLdYDq1ff3PO7PFKRD8zqihmXxAQ/tu','123456','USER','eve',33),
(29,'GMA','2004-08-24','sevillapaulo364@gmail.com','MALE','','2025-06-01 03:11:33.000000','Sevilla','$2a$10$16kKLkyMb8CBM.4w.0wEE.cEgX1g6hL99.7FmEtPKOtG9bgbemuUW','09550367575','USER','Sevilla',23),
(30,'GMA','2008-02-02','pauloneil3334@gmail.com','MALE','','2025-06-02 08:52:25.000000','PauloUser','$2a$10$DtWXwtuwhVsyN3tIWE7z0.MGujW3238ri1SVATclxc8F8u5FbIYP2','09550367575','USER','Paulo2',25);
/*!40000 ALTER TABLE `archived_user` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_categories_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `categories` VALUES
(1,'Mask','a mask!'),
(3,'Other',NULL),
(4,'Eyeglasses',NULL),
(5,'Wig',NULL),
(6,'Confetti',NULL),
(7,'Garland',NULL),
(11,'Pinata',NULL),
(12,'Party Hats',NULL),
(13,'LED',NULL),
(15,'Balloon',NULL),
(16,'Banner',NULL),
(17,'Hanging',NULL),
(18,'Props',NULL),
(19,'String Lights',NULL),
(20,'Table Centerpiece',NULL);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `message` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `tracking_number` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb0yvoep4h4k92ipon31wmdf7e` (`user_id`),
  CONSTRAINT `FKb0yvoep4h4k92ipon31wmdf7e` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `notification` VALUES
(1,'2025-06-23 04:43:26.871809',0x01,'Your order #90 has been placed','PENDING','D7610CDB-208',22,NULL),
(2,'2025-06-23 04:44:13.704162',0x01,'Your order #75 is being prepared for shipping','TO_SHIP','56898E9A-0E0',22,NULL),
(3,'2025-06-23 04:52:18.176346',0x01,'Your order #73 has been delivered','COMPLETED','DC340F4E-612',22,NULL),
(4,'2025-06-23 05:30:40.740435',0x01,'Your order #75 has been shipped','TO_RECEIVE','56898E9A-0E0',22,75),
(5,'2025-06-23 05:34:47.449222',0x01,'Your order #91 has been placed','PENDING','923C0036-61C',22,91),
(9,'2025-06-24 05:48:56.656339',0x01,'Your order #96 has been placed','PENDING','505E76CB-433',22,96),
(10,'2025-06-24 05:52:34.465777',0x01,'Your order #97 has been placed','PENDING','BBFCBD78-7FB',22,97),
(11,'2025-06-24 06:00:42.306683',0x01,'Your order #98 has been placed','PENDING','0F3C30C2-733',22,98),
(12,'2025-06-24 13:00:02.461847',0x01,'Your order #73 has been shipped','TO_RECEIVE','DC340F4E-612',22,73),
(13,'2025-06-24 13:00:14.043019',0x01,'Your order #73 has been delivered','COMPLETED','DC340F4E-612',22,73),
(14,'2025-06-24 13:02:37.299027',0x00,'Your order #99 is being prepared for shipping','TO_SHIP','6A48BB49-6C0',60,99),
(15,'2025-06-24 13:04:16.804841',0x01,'Your order #96 has been shipped','TO_RECEIVE','505E76CB-433',22,96),
(16,'2025-06-25 23:32:18.395916',0x01,'Your order #100 has been placed','PENDING','B9617017-346',22,100),
(17,'2025-06-27 08:03:43.302311',0x01,'Your order #101 has been placed','PENDING','5D9E2F96-D44',22,101),
(18,'2025-06-27 08:04:27.383221',0x00,'Your order #102 has been placed','PENDING','2D5B4F8B-592',62,102),
(19,'2025-06-27 08:14:44.506089',0x01,'Your order #103 has been placed','PENDING','90AF6157-549',22,103),
(20,'2025-06-27 08:15:03.941687',0x01,'Your order #103 is being prepared for shipping','TO_SHIP','90AF6157-549',22,103),
(21,'2025-06-27 08:22:06.232675',0x01,'Your order #104 has been placed','PENDING','3F9477A9-CFE',22,104),
(22,'2025-06-27 10:52:48.607621',0x01,'Your order #105 has been placed','PENDING','DC695FDF-386',22,105),
(23,'2025-06-29 11:21:52.121897',0x01,'Your order #106 has been placed','PENDING','695D486F-A68',22,106),
(24,'2025-06-29 17:29:26.698818',0x00,'Your order #116 has been placed','PENDING','6294219E-6C5',22,116);
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `product_ref` varchar(255) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  `is_custom` tinyint(1) DEFAULT 0,
  `custom_size` varchar(255) DEFAULT NULL,
  `event_type` varchar(255) DEFAULT NULL,
  `personalized_message` varchar(255) DEFAULT NULL,
  `tarpaulin_finish` varchar(255) DEFAULT NULL,
  `tarpaulin_thickness` varchar(255) DEFAULT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`,`user_id`),
  KEY `fk_product_ref` (`product_ref`),
  KEY `fk_order_items_user` (`user_id`),
  CONSTRAINT `fk_order_items_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=239 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
set autocommit=0;
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `order_date` timestamp NULL DEFAULT current_timestamp(),
  `total_amount` decimal(38,2) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `payment_method` varchar(255) DEFAULT NULL,
  `shipping_address` varchar(255) DEFAULT NULL,
  `tracking_number` varchar(255) DEFAULT NULL,
  `shipping_option` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_id` (`user_id`),
  CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `orders_chk_1` CHECK (`status` in ('PENDING','TO_SHIP','TO_RECEIVE','COMPLETED','CANCELLED'))
) ENGINE=InnoDB AUTO_INCREMENT=117 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `orders` VALUES
(1,22,'2025-06-15 15:03:05',1097.80,'PENDING','paypal','San Pedro','C44043AA-F20','standard'),
(2,22,'2025-06-15 15:10:46',1239.80,'PENDING','paypal','San Pedro','4B457247-C02','express'),
(3,22,'2025-06-15 15:47:13',8766.20,'PENDING','paypal','San Pedro','005EEF46-065','express'),
(4,22,'2025-06-15 16:26:12',8766.20,'PENDING','paypal','San Pedro','122618F3-519','express'),
(5,22,'2025-06-15 16:50:20',10129.20,'PENDING','paypal','San Pedro','A4C4106F-ECA','overnight'),
(6,22,'2025-06-15 16:52:26',10024.20,'PENDING','paypal','San Pedro','985748AB-EAF','standard'),
(7,22,'2025-06-15 16:53:33',10054.20,'PENDING','paypal','San Pedro','94A585F0-E82','express'),
(9,22,'2025-06-15 17:42:20',1027.00,'PENDING','paypal','San Pedro','FFB3AAAB-42D','express'),
(13,22,'2025-06-18 05:51:36',542.00,'PENDING','paypal','San Pedro','02A7C57A-150','overnight'),
(14,22,'2025-06-18 07:25:07',542.00,'PENDING','paypal','San Pedro','0771CD78-7BB','overnight'),
(15,22,'2025-06-19 18:18:34',885.00,'PENDING','paypal','San Pedro','AC92FA58-7E5','standard'),
(16,22,'2025-06-19 18:35:25',885.00,'PENDING','paypal','San Pedro','65D12DD6-34A','standard'),
(72,22,'2025-06-22 00:41:06',75.00,'COMPLETED','paypal','San Pedro','2E4976CD-28B','express'),
(73,22,'2025-06-22 00:49:43',75.00,'COMPLETED','banktransfer','San Pedro','DC340F4E-612','express'),
(74,22,'2025-06-22 00:50:48',75.00,'COMPLETED','gcash','San Pedro','95FAA7EF-B2F','express'),
(75,22,'2025-06-22 00:52:51',75.00,'COMPLETED','gcash','San Pedro','56898E9A-0E0','express'),
(90,22,'2025-06-22 20:43:26',549.00,'PENDING','paypal','San Pedro','D7610CDB-208','standard'),
(91,22,'2025-06-22 21:34:47',493.00,'PENDING','paypal','San Pedro','923C0036-61C','standard'),
(96,22,'2025-06-23 21:48:56',411.00,'COMPLETED','paypal','San Pedro','505E76CB-433','express'),
(97,22,'2025-06-23 21:52:34',437.00,'PENDING','paypal','San Pedro','BBFCBD78-7FB','standard'),
(98,22,'2025-06-23 22:00:42',381.00,'PENDING','cod','San Pedro','0F3C30C2-733','standard'),
(99,60,'2025-06-23 11:24:06',605.00,'TO_SHIP','paypal','San Pedro','6A48BB49-6C0','standard'),
(100,22,'2025-06-25 15:32:18',28045.00,'PENDING','cod','Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','B9617017-346','standard'),
(101,22,'2025-06-27 00:03:43',325.00,'PENDING','gcash','Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','5D9E2F96-D44','standard'),
(102,62,'2025-06-27 00:04:27',467.00,'PENDING','gcash','General Mariano Alvarez','2D5B4F8B-592','express'),
(103,22,'2025-06-27 00:14:44',381.00,'TO_SHIP','cod','Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','90AF6157-549','standard'),
(104,22,'2025-06-27 00:22:06',1165.00,'PENDING','gcash','Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','3F9477A9-CFE','standard'),
(105,22,'2025-06-27 02:52:48',381.00,'PENDING','cod','Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','DC695FDF-386','standard'),
(106,22,'2025-06-29 03:21:52',1221.00,'PENDING','cod','Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','695D486F-A68','standard'),
(107,65,'2025-06-21 14:29:09',915.00,'PENDING','paypal','san pedro','91E34B2C-FDA','express'),
(108,65,'2025-06-21 14:35:54',773.00,'PENDING','paypal','san pedro','6BB87752-8D5','standard'),
(109,65,'2025-06-21 14:41:53',859.00,'PENDING','paypal','san pedro','CFA10642-4F2','express'),
(110,65,'2025-06-21 14:54:53',773.00,'COMPLETED','paypal','san pedro','E681BF0A-052','standard'),
(111,65,'2025-06-21 15:22:33',717.00,'COMPLETED','paypal','General Mariano Alvarez','F04E6A7C-91B','standard'),
(112,65,'2025-06-21 15:30:02',717.00,'COMPLETED','paypal','General Mariano Alvarez','22D2DFFB-59F','standard'),
(113,65,'2025-06-21 20:38:04',45.00,'PENDING','paypal','General Mariano Alvarez','A9F2D30E-EC4','standard'),
(114,65,'2025-06-21 20:58:20',45.00,'COMPLETED','paypal','General Mariano Alvarez','8824132B-8CB','standard'),
(115,65,'2025-06-22 00:53:51',75.00,'PENDING','gcash','General Mariano Alvarez','020ACD9D-ECA','express'),
(116,22,'2025-06-29 09:29:26',985.80,'PENDING','cod','Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','6294219E-6C5','standard');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  `order_id` bigint(20) unsigned DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `amount` decimal(38,2) NOT NULL,
  `order_date` timestamp NULL DEFAULT current_timestamp(),
  `transaction_id` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `payment_method_details` text DEFAULT NULL,
  `shipping_option` varchar(255) DEFAULT NULL,
  `days_left` int(11) NOT NULL,
  `quantity` bigint(20) DEFAULT NULL,
  `custom_product_ref` varchar(255) DEFAULT NULL,
  `is_custom` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`,`user_id`),
  KEY `fk_order_id_2` (`order_id`),
  KEY `fk_user` (`user_id`),
  KEY `fk_product` (`product_id`),
  CONSTRAINT `fk_order_id_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `payments_chk_1` CHECK (`status` in (_utf8mb4'PENDING',_utf8mb4'TO_SHIP',_utf8mb4'TO_RECEIVE',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED'))
) ENGINE=InnoDB AUTO_INCREMENT=135 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `payments` VALUES
(85,22,30,72,'Japanese Paper Lanterns',400.00,'2025-06-22 00:41:06','2E4976CD-28B','COMPLETED','paypal','express',5,1,NULL,0),
(86,22,27,73,'LED String Lights',300.00,'2025-06-22 00:49:43','DC340F4E-612','COMPLETED','banktransfer','express',5,1,NULL,0),
(88,22,1,75,'Custom Tarpaulin (medium)',530.00,'2025-06-22 00:52:51','56898E9A-0E0','COMPLETED','gcash','express',5,1,'Custom Tarpaulin (medium)',1),
(101,22,3,90,'Party Confetti',350.00,'2025-06-22 20:43:26','D7610CDB-208','PENDING','paypal','standard',5,1,NULL,0),
(102,22,2,90,'Clown Wig',100.00,'2025-06-22 20:43:26','D7610CDB-208','PENDING','paypal','standard',5,1,NULL,0),
(103,22,30,91,'Japanese Paper Lanterns',400.00,'2025-06-22 21:34:47','923C0036-61C','PENDING','paypal','standard',5,1,NULL,0),
(116,22,27,96,'LED String Lights',300.00,'2025-06-23 21:48:56','505E76CB-433','COMPLETED','paypal','express',5,1,NULL,0),
(119,60,18,99,'Garlands',100.00,'2025-06-23 23:07:31','6A48BB49-6C0','TO_SHIP','paypal','standard',5,1,NULL,0),
(120,60,1,99,'Custom Tarpaulin (medium)',400.00,'2025-06-23 23:07:31','6A48BB49-6C0','TO_SHIP','paypal','standard',5,1,'Custom Tarpaulin (medium)',1),
(121,22,31,100,'Charess Standee',25000.00,'2025-06-25 15:32:18','B9617017-346','PENDING','cod','standard',5,1,NULL,0),
(122,22,1,101,'Birthday Party Cone Hats',250.00,'2025-06-27 00:03:43','5D9E2F96-D44','PENDING','gcash','standard',5,1,'Birthday Party Cone Hats',1),
(123,62,4,102,'Funny Eyeglasses',350.00,'2025-06-27 00:04:27','2D5B4F8B-592','PENDING','gcash','express',5,1,NULL,0),
(124,22,27,103,'LED String Lights',300.00,'2025-06-27 00:14:44','90AF6157-549','TO_SHIP','cod','standard',5,1,NULL,0),
(125,22,1,104,'Clown Wig',1000.00,'2025-06-27 00:22:06','3F9477A9-CFE','CANCELLED','gcash','standard',5,10,'Clown Wig',1),
(126,22,1,105,'LED String Lights',300.00,'2025-06-27 02:52:48','DC695FDF-386','PENDING','cod','standard',5,1,'LED String Lights',1),
(127,22,1,106,'Party Confetti',700.00,'2025-06-29 03:21:52','695D486F-A68','PENDING','cod','standard',5,2,'Party Confetti',1),
(128,22,1,106,'Funny Eyeglasses',350.00,'2025-06-29 03:21:52','695D486F-A68','PENDING','cod','standard',5,1,'Funny Eyeglasses',1),
(129,65,29,110,'Carnival Themed Party Banners',300.00,'2025-06-29 04:28:48','E681BF0A-052','COMPLETED','paypal','standard',5,1,NULL,0),
(130,65,1,111,'Custom Tarpaulin (large)',600.00,'2025-06-29 04:28:48','F04E6A7C-91B','COMPLETED','paypal','standard',5,1,'Custom Tarpaulin (large)',1),
(131,65,1,112,'Custom Tarpaulin (large)',600.00,'2025-06-29 04:28:48','22D2DFFB-59F','COMPLETED','paypal','standard',5,1,'Custom Tarpaulin (large)',1),
(132,65,31,114,'Charess Standee',25000.00,'2025-06-29 04:28:48','8824132B-8CB','COMPLETED','paypal','standard',5,1,NULL,0),
(133,65,3,115,'Party Confetti',350.00,'2025-06-29 04:28:48','020ACD9D-ECA','PENDING','gcash','express',5,1,NULL,0),
(134,22,25,116,'Cinco de Mayo Piñata',840.00,'2025-06-29 09:29:26','6294219E-6C5','PENDING','cod','standard',5,1,NULL,0);
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_name` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `stock` bigint(20) NOT NULL,
  `category` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `image_loc` varchar(255) DEFAULT NULL,
  `created_at` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_categ_name` (`category`),
  KEY `fk_item_name` (`item_name`),
  CONSTRAINT `fk_categ_name` FOREIGN KEY (`category`) REFERENCES `categories` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `products` VALUES
(1,'Blue Mask',85,500,'Mask','Marvelous! Step into the spotlight with our stunning blue masquerade mask. Crafted with meticulous detail, this mask features a rich, vibrant blue hue that evokes a sense of sophistication and allure. Whether you\'re attending a Venetian ball, a costume party, or a theatrical performance, this mask will ensure you make a memorable entrance. The comfortable and secure fit allows you to dance the night away with confidence and grace.','/uploads/523bc2c2-6896-4a4d-9e6f-7ed8f8c55a24_bluemasq.png','2025-05-13 22:28:46'),
(2,'Clown Wig',100,499,'Wig','Get ready for fun with our vibrant Clown Wig! This colorful, multicolor wig is perfect for kids\' birthday parties, costume events, or just adding a touch of silliness to any day. Made with synthetic fibers, it\'s lightweight and comfortable to wear.','/uploads/1ed1a14d-c872-42a3-b032-cd879c6dd208_clownwig.png','2025-05-13 22:28:46'),
(3,'Party Confetti',350,498,'Confetti','Bring the excitement to your celebrations with our Party Confetti Poppers Set! This set includes four colorful confetti cannons, ready to burst with vibrant paper confetti. Perfect for birthdays, graduations, weddings, and any festive occasion.','/uploads/b1a6f549-4a2b-42ea-8ea2-3c95b567485d_confetti.png','2025-05-13 22:28:46'),
(4,'Funny Eyeglasses',350,498,'Eyeglasses',' Add a hilarious touch to any party with our Disguise Nose Glasses Prop! This funny accessory features oversized glasses with attached bushy eyebrows and a comical nose. Perfect for kids and adults alike, it\'s guaranteed to bring laughter to birthdays, costume parties, and photo booths.','/uploads/39ffe299-30f2-4468-8e19-b1cf7fd9b2d8_nosedisguise.png','2025-05-13 22:28:46'),
(18,'Garlands',100,499,'Garland','These orange flower vines are perfect for room decor, wedding aisle decorations, flower centerpieces for tables, floral birthday decorations, tea party decorations, etc. Adding more romantic atmosphere to your life!','/uploads/c234f2ae-6784-4acc-ae3d-e5a186add469_shopping.webp','2025-06-08 23:32:58'),
(25,'Cinco de Mayo Piñata',840,99,'Pinata','Thrill your guests with an exclusive Cinco de Mayo Piñata Donkey Decoration! The donkey-shaped piñata has an inner cavity for filling with prizes. It\'s covered with colorful tiered fringe, with black fringe wound around each \"hoof.\" The tail and halter are both made of long streamers. Hang it from a ceiling, a branch, or another sturdy support. ','/uploads/5a889284-01bd-439b-93bd-8d84d6938b8a_pinata 1.png','2025-06-14T22:51:04.637805700'),
(26,'Birthday Party Cone Hats',250,500,'Party Hats','(20 PCS)\r\nThese dazzling party accessories are perfect for adding a touch of glamour and fun to your celebration.\r\n\r\nEach package contains 6 conical hats, ready for your guests to wear. Their design sparkling And festive will add a touch of magic to your party atmosphere.\r\n\r\nWhether for a birthday, a company party or any other gathering, these hats will be the perfect accessory to make your guests shine and create unforgettable memories !','/uploads/82c98bbc-5f53-4e72-838d-42674e13b573_party hats 1.png','2025-06-14T22:58:49.669714200'),
(27,'LED String Lights',300,495,'LED','(40 Lights)\r\nEasy to place anywhere as it is battery operated and does not need to be connected to the main supply.\r\n\r\nUses LEDs, which consume up to 85% less energy and last 20 times longer than incandescent bulbs.','/uploads/758a7252-0423-4f62-8355-a7a9d03585f8_shopping (1).png','2025-06-15T03:41:47.377227300'),
(28,'Red Confetti Balloon Set',150,500,'Balloon','(10-in-1)\r\nIdeal for any party decorations, such as Graduations, birthday, anniversaries, baby showers, , New Year and so on. Decorate indoor or outdoor as a photo booth backdrop to get a big hit and create lasting memories.','/uploads/c37b68e5-4655-4bea-a4ff-23b4e3eeffa6_shopping.webp','2025-06-15T03:46:47.537732400'),
(29,'Carnival Themed Party Banners',300,500,'Banner','(120ft 720pcs) The banner is made of thick nylon rope for durability. The pennant flag is cut neatly and will not fall off or break easily.\r\nCan be used to decorate weddings, parties, holiday parties, dances, grand opening, carnivals, birthdays, Christmas, sporting events, school events and celebrations.','/uploads/37078a77-e913-4518-9cb3-971027f141de_71dobB6eusL._AC_SX679_.jpg','2025-06-15T03:51:08.531223'),
(30,'Japanese Paper Lanterns',400,499,'Hanging','(10pcs) Cherry party supplies are suitable for most occasions, such as Asian and sakura themed party, birthday party, weddings, etc., no matter where they are placed, they can complement different party decorations to give you the desired effect.\r\nFeatures with elegant and beautiful cherry design, these cherry paper lanterns can create an oriental style, add outdoor and indoor party atmosphere, and attract the attention of adults and children; At the same time, red round lanterns are more oriental and charming.','/uploads/b56b3050-24de-4860-8687-f965aa940f74_81a3PDJLFDL._AC_SX679_PIbundle-20,TopRight,0,0_SH20_.jpg','2025-06-15T03:53:39.291799700'),
(31,'Charess Standee',25000,0,'Props','25k pesos is a good investment.\r\n\r\nDon\'t ever think about buying this.','/uploads/bf9074cb-fbc9-4c36-acd3-752cccb83d06_Fk-l6yhagAEfoBY.jpg','2025-06-15T04:00:05.506758200'),
(32,'Patio String Lights',1000,500,'String Lights','(100ft) Our weatherproof technology ensures that these durable outdoor string lights can withstand rain, wind, snow, and extreme temperatures up to 158 degrees Fahrenheit, allowing you to confidently leave them outside year-round. Furthermore, the bulb shells are made of hard anti-drop plastic, protecting you and your family while hanging the waterproof backyard string lights, eliminating worries about sudden broken glass shards.','/uploads/4592a1c3-e087-4b54-be0a-c40185cc75cb_71JM1dsFQ1L.__AC_SY445_SX342_QL70_FMwebp_.webp','2025-06-15T04:02:45.514769600'),
(51,'Anniv Blush Centerpiece',300,119,'Table Centerpiece','This festive, sparkling centerpiece is the perfect ornament to celebrate a birthday in style. Featuring shiny rose gold fringes and decorative \'Happy Birthday\' tags, it instantly adds a touch of glamor to your decor.\r\n\r\nEasy to set up, this attractive centerpiece is ideal for all age groups and will bring a cheerful atmosphere and a personal touch to your festivities.','/uploads/d56473ea-2ec2-4760-8b3c-958a93e7fc58_Centre-de-table_1200x.webp','2025-06-15T04:07:21.319235');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `last_login` timestamp NULL DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `prof_img_loc` varchar(255) NOT NULL DEFAULT '/img/default-profile.png',
  PRIMARY KEY (`id`),
  CONSTRAINT `user_chk_1` CHECK (`role` in (_utf8mb4'ADMIN',_utf8mb4'USER'))
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `user` VALUES
(22,'Block 11 Excess Lot, Sampaloc Street, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','pauloneil3334@gmail.com','$2a$12$L..6Q18MRGrt3eTM3asQ3O4pMsQo2qphjhnRXbRFmopZqShHC71yW','ADMIN','Paulo','2025-05-31 18:22:37','2025-06-29 09:37:35','MALE','Paulo Neil','09550367575','2004-08-24','/uploads/profiles/Paulo-368e53a9-7819-4b16-8746-0f2156de07c0-images.jpeg'),
(60,'San Pedro','junkemail5@gmail.com','$2a$12$YUQHzdv/0MO.ZF7bb.DPp.RIF/TZzpvfaKNRGpwuf8Y7yUf9dEB8y','USER','grace','2025-06-23 23:07:31','2025-06-22 07:59:09','FEMALE','Grace','123456','2000-02-22','/uploads/profiles/grace-b8cd3ee8-d316-4df0-806e-3f385add3123-grace.jpeg'),
(62,'General Mariano Alvarez','junkemail6@gmail.com','$2a$10$5UN06wz.9bxlQ2pHhXI82eu4ZtauYvJfUiyP1/p5kjG4UI5l8KzxK','USER','yanagi','2025-06-24 05:39:06','2025-06-24 05:39:06','FEMALE','Yanagi','09969529267','2003-02-01','/uploads/profiles/yanagi-ba1ddcc0-fd0b-4291-a573-6cceb44d5314-d70210e934863e5b5f87e5f98cabca68.jpg'),
(64,'Block 11 Excess Lot, Sampaloc Street, Teresita, Santo Niño, South Cotabato, SOCCSKSARGEN','sevillapaulo364@gmail.com','$2a$10$DRdZRvRbjPgIQQOIyMv5LOLpoK3uuXmIYmRXeM9nn.w5W3A25akIy','USER','neil','2025-06-25 03:40:10','2025-06-25 03:40:10','MALE','Paulo Neil','09969529267','2004-08-24','/uploads/profiles/neil-908b2300-afed-45a3-ba7d-2e4ed445afb3-Screenshot_20250613_062022.png'),
(65,'New Eridu, Lumina Square, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','junkemail@email.com','$2a$10$hXgyO0zsjDDbHL78ttmwv.y41UMtB7m0zRsfjTtI5xsU9GTyQlHnW','USER','zhu','2025-06-29 04:28:48','2025-06-08 15:43:22','FEMALE','Zhu Yuan','12345','1977-07-28','/uploads/profiles/zhu-238fa235-c3bb-413c-bde4-8875f4198330-zhu-yuan-zenless-zone-zero.png'),
(66,'admin, admin, Barangay 4 Poblacion, Gen. Mariano Alvarez, Cavite, CALABARZON','admin@admin.com','$2a$10$oMVdwf4DF/9NM17dJElzdeeGlwSbCrcHDDPIyejnZn3uJ/r.0xoY6','ADMIN','admin','2025-06-29 04:50:54','2025-06-29 04:51:47','OTHER','admin','09969529267','2000-01-01','/img/default-profile.png');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Dumping routines for database 'shopinv'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-06-29 17:47:39
