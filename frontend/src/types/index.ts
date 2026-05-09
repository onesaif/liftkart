export interface User {
    id: string;
    email: string;
    fullName: string;
    phone?: string;
    role: 'CUSTOMER' | 'VENDOR' | 'ADMIN';
    isActive: boolean;
    isEmailVerified: boolean;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    user: User;
}

export interface Product {
    id: string;
    vendorId: string;
    categoryName: string;
    name: string;
    slug: string;
    description: string;
    price: number;
    comparePrice?: number;
    stockQuantity: number;
    sku?: string;
    status: string;
    averageRating: number;
    reviewCount: number;
    imageUrls: string[];
    createdAt: string;
}

export interface Category {
    id: string;
    name: string;
    slug: string;
    parentId?: string;
    iconUrl?: string;
    displayOrder: number;
    children: Category[];
}

export interface CartItem {
    id: string;
    productId: string;
    productName: string;
    imageUrl?: string;
    quantity: number;
    priceSnapshot: number;
    totalPrice: number;
    savedForLater: boolean;
}

export interface Cart {
    id: string;
    customerId: string;
    items: CartItem[];
    savedForLater: CartItem[];
    totalItems: number;
    subtotal: number;
}

export interface Order {
    id: string;
    customerId: string;
    addressSnapshot: Record<string, any>;
    status: string;
    paymentMethod: string;
    paymentStatus: string;
    subtotal: number;
    deliveryCharge: number;
    discountAmount: number;
    totalAmount: number;
    notes?: string;
    items: OrderItem[];
    statusHistory: OrderStatusHistory[];
    createdAt: string;
}

export interface OrderItem {
    id: string;
    productId: string;
    vendorId: string;
    productName: string;
    productImageUrl?: string;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
    status: string;
}

export interface OrderStatusHistory {
    oldStatus?: string;
    newStatus: string;
    changedByRole: string;
    comment?: string;
    changedAt: string;
}

export interface Address {
    id: string;
    fullName: string;
    phone: string;
    line1: string;
    line2?: string;
    city: string;
    state: string;
    pincode: string;
    isDefault: boolean;
}

export interface Notification {
    id: string;
    userId: string;
    title: string;
    message: string;
    type: string;
    referenceId?: string;
    referenceType?: string;
    isRead: boolean;
    readAt?: string;
    createdAt: string;
}

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
}

export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    size: number;
    number: number;
}