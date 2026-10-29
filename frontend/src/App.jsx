import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Plans from './pages/Plans';
import MySubscriptions from './pages/MySubscriptions';
import AdminPlans from './pages/AdminPlans';

const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

// Layout component to render Navbar conditionally on app pages
const AppContent = () => {
  const location = useLocation();
  const hideNavbarRoutes = ['/', '/login', '/register'];
  const showNavbar = !hideNavbarRoutes.includes(location.pathname);

  return (
    <div
      className="min-h-screen flex flex-col font-sans app-container"
      style={{
        backgroundColor: 'var(--bg-primary)',
        color: 'var(--text-primary)',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      {showNavbar && <Navbar />}
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/plans" element={<Plans />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/subscriptions"
            element={
              <ProtectedRoute>
                <MySubscriptions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/plans"
            element={
              <ProtectedRoute>
                <AdminPlans />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
};

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <AppContent />
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
