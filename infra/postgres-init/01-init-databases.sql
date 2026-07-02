-- POSTGRES_DB already creates "productdb" on first init; this adds "orderdb"
-- so order-service and product-service each get their own database/WAL.
CREATE DATABASE orderdb;
