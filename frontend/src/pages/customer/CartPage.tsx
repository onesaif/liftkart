import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../../context/CartContext';
import axiosInstance from '../../config/axios';
import toast from 'react-hot-toast';
import { FiTrash2, FiShoppingBag } from 'react-icons/fi';

const CartPage: React.FC = () => {
    const { cart, fetchCart } = useCart();
    const navigate = useNavigate();

    useEffect(() => { fetchCart(); }, []);

    const updateQuantity = async (itemId: string, quantity: number) => {
        try {
            await axiosInstance.put(`/api/cart/items/${itemId}`, { quantity });
            await fetchCart();
        } catch { toast.error('Failed to update quantity'); }
    };

    const removeItem = async (itemId: string) => {
        try {
            await axiosInstance.delete(`/api/cart/items/${itemId}`);
            await fetchCart();
            toast.success('Item removed');
        } catch { toast.error('Failed to remove item'); }
    };

    if (!cart || cart.items.length === 0) return (
        <div className="text-center py-20">
            <FiShoppingBag size={64} className="mx-auto text-gray-300 mb-4" />
            <h2 className="text-xl font-semibold text-gray-700 mb-2">Your cart is empty</h2>
            <p className="text-gray-500 mb-6">Add some products to get started</p>
            <Link to="/" className="bg-primary-500 text-white px-6 py-3
                              rounded-lg hover:bg-primary-600 transition">
                Continue Shopping
            </Link>
        </div>
    );

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-4">
                <h1 className="text-2xl font-bold text-gray-900 mb-4">
                    My Cart ({cart.totalItems} items)
                </h1>
                {cart.items.map(item => (
                    <div key={item.id}
                         className="bg-white rounded-xl border border-gray-100 p-4 flex gap-4">
                        <img
                            src={item.imageUrl || ''}
                            alt={item.productName}
                            className="w-20 h-20 object-cover rounded-lg bg-gray-100"
                            onError={(e) => {
                                (e.target as HTMLImageElement).style.display = 'none';
                            }}
                        />
                        <div className="flex-1">
                            <h3 className="font-medium text-gray-800 mb-1">{item.productName}</h3>
                            <p className="text-primary-500 font-semibold">
                                ₹{item.priceSnapshot.toLocaleString('en-IN')}
                            </p>
                            <div className="flex items-center gap-4 mt-2">
                                <div className="flex items-center border border-gray-300 rounded-lg text-sm">
                                    <button
                                        onClick={() => updateQuantity(item.id, item.quantity - 1)}
                                        disabled={item.quantity <= 1}
                                        className="px-2 py-1 text-gray-600 disabled:opacity-30"
                                    >−</button>
                                    <span className="px-3 py-1">{item.quantity}</span>
                                    <button
                                        onClick={() => updateQuantity(item.id, item.quantity + 1)}
                                        className="px-2 py-1 text-gray-600"
                                    >+</button>
                                </div>
                                <button onClick={() => removeItem(item.id)}
                                        className="text-red-400 hover:text-red-600">
                                    <FiTrash2 size={16} />
                                </button>
                            </div>
                        </div>
                        <div className="text-right">
                            <p className="font-bold text-gray-900">
                                ₹{item.totalPrice.toLocaleString('en-IN')}
                            </p>
                        </div>
                    </div>
                ))}
            </div>

            <div className="lg:col-span-1">
                <div className="bg-white rounded-xl border border-gray-100 p-6 sticky top-24">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>
                    <div className="space-y-3 mb-4">
                        <div className="flex justify-between text-sm text-gray-600">
                            <span>Subtotal ({cart.totalItems} items)</span>
                            <span>₹{cart.subtotal.toLocaleString('en-IN')}</span>
                        </div>
                        <div className="flex justify-between text-sm text-gray-600">
                            <span>Delivery</span>
                            <span className="text-green-500">FREE</span>
                        </div>
                    </div>
                    <div className="border-t border-gray-200 pt-4 mb-6">
                        <div className="flex justify-between font-bold text-gray-900">
                            <span>Total</span>
                            <span>₹{cart.subtotal.toLocaleString('en-IN')}</span>
                        </div>
                    </div>
                    <button
                        onClick={() => navigate('/checkout')}
                        className="w-full bg-primary-500 text-white py-3 rounded-lg
                       font-medium hover:bg-primary-600 transition"
                    >
                        Proceed to Checkout
                    </button>
                    <Link to="/" className="block text-center text-sm text-primary-500 mt-4">
                        Continue Shopping
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default CartPage;