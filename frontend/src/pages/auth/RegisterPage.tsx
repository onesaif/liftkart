import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosInstance from '../../config/axios';
import toast from 'react-hot-toast';

const RegisterPage: React.FC = () => {
    const [form, setForm] = useState({
        fullName: '', email: '', password: '',
        phone: '', role: 'CUSTOMER',
        storeName: '', storeDescription: '',
    });
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate  = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement |
        HTMLSelectElement | HTMLTextAreaElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            const endpoint = form.role === 'VENDOR'
                ? '/api/auth/register/vendor'
                : '/api/auth/register';

            const res = await axiosInstance.post(endpoint, form);

            if (form.role === 'VENDOR') {
                toast.success('Registration submitted! Await admin approval.');
                navigate('/login');
            } else {
                const { accessToken, refreshToken, user } = res.data.data;
                localStorage.setItem('refreshToken', refreshToken);
                login(accessToken, user);
                toast.success('Account created successfully!');
                navigate('/');
            }
        } catch (err: any) {
            toast.error(err.response?.data?.message || 'Registration failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center py-8">
            <div className="bg-white p-8 rounded-2xl shadow-sm border
                      border-gray-200 w-full max-w-md">
                <div className="text-center mb-8">
                    <div className="w-12 h-12 bg-primary-500 rounded-xl
                          flex items-center justify-center mx-auto mb-3">
                        <span className="text-white font-bold text-xl">L</span>
                    </div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        Create account
                    </h1>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Account Type
                        </label>
                        <select
                            name="role"
                            value={form.role}
                            onChange={handleChange}
                            className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary-500"
                        >
                            <option value="CUSTOMER">Customer</option>
                            <option value="VENDOR">Vendor / Seller</option>
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Full Name
                        </label>
                        <input
                            type="text" name="fullName" value={form.fullName}
                            onChange={handleChange} required
                            className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary-500"
                            placeholder="John Doe"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Email
                        </label>
                        <input
                            type="email" name="email" value={form.email}
                            onChange={handleChange} required
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
                            type="password" name="password" value={form.password}
                            onChange={handleChange} required
                            className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary-500"
                            placeholder="Min 8 chars, 1 uppercase, 1 number"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Phone
                        </label>
                        <input
                            type="text" name="phone" value={form.phone}
                            onChange={handleChange}
                            className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                         text-sm focus:outline-none focus:ring-2
                         focus:ring-primary-500"
                            placeholder="9876543210"
                        />
                    </div>

                    {form.role === 'VENDOR' && (
                        <>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Store Name
                                </label>
                                <input
                                    type="text" name="storeName" value={form.storeName}
                                    onChange={handleChange} required
                                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                             text-sm focus:outline-none focus:ring-2
                             focus:ring-primary-500"
                                    placeholder="My Awesome Store"
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Store Description
                                </label>
                                <input
                                    type="text" name="storeDescription"
                                    value={form.storeDescription}
                                    onChange={handleChange}
                                    className="w-full border border-gray-300 rounded-lg px-4 py-2.5
                             text-sm focus:outline-none focus:ring-2
                             focus:ring-primary-500"
                                    placeholder="What do you sell?"
                                />
                            </div>
                        </>
                    )}

                    <button
                        type="submit" disabled={loading}
                        className="w-full bg-primary-500 text-white py-2.5 rounded-lg
                       font-medium hover:bg-primary-600 transition
                       disabled:opacity-50"
                    >
                        {loading ? 'Creating account...' : 'Create Account'}
                    </button>
                </form>

                <p className="text-center text-sm text-gray-500 mt-6">
                    Already have an account?{' '}
                    <Link to="/login" className="text-primary-500 font-medium
                                       hover:text-primary-600">
                        Sign In
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default RegisterPage;