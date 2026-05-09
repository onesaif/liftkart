import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../config/axios';
import { Address } from '../../types';
import { useCart } from '../../context/CartContext';
import toast from 'react-hot-toast';
import { FiPlus } from 'react-icons/fi';

const CheckoutPage: React.FC = () => {
    const [addresses, setAddresses]           = useState<Address[]>([]);
    const [selectedAddress, setSelectedAddress] = useState<string>('');
    const [paymentMethod, setPaymentMethod]   = useState('UPI');
    const [showAddressForm, setShowAddressForm] = useState(false);
    const [loading, setLoading]               = useState(false);
    const [newAddress, setNewAddress]         = useState({
        fullName: '', phone: '', line1: '', line2: '',
        city: '', state: '', pincode: '', isDefault: false,
    });
    const { cart, fetchCart, clearCartState } = useCart();
    const navigate = useNavigate();

    useEffect(() => {
        fetchCart();
        fetchAddresses();
    }, []);

    const fetchAddresses = async () => {
        try {
            const res = await axiosInstance.get('/api/addresses');
            setAddresses(res.data.data);
            const def = res.data.data.find((a: Address) => a.isDefault);
            if (def) setSelectedAddress(def.id);
        } catch { /* ignore */ }
    };

    const addAddress = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await axiosInstance.post('/api/addresses', newAddress);
            await fetchAddresses();
            setShowAddressForm(false);
            toast.success('Address added');
        } catch { toast.error('Failed to add address'); }
    };

    const placeOrder = async () => {
        if (!selectedAddress) {
            toast.error('Please select a delivery address');
            return;
        }
        if (!cart || cart.items.length === 0) {
            toast.error('Your cart is empty');
            return;
        }

        setLoading(true);
        try {
            const items = cart.items.map(item => ({
                productId:   item.productId,
                productName: item.productName,
                imageUrl:    item.imageUrl,
                price:       item.priceSnapshot,
                quantity:    item.quantity,
            }));

            const res = await axiosInstance.post('/api/orders', {
                addressId:     selectedAddress,
                paymentMethod,
                items,
            });

            clearCartState();
            toast.success('Order placed successfully!');
            navigate(`/orders/${res.data.data.id}`);
        } catch (err: any) {
            toast.error(err.response?.data?.message || 'Failed to place order');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-6">
                <h1 className="text-2xl font-bold text-gray-900">Checkout</h1>

                {/* Delivery Address */}
                <div className="bg-white rounded-xl border border-gray-100 p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-lg font-semibold text-gray-800">
                            Delivery Address
                        </h2>
                        <button
                            onClick={() => setShowAddressForm(!showAddressForm)}
                            className="flex items-center gap-1 text-sm text-primary-500
                         hover:text-primary-600"
                        >
                            <FiPlus size={16} /> Add New
                        </button>
                    </div>

                    {showAddressForm && (
                        <form onSubmit={addAddress}
                              className="grid grid-cols-2 gap-3 mb-4 p-4 bg-gray-50
                             rounded-lg">
                            {[
                                ['fullName', 'Full Name'],
                                ['phone', 'Phone'],
                                ['line1', 'Address Line 1'],
                                ['line2', 'Address Line 2 (Optional)'],
                                ['city', 'City'],
                                ['state', 'State'],
                                ['pincode', 'Pincode'],
                            ].map(([field, label]) => (
                                <div key={field}
                                     className={field === 'line1' ||
                                     field === 'line2' ? 'col-span-2' : ''}>
                                    <label className="block text-xs text-gray-600 mb-1">
                                        {label}
                                    </label>
                                    <input
                                        type="text"
                                        value={(newAddress as any)[field]}
                                        onChange={e => setNewAddress({
                                            ...newAddress, [field]: e.target.value
                                        })}
                                        required={field !== 'line2'}
                                        className="w-full border border-gray-300 rounded-lg
                               px-3 py-2 text-sm focus:outline-none
                               focus:ring-2 focus:ring-primary-500"
                                    />
                                </div>
                            ))}
                            <div className="col-span-2 flex gap-2">
                                <button type="submit"
                                        className="bg-primary-500 text-white px-4 py-2
                                   rounded-lg text-sm">
                                    Save Address
                                </button>
                                <button type="button"
                                        onClick={() => setShowAddressForm(false)}
                                        className="border border-gray-300 px-4 py-2
                                   rounded-lg text-sm text-gray-600">
                                    Cancel
                                </button>
                            </div>
                        </form>
                    )}

                    <div className="space-y-3">
                        {addresses.map(addr => (
                            <label key={addr.id}
                                   className={`flex gap-3 p-4 rounded-lg border-2
                                 cursor-pointer transition ${
                                       selectedAddress === addr.id
                                           ? 'border-primary-500 bg-primary-50'
                                           : 'border-gray-200'
                                   }`}>
                                <input
                                    type="radio" name="address"
                                    value={addr.id}
                                    checked={selectedAddress === addr.id}
                                    onChange={() => setSelectedAddress(addr.id)}
                                    className="mt-1"
                                />
                                <div>
                                    <p className="font-medium text-gray-800 text-sm">
                                        {addr.fullName} · {addr.phone}
                                    </p>
                                    <p className="text-sm text-gray-500">
                                        {addr.line1}{addr.line2 ? `, ${addr.line2}` : ''},
                                        {addr.city}, {addr.state} - {addr.pincode}
                                    </p>
                                    {addr.isDefault && (
                                        <span className="text-xs text-primary-500 font-medium">
                      Default
                    </span>
                                    )}
                                </div>
                            </label>
                        ))}
                        {addresses.length === 0 && !showAddressForm && (
                            <p className="text-sm text-gray-500 text-center py-4">
                                No addresses saved. Add one above.
                            </p>
                        )}
                    </div>
                </div>

                {/* Payment Method */}
                <div className="bg-white rounded-xl border border-gray-100 p-6">
                    <h2 className="text-lg font-semibold text-gray-800 mb-4">
                        Payment Method
                    </h2>
                    <div className="grid grid-cols-3 gap-3">
                        {['UPI', 'CARD', 'COD'].map(method => (
                            <label key={method}
                                   className={`flex items-center justify-center p-3
                                 rounded-lg border-2 cursor-pointer
                                 font-medium text-sm transition ${
                                       paymentMethod === method
                                           ? 'border-primary-500 bg-primary-50 text-primary-600'
                                           : 'border-gray-200 text-gray-600'
                                   }`}>
                                <input
                                    type="radio" name="payment" value={method}
                                    checked={paymentMethod === method}
                                    onChange={() => setPaymentMethod(method)}
                                    className="hidden"
                                />
                                {method === 'UPI' ? '📱 UPI' :
                                    method === 'CARD' ? '💳 Card' : '💵 COD'}
                            </label>
                        ))}
                    </div>
                    <p className="text-xs text-gray-400 mt-3">
                        * Payment is simulated for demo purposes
                    </p>
                </div>
            </div>

            {/* Order Summary */}
            <div>
                <div className="bg-white rounded-xl border border-gray-100 p-6
                        sticky top-24">
                    <h2 className="text-lg font-bold text-gray-900 mb-4">
                        Order Summary
                    </h2>

                    <div className="space-y-3 mb-4">
                        {cart?.items.map(item => (
                            <div key={item.id}
                                 className="flex justify-between text-sm text-gray-600">
                <span className="line-clamp-1 flex-1 mr-2">
                  {item.productName} × {item.quantity}
                </span>
                                <span className="font-medium whitespace-nowrap">
                  ₹{item.totalPrice.toLocaleString('en-IN')}
                </span>
                            </div>
                        ))}
                    </div>

                    <div className="border-t border-gray-200 pt-4 mb-6 space-y-2">
                        <div className="flex justify-between text-sm text-gray-600">
                            <span>Subtotal</span>
                            <span>₹{cart?.subtotal.toLocaleString('en-IN')}</span>
                        </div>
                        <div className="flex justify-between text-sm text-green-500">
                            <span>Delivery</span>
                            <span>FREE</span>
                        </div>
                        <div className="flex justify-between font-bold text-gray-900
                            text-base pt-2">
                            <span>Total</span>
                            <span>₹{cart?.subtotal.toLocaleString('en-IN')}</span>
                        </div>
                    </div>

                    <button
                        onClick={placeOrder}
                        disabled={loading || !selectedAddress}
                        className="w-full bg-primary-500 text-white py-3 rounded-lg
                       font-medium hover:bg-primary-600 transition
                       disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? 'Placing Order...' : 'Place Order'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CheckoutPage;