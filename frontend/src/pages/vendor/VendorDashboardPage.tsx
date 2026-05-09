import React, { useEffect, useState } from 'react';
import axiosInstance from '../../config/axios';
import { useAuth } from '../../context/AuthContext';
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer
} from 'recharts';
import { FiPackage, FiDollarSign,
    FiTrendingUp, FiShoppingBag } from 'react-icons/fi';

const VendorDashboardPage: React.FC = () => {
    const [dashboard, setDashboard] = useState<any>(null);
    const [products, setProducts]   = useState<any[]>([]);
    const { user } = useAuth();

    useEffect(() => {
        fetchDashboard();
        fetchProducts();
    }, []);

    const fetchDashboard = async () => {
        try {
            const res = await axiosInstance.get('/api/analytics/vendor');
            setDashboard(res.data.data);
        } catch { /* ignore */ }
    };

    const fetchProducts = async () => {
        try {
            const res = await axiosInstance.get('/api/products/vendor/my-products');
            setProducts(res.data.data);
        } catch { /* ignore */ }
    };

    const chartData = dashboard?.last7Days?.map((d: any) => ({
        date: new Date(d.summaryDate).toLocaleDateString('en-IN',
            { month: 'short', day: 'numeric' }),
        revenue: d.totalRevenue,
        orders:  d.totalOrders,
    })) || [];

    const stats = [
        { label: 'Orders Today', value: dashboard?.totalOrdersToday ?? 0,
            icon: FiShoppingBag, color: 'bg-blue-50 text-blue-500' },
        { label: 'Revenue Today',
            value: `₹${(dashboard?.totalRevenueToday ?? 0)
                .toLocaleString('en-IN')}`,
            icon: FiDollarSign, color: 'bg-green-50 text-green-500' },
        { label: 'Orders This Month',
            value: dashboard?.totalOrdersThisMonth ?? 0,
            icon: FiPackage, color: 'bg-purple-50 text-purple-500' },
        { label: 'Revenue This Month',
            value: `₹${(dashboard?.totalRevenueThisMonth ?? 0)
                .toLocaleString('en-IN')}`,
            icon: FiTrendingUp, color: 'bg-orange-50 text-orange-500' },
    ];

    return (
        <div>
            <h1 className="text-2xl font-bold text-gray-900 mb-2">
                Vendor Dashboard
            </h1>
            <p className="text-gray-500 mb-6">
                Welcome back, {user?.fullName?.split(' ')[0]}!
            </p>

            {/* Stats Grid */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                {stats.map(stat => (
                    <div key={stat.label}
                         className="bg-white rounded-xl border border-gray-100 p-5">
                        <div className={`w-10 h-10 rounded-lg flex items-center
                             justify-center mb-3 ${stat.color}`}>
                            <stat.icon size={20} />
                        </div>
                        <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                        <p className="text-sm text-gray-500 mt-1">{stat.label}</p>
                    </div>
                ))}
            </div>

            {/* Revenue Chart */}
            {chartData.length > 0 && (
                <div className="bg-white rounded-xl border border-gray-100 p-6 mb-8">
                    <h2 className="font-semibold text-gray-800 mb-4">
                        Revenue (Last 7 Days)
                    </h2>
                    <ResponsiveContainer width="100%" height={250}>
                        <LineChart data={chartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                            <YAxis tick={{ fontSize: 12 }} />
                            <Tooltip
                                formatter={(value: any) =>
                                    [`₹${Number(value).toLocaleString('en-IN')}`, 'Revenue']}
                            />
                            <Line type="monotone" dataKey="revenue"
                                  stroke="#f97316" strokeWidth={2} dot={{ r: 4 }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            )}

            {/* My Products */}
            <div className="bg-white rounded-xl border border-gray-100 p-6">
                <h2 className="font-semibold text-gray-800 mb-4">
                    My Products ({products.length})
                </h2>
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead>
                        <tr className="text-left text-gray-500 border-b border-gray-100">
                            <th className="pb-3 pr-4">Product</th>
                            <th className="pb-3 pr-4">Price</th>
                            <th className="pb-3 pr-4">Stock</th>
                            <th className="pb-3 pr-4">Rating</th>
                            <th className="pb-3">Status</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-50">
                        {products.map(p => (
                            <tr key={p.id}>
                                <td className="py-3 pr-4">
                                    <div className="flex items-center gap-3">
                                        <img
                                            src={p.imageUrls?.[0] ||
                                                'https://via.placeholder.com/40?text=LK'}
                                            alt={p.name}
                                            className="w-10 h-10 object-cover rounded-lg"
                                            onError={(e) => {
                                                (e.target as HTMLImageElement).src =
                                                    'https://via.placeholder.com/40?text=LK';
                                            }}
                                        />
                                        <span className="font-medium text-gray-800
                                       line-clamp-1 max-w-xs">
                        {p.name}
                      </span>
                                    </div>
                                </td>
                                <td className="py-3 pr-4 text-gray-600">
                                    ₹{p.price.toLocaleString('en-IN')}
                                </td>
                                <td className="py-3 pr-4">
                    <span className={p.stockQuantity <= 10
                        ? 'text-red-500' : 'text-green-500'}>
                      {p.stockQuantity}
                    </span>
                                </td>
                                <td className="py-3 pr-4 text-gray-600">
                                    ⭐ {p.averageRating.toFixed(1)}
                                    ({p.reviewCount})
                                </td>
                                <td className="py-3">
                    <span className={`px-2 py-1 rounded-full text-xs
                                     font-medium ${
                        p.status === 'ACTIVE'
                            ? 'bg-green-100 text-green-600'
                            : 'bg-gray-100 text-gray-600'
                    }`}>
                      {p.status}
                    </span>
                                </td>
                            </tr>
                        ))}
                        {products.length === 0 && (
                            <tr>
                                <td colSpan={5}
                                    className="py-8 text-center text-gray-400 text-sm">
                                    No products yet
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default VendorDashboardPage;