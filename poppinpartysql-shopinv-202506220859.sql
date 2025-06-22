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
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
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
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archived_orders`
--

LOCK TABLES `archived_orders` WRITE;
/*!40000 ALTER TABLE `archived_orders` DISABLE KEYS */;
set autocommit=0;
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
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
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
  `description` varchar(255) DEFAULT NULL,
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
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `archived_user`
--

LOCK TABLES `archived_user` WRITE;
/*!40000 ALTER TABLE `archived_user` DISABLE KEYS */;
set autocommit=0;
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
(8,'fatass teto',NULL),
(11,'Pinata',NULL),
(12,'Party Hats',NULL),
(13,'LED',NULL),
(14,'Baloon',NULL),
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
) ENGINE=InnoDB AUTO_INCREMENT=170 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
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
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
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
(73,22,'2025-06-22 00:49:43',75.00,'TO_SHIP','banktransfer','San Pedro','DC340F4E-612','express'),
(74,22,'2025-06-22 00:50:48',75.00,'COMPLETED','gcash','San Pedro','95FAA7EF-B2F','express'),
(75,22,'2025-06-22 00:52:51',75.00,'PENDING','gcash','San Pedro','56898E9A-0E0','express'),
(77,51,'2025-06-21 14:29:09',915.00,'PENDING','paypal','san pedro','91E34B2C-FDA','express'),
(78,51,'2025-06-21 14:35:54',773.00,'PENDING','paypal','san pedro','6BB87752-8D5','standard'),
(79,51,'2025-06-21 14:41:53',859.00,'PENDING','paypal','san pedro','CFA10642-4F2','express'),
(80,51,'2025-06-21 14:54:53',773.00,'COMPLETED','paypal','san pedro','E681BF0A-052','standard'),
(81,51,'2025-06-21 15:22:33',717.00,'TO_RECEIVE','paypal','General Mariano Alvarez','F04E6A7C-91B','standard'),
(82,51,'2025-06-21 15:30:02',717.00,'COMPLETED','paypal','General Mariano Alvarez','22D2DFFB-59F','standard'),
(83,51,'2025-06-21 20:38:04',45.00,'PENDING','paypal','General Mariano Alvarez','A9F2D30E-EC4','standard'),
(84,51,'2025-06-21 20:58:20',45.00,'COMPLETED','paypal','General Mariano Alvarez','8824132B-8CB','standard'),
(85,51,'2025-06-22 00:53:51',75.00,'PENDING','gcash','General Mariano Alvarez','020ACD9D-ECA','express');
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
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `payments` VALUES
(85,22,30,72,'Japanese Paper Lanterns',400.00,'2025-06-22 00:41:06','2E4976CD-28B','COMPLETED','paypal','express',5,1,NULL,0),
(86,22,27,73,'LED String Lights',300.00,'2025-06-22 00:49:43','DC340F4E-612','TO_SHIP','banktransfer','express',5,1,NULL,0),
(87,22,19,74,'Tralalelo Tralala',350.00,'2025-06-22 00:50:48','95FAA7EF-B2F','COMPLETED','gcash','express',5,1,NULL,0),
(88,22,1,75,'Custom Tarpaulin (medium)',530.00,'2025-06-22 00:52:51','56898E9A-0E0','PENDING','gcash','express',5,1,'Custom Tarpaulin (medium)',1),
(90,51,19,80,'Tralalelo Tralala',350.00,'2025-06-22 00:55:27','E681BF0A-052','COMPLETED','paypal','standard',5,1,NULL,0),
(91,51,29,80,'Carnival Themed Party Banners',300.00,'2025-06-22 00:55:27','E681BF0A-052','COMPLETED','paypal','standard',5,1,NULL,0),
(92,51,1,81,'Custom Tarpaulin (large)',600.00,'2025-06-22 00:55:27','F04E6A7C-91B','TO_RECEIVE','paypal','standard',5,1,'Custom Tarpaulin (large)',1),
(93,51,1,82,'Custom Tarpaulin (large)',600.00,'2025-06-22 00:55:27','22D2DFFB-59F','COMPLETED','paypal','standard',5,1,'Custom Tarpaulin (large)',1),
(94,51,19,83,'Tralalelo Tralala',2100.00,'2025-06-22 00:55:27','A9F2D30E-EC4','CANCELLED','paypal','standard',5,6,NULL,0),
(95,51,31,84,'Charess Standee',25000.00,'2025-06-22 00:55:27','8824132B-8CB','COMPLETED','paypal','standard',5,1,NULL,0),
(96,51,3,85,'Party Confetti',350.00,'2025-06-22 00:55:27','020ACD9D-ECA','PENDING','gcash','express',5,1,NULL,0);
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
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `products` VALUES
(1,'Blue Mask',85,500,'Mask','Marvelous! Step into the spotlight with our stunning blue masquerade mask. Crafted with meticulous detail, this mask features a rich, vibrant blue hue that evokes a sense of sophistication and allure. Whether you\'re attending a Venetian ball, a costume party, or a theatrical performance, this mask will ensure you make a memorable entrance. The comfortable and secure fit allows you to dance the night away with confidence and grace.','/uploads/523bc2c2-6896-4a4d-9e6f-7ed8f8c55a24_bluemasq.png','2025-05-13 22:28:46'),
(2,'Clown Wig',100,500,'Wig','Get ready for fun with our vibrant Clown Wig! This colorful, multicolor wig is perfect for kids\' birthday parties, costume events, or just adding a touch of silliness to any day. Made with synthetic fibers, it\'s lightweight and comfortable to wear.','/uploads/1ed1a14d-c872-42a3-b032-cd879c6dd208_clownwig.png','2025-05-13 22:28:46'),
(3,'Party Confetti',350,499,'Confetti','Bring the excitement to your celebrations with our Party Confetti Poppers Set! This set includes four colorful confetti cannons, ready to burst with vibrant paper confetti. Perfect for birthdays, graduations, weddings, and any festive occasion.','/uploads/b1a6f549-4a2b-42ea-8ea2-3c95b567485d_confetti.png','2025-05-13 22:28:46'),
(4,'Funny Eyeglasses',350,500,'Eyeglasses',' Add a hilarious touch to any party with our Disguise Nose Glasses Prop! This funny accessory features oversized glasses with attached bushy eyebrows and a comical nose. Perfect for kids and adults alike, it\'s guaranteed to bring laughter to birthdays, costume parties, and photo booths.','/uploads/39ffe299-30f2-4468-8e19-b1cf7fd9b2d8_nosedisguise.png','2025-05-13 22:28:46'),
(18,'Garlands',100,500,'Garland','These orange flower vines are perfect for room decor, wedding aisle decorations, flower centerpieces for tables, floral birthday decorations, tea party decorations, etc. Adding more romantic atmosphere to your life!','/uploads/c234f2ae-6784-4acc-ae3d-e5a186add469_shopping.webp','2025-06-08 23:32:58'),
(19,'Tralalelo Tralala',350,499,'Other','tralalelo tralala','/uploads/0ad10595-4d98-4204-b243-e48a4855fdc5_images (4).jpg','2025-06-09T02:06:56.037669600'),
(21,'teto fatass',85,95,'fatass teto','HER ass is not hiding!','/uploads/06ce3e12-a1d6-4b43-878c-68e72d928eb0_fatass-teto-vs-fatass-miku-who-would-win-v0-o7zqmbj87bge1.webp','2025-06-09T16:11:42.606054300'),
(25,'Cinco de Mayo Piñata',840,100,'Pinata','Thrill your guests with an exclusive Cinco de Mayo Piñata Donkey Decoration! The donkey-shaped piñata has an inner cavity for filling with prizes. It\'s covered with colorful tiered fringe, with black fringe wound around each \"hoof.\" The tail and halter are both made of long streamers. Hang it from a ceiling, a branch, or another sturdy support. ','/uploads/5a889284-01bd-439b-93bd-8d84d6938b8a_pinata 1.png','2025-06-14T22:51:04.637805700'),
(26,'Birthday Party Cone Hats',250,500,'Party Hats','(20 PCS)\r\nThese dazzling party accessories are perfect for adding a touch of glamour and fun to your celebration.\r\n\r\nEach package contains 6 conical hats, ready for your guests to wear. Their design sparkling And festive will add a touch of magic to your party atmosphere.\r\n\r\nWhether for a birthday, a company party or any other gathering, these hats will be the perfect accessory to make your guests shine and create unforgettable memories !','/uploads/82c98bbc-5f53-4e72-838d-42674e13b573_party hats 1.png','2025-06-14T22:58:49.669714200'),
(27,'LED String Lights',300,499,'LED','(40 Lights)\r\nEasy to place anywhere as it is battery operated and does not need to be connected to the main supply.\r\n\r\nUses LEDs, which consume up to 85% less energy and last 20 times longer than incandescent bulbs.','/uploads/758a7252-0423-4f62-8355-a7a9d03585f8_shopping (1).png','2025-06-15T03:41:47.377227300'),
(28,'Red Confetti Balloon Set',150,500,'Balloon','(10-in-1)\r\nIdeal for any party decorations, such as Graduations, birthday, anniversaries, baby showers, , New Year and so on. Decorate indoor or outdoor as a photo booth backdrop to get a big hit and create lasting memories.','/uploads/c37b68e5-4655-4bea-a4ff-23b4e3eeffa6_shopping.webp','2025-06-15T03:46:47.537732400'),
(29,'Carnival Themed Party Banners',300,500,'Banner','(120ft 720pcs) The banner is made of thick nylon rope for durability. The pennant flag is cut neatly and will not fall off or break easily.\r\nCan be used to decorate weddings, parties, holiday parties, dances, grand opening, carnivals, birthdays, Christmas, sporting events, school events and celebrations.','/uploads/37078a77-e913-4518-9cb3-971027f141de_71dobB6eusL._AC_SX679_.jpg','2025-06-15T03:51:08.531223'),
(30,'Japanese Paper Lanterns',400,499,'Hanging','(10pcs) Cherry party supplies are suitable for most occasions, such as Asian and sakura themed party, birthday party, weddings, etc., no matter where they are placed, they can complement different party decorations to give you the desired effect.\r\nFeatures with elegant and beautiful cherry design, these cherry paper lanterns can create an oriental style, add outdoor and indoor party atmosphere, and attract the attention of adults and children; At the same time, red round lanterns are more oriental and charming.','/uploads/b56b3050-24de-4860-8687-f965aa940f74_81a3PDJLFDL._AC_SX679_PIbundle-20,TopRight,0,0_SH20_.jpg','2025-06-15T03:53:39.291799700'),
(31,'Charess Standee',25000,0,'Props','25k pesos is a good investment.\r\n\r\nDon\'t ever think about buying this.','/uploads/dab846f6-c224-413d-bdab-4aaa01fa0a4c_images.jpg','2025-06-15T04:00:05.506758200'),
(32,'Patio String Lights',1000,500,'String Lights','(100ft) Our weatherproof technology ensures that these durable outdoor string lights can withstand rain, wind, snow, and extreme temperatures up to 158 degrees Fahrenheit, allowing you to confidently leave them outside year-round. Furthermore, the bulb shells are made of hard anti-drop plastic, protecting you and your family while hanging the waterproof backyard string lights, eliminating worries about sudden broken glass shards.','/uploads/4592a1c3-e087-4b54-be0a-c40185cc75cb_71JM1dsFQ1L.__AC_SY445_SX342_QL70_FMwebp_.webp','2025-06-15T04:02:45.514769600'),
(33,'Anniv Blush Centerpiece',300,120,'Table Centerpiece','This festive, sparkling centerpiece is the perfect ornament to celebrate a birthday in style. Featuring shiny rose gold fringes and decorative \'Happy Birthday\' tags, it instantly adds a touch of glamor to your decor.\r\n\r\nEasy to set up, this attractive centerpiece is ideal for all age groups and will bring a cheerful atmosphere and a personal touch to your festivities.','/uploads/d56473ea-2ec2-4760-8b3c-958a93e7fc58_Centre-de-table_1200x.webp','2025-06-15T04:07:21.319235'),
(36,'Jdent Pascual',1,1,'Other','jdent pascual','/uploads/66b757a1-ed62-4359-b0b1-34ea77adf4f8_Screenshot_20250619_210141.png','2025-06-19T21:02:12.931155212'),
(38,'Trigger\'s Cake',999,500,'Other','Huge.','/uploads/da77d90c-c04d-4ee9-b1a9-1aab2f0624f8_6EWDT4Ys_400x400.jpg','2025-06-20T02:39:18.896934959');
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
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
set autocommit=0;
INSERT INTO `user` VALUES
(22,'San Pedro','pauloneil3334@gmail.com','$2a$10$NCmNK2cfPDLx15owfmEFsuW73om6BnCfrafNRrStnw2WBOPyr5sla','ADMIN','Paulo','2025-05-31 18:22:37','2025-06-22 00:54:26','MALE','Paulo Neil','09550367575','2004-08-24','/uploads/profiles/Paulo-7e33a69c-e1bb-46d1-8673-e0a264ed377d-aasdas.PNG'),
(23,'GMA','sevillapaulo364@gmail.com','$2a$10$16kKLkyMb8CBM.4w.0wEE.cEgX1g6hL99.7FmEtPKOtG9bgbemuUW','USER','Sevilla','2025-05-31 19:11:32','2025-05-31 19:11:33','MALE','Sevilla','09550367575','2004-08-24',''),
(25,'GMA','pauloneil3334@gmail.com','$2a$10$DtWXwtuwhVsyN3tIWE7z0.MGujW3238ri1SVATclxc8F8u5FbIYP2','USER','Paulo2','2025-06-02 00:52:25','2025-06-02 00:52:25','MALE','PauloUser','09550367575','2008-02-02',''),
(26,'san pedro','pauloneil3334@mail.com','$2a$10$4o.u69fkeHcKB2.jlQDpYepFljzaAjuihZZ4kcHI5/LExCKIP/rde','USER','usertest','2025-06-02 01:45:23','2025-06-02 01:45:24','MALE','paulo','09550367575','2018-07-27',''),
(33,'San Pedro','junkemail3@gmail.com','$2a$10$iNXzGoIOk8x0Isk0yvWQl.PLdYDq1ff3PO7PFKRD8zqihmXxAQ/tu','USER','eve','2025-06-20 18:56:26','2025-06-20 17:34:35','FEMALE','Evelyn','123456','2008-09-24','/img/default-profile.png'),
(51,'General Mariano Alvarez','junkemail@email.com','$2a$10$oV7ZUvvMEUPn7rlmrWG1vupQJloi.uHmszHmz3zMxBrk/V6Ux02tO','USER','zhu','2025-06-22 00:55:27','2025-06-08 15:43:22','FEMALE','Zhu Yuan','12345','1977-07-28','/uploads/profiles/zhu-238fa235-c3bb-413c-bde4-8875f4198330-zhu-yuan-zenless-zone-zero.png');
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

-- Dump completed on 2025-06-22  8:59:50
