import React, { useEffect, useState } from 'react';
import axiosInstance from '../../config/axios';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer
} from 'recharts';
import { FiShoppingBag, FiDollarSign,
    FiTrendingUp, FiCalendar } from 'react-icons/fi';

const AdminDashboardPage: React.FC = () => {
    const [dashboard, setDashboard] = useState<any>(null);

    useEffect(() => { fetchDashboard(); }, []);

    const fetchDashboard = async () => {
        try {
            const res = await axiosInstance.get('/api/analytics/platform');
            setDashboard(res.data.data);
        } catch { /* ignore */ }
    };

    const chartData = dashboard?.last30Days?.map((d: any) => ({
        date: new Date(d.summaryDate).toLocaleDateString('en-IN',
            { month: 'short', day: 'numeric' }),
        revenue: d.totalRevenue,
        orders:  d.totalOrders,
    })) || [];

    const stats = [
        { label: 'Orders Today',
            value: dashboard?.totalOrdersToday ?? 0,
            icon: FiShoppingBag, color: 'bg-blue-50 text-blue-500' },
        { label: 'Revenue Today',
            value: `₹${(dashboard?.totalRevenueToday ?? 0)
                .toLocaleString('en-IN')}`,
            icon: FiDollarSign, color: 'bg-green-50 text-green-500' },
        { label: 'Orders This Month',
            value: dashboard?.totalOrdersThisMonth ?? 0,
            icon: FiCalendar, color: 'bg-purple-50 text-purple-500' },
        { label: 'Revenue This Month',
            value: `₹${(dashboard?.totalRevenueThisMonth ?? 0)
                .toLocaleString('en-IN')}`,
            icon: FiTrendingUp, color: 'bg-orange-50 text-orange-500' },
    ];

    return (
        <div>
            <h1 className="text-2xl font-bold text-gray-900 mb-6">
                Admin Dashboard
            </h1>

            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                {stats.map(stat => {
                    const Icon = stat.icon;  // ← add this line
                    return (
                        <div key={stat.label}
                             className="bg-white rounded-xl border border-gray-100 p-5">
                            <div className={`w-10 h-10 rounded-lg flex items-center
                       justify-center mb-3 ${stat.color}`}>
                                <Icon size={20} />  {/* ← use Icon not stat.icon */}
                            </div>
                            <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                            <p className="text-sm text-gray-500 mt-1">{stat.label}</p>
                        </div>
                    );
                })}
            </div>

            {chartData.length > 0 && (
                <div className="bg-white rounded-xl border border-gray-100 p-6">
                    <h2 className="font-semibold text-gray-800 mb-4">
                        Platform Revenue (Last 30 Days)
                    </h2>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={chartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                            <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                            <YAxis tick={{ fontSize: 11 }} />
                            <Tooltip
                                formatter={(value: any) =>
                                    [`₹${Number(value).toLocaleString('en-IN')}`, 'Revenue']}
                            />
                            <Bar dataKey="revenue" fill="#f97316" radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            )}
        </div>
    );
};

export default AdminDashboardPage;