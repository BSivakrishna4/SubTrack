import React, { createContext, useContext, useState, useEffect } from 'react';

const ThemeContext = createContext();

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

const getInitialTheme = () => {
  try {
    const saved = localStorage.getItem('subtrack_theme');
    if (saved === 'light' || saved === 'dark') {
      return saved;
    }
    // Detect system preference only if no previous choice
    if (typeof window !== 'undefined' && window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
      return 'light';
    }
  } catch (e) {
    console.warn('Unable to access localStorage or matchMedia:', e);
  }
  return 'dark'; // Default theme remains Dark Mode
};

export const ThemeProvider = ({ children }) => {
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    const root = document.documentElement;
    const body = document.body;

    // Apply quick smooth theme transition class
    root.classList.add('theme-transition');

    // Set attributes and class names on html and body
    root.setAttribute('data-theme', theme);
    body.setAttribute('data-theme', theme);

    if (theme === 'light') {
      root.classList.add('light');
      root.classList.remove('dark');
      body.classList.add('light');
      body.classList.remove('dark');
    } else {
      root.classList.add('dark');
      root.classList.remove('light');
      body.classList.add('dark');
      body.classList.remove('light');
    }

    try {
      localStorage.setItem('subtrack_theme', theme);
    } catch (e) {
      console.warn('Failed to store theme preference in localStorage:', e);
    }

    // Remove transition class after transition completes
    const timer = setTimeout(() => {
      root.classList.remove('theme-transition');
    }, 280);

    return () => clearTimeout(timer);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => (prev === 'dark' ? 'light' : 'dark'));
  };

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, setTheme, isDark: theme === 'dark' }}>
      {children}
    </ThemeContext.Provider>
  );
};

export default ThemeContext;
