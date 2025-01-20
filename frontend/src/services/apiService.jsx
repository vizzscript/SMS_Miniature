import axios from 'axios';

const API_URL = 'http://localhost:8080/client';

export const signup = async (signupData) => {
    try {
        const response = await axios.post(`${API_URL}/signup`, signupData, {
            headers: {
                'Content-Type': 'application/json',
            },
        });
        return response.data;
    } catch (error) {
        console.error('Signup error:', error.response?.data || error.message);
        throw error;
    }
};

export const login = async (loginData) => {
    try {
        const response = await axios.post(`${API_URL}/login`, loginData,{
            headers: {
                'Content-Type': 'application/json',
            },
        });
        return response.data;
    } catch (error) {
        console.error('Login error:', error.response?.data || error.message);
        throw error;
    }
};

export const getHello = async () => {
    try {
        const response = await axios.get(`${API_URL}/hello`);
        return response.data;
    } catch (error) {
        console.error('Hello error:', error.response?.data || error.message);
        throw error;
    }
};