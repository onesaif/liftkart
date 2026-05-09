import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '../../config/axios';
import { Order } from '../../types';
import toast from 'react-hot-toast';

const statusColors: Record<string, string> = {
    PENDING: 'bg-yellow-100 text-yellow-700',
    CONFIRMED: 'bg-blue-100 text-blue-700',
    SHIPPED: 'bg-indigo-100 text-indigo-700',
    DELIVERED: 'bg-green-100 text-green-700',
    CANCELLED: 'bg-red-100 text-red-700',
};

const OrderDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [order, setOrder] = useState<Order | null>(null);
    const navigate = useNavigate();

    useEffect(() => { fetchOrder(); }, [id]);

    const fetchOrder = async () => {
        try {
            const res = await axiosInstance.get(`/api/orders/${id}`);
            setOrder(res.data.data);
        } catch { toast.error('Order not found'); }
    };

    const cancelOrder = async () => {
        try {
            await axiosInstance.post(`/api/orders/${id}/cancel`);
            await fetchOrder();
            toast.success('Order cancelled');
        } catch (err: any) {
            toast.error(err.response?.data?.message || 'Cannot cancel order');
        }
    };

    if (!order) return (
        <div className="text-center py-20 text-gray-500">Loading...</div>
    );

    const addr = order.addressSnapshot;

    return (
        <div className="max-w-3xl mx-auto">
            <button onClick={() => navigate('/orders')}
                    className="text-sm text-primary-500 mb-6 flex items-center gap-1">
                ← Back to Orders
            </button>

            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        Order #{order.id.substring(0, 8).toUpperCase()}
                    </h1>
                    <p className="text-sm text-gray-500">
                        {new Date(order.createdAt).toLocaleDateString('en-IN', {
                            day: 'numeric', month: 'long', year: 'numeric',
                            hour: '2-digit', minute: '2-digit'
                        })}
                    </p>
                </div>
                <span className={`px-4 py-2 rounded-full text-sm font-medium
                         ${statusColors[order.status] ||
                'bg-gray-100 text-gray-700'}`}>
          {order.status}
        </span>
            </div>

            {/* Status Timeline */}
            <div className="bg-white rounded-xl border border-gray-100 p-6 mb-4">
                <h2 className="font-semibold text-gray-800 mb-4">Order Timeline</h2>
                <div className="space-y-3">
                    {order.statusHistory.map((h, i) => (
                        <div key={i} className="flex gap-3">
                            <div className="flex flex-col items-center">
                                <div className="w-3 h-3 bg-primary-500 rounded-full mt-1" />
                                {i < order.statusHistory.length - 1 && (
                                    <div className="w-0.5 h-8 bg-gray-200 mt-1" />
                                )}
                            </div>
                            <div>
                                <p className="text-sm font-medium text-gray-800">
                                    {h.newStatus}
                                </p>
                                <p className="text-xs text-gray-500">
                                    {h.comment} · {h.changedByRole}
                                </p>
                                <p className="text-xs text-gray-400">
                                    {new Date(h.changedAt).toLocaleTimeString('en-IN')}
                                </p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Items */}
            <div className="bg-white rounded-xl border border-gray-100 p-6 mb-4">
                <h2 className="font-semibold text-gray-800 mb-4">Items Ordered</h2>
                <div className="space-y-3">
                    {order.items.map(item => (
                        <div key={item.id} className="flex gap-3">
                            <img
                                src={item.productImageUrl ||
                                    'https://via.placeholder.com/60x60?text=LK'}
                                alt={item.productName}
                                className="w-16 h-16 object-cover rounded-lg"
                                onError={(e) => {
                                    (e.target as HTMLImageElement).src =
                                        'https://via.placeholder.com/60x60?text=LK';
                                }}
                            />
                            <div className="flex-1">
                                <p className="font-medium text-gray-800 text-sm">
                                    {item.productName}
                                </p>
                                <p className="text-xs text-gray-500">
                                    Qty: {item.quantity} × ₹
                                    {item.unitPrice.toLocaleString('en-IN')}
                                </p>
                            </div>
                            <p className="font-semibold text-gray-900">
                                ₹{item.totalPrice.toLocaleString('en-IN')}
                            </p>
                        </div>
                    ))}
                </div>

                <div className="border-t border-gray-200 mt-4 pt-4 space-y-2">
                    <div className="flex justify-between text-sm text-gray-600">
                        <span>Subtotal</span>
                        <span>₹{order.subtotal.toLocaleString('en-IN')}</span>
                    </div>
                    <div className="flex justify-between font-bold text-gray-900">
                        <span>Total</span>
                        <span>₹{order.totalAmount.toLocaleString('en-IN')}</span>
                    </div>
                </div>
            </div>

            {/* Delivery Address */}
            <div className="bg-white rounded-xl border border-gray-100 p-6 mb-4">
                <h2 className="font-semibold text-gray-800 mb-2">
                    Delivery Address
                </h2>
                <p className="text-sm text-gray-600">
                    {addr.fullName} · {addr.phone}
                </p>
                <p className="text-sm text-gray-500">
                    {addr.line1}{addr.line2 ? `, ${addr.line2}` : ''}, {addr.city},
                    {addr.state} - {addr.pincode}
                </p>
            </div>

            {/* Payment */}
            <div className="bg-white rounded-xl border border-gray-100 p-6 mb-6">
                <h2 className="font-semibold text-gray-800 mb-2">Payment</h2>
                <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Method</span>
                    <span className="font-medium">{order.paymentMethod}</span>
                </div>
                <div className="flex justify-between text-sm mt-1">
                    <span className="text-gray-600">Status</span>
                    <span className={`font-medium ${
                        order.paymentStatus === 'PAID'
                            ? 'text-green-500' : 'text-yellow-500'
                    }`}>
            {order.paymentStatus}
          </span>
                </div>
            </div>

            {/* Cancel Button */}
            {(order.status === 'PENDING' || order.status === 'CONFIRMED') && (
                <button
                    onClick={cancelOrder}
                    className="w-full border-2 border-red-400 text-red-500 py-3
                     rounded-lg font-medium hover:bg-red-50 transition"
                >
                    Cancel Order
                </button>
            )}
        </div>
    );
};

export default OrderDetailPage;