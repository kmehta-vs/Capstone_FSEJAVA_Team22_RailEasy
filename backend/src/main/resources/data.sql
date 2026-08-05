-- ---------------------------------------------------------------------------
-- RailEasy seed data (H2 dialect)
-- Runs on startup (spring.sql.init.mode=always) after Hibernate creates schema
-- (spring.jpa.defer-datasource-initialization=true).
--
-- NOTE: The admin user is seeded in Sprint 2, once the `users` table (User
-- entity) exists. Sprint 1 seeds trains + schedules only.
-- ---------------------------------------------------------------------------

-- Trains -------------------------------------------------------------------
INSERT INTO train (id, train_number, train_name, seats_per_class, created_at) VALUES
 (RANDOM_UUID(), '12163', 'Chennai Express',  64, CURRENT_TIMESTAMP),
 (RANDOM_UUID(), '22691', 'Rajdhani Express', 64, CURRENT_TIMESTAMP);

-- Schedules (Chennai Central -> Mumbai CSMT on 2025-10-21) ------------------
INSERT INTO schedule (id, train_id, from_station, to_station, departure_time, arrival_time, journey_date, fare_sleeper, fare_ac3, fare_ac2, created_at)
SELECT RANDOM_UUID(), t.id, 'Chennai Central', 'Mumbai CSMT',
       TIMESTAMP '2025-10-21 06:00:00', TIMESTAMP '2025-10-22 05:30:00', DATE '2025-10-21',
       450.00, 1200.00, 1800.00, CURRENT_TIMESTAMP
FROM train t WHERE t.train_number = '12163';

INSERT INTO schedule (id, train_id, from_station, to_station, departure_time, arrival_time, journey_date, fare_sleeper, fare_ac3, fare_ac2, created_at)
SELECT RANDOM_UUID(), t.id, 'Chennai Central', 'Mumbai CSMT',
       TIMESTAMP '2025-10-21 08:00:00', TIMESTAMP '2025-10-22 07:45:00', DATE '2025-10-21',
       500.00, 1350.00, 2000.00, CURRENT_TIMESTAMP
FROM train t WHERE t.train_number = '22691';
