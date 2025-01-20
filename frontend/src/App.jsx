import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import HomePage from './pages/HomePage';
import SignupPage from './pages/SignupPage';
import LoginPage from './pages/LoginPage';

const App = () => {
    return (
        <Router>
            {/* Navigation Bar */}
            <nav className="bg-gradient-to-r from-blue-500 to-purple-600 p-4 shadow-lg">
                <div className="container mx-auto flex justify-between items-center">
                    <Link
                        to="/"
                        className="text-white text-2xl font-bold hover:text-gray-200 transition duration-300"
                    >
                        My App
                    </Link>
                    <div className="space-x-4">
                        <Link
                            to="/"
                            className="text-white hover:text-gray-200 transition duration-300"
                        >
                            Home
                        </Link>
                        <Link
                            to="/signup"
                            className="text-white hover:text-gray-200 transition duration-300"
                        >
                            Sign Up
                        </Link>
                        <Link
                            to="/login"
                            className="text-white hover:text-gray-200 transition duration-300"
                        >
                            Log In
                        </Link>
                    </div>
                </div>
            </nav>

            {/* Main Content */}
            <div className="min-h-screen bg-gray-100">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/signup" element={<SignupPage />} />
                    <Route path="/login" element={<LoginPage />} />
                </Routes>
            </div>
        </Router>
    );
};

export default App;