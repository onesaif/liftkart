import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import axiosInstance from '../../config/axios';
import { Product, Category, PageResponse } from '../../types';
import { FiStar, FiShoppingCart } from 'react-icons/fi';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const HomePage: React.FC = () => {
    const [products, setProducts]   = useState<Product[]>([]);
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading]     = useState(true);
    const [selectedCategory, setSelectedCategory] = useState<string>('');
    const [searchParams] = useSearchParams();
    const { addToCart } = useCart();
    const { isAuthenticated, isCustomer } = useAuth();
    const keyword = searchParams.get('q') || '';

    useEffect(() => {
        fetchCategories();
    }, []);

    useEffect(() => {
        fetchProducts();
    }, [keyword, selectedCategory]);

    const fetchCategories = async () => {
        try {
            const res = await axiosInstance.get('/api/categories');
            setCategories(res.data.data);
        } catch { /* ignore */ }
    };

    const fetchProducts = async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            if (keyword)          params.append('q', keyword);
            if (selectedCategory) params.append('categoryId', selectedCategory);

            const endpoint = keyword || selectedCategory
                ? `/api/products/search?${params}`
                : '/api/products';

            const res = await axiosInstance.get(endpoint);
            const data: PageResponse<Product> = res.data.data;
            setProducts(data.content || []);
        } catch { /* ignore */ }
        finally { setLoading(false); }
    };

    const handleAddToCart = async (product: Product) => {
        if (!isAuthenticated || !isCustomer) {
            toast.error('Please login as a customer to add items to cart');
            return;
        }
        try {
            await addToCart(
                product.id, product.name,
                product.imageUrls[0] || '', 1, product.price
            );
            toast.success('Added to cart!');
        } catch {
            toast.error('Failed to add to cart');
        }
    };

    // Flatten categories for filter
    const allCategories = categories.flatMap(cat => [cat, ...cat.children]);

    return (
        <div>
            {/* Hero */}
            <div className="bg-gradient-to-r from-primary-500 to-primary-700
                      rounded-2xl p-8 mb-8 text-white">
                <h1 className="text-3xl font-bold mb-2">
                    Your Cart, Elevated. 🛒
                </h1>
                <p className="text-primary-100 mb-4">
                    Discover thousands of products from verified vendors
                </p>
                <div className="flex gap-3">
                    <Link to="/register"
                          className="bg-white text-primary-600 px-5 py-2 rounded-lg
                           font-medium text-sm hover:bg-gray-50 transition">
                        Start Shopping
                    </Link>
                    <Link to="/register?role=vendor"
                          className="border border-white text-white px-5 py-2
                           rounded-lg font-medium text-sm
                           hover:bg-primary-600 transition">
                        Sell on LiftKart
                    </Link>
                </div>
            </div>

            {/* Category Filter */}
            <div className="flex gap-2 overflow-x-auto pb-2 mb-6">
                <button
                    onClick={() => setSelectedCategory('')}
                    className={`flex-shrink-0 px-4 py-2 rounded-full text-sm
                      font-medium transition ${
                        !selectedCategory
                            ? 'bg-primary-500 text-white'
                            : 'bg-white text-gray-600 border border-gray-200'
                    }`}
                >
                    All
                </button>
                {allCategories.map(cat => (
                    <button
                        key={cat.id}
                        onClick={() => setSelectedCategory(cat.id)}
                        className={`flex-shrink-0 px-4 py-2 rounded-full text-sm
                        font-medium transition ${
                            selectedCategory === cat.id
                                ? 'bg-primary-500 text-white'
                                : 'bg-white text-gray-600 border border-gray-200'
                        }`}
                    >
                        {cat.name}
                    </button>
                ))}
            </div>

            {/* Search Result Header */}
            {keyword && (
                <p className="text-gray-600 mb-4 text-sm">
                    Showing results for "<strong>{keyword}</strong>"
                    — {products.length} products found
                </p>
            )}

            {/* Product Grid */}
            {loading ? (
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    {[...Array(8)].map((_, i) => (
                        <div key={i} className="bg-white rounded-xl p-4 animate-pulse">
                            <div className="bg-gray-200 rounded-lg h-48 mb-3" />
                            <div className="bg-gray-200 rounded h-4 mb-2" />
                            <div className="bg-gray-200 rounded h-4 w-2/3" />
                        </div>
                    ))}
                </div>
            ) : products.length === 0 ? (
                <div className="text-center py-16">
                    <p className="text-gray-500 text-lg">No products found</p>
                    <button
                        onClick={() => setSelectedCategory('')}
                        className="mt-3 text-primary-500 text-sm"
                    >
                        Clear filters
                    </button>
                </div>
            ) : (
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                    {products.map(product => (
                        <div key={product.id}
                             className="bg-white rounded-xl border border-gray-100
                            hover:shadow-md transition group">
                            <Link to={`/products/${product.id}`}>
                                <div className="relative overflow-hidden rounded-t-xl">
                                    <img
                                        src={product.imageUrls[0] ||
                                            'https://via.placeholder.com/300x200?text=No+Image'}
                                        alt={product.name}
                                        className="w-full h-48 object-cover group-hover:scale-105
                               transition duration-300"
                                        onError={(e) => {
                                            (e.target as HTMLImageElement).src =
                                                'https://via.placeholder.com/300x200?text=LiftKart';
                                        }}
                                    />
                                    {product.comparePrice && (
                                        <span className="absolute top-2 left-2 bg-red-500
                                     text-white text-xs px-2 py-1 rounded-full">
                      {Math.round((1 - product.price / product.comparePrice)
                          * 100)}% OFF
                    </span>
                                    )}
                                </div>
                            </Link>

                            <div className="p-3">
                                <p className="text-xs text-gray-400 mb-1">
                                    {product.categoryName}
                                </p>
                                <Link to={`/products/${product.id}`}>
                                    <h3 className="text-sm font-medium text-gray-800
                                 line-clamp-2 hover:text-primary-500 mb-2">
                                        {product.name}
                                    </h3>
                                </Link>

                                {/* Rating */}
                                {product.reviewCount > 0 && (
                                    <div className="flex items-center gap-1 mb-2">
                                        <FiStar size={12}
                                                className="text-yellow-400 fill-yellow-400" />
                                        <span className="text-xs text-gray-500">
                      {product.averageRating.toFixed(1)}
                                            ({product.reviewCount})
                    </span>
                                    </div>
                                )}

                                {/* Price */}
                                <div className="flex items-center gap-2 mb-3">
                  <span className="text-base font-bold text-gray-900">
                    ₹{product.price.toLocaleString('en-IN')}
                  </span>
                                    {product.comparePrice && (
                                        <span className="text-xs text-gray-400 line-through">
                      ₹{product.comparePrice.toLocaleString('en-IN')}
                    </span>
                                    )}
                                </div>

                                <button
                                    onClick={() => handleAddToCart(product)}
                                    className="w-full flex items-center justify-center gap-2
                             bg-primary-50 text-primary-600 py-2 rounded-lg
                             text-sm font-medium hover:bg-primary-500
                             hover:text-white transition"
                                >
                                    <FiShoppingCart size={14} />
                                    Add to Cart
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default HomePage;