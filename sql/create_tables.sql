-- PostgreSQL schema for FoodApp

CREATE TABLE IF NOT EXISTS admins (
    id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    phone TEXT,
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS delivery_partners (
    id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    name TEXT NOT NULL,
    phone TEXT,
    vehicle_type TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price NUMERIC NOT NULL,
    category TEXT,
    cuisine_type TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS carts (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE,
    menu_item_id BIGINT,
    item_name TEXT,
    price NUMERIC,
    quantity INTEGER
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    customer_name TEXT,
    items TEXT,
    total_amount NUMERIC,
    discount_amount NUMERIC,
    final_amount NUMERIC,
    payment_mode TEXT,
    delivery_partner_id BIGINT REFERENCES delivery_partners(id),
    delivery_partner_name TEXT,
    status TEXT,
    ordered_at TEXT
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id BIGINT,
    item_name TEXT,
    price NUMERIC,
    quantity INTEGER
);
