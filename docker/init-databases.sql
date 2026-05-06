CREATE DATABASE liftkart_auth_db;
CREATE DATABASE liftkart_product_db;
CREATE DATABASE liftkart_cart_db;
CREATE DATABASE liftkart_order_db;
CREATE DATABASE liftkart_notification_db;
CREATE DATABASE liftkart_analytics_db;

GRANT ALL PRIVILEGES ON DATABASE liftkart_auth_db         TO liftkart;
GRANT ALL PRIVILEGES ON DATABASE liftkart_product_db      TO liftkart;
GRANT ALL PRIVILEGES ON DATABASE liftkart_cart_db         TO liftkart;
GRANT ALL PRIVILEGES ON DATABASE liftkart_order_db        TO liftkart;
GRANT ALL PRIVILEGES ON DATABASE liftkart_notification_db TO liftkart;
GRANT ALL PRIVILEGES ON DATABASE liftkart_analytics_db    TO liftkart;
