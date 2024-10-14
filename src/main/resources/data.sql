-- Customers
INSERT INTO customers (email, username, password)
VALUES ('john.doe@example.com', 'johndoe', '$2a$10$rDkPvvAFV8kqwvKJzwXgbewjw3E6mMrWUV7wTWNyASYLBesAjSmrK'),
       ('jane.smith@example.com', 'janesmith', '$2a$10$X7VYoYdHDDd3bNJfidmO8O8X3.5NQCR6RJi9GczB4ZW3QYP5kFYDu');

-- Assets
INSERT INTO asset (customer_id, asset_name, size, usable_size)
SELECT id, 'TRY', 10000.00, 10000.00
FROM customers
WHERE username = 'johndoe';

INSERT INTO asset (customer_id, asset_name, size, usable_size)
SELECT id, 'AAPL', 100.00, 100.00
FROM customers
WHERE username = 'johndoe';

INSERT INTO orders (customer_id, asset_name, order_side, size, price, status, create_date)
SELECT id, 'AAPL', 'BUY', 10.00, 150.00, 'PENDING', CURRENT_TIMESTAMP
FROM customers
WHERE username = 'johndoe';

INSERT INTO asset (customer_id, asset_name, size, usable_size)
SELECT id, 'AAPL', 50.00, 50.00
FROM customers
WHERE username = 'janesmith';

INSERT INTO asset (customer_id, asset_name, size, usable_size)
SELECT id, 'TRY', 15000.00, 15000.00
FROM customers
WHERE username = 'janesmith';

INSERT INTO asset (customer_id, asset_name, size, usable_size)
SELECT id, 'GOOGL', 50.00, 50.00
FROM customers
WHERE username = 'janesmith';

-- Orders
INSERT INTO orders (customer_id, asset_name, order_side, size, price, status, create_date)
SELECT id, 'AAPL', 'BUY', 10.00, 150.00, 'PENDING', CURRENT_TIMESTAMP
FROM customers
WHERE username = 'johndoe';

INSERT INTO orders (customer_id, asset_name, order_side, size, price, status, create_date)
SELECT id, 'GOOGL', 'SELL', 5.00, 2500.00, 'PENDING', CURRENT_TIMESTAMP
FROM customers
WHERE username = 'janesmith';

INSERT INTO orders (customer_id, asset_name, order_side, size, price, status, create_date)
SELECT id, 'GOOGL', 'SELL', 5.00, 2500.00, 'MATCHED', CURRENT_TIMESTAMP
FROM customers
WHERE username = 'janesmith';

INSERT INTO orders (customer_id, asset_name, order_side, size, price, status, create_date)
SELECT id, 'AAPL', 'BUY', 5.00, 155.00, 'PENDING', CURRENT_TIMESTAMP
FROM customers
WHERE username = 'johndoe';

INSERT INTO orders (customer_id, asset_name, order_side, size, price, status, create_date)
SELECT id, 'AAPL', 'SELL', 7.00, 153.00, 'PENDING', CURRENT_TIMESTAMP
FROM customers
WHERE username = 'janesmith';

