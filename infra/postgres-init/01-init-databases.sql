-- POSTGRES_DB already creates "productdb" on first init; this adds "paymentdb"
-- and "notificationdb" so each service gets its own database.
-- order-service uses MySQL instead (see the "mysql" service in docker-compose.yml),
-- not Postgres.
-- notificationdb has no Debezium connector (notification-service is consumer-only,
-- it never publishes outbox events) but it still needs a plain JPA-backed database.
CREATE DATABASE paymentdb;
CREATE DATABASE notificationdb;
