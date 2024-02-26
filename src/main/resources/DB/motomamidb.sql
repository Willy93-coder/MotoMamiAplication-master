CREATE DATABASE IF NOT EXISTS motomamidb;
USE motomamidb;

CREATE TABLE `mm_address` (
  `id` int NOT NULL AUTO_INCREMENT,
  `street` varchar(100) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `st_number` int DEFAULT NULL,
  `city` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `post_code` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE `mm_customer` (
  `dni` varchar(10) COLLATE utf8mb4_spanish_ci NOT NULL,
  `customer_name` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `surname1` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `surname2` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `gender` varchar(10) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `address_id` int DEFAULT NULL,
  PRIMARY KEY (`dni`),
  KEY `address_id` (`address_id`),
  CONSTRAINT `mm_customer_ibfk_1` FOREIGN KEY (`address_id`) REFERENCES `mm_address` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE `mm_intcustomers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `idProv` varchar(25) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `idExternal` varchar(10) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `contJson` longblob,
  `creationDate` datetime DEFAULT NULL,
  `lastUpdate` datetime DEFAULT NULL,
  `createdBy` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `updatedBy` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `statusProcess` varchar(1) COLLATE utf8mb4_spanish_ci DEFAULT 'N',
  `msgError` longtext COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `codeError` varchar(10) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `operation` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT 'new',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE `mm_intparts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `idProv` varchar(25) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `idExternal` varchar(25) DEFAULT NULL,
  `contJson` blob,
  `creationDate` datetime DEFAULT NULL,
  `lastUpdate` datetime DEFAULT NULL,
  `createdBy` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `updatedBy` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `statusProcess` varchar(1) COLLATE utf8mb4_spanish_ci DEFAULT 'N',
  `msgError` longtext COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `codeError` varchar(10) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `operation` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT 'new',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE `mm_intvehicles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `idProv` varchar(25) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `idExternal` varchar(10) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `contJson` blob,
  `creationDate` datetime DEFAULT NULL,
  `lastUpdate` datetime DEFAULT NULL,
  `createdBy` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `updatedBy` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `statusProcess` varchar(1) COLLATE utf8mb4_spanish_ci DEFAULT 'N',
  `msgError` longtext COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `codeError` varchar(10) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `operation` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT 'new',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE `mm_vehicle` (
  `id` int NOT NULL AUTO_INCREMENT,
  `customerDni` varchar(20) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `idVehicleExternal` int DEFAULT NULL,
  `numberPlate` varchar(20) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `vehicleType` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `brand` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `model` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `color` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `serialNumber` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `customerDni` (`customerDni`),
  CONSTRAINT `mm_vehicle_ibfk_1` FOREIGN KEY (`customerDni`) REFERENCES `mm_customer` (`dni`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE `mm_part` (
  `id` int NOT NULL AUTO_INCREMENT,
  `datePartExternal` date DEFAULT NULL,
  `descriptionPartExternal` longtext COLLATE utf8mb4_spanish_ci,
  `codeDamageExternal` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `codeDamage` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `identityCode` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  `idExternal` varchar(50) COLLATE utf8mb4_spanish_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `identityCode` (`identityCode`),
  CONSTRAINT `mm_part_ibfk_2` FOREIGN KEY (`identityCode`) REFERENCES `mm_customer` (`dni`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE `mm_invoice` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `invoice_num` BIGINT,
    `invoice_date` date,
    `provider_name` varchar(100),
    `people_quantity` int,
    `unit_price` DECIMAL(4, 2),
    `tax` int default 21,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

