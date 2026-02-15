-- =====================================================
-- SCRIPT DE BASE DE DATOS - PRUEBA TÉCNICA MICROSERVICIOS
-- =====================================================
-- Autor: GVIVAS
-- Descripción: Script para crear bases de datos, tablas y datos iniciales
-- para los microservicios de Banking (Customer y Account)
-- =====================================================

-- =====================================================
-- CUSTOMER DATABASE
-- =====================================================

-- Crear base de datos para Customer Service
DROP DATABASE IF EXISTS customer_db;
CREATE DATABASE customer_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

-- Conectarse a customer_db
\c customer_db;

-- Crear tabla de clientes
CREATE TABLE IF NOT EXISTS customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    identification VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status BOOLEAN NOT NULL DEFAULT TRUE
);

-- Índices para optimizar búsquedas
CREATE INDEX idx_customer_identification ON customer(identification);
CREATE INDEX idx_customer_status ON customer(status);

-- Comentarios en la tabla
COMMENT ON TABLE customer IS 'Tabla de clientes que hereda los atributos de persona';
COMMENT ON COLUMN customer.name IS 'Nombre completo del cliente';
COMMENT ON COLUMN customer.gender IS 'Género del cliente (MALE, FEMALE, OTHER)';
COMMENT ON COLUMN customer.identification IS 'Número de identificación único';
COMMENT ON COLUMN customer.address IS 'Dirección del cliente';
COMMENT ON COLUMN customer.phone IS 'Teléfono de contacto';
COMMENT ON COLUMN customer.password IS 'Contraseña del cliente (debe estar encriptada)';
COMMENT ON COLUMN customer.status IS 'Estado del cliente (activo/inactivo)';

-- =====================================================
-- DATOS DE PRUEBA - CUSTOMER SERVICE
-- =====================================================

-- Insertar clientes de prueba (Caso de Uso 1)
INSERT INTO customer (name, gender, identification, address, phone, password, status) VALUES
('Jose Lema', 'MALE', '1234567890', 'Otavalo sn y principal', '098254785', '1234', TRUE),
('Marianela Montalvo', 'FEMALE', '0987654321', 'Amazonas y NNUU', '097548965', '5678', TRUE),
('Juan Osorio', 'MALE', '1122334455', '13 junio y Equinoccial', '098874587', '1245', TRUE);


-- =====================================================
-- ACCOUNT DATABASE
-- =====================================================

-- Crear base de datos para Account Service
DROP DATABASE IF EXISTS account_db;
CREATE DATABASE account_db
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE = template0;

-- Conectarse a account_db
\c account_db;

-- Crear tabla de cuentas
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    initial_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    customer_id BIGINT NOT NULL,
    CONSTRAINT chk_initial_balance CHECK (initial_balance >= 0),
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CHECKING'))
);

-- Crear tabla de movimientos
CREATE TABLE IF NOT EXISTS movement (
    id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    movement_type VARCHAR(20) NOT NULL,
    value DECIMAL(15,2) NOT NULL,
    balance DECIMAL(15,2) NOT NULL,
    account_id BIGINT NOT NULL,
    CONSTRAINT fk_movement_account FOREIGN KEY (account_id) 
        REFERENCES account(id) ON DELETE CASCADE,
    CONSTRAINT chk_movement_type CHECK (movement_type IN ('CREDIT', 'DEBIT')),
    CONSTRAINT chk_value CHECK (value > 0)
);

-- Índices para optimizar búsquedas
CREATE INDEX idx_account_number ON account(account_number);
CREATE INDEX idx_account_customer_id ON account(customer_id);
CREATE INDEX idx_account_status ON account(status);
CREATE INDEX idx_movement_account_id ON movement(account_id);
CREATE INDEX idx_movement_date ON movement(date);
CREATE INDEX idx_movement_account_date ON movement(account_id, date);

-- Comentarios en las tablas
COMMENT ON TABLE account IS 'Tabla de cuentas bancarias';
COMMENT ON COLUMN account.account_number IS 'Número único de cuenta';
COMMENT ON COLUMN account.account_type IS 'Tipo de cuenta (SAVINGS=Ahorros, CHECKING=Corriente)';
COMMENT ON COLUMN account.initial_balance IS 'Saldo inicial de la cuenta';
COMMENT ON COLUMN account.current_balance IS 'Saldo actual de la cuenta';
COMMENT ON COLUMN account.status IS 'Estado de la cuenta (activa/inactiva)';
COMMENT ON COLUMN account.customer_id IS 'ID del cliente propietario (referencia a customer_db)';

COMMENT ON TABLE movement IS 'Tabla de movimientos bancarios';
COMMENT ON COLUMN movement.date IS 'Fecha y hora del movimiento';
COMMENT ON COLUMN movement.movement_type IS 'Tipo de movimiento (CREDIT=Depósito, DEBIT=Retiro)';
COMMENT ON COLUMN movement.value IS 'Valor del movimiento (siempre positivo)';
COMMENT ON COLUMN movement.balance IS 'Saldo resultante después del movimiento';
COMMENT ON COLUMN movement.account_id IS 'ID de la cuenta asociada';

-- =====================================================
-- DATOS DE PRUEBA - ACCOUNT SERVICE
-- =====================================================

-- Insertar cuentas de prueba (Caso de Uso 2 y 3)
-- Nota: Los customer_id deben corresponder a los IDs generados en customer_db
-- Para este ejemplo, asumimos que Jose Lema=1, Marianela Montalvo=2, Juan Osorio=3

INSERT INTO account (account_number, account_type, initial_balance, current_balance, status, customer_id) VALUES
('478758', 'SAVINGS', 2000.00, 2000.00, TRUE, 1),    -- Jose Lema
('225487', 'CHECKING', 100.00, 100.00, TRUE, 2),     -- Marianela Montalvo
('495878', 'SAVINGS', 0.00, 0.00, TRUE, 3),          -- Juan Osorio
('496825', 'SAVINGS', 540.00, 540.00, TRUE, 2),      -- Marianela Montalvo
('585545', 'CHECKING', 1000.00, 1000.00, TRUE, 1);   -- Jose Lema (Caso 3)
-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
