import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosInstance from '../../config/axios';
import toast from 'react-hot-toast';

const LoginPage: React.FC = () => {
    const [email, setEmail]       = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading]   = useState(false);
    const { login } = useAuth();
    const navigate  = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            const res = await axiosInstance.post('/api/auth/login',
                { email, password });
            const { accessToken, refreshToken, user } = res.data.data;
            localStorage.setItem('refreshToken', refreshToken);
            login(accessToken, user);
            toast.success(`Welcome back, ${user.fullName.split(' ')[0]}!`);

            // Redirect based on role
            if (user.role === 'VENDOR') navigate('/vendor');
            else if (user.role === 'ADMIN') navigate('/admin');
            else navigate('/');
        } catch (err: any) {
            toast.error(err.response?.data?.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
            <div className="bg-white p-8 rounded-2xl shadow-sm border
                      border-gray-200 w-full max-w-md">

                {/* Logo */}
                <div className="text-center mb-8">
                    <div className="w-12 h-12 bg-primary-500 rounded-xl
                          flex items-center justify-center mx-auto mb-3">
                        <span className="text-white font-bold text-xl">L</span>
                    </div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        Welcome back
                    </h1>
                    <p className="text-gray-500 text-sm mt-1">
                        Sign in to your LiftKart account
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Email
                        </label>
                        <input
                            type="email"
                            value={email}
                            onChange={e => setEmail(e.target.value)}
                            required
                            className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary-500"
                            placeholder="john@example.com"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Password
                        </label>
                        <input
                            type="password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                            required
                            className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary-500"
                            placeholder="••••••••"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-primary-500 text-white py-2.5 rounded-lg
                       font-medium hover:bg-primary-600 transition
                       disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? 'Signing in...' : 'Sign In'}
                    </button>
                </form>

                {/* Quick login hints */}
                <div className="mt-4 p-3 bg-gray-50 rounded-lg text-xs text-gray-500">
                    <p className="font-medium mb-1">Test accounts:</p>
                    <p>Admin: admin@liftkart.com / Password123</p>
                    <p>Customer: john@example.com / Password123</p>
                </div>

                <p className="text-center text-sm text-gray-500 mt-6">
                    Don't have an account?{' '}
                    <Link to="/register" className="text-primary-500 font-medium
                                          hover:text-primary-600">
                        Register
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default LoginPage;