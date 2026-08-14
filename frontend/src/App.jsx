import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import { getAccessToken, clearTokens } from './api';
import Login from './Login';
import Register from './Register';
import Dashboard from './Dashboard';
import Profile from './Profile';
import AiPlanner from './AiPlanner';
import ActuatorMonitor from './ActuatorMonitor';
import OAuth2Callback from './OAuth2Callback';
import { LayoutDashboard, BrainCircuit, Activity, Settings, LogOut, CheckSquare } from 'lucide-react';

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!getAccessToken());
  const [userInfo, setUserInfo] = useState(null);

  useEffect(() => {
    // Listen for global custom logout events triggered by 401 expiration failures in API client
    const handleLogoutEvent = () => {
      setIsAuthenticated(false);
      setUserInfo(null);
    };

    window.addEventListener('auth-logout', handleLogoutEvent);
    
    // Load local user details if authenticated
    if (isAuthenticated) {
      try {
        const info = JSON.parse(localStorage.getItem('user_info'));
        setUserInfo(info);
      } catch (e) {
        console.error(e);
      }
    }

    return () => window.removeEventListener('auth-logout', handleLogoutEvent);
  }, [isAuthenticated]);

  const handleLoginSuccess = () => {
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    clearTokens();
    setIsAuthenticated(false);
    setUserInfo(null);
  };

  return (
    <BrowserRouter>
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        
        {/* Navigation Header */}
        <header className="glass-panel" style={{
          margin: '16px 24px',
          padding: '16px 32px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderRadius: '16px',
          border: '1px solid var(--border-glow)'
        }}>
          <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '10px', textDecoration: 'none', color: '#fff' }}>
            <CheckSquare size={24} style={{ color: 'var(--primary)' }} />
            <h1 style={{ fontSize: '20px', margin: 0, fontWeight: 700, letterSpacing: '-0.5px' }}>LifeOS</h1>
          </Link>

          {isAuthenticated && (
            <nav style={{ display: 'flex', gap: '24px', alignItems: 'center' }}>
              <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none', color: 'var(--text-muted)', fontSize: '14px', fontWeight: 600 }}>
                <LayoutDashboard size={16} />
                Dashboard
              </Link>
              <Link to="/ai-planner" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none', color: 'var(--text-muted)', fontSize: '14px', fontWeight: 600 }}>
                <BrainCircuit size={16} />
                AI Planner
              </Link>
              <Link to="/actuator" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none', color: 'var(--text-muted)', fontSize: '14px', fontWeight: 600 }}>
                <Activity size={16} />
                System Health
              </Link>
              <Link to="/settings" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none', color: 'var(--text-muted)', fontSize: '14px', fontWeight: 600 }}>
                <Settings size={16} />
                Settings
              </Link>
            </nav>
          )}

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            {isAuthenticated ? (
              <>
                {userInfo && (
                  <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>
                    Hi, <strong style={{ color: 'var(--text-main)' }}>{userInfo.username}</strong>
                  </span>
                )}
                <button id="logout-btn" onClick={handleLogout} className="btn-secondary" style={{ padding: '8px 16px', fontSize: '13px', display: 'flex', gap: '6px' }}>
                  <LogOut size={14} /> Log Out
                </button>
              </>
            ) : (
              <div style={{ display: 'flex', gap: '12px' }}>
                <Link to="/login" className="btn-secondary" style={{ textDecoration: 'none', padding: '8px 16px', fontSize: '13px' }}>Sign In</Link>
                <Link to="/register" className="btn-primary" style={{ textDecoration: 'none', padding: '8px 16px', fontSize: '13px' }}>Sign Up</Link>
              </div>
            )}
          </div>
        </header>

        {/* Main Content Area */}
        <main style={{ flex: 1, paddingBottom: '40px' }}>
          <Routes>
            {/* Protected Routes */}
            <Route path="/" element={isAuthenticated ? <Dashboard /> : <Navigate to="/login" replace />} />
            <Route path="/ai-planner" element={isAuthenticated ? <AiPlanner /> : <Navigate to="/login" replace />} />
            <Route path="/actuator" element={isAuthenticated ? <ActuatorMonitor /> : <Navigate to="/login" replace />} />
            <Route path="/settings" element={isAuthenticated ? <Profile /> : <Navigate to="/login" replace />} />

            {/* Public Routes */}
            <Route path="/login" element={!isAuthenticated ? <Login onLoginSuccess={handleLoginSuccess} /> : <Navigate to="/" replace />} />
            <Route path="/register" element={!isAuthenticated ? <Register /> : <Navigate to="/" replace />} />
            
            {/* OAuth2 Redirect Callback */}
            <Route path="/oauth2/callback" element={<OAuth2Callback onLoginSuccess={handleLoginSuccess} />} />

            {/* Catch-all fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
        
      </div>
    </BrowserRouter>
  );
}
