-- Seed Users (passwords: admin123, user123 - BCrypt hashed)
INSERT INTO users (id, email, password_hash, name, is_admin, created_at) VALUES
    ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'admin@raileasy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', true, NOW()),
    ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', 'user@raileasy.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test User', false, NOW());

-- Seed Trains
INSERT INTO train (id, train_number, train_name, seats_per_class, created_at) VALUES
    ('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', '12163', 'Chennai Express', 64, NOW()),
    ('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', '22691', 'Rajdhani Express', 64, NOW()),
    ('e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', '12951', 'Mumbai Rajdhani', 64, NOW());

-- Seed Schedules (using future dates)
INSERT INTO schedule (id, train_id, from_station, to_station, departure_time, arrival_time, journey_date, fare_sleeper, fare_ac3, fare_ac2, created_at) VALUES
    ('f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'Chennai Central', 'Mumbai CSMT', '2025-10-21 06:00:00', '2025-10-22 05:30:00', '2025-10-21', 450.00, 1200.00, 1800.00, NOW()),
    ('17171717-1717-1717-1717-171717171717', 'd4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', 'Chennai Central', 'Mumbai CSMT', '2025-10-21 08:00:00', '2025-10-22 07:45:00', '2025-10-21', 500.00, 1350.00, 2000.00, NOW()),
    ('28282828-2828-2828-2828-282828282828', 'e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', 'Delhi Junction', 'Kolkata Howrah', '2025-10-22 16:00:00', '2025-10-23 10:30:00', '2025-10-22', 400.00, 1100.00, 1650.00, NOW()),
    ('39393939-3939-3939-3939-393939393939', 'c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', 'Delhi Junction', 'Kolkata Howrah', '2025-10-22 20:00:00', '2025-10-23 14:00:00', '2025-10-22', 420.00, 1150.00, 1700.00, NOW());
