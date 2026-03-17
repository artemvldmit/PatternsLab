-- Initialization SQL for shopdb
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    user_id INT REFERENCES users(id),
    product_id INT REFERENCES products(id),
    PRIMARY KEY (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    total DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    order_id INT REFERENCES orders(id),
    product_id INT REFERENCES products(id),
    quantity INT,
    decorators TEXT
);

CREATE TABLE IF NOT EXISTS cart_snapshots (
    user_id INT PRIMARY KEY,
    snapshot_data TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS admin_commands (
    id SERIAL PRIMARY KEY,
    command_data TEXT NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (email, role) VALUES ('demo@shop.local', 'USER') ON CONFLICT DO NOTHING;
