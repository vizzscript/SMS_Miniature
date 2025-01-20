import { useState } from 'react';
import { login } from '../services/apiService';
import { useNavigate } from 'react-router-dom';
import Toast from './Toast'; // Import the Toast component

const LoginForm = () => {
    const [formData, setFormData] = useState({
        userName: '',
        pwd: '',
    });
    const [showToast, setShowToast] = useState(false);
    const [toastMessage, setToastMessage] = useState('');
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await login(formData);
            setToastMessage(`API-Key: ${response.apiKey}`); // Customize the message
            setShowToast(true);
            setTimeout(() => {
                navigate('/login'); // Redirect to UserDetails page after 3 seconds
            }, 3000);
        } catch (error) {
            setToastMessage('Login failed. Please try again.',error);
            setShowToast(true);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-r from-blue-500 to-purple-600">
            <form
                onSubmit={handleSubmit}
                className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md"
            >
                <h2 className="text-3xl font-bold mb-6 text-center text-gray-800">
                    Log In
                </h2>
                <div className="mb-4">
                    <label
                        htmlFor="userName"
                        className="block text-sm font-medium text-gray-700"
                    >
                        Username
                    </label>
                    <input
                        type="text"
                        name="userName"
                        placeholder="Enter your username"
                        value={formData.userName}
                        onChange={handleChange}
                        required
                        className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>
                <div className="mb-6">
                    <label
                        htmlFor="pwd"
                        className="block text-sm font-medium text-gray-700"
                    >
                        Password
                    </label>
                    <input
                        type="password"
                        name="pwd"
                        placeholder="Enter your password"
                        value={formData.pwd}
                        onChange={handleChange}
                        required
                        className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>
                <button
                    type="submit"
                    className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                >
                    Log In
                </button>
            </form>

            {/* Toast Notification */}
            {showToast && (
                <Toast
                    message={toastMessage}
                    onClose={() => setShowToast(false)}
                />
            )}
        </div>
    );
};

export default LoginForm;