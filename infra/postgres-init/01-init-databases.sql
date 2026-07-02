-- POSTGRES_DB already creates "productdb" on first init; this adds "orderdb",
-- "paymentdb" and "notificationdb" so each service gets its own database.
-- notificationdb has no Debezium connector (notification-service is consumer-only,
-- it never publishes outbox events) but it still needs a plain JPA-backed database.
CREATE DATABASE orderdb;
CREATE DATABASE paymentdb;
CREATE DATABASE notificationdb;
