CREATE DATABASE IF NOT EXISTS fleetlog_db;
USE fleetlog_db;

DROP TABLE IF EXISTS vehicles;

CREATE TABLE vehicles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    plate VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    color VARCHAR(30) DEFAULT 'Blanco',
    mileage INT DEFAULT 0, -- Kilometraje
    status VARCHAR(20) DEFAULT 'Activo', -- Activo, Mantenimiento, Inactivo
    image_base64 LONGTEXT,
    is_pickup TINYINT(1) DEFAULT 0
);