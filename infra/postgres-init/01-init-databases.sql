-- POSTGRES_DB already creates "productdb" on first init; this adds "orderdb" and
-- "paymentdb" so order-service, product-service and payment-service each get their
-- own database/WAL.
CREATE DATABASE orderdb;
CREATE DATABASE paymentdb;
