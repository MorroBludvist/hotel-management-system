-- Удаляем старые таблицы если существуют (в правильном порядке из-за foreign keys)
DROP TABLE IF EXISTS booking_history;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS staff;

-- Таблица сотрудников
CREATE TABLE IF NOT EXISTS staff (
    passport_number TEXT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    position TEXT NOT NULL,
    phone_number TEXT,
    email TEXT,
    hire_date TEXT NOT NULL,
    salary REAL NOT NULL,
    department TEXT NOT NULL,
    status TEXT DEFAULT 'active'
);

-- Таблица номеров (убрана лишняя запятая!)
CREATE TABLE IF NOT EXISTS rooms (
    room_number INTEGER PRIMARY KEY,
    room_type TEXT NOT NULL,
    status TEXT DEFAULT 'free'
);

-- Таблица клиентов
CREATE TABLE IF NOT EXISTS clients (
    passport_number TEXT PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    phone_number TEXT,
    email TEXT,
    check_in_date TEXT NOT NULL,
    check_out_date TEXT NOT NULL,
    room_number INTEGER NOT NULL,
    status TEXT DEFAULT 'active',
    FOREIGN KEY (room_number) REFERENCES rooms(room_number) ON DELETE CASCADE
);

-- Таблица истории бронирований
CREATE TABLE IF NOT EXISTS booking_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number INTEGER NOT NULL,
    client_passport TEXT NOT NULL,
    check_in_date TEXT NOT NULL,
    check_out_date TEXT NOT NULL,
    total_price REAL NOT NULL,
    status TEXT DEFAULT 'completed',
    FOREIGN KEY (client_passport) REFERENCES clients(passport_number) ON DELETE CASCADE,
    FOREIGN KEY (room_number) REFERENCES rooms(room_number) ON DELETE CASCADE
);

-- Индексы для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_clients_room_number ON clients(room_number);
CREATE INDEX IF NOT EXISTS idx_clients_status ON clients(status);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_booking_history_client ON booking_history(client_passport);
CREATE INDEX IF NOT EXISTS idx_booking_history_room ON booking_history(room_number);