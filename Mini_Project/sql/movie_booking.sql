
CREATE DATABASE IF NOT EXISTS movie_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE movie_booking;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `booking_seats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_seats` (
  `seat_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `show_id` int NOT NULL,
  `seat_label` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`seat_id`),
  UNIQUE KEY `uniq_show_seat` (`show_id`,`seat_label`),
  KEY `fk_seat_booking` (`booking_id`),
  CONSTRAINT `fk_seat_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`),
  CONSTRAINT `fk_seat_show` FOREIGN KEY (`show_id`) REFERENCES `shows` (`show_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `booking_seats` WRITE;
/*!40000 ALTER TABLE `booking_seats` DISABLE KEYS */;
INSERT INTO `booking_seats` VALUES (1,1,43,'A5'),(2,1,43,'A6'),(3,2,43,'B2'),(4,2,43,'B3'),(5,2,43,'B4'),(6,2,43,'E8'),(7,2,43,'E9'),(8,3,50,'C4'),(9,3,50,'C5'),(10,3,50,'D1');
/*!40000 ALTER TABLE `booking_seats` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `show_id` int NOT NULL,
  `booking_code` varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `seat_count` int NOT NULL,
  `seat_summary` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CONFIRMED',
  `booked_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `booking_code` (`booking_code`),
  KEY `fk_booking_user` (`user_id`),
  KEY `fk_booking_show` (`show_id`),
  CONSTRAINT `fk_booking_show` FOREIGN KEY (`show_id`) REFERENCES `shows` (`show_id`),
  CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,2,43,'CBSEED432',360.00,2,'A5, A6','CONFIRMED','2026-10-11 13:24:29'),(2,3,43,'CBSEED433',900.00,5,'B2, B3, B4, E8, E9','CONFIRMED','2026-10-11 13:24:29'),(3,3,50,'CBSEED503',750.00,3,'C4, C5, D1','CONFIRMED','2026-10-11 13:24:29');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `movies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movies` (
  `movie_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `genre` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `language` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `duration_min` int NOT NULL,
  `certificate` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `poster_color` varchar(7) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`movie_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `movies` WRITE;
/*!40000 ALTER TABLE `movies` DISABLE KEYS */;
INSERT INTO `movies` VALUES (1,'Inception','A thief enters dreams to plant an idea that will not stay buried.','Sci-Fi','English',148,'UA','#12344d','NOW_SHOWING'),(2,'Interstellar','Pilots cross a wormhole looking for a home when Earth begins to fail.','Sci-Fi','English',169,'UA','#3a2a1a','NOW_SHOWING'),(3,'The Dark Knight','Gotham\'s masked protector faces a criminal who wants the city to break.','Action','English',152,'UA','#3d2b4a','NOW_SHOWING'),(4,'Dune','An heir arrives on a desert planet where spice, prophecy, and empire collide.','Sci-Fi','English',155,'UA','#8a5a2b','NOW_SHOWING'),(5,'Parasite','Two families share one house and a secret that cannot survive the rain.','Thriller','Korean',132,'A','#1f4d3a','NOW_SHOWING'),(6,'Spirited Away','A girl trapped in a spirit bathhouse has to remember who she still is.','Animation','Japanese',125,'U','#2a6f8f','NOW_SHOWING'),(7,'La La Land','A pianist and an actress fall in love while chasing separate careers.','Musical','English',128,'U','#6b2d4a','NOW_SHOWING'),(8,'Whiplash','A drummer pushes past injury for a conductor who only respects perfection.','Drama','English',107,'A','#7a1e1e','NOW_SHOWING'),(9,'Oppenheimer','The story of the scientist who built the bomb and then had to live with it.','Biography','English',180,'UA','#4a4638','COMING_SOON'),(10,'Past Lives','Two childhood friends meet again and measure the lives they did not live.','Romance','Korean',106,'U','#2c4a3e','COMING_SOON');
/*!40000 ALTER TABLE `movies` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `screens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `screens` (
  `screen_id` int NOT NULL AUTO_INCREMENT,
  `theater_id` int NOT NULL,
  `name` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_rows` int NOT NULL,
  `seats_per_row` int NOT NULL,
  PRIMARY KEY (`screen_id`),
  KEY `fk_screen_theater` (`theater_id`),
  CONSTRAINT `fk_screen_theater` FOREIGN KEY (`theater_id`) REFERENCES `theaters` (`theater_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `screens` WRITE;
/*!40000 ALTER TABLE `screens` DISABLE KEYS */;
INSERT INTO `screens` VALUES (1,1,'Screen 1',8,10),(2,1,'Screen 2',8,10),(3,2,'Screen 1',8,10),(4,2,'Screen 2',8,10);
/*!40000 ALTER TABLE `screens` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `shows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shows` (
  `show_id` int NOT NULL AUTO_INCREMENT,
  `movie_id` int NOT NULL,
  `screen_id` int NOT NULL,
  `show_date` date NOT NULL,
  `show_time` time NOT NULL,
  `price` decimal(8,2) NOT NULL,
  PRIMARY KEY (`show_id`),
  KEY `fk_show_movie` (`movie_id`),
  KEY `fk_show_screen` (`screen_id`),
  CONSTRAINT `fk_show_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`movie_id`),
  CONSTRAINT `fk_show_screen` FOREIGN KEY (`screen_id`) REFERENCES `screens` (`screen_id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `shows` WRITE;
/*!40000 ALTER TABLE `shows` DISABLE KEYS */;
INSERT INTO `shows` VALUES (1,1,1,'2026-10-11','11:00:00',180.00),(2,1,2,'2026-10-12','14:30:00',250.00),(3,1,3,'2026-10-13','18:00:00',320.00),(4,1,4,'2026-10-14','21:15:00',180.00),(5,1,1,'2026-10-15','11:00:00',250.00),(6,1,2,'2026-10-16','14:30:00',320.00),(7,1,3,'2026-10-17','18:00:00',180.00),(8,2,2,'2026-10-11','11:00:00',250.00),(9,2,3,'2026-10-12','14:30:00',320.00),(10,2,4,'2026-10-13','18:00:00',180.00),(11,2,1,'2026-10-14','21:15:00',250.00),(12,2,2,'2026-10-15','11:00:00',320.00),(13,2,3,'2026-10-16','14:30:00',180.00),(14,2,4,'2026-10-17','18:00:00',250.00),(15,3,3,'2026-10-11','14:30:00',320.00),(16,3,4,'2026-10-12','18:00:00',180.00),(17,3,1,'2026-10-13','21:15:00',250.00),(18,3,2,'2026-10-14','11:00:00',320.00),(19,3,3,'2026-10-15','14:30:00',180.00),(20,3,4,'2026-10-16','18:00:00',250.00),(21,3,1,'2026-10-17','21:15:00',320.00),(22,4,4,'2026-10-11','14:30:00',180.00),(23,4,1,'2026-10-12','18:00:00',250.00),(24,4,2,'2026-10-13','21:15:00',320.00),(25,4,3,'2026-10-14','11:00:00',180.00),(26,4,4,'2026-10-15','14:30:00',250.00),(27,4,1,'2026-10-16','18:00:00',320.00),(28,4,2,'2026-10-17','21:15:00',180.00),(29,5,1,'2026-10-11','18:00:00',250.00),(30,5,2,'2026-10-12','21:15:00',320.00),(31,5,3,'2026-10-13','11:00:00',180.00),(32,5,4,'2026-10-14','14:30:00',250.00),(33,5,1,'2026-10-15','18:00:00',320.00),(34,5,2,'2026-10-16','21:15:00',180.00),(35,5,3,'2026-10-17','11:00:00',250.00),(36,6,2,'2026-10-11','18:00:00',320.00),(37,6,3,'2026-10-12','21:15:00',180.00),(38,6,4,'2026-10-13','11:00:00',250.00),(39,6,1,'2026-10-14','14:30:00',320.00),(40,6,2,'2026-10-15','18:00:00',180.00),(41,6,3,'2026-10-16','21:15:00',250.00),(42,6,4,'2026-10-17','11:00:00',320.00),(43,7,3,'2026-10-11','21:15:00',180.00),(44,7,4,'2026-10-12','11:00:00',250.00),(45,7,1,'2026-10-13','14:30:00',320.00),(46,7,2,'2026-10-14','18:00:00',180.00),(47,7,3,'2026-10-15','21:15:00',250.00),(48,7,4,'2026-10-16','11:00:00',320.00),(49,7,1,'2026-10-17','14:30:00',180.00),(50,8,4,'2026-10-11','21:15:00',250.00),(51,8,1,'2026-10-12','11:00:00',320.00),(52,8,2,'2026-10-13','14:30:00',180.00),(53,8,3,'2026-10-14','18:00:00',250.00),(54,8,4,'2026-10-15','21:15:00',320.00),(55,8,1,'2026-10-16','11:00:00',180.00),(56,8,2,'2026-10-17','14:30:00',250.00);
/*!40000 ALTER TABLE `shows` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `theaters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `theaters` (
  `theater_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `city` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`theater_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `theaters` WRITE;
/*!40000 ALTER TABLE `theaters` DISABLE KEYS */;
INSERT INTO `theaters` VALUES (1,'Aurora Grand','Pune','Lane 7, Koregaon Park'),(2,'Nova Multiplex','Pune','Baner Road');
/*!40000 ALTER TABLE `theaters` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` char(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Admin','admin@cinebook.com','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','9000000001','ADMIN','2026-10-11 13:24:29'),(2,'Demo User','user@cinebook.com','e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446','9000000002','USER','2026-10-11 13:24:29'),(3,'Guest Buyer','guest@cinebook.com','e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446','9000000003','USER','2026-10-11 13:24:29');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

