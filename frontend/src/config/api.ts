const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
    LOGIN:            `${API_BASE_URL}/api/auth/login`,
    REGISTER:         `${API_BASE_URL}/api/auth/register`,
    REGISTER_VENDOR:  `${API_BASE_URL}/api/auth/register/vendor`,
    LOGOUT:           `${API_BASE_URL}/api/auth/logout`,
    REFRESH:          `${API_BASE_URL}/api/auth/refresh`,
    ME:               `${API_BASE_URL}/api/auth/me`,
    PRODUCTS:         `${API_BASE_URL}/api/products`,
    PRODUCT_SEARCH:   `${API_BASE_URL}/api/products/search`,
    CATEGORIES:       `${API_BASE_URL}/api/categories`,
    CART:             `${API_BASE_URL}/api/cart`,
    CART_ITEMS:       `${API_BASE_URL}/api/cart/items`,
    ORDERS:           `${API_BASE_URL}/api/orders`,
    NOTIFICATIONS:    `${API_BASE_URL}/api/notifications`,
    ANALYTICS_VENDOR: `${API_BASE_URL}/api/analytics/vendor`,
    ANALYTICS_ADMIN:  `${API_BASE_URL}/api/analytics/platform`,
};

export default API_BASE_URL;