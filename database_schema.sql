-- Создание базы данных для ювелирной мастерской
CREATE DATABASE IF NOT EXISTS jewelry_workshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jewelry_workshop;

-- Таблица ролей пользователей
CREATE TABLE IF NOT EXISTS roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Таблица клиентов
CREATE TABLE IF NOT EXISTS clients (
    client_id INT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    total_purchases DECIMAL(10,2) DEFAULT 0.00,
    discount INT DEFAULT 0
);

-- Таблица материалов
CREATE TABLE IF NOT EXISTS materials (
    material_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    cost_per_gram DECIMAL(10,2) NOT NULL
);

-- Таблица типов изделий
CREATE TABLE IF NOT EXISTS product_types (
    type_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    labor_cost DECIMAL(10,2) NOT NULL
);

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT NOT NULL,
    product_type_id INT NOT NULL,
    order_date DATE NOT NULL,
    status ENUM('IN_PROCESS', 'READY', 'COMPLETED', 'CANCELLED') DEFAULT 'IN_PROCESS',
    total_weight DECIMAL(8,2) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(client_id),
    FOREIGN KEY (product_type_id) REFERENCES product_types(type_id)
);

-- Таблица материалов в заказе
CREATE TABLE IF NOT EXISTS order_materials (
    order_id INT NOT NULL,
    material_id INT NOT NULL,
    weight DECIMAL(8,2) NOT NULL,
    PRIMARY KEY (order_id, material_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materials(material_id)
);

-- Вставка тестовых данных

-- Роли
INSERT INTO roles (role_name) VALUES 
('ADMIN'),
('MANAGER'),
('MASTER');

-- Пользователи (пароль: admin123)
INSERT INTO users (username, password_hash, role_id) VALUES 
('admin', 'sha256_hash_here', 1),
('manager', 'sha256_hash_here', 2),
('master', 'sha256_hash_here', 3);

-- Клиенты
INSERT INTO clients (phone, full_name, email, total_purchases, discount) VALUES 
('+7(999)123-45-67', 'Иванов Иван Иванович', 'ivanov@mail.ru', 50000.00, 5),
('+7(999)234-56-78', 'Петрова Анна Сергеевна', 'petrova@mail.ru', 150000.00, 10),
('+7(999)345-67-89', 'Сидоров Петр Александрович', 'sidorov@mail.ru', 25000.00, 0),
('+7(999)456-78-90', 'Козлова Мария Владимировна', 'kozlova@mail.ru', 75000.00, 5),
('+7(999)567-89-01', 'Николаев Дмитрий Петрович', 'nikolaev@mail.ru', 200000.00, 15);

-- Материалы
INSERT INTO materials (name, cost_per_gram) VALUES 
('Золото 585 пробы', 3500.00),
('Серебро 925 пробы', 45.00),
('Платина 950 пробы', 2500.00),
('Палладий 850 пробы', 1800.00),
('Бронза', 2.50);

-- Типы изделий
INSERT INTO product_types (name, labor_cost) VALUES 
('Кольцо', 5000.00),
('Серьги', 3000.00),
('Кулон', 4000.00),
('Браслет', 8000.00),
('Цепочка', 2000.00);

-- Заказы
INSERT INTO orders (client_id, product_type_id, order_date, status, total_weight, price) VALUES 
(1, 1, '2024-01-15', 'COMPLETED', 5.2, 23200.00),
(2, 2, '2024-01-20', 'READY', 3.8, 17100.00),
(3, 3, '2024-01-25', 'IN_PROCESS', 4.5, 20250.00),
(4, 4, '2024-02-01', 'IN_PROCESS', 12.0, 54000.00),
(5, 5, '2024-02-05', 'COMPLETED', 8.0, 28000.00);

-- Материалы в заказах
INSERT INTO order_materials (order_id, material_id, weight) VALUES 
(1, 1, 5.2),
(2, 1, 3.8),
(3, 2, 4.5),
(4, 1, 12.0),
(5, 2, 8.0);

-- Создание индексов для оптимизации
CREATE INDEX idx_clients_phone ON clients(phone);
CREATE INDEX idx_orders_client ON orders(client_id);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_orders_status ON orders(status); 