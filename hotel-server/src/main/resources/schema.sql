-- Удаляем старые таблицы если существуют
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS staff;

-- Таблица номеров (БЕЗ foreign key на clients)
CREATE TABLE IF NOT EXISTS rooms (
    room_number INTEGER PRIMARY KEY,
    room_type TEXT NOT NULL,
    status TEXT DEFAULT 'free',
    client_passport TEXT,
    check_in_date TEXT,
    check_out_date TEXT
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
    room_type TEXT NOT NULL,
    status TEXT DEFAULT 'active'
);

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