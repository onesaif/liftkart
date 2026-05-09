import React, { createContext, useContext, useState } from 'react';
import { Cart } from '../types';
import axiosInstance from '../config/axios';

interface CartContextType {
    cart: Cart | null;
    cartCount: number;
    fetchCart: () => Promise<void>;
    addToCart: (productId: string, productName: string,
                imageUrl: string, quantity: number,
                price: number) => Promise<void>;
    clearCartState: () => void;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({
                                                                          children,
                                                                      }) => {
    const [cart, setCart] = useState<Cart | null>(null);

    const fetchCart = async () => {
        try {
            const res = await axiosInstance.get('/api/cart');
            setCart(res.data.data);
        } catch {
            setCart(null);
        }
    };

    const addToCart = async (
        productId: string, productName: string,
        imageUrl: string, quantity: number, price: number
    ) => {
        console.log('addToCart called', {productId, productName,price})
        await axiosInstance.post('/api/cart/items', {
            productId, productName, imageUrl, quantity, price,
        });
        await fetchCart();
    };

    const clearCartState = () => setCart(null);

    return (
        <CartContext.Provider
            value={{
                cart,
                cartCount: cart?.totalItems ?? 0,
                fetchCart,
                addToCart,
                clearCartState,
            }}
        >
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) throw new Error('useCart must be used within CartProvider');
    return context;
};