import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('subtrack_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [loading, setLoading] = useState(false);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await api.post('/api/users/login', { email, password });
      const { token, userId, name, role } = response.data;
      
      const userData = { userId, name, email, role, token };
      localStorage.setItem('subtrack_token', token);
      localStorage.setItem('subtrack_user', JSON.stringify(userData));
      
      setUser(userData);
      return { success: true, user: userData };
    } catch (error) {
      const msg = error.response?.data?.message || 'Login failed. Please check credentials.';
      return { success: false, error: msg };
    } finally {
      setLoading(false);
    }
  };

  const register = async (name, email, password, role = 'USER') => {
    setLoading(true);
    try {
      await api.post('/api/users/register', { name, email, password, role });
      // Auto login after registration
      return await login(email, password);
    } catch (error) {
      const msg = error.response?.data?.message || 'Registration failed.';
      return { success: false, error: msg };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('subtrack_token');
    localStorage.removeItem('subtrack_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
