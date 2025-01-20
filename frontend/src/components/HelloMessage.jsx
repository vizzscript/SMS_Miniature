import { useEffect, useState } from 'react';
import { getHello } from '../services/apiService';

const HelloMessage = () => {
    const [message, setMessage] = useState('');

    useEffect(() => {
        const fetchHello = async () => {
            try {
                const response = await getHello();
                setMessage(response);
            } catch (error) {
                console.error('Error fetching hello message:', error);
            }
        };
        fetchHello();
    }, []);

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-r from-blue-500 to-purple-600">
            <div className="bg-white p-8 rounded-lg shadow-lg text-center max-w-md">
                <h1 className="text-4xl font-bold text-gray-800 mb-4">
                    Welcome to Our App
                </h1>
                <p className="text-lg text-gray-700 mb-6">
                    {message || 'Loading...'}
                </p>
                <div className="space-y-4">
                    <a
                        href="/signup"
                        className="block w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition duration-300"
                    >
                        Sign Up
                    </a>
                    <a
                        href="/login"
                        className="block w-full bg-purple-600 text-white py-2 px-4 rounded-md hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 transition duration-300"
                    >
                        Log In
                    </a>
                </div>
            </div>
        </div>
    );
};

export default HelloMessage;