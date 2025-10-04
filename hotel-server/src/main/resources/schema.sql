-- Удаляем старые таблицы если существуют
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS staff;

-- Таблица номеров
CREATE TABLE IF NOT EXISTS rooms (
    room_number INTEGER PRIMARY KEY,
    room_type TEXT NOT NULL,
    status TEXT DEFAULT 'free', -- free, occupied
    client_passport TEXT,
    check_in_date TEXT,
    check_out_date TEXT,
    FOREIGN KEY (client_passport) REFERENCES clients(passport_number)
);

-- Таблица клиентов (обновленная)
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
    status TEXT DEFAULT 'active',
    FOREIGN KEY (room_number) REFERENCES rooms(room_number)
);

-- Таблица сотрудников (без изменений)
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

-- Заполняем таблицу номеров
INSERT INTO rooms (room_number, room_type) VALUES
-- Эконом (10 номеров)
(101, 'Эконом'), (102, 'Эконом'), (103, 'Эконом'), (104, 'Эконом'), (105, 'Эконом'),
(106, 'Эконом'), (107, 'Эконом'), (108, 'Эконом'), (109, 'Эконом'), (110, 'Эконом'),

-- Стандарт (20 номеров)
(201, 'Стандарт'), (202, 'Стандарт'), (203, 'Стандарт'), (204, 'Стандарт'), (205, 'Стандарт'),
(206, 'Стандарт'), (207, 'Стандарт'), (208, 'Стандарт'), (209, 'Стандарт'), (210, 'Стандарт'),
(211, 'Стандарт'), (212, 'Стандарт'), (213, 'Стандарт'), (214, 'Стандарт'), (215, 'Стандарт'),
(216, 'Стандарт'), (217, 'Стандарт'), (218, 'Стандарт'), (219, 'Стандарт'), (220, 'Стандарт'),

-- Бизнес (7 номеров)
(301, 'Бизнес'), (302, 'Бизнес'), (303, 'Бизнес'), (304, 'Бизнес'),
(305, 'Бизнес'), (306, 'Бизнес'), (307, 'Бизнес'),

-- Люкс (3 номера)
(401, 'Люкс'), (402, 'Люкс'), (403, 'Люкс');