import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axiosInstance from '../../config/axios';
import { Order, PageResponse } from '../../types';

const statusColors: Record<string, string> = {
    PENDING:    'bg-yellow-100 text-yellow-700',
    CONFIRMED:  'bg-blue-100 text-blue-700',
    PROCESSING: 'bg-purple-100 text-purple-700',
    SHIPPED:    'bg-indigo-100 text-indigo-700',
    DELIVERED:  'bg-green-100 text-green-700',
    CANCELLED:  'bg-red-100 text-red-700',
    RETURNED:   'bg-gray-100 text-gray-700',
};

const OrdersPage: React.FC = () => {
    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const res = await axiosInstance.get('/api/orders');
            const data: PageResponse<Order> = res.data.data;
            setOrders(data.content);
        } catch { /* ignore */ }
        finally { setLoading(false); }
    };

    if (loading) return (
        <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
                <div key={i}
                     className="bg-white rounded-xl p-6 animate-pulse h-32" />
            ))}
        </div>
    );

    if (orders.length === 0) return (
        <div className="text-center py-20">
            <h2 className="text-xl font-semibold text-gray-700 mb-2">
                No orders yet
            </h2>
            <Link to="/" className="text-primary-500">Start shopping</Link>
        </div>
    );

    return (
        <div>
            <h1 className="text-2xl font-bold text-gray-900 mb-6">My Orders</h1>
            <div className="space-y-4">
                {orders.map(order => (
                    <div key={order.id}
                         className="bg-white rounded-xl border border-gray-100 p-6">
                        <div className="flex justify-between items-start mb-3">
                            <div>
                                <p className="text-sm text-gray-500">
                                    Order #{order.id.substring(0, 8).toUpperCase()}
                                </p>
                                <p className="text-xs text-gray-400">
                                    {new Date(order.createdAt).toLocaleDateString('en-IN', {
                                        day: 'numeric', month: 'long', year: 'numeric'
                                    })}
                                </p>
                            </div>
                            <span className={`text-xs font-medium px-3 py-1 rounded-full
                               ${statusColors[order.status] ||
                            'bg-gray-100 text-gray-700'}`}>
                {order.status}
              </span>
                        </div>

                        <div className="flex gap-2 mb-3">
                            {order.items.slice(0, 3).map(item => (
                                <img
                                    key={item.id}
                                    src={item.productImageUrl ||
                                        'https://via.placeholder.com/50x50?text=LK'}
                                    alt={item.productName}
                                    className="w-12 h-12 object-cover rounded-lg border
                             border-gray-100"
                                    onError={(e) => {
                                        (e.target as HTMLImageElement).src =
                                            'https://via.placeholder.com/50x50?text=LK';
                                    }}
                                />
                            ))}
                            {order.items.length > 3 && (
                                <div className="w-12 h-12 bg-gray-100 rounded-lg
                                flex items-center justify-center
                                text-xs text-gray-500">
                                    +{order.items.length - 3}
                                </div>
                            )}
                        </div>

                        <div className="flex justify-between items-center">
              <span className="font-semibold text-gray-900">
                ₹{order.totalAmount.toLocaleString('en-IN')}
              </span>
                            <Link to={`/orders/${order.id}`}
                                  className="text-sm text-primary-500
                               hover:text-primary-600 font-medium">
                                View Details →
                            </Link>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default OrdersPage;