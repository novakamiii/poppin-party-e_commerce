-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: shopinv
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `idx_categories_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Mask','a mask!'),(3,'Other',NULL),(4,'Eyeglasses',NULL),(5,'Wig',NULL),(6,'Confetti',NULL),(7,'Garland',NULL),(8,'fatass teto',NULL),(11,'Pinata',NULL),(12,'Party Hats',NULL),(13,'LED',NULL),(14,'Baloon',NULL),(15,'Balloon',NULL),(16,'Banner',NULL),(17,'Hanging',NULL),(18,'Props',NULL),(19,'String Lights',NULL),(20,'Table Centerpiece',NULL);
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `product_ref` varchar(255) DEFAULT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`,`user_id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `unique_order_product` (`product_ref`),
  KEY `fk_product_ref` (`product_ref`) /*!80000 INVISIBLE */,
  KEY `fk_order_items_user` (`user_id`),
  CONSTRAINT `fk_order_items_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_product_ref` FOREIGN KEY (`product_ref`) REFERENCES `products` (`item_name`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (10,29,'Party Confetti',12,350.00),(13,29,'Japanese Paper Lanterns',10,400.00),(14,22,'Cinco de Mayo Piñata',1,840.00),(15,22,'Clown Wig',1,100.00);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `order_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `total_amount` decimal(10,2) NOT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `payment_method` varchar(50) DEFAULT NULL,
  `shipping_address` text NOT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_user_id` (`user_id`),
  CONSTRAINT `fk_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `orders_chk_1` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'PROCESSING',_utf8mb4'SHIPPED',_utf8mb4'DELIVERED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint unsigned DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `transaction_id` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `payment_method_details` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `fk_order_id_2` (`order_id`),
  CONSTRAINT `fk_order_id_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `payments_chk_1` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'COMPLETED',_utf8mb4'FAILED',_utf8mb4'REFUNDED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_name` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `stock` bigint NOT NULL,
  `category` varchar(255) DEFAULT NULL,
  `description` text,
  `image_loc` varchar(255) DEFAULT NULL,
  `created_at` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_categ_name` (`category`),
  KEY `fk_item_name` (`item_name`),
  CONSTRAINT `fk_categ_name` FOREIGN KEY (`category`) REFERENCES `categories` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Blue Mask',85,500,'Mask','Marvelous! Step into the spotlight with our stunning blue masquerade mask. Crafted with meticulous detail, this mask features a rich, vibrant blue hue that evokes a sense of sophistication and allure. Whether you\'re attending a Venetian ball, a costume party, or a theatrical performance, this mask will ensure you make a memorable entrance. The comfortable and secure fit allows you to dance the night away with confidence and grace.','/uploads/523bc2c2-6896-4a4d-9e6f-7ed8f8c55a24_bluemasq.png','2025-05-13 22:28:46'),(2,'Clown Wig',100,500,'Wig','Get ready for fun with our vibrant Clown Wig! This colorful, multicolor wig is perfect for kids\' birthday parties, costume events, or just adding a touch of silliness to any day. Made with synthetic fibers, it\'s lightweight and comfortable to wear.','/uploads/1ed1a14d-c872-42a3-b032-cd879c6dd208_clownwig.png','2025-05-13 22:28:46'),(3,'Party Confetti',350,500,'Confetti','Bring the excitement to your celebrations with our Party Confetti Poppers Set! This set includes four colorful confetti cannons, ready to burst with vibrant paper confetti. Perfect for birthdays, graduations, weddings, and any festive occasion.','/uploads/b1a6f549-4a2b-42ea-8ea2-3c95b567485d_confetti.png','2025-05-13 22:28:46'),(4,'Funny Eyeglasses',350,500,'Eyeglasses',' Add a hilarious touch to any party with our Disguise Nose Glasses Prop! This funny accessory features oversized glasses with attached bushy eyebrows and a comical nose. Perfect for kids and adults alike, it\'s guaranteed to bring laughter to birthdays, costume parties, and photo booths.','/uploads/39ffe299-30f2-4468-8e19-b1cf7fd9b2d8_nosedisguise.png','2025-05-13 22:28:46'),(18,'Garlands',100,500,'Garland','These orange flower vines are perfect for room decor, wedding aisle decorations, flower centerpieces for tables, floral birthday decorations, tea party decorations, etc. Adding more romantic atmosphere to your life!','/uploads/c234f2ae-6784-4acc-ae3d-e5a186add469_shopping.webp','2025-06-08 23:32:58'),(19,'Tralalelo Tralala',350,500,'Other','tralalelo tralala','/uploads/0ad10595-4d98-4204-b243-e48a4855fdc5_images (4).jpg','2025-06-09T02:06:56.037669600'),(21,'teto fatass',85,95,'fatass teto','HER ass is not hiding!','/uploads/06ce3e12-a1d6-4b43-878c-68e72d928eb0_fatass-teto-vs-fatass-miku-who-would-win-v0-o7zqmbj87bge1.webp','2025-06-09T16:11:42.606054300'),(25,'Cinco de Mayo Piñata',840,100,'Pinata','Thrill your guests with an exclusive Cinco de Mayo Piñata Donkey Decoration! The donkey-shaped piñata has an inner cavity for filling with prizes. It\'s covered with colorful tiered fringe, with black fringe wound around each \"hoof.\" The tail and halter are both made of long streamers. Hang it from a ceiling, a branch, or another sturdy support. ','/uploads/5a889284-01bd-439b-93bd-8d84d6938b8a_pinata 1.png','2025-06-14T22:51:04.637805700'),(26,'Birthday Party Cone Hats',250,500,'Party Hats','(20 PCS)\r\nThese dazzling party accessories are perfect for adding a touch of glamour and fun to your celebration.\r\n\r\nEach package contains 6 conical hats, ready for your guests to wear. Their design sparkling And festive will add a touch of magic to your party atmosphere.\r\n\r\nWhether for a birthday, a company party or any other gathering, these hats will be the perfect accessory to make your guests shine and create unforgettable memories !','/uploads/82c98bbc-5f53-4e72-838d-42674e13b573_party hats 1.png','2025-06-14T22:58:49.669714200'),(27,'LED String Lights',300,500,'LED','(40 Lights)\r\nEasy to place anywhere as it is battery operated and does not need to be connected to the main supply.\r\n\r\nUses LEDs, which consume up to 85% less energy and last 20 times longer than incandescent bulbs.','/uploads/758a7252-0423-4f62-8355-a7a9d03585f8_shopping (1).png','2025-06-15T03:41:47.377227300'),(28,'Red Confetti Balloon Set',150,500,'Balloon','(10-in-1)\r\nIdeal for any party decorations, such as Graduations, birthday, anniversaries, baby showers, , New Year and so on. Decorate indoor or outdoor as a photo booth backdrop to get a big hit and create lasting memories.','/uploads/c37b68e5-4655-4bea-a4ff-23b4e3eeffa6_shopping.webp','2025-06-15T03:46:47.537732400'),(29,'Carnival Themed Party Banners',300,500,'Banner','(120ft 720pcs) The banner is made of thick nylon rope for durability. The pennant flag is cut neatly and will not fall off or break easily.\r\nCan be used to decorate weddings, parties, holiday parties, dances, grand opening, carnivals, birthdays, Christmas, sporting events, school events and celebrations.','/uploads/37078a77-e913-4518-9cb3-971027f141de_71dobB6eusL._AC_SX679_.jpg','2025-06-15T03:51:08.531223'),(30,'Japanese Paper Lanterns',400,500,'Hanging','(10pcs) Cherry party supplies are suitable for most occasions, such as Asian and sakura themed party, birthday party, weddings, etc., no matter where they are placed, they can complement different party decorations to give you the desired effect.\r\nFeatures with elegant and beautiful cherry design, these cherry paper lanterns can create an oriental style, add outdoor and indoor party atmosphere, and attract the attention of adults and children; At the same time, red round lanterns are more oriental and charming.','/uploads/b56b3050-24de-4860-8687-f965aa940f74_81a3PDJLFDL._AC_SX679_PIbundle-20,TopRight,0,0_SH20_.jpg','2025-06-15T03:53:39.291799700'),(31,'Charess Standee',25000,1,'Props','25k pesos is a good investment.\r\n\r\nDon\'t ever think about buying this.','/uploads/dab846f6-c224-413d-bdab-4aaa01fa0a4c_images.jpg','2025-06-15T04:00:05.506758200'),(32,'Patio String Lights',1000,500,'String Lights','(100ft) Our weatherproof technology ensures that these durable outdoor string lights can withstand rain, wind, snow, and extreme temperatures up to 158 degrees Fahrenheit, allowing you to confidently leave them outside year-round. Furthermore, the bulb shells are made of hard anti-drop plastic, protecting you and your family while hanging the waterproof backyard string lights, eliminating worries about sudden broken glass shards.','/uploads/4592a1c3-e087-4b54-be0a-c40185cc75cb_71JM1dsFQ1L.__AC_SY445_SX342_QL70_FMwebp_.webp','2025-06-15T04:02:45.514769600'),(33,'Anniv Blush Centerpiece',300,120,'Table Centerpiece','This festive, sparkling centerpiece is the perfect ornament to celebrate a birthday in style. Featuring shiny rose gold fringes and decorative \'Happy Birthday\' tags, it instantly adds a touch of glamor to your decor.\r\n\r\nEasy to set up, this attractive centerpiece is ideal for all age groups and will bring a cheerful atmosphere and a personal touch to your festivities.','/uploads/d56473ea-2ec2-4760-8b3c-958a93e7fc58_Centre-de-table_1200x.webp','2025-06-15T04:07:21.319235');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `last_login` timestamp NULL DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `birth_date` date DEFAULT NULL,
  `prof_img_loc` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `user_chk_1` CHECK ((`role` in (_utf8mb4'ADMIN',_utf8mb4'USER')))
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (22,'San Pedro','pauloneil3334@gmail.com','$2a$10$NCmNK2cfPDLx15owfmEFsuW73om6BnCfrafNRrStnw2WBOPyr5sla','ADMIN','Paulo','2025-05-31 18:22:37','2025-06-15 11:56:56','MALE','Paulo Neil','09550367575','2004-08-24','/uploads/profiles/Paulo-6ac67500-3ce4-49a4-9134-0052f02c1c5e-aasdas.PNG'),(23,'GMA','sevillapaulo364@gmail.com','$2a$10$16kKLkyMb8CBM.4w.0wEE.cEgX1g6hL99.7FmEtPKOtG9bgbemuUW','USER','Sevilla','2025-05-31 19:11:32','2025-05-31 19:11:33','MALE','Sevilla','09550367575','2004-08-24',''),(25,'GMA','pauloneil3334@gmail.com','$2a$10$DtWXwtuwhVsyN3tIWE7z0.MGujW3238ri1SVATclxc8F8u5FbIYP2','USER','Paulo2','2025-06-02 00:52:25','2025-06-02 00:52:25','MALE','PauloUser','09550367575','2008-02-02',''),(26,'san pedro','pauloneil3334@mail.com','$2a$10$4o.u69fkeHcKB2.jlQDpYepFljzaAjuihZZ4kcHI5/LExCKIP/rde','USER','usertest','2025-06-02 01:45:23','2025-06-02 01:45:24','MALE','paulo','09550367575','2018-07-27',''),(27,'GMA','pauloneil75@yahoo.com','$2a$10$89GzjBU6CgU./Z.CY4O0VeR24DCC1nXd5U6lRiLEF3Y1sUxrI8Iii','USER','janedoe22','2025-06-08 12:26:27','2025-06-08 12:26:27','FEMALE','Jane','12345','2004-08-24',''),(29,'san pedro','junkemail@email.com','$2a$10$oV7ZUvvMEUPn7rlmrWG1vupQJloi.uHmszHmz3zMxBrk/V6Ux02tO','USER','zhu','2025-06-08 15:43:22','2025-06-08 15:43:22','FEMALE','Zhu Yuan','12345','1977-07-28','/uploads/profiles/zhu-238fa235-c3bb-413c-bde4-8875f4198330-zhu-yuan-zenless-zone-zero.png');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-15 21:20:31
