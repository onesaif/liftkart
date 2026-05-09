import React, { useEffect, useState } from 'react';
import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import axiosInstance from '../../config/axios';
import toast from 'react-hot-toast';
import { FiShoppingCart, FiBell, FiUser, FiLogOut,
    FiPackage, FiBarChart2 } from 'react-icons/fi';

const Layout: React.FC = () => {
    const { user, logout, isAuthenticated, isCustomer,
        isVendor, isAdmin } = useAuth();
    const { cartCount, fetchCart } = useCart();
    const navigate = useNavigate();
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        if (isAuthenticated && isCustomer) {
            fetchCart();
            fetchUnreadCount();
        }
    }, [isAuthenticated]);

    const fetchUnreadCount = async () => {
        try {
            const res = await axiosInstance.get(
                '/api/notifications/unread-count');
            setUnreadCount(res.data.data.unreadCount);
        } catch { /* ignore */ }
    };

    const handleLogout = async () => {
        try {
            await axiosInstance.post('/api/auth/logout');
        } catch { /* ignore */ }
        logout();
        toast.success('Logged out successfully');
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Navbar */}
            <nav className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center h-16">

                        {/* Logo */}
                        <Link to="/" className="flex items-center gap-2">
                            <div className="w-8 h-8 bg-primary-500 rounded-lg
                              flex items-center justify-center">
                                <span className="text-white font-bold text-sm">L</span>
                            </div>
                            <span className="text-xl font-bold text-gray-900">
                LiftKart
              </span>
                        </Link>

                        {/* Search Bar */}
                        <div className="hidden md:flex flex-1 max-w-lg mx-8">
                            <div className="relative w-full">
                                <input
                                    type="text"
                                    placeholder="Search products..."
                                    className="w-full border border-gray-300 rounded-lg
                             px-4 py-2 text-sm focus:outline-none
                             focus:ring-2 focus:ring-primary-500"
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                            const val = (e.target as HTMLInputElement).value;
                                            navigate(`/?q=${val}`);
                                        }
                                    }}
                                />
                            </div>
                        </div>

                        {/* Right side */}
                        <div className="flex items-center gap-4">
                            {isAuthenticated ? (
                                <>
                                    {isCustomer && (
                                        <>
                                            {/* Cart */}
                                            <Link to="/cart" className="relative p-2 text-gray-600
                                                   hover:text-primary-500">
                                                <FiShoppingCart size={22} />
                                                {cartCount > 0 && (
                                                    <span className="absolute -top-1 -right-1 bg-primary-500
                                          text-white text-xs rounded-full
                                          w-5 h-5 flex items-center justify-center">
                            {cartCount}
                          </span>
                                                )}
                                            </Link>

                                            {/* Notifications */}
                                            <button className="relative p-2 text-gray-600
                                        hover:text-primary-500"
                                                    onClick={fetchUnreadCount}>
                                                <FiBell size={22} />
                                                {unreadCount > 0 && (
                                                    <span className="absolute -top-1 -right-1 bg-red-500
                                          text-white text-xs rounded-full
                                          w-5 h-5 flex items-center justify-center">
                            {unreadCount}
                          </span>
                                                )}
                                            </button>

                                            {/* Orders */}
                                            <Link to="/orders"
                                                  className="hidden md:flex items-center gap-1
                                       text-sm text-gray-600 hover:text-primary-500">
                                                <FiPackage size={18} />
                                                Orders
                                            </Link>
                                        </>
                                    )}

                                    {isVendor && (
                                        <Link to="/vendor"
                                              className="flex items-center gap-1 text-sm
                                     text-gray-600 hover:text-primary-500">
                                            <FiBarChart2 size={18} />
                                            Dashboard
                                        </Link>
                                    )}

                                    {isAdmin && (
                                        <Link to="/admin"
                                              className="flex items-center gap-1 text-sm
                                     text-gray-600 hover:text-primary-500">
                                            <FiBarChart2 size={18} />
                                            Admin
                                        </Link>
                                    )}

                                    {/* User Menu */}
                                    <div className="flex items-center gap-2">
                                        <div className="w-8 h-8 bg-primary-100 rounded-full
                                    flex items-center justify-center">
                                            <FiUser size={16} className="text-primary-600" />
                                        </div>
                                        <span className="hidden md:block text-sm text-gray-700">
                      {user?.fullName?.split(' ')[0]}
                    </span>
                                    </div>

                                    <button
                                        onClick={handleLogout}
                                        className="flex items-center gap-1 text-sm text-gray-600
                               hover:text-red-500"
                                    >
                                        <FiLogOut size={18} />
                                    </button>
                                </>
                            ) : (
                                <div className="flex items-center gap-3">
                                    <Link to="/login"
                                          className="text-sm text-gray-600 hover:text-primary-500">
                                        Login
                                    </Link>
                                    <Link to="/register"
                                          className="text-sm bg-primary-500 text-white px-4 py-2
                                   rounded-lg hover:bg-primary-600 transition">
                                        Register
                                    </Link>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </nav>

            {/* Page Content */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <Outlet />
            </main>

            {/* Footer */}
            <footer className="bg-white border-t border-gray-200 mt-16">
                <div className="max-w-7xl mx-auto px-4 py-8 text-center
                        text-sm text-gray-500">
                    © 2026 LiftKart. Built with Spring Boot & React.
                </div>
            </footer>
        </div>
    );
};

export default Layout;