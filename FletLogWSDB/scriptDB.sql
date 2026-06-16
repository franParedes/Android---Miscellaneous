DROP DATABASE IF EXISTS fleetlog_db;
CREATE DATABASE fleetlog_db;

USE fleetlog_db;

-- -- Tabla de Vehiculos
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

-- Tabla de Conductores
CREATE TABLE IF NOT EXISTS drivers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20)
);

-- Tabla de Mantenimientos (Relacionada a los vehículos)
CREATE TABLE IF NOT EXISTS maintenance_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    description TEXT NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    service_date DATE NOT NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

-- Tabla de Registro de Combustible (Relacionada a los vehículos)
CREATE TABLE IF NOT EXISTS fuel_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    gallons DECIMAL(10, 2) NOT NULL,
    total_cost DECIMAL(10, 2) NOT NULL,
    date_filled DATE NOT NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);