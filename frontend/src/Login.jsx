import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from './api';
import { LogIn } from 'lucide-react';

export default function Login({ onLoginSuccess }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await authApi.login(username, password);
      onLoginSuccess();
      navigate('/');
    } catch (err) {
      setError(err.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    const clientId = 'YOUR_GOOGLE_CLIENT_ID';
    const redirectUri = 'http://localhost:5173/oauth2/callback';
    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${encodeURIComponent(redirectUri)}&response_type=code&scope=email%20profile%20openid`;
    window.location.href = googleAuthUrl;
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '400px', padding: '40px', textAlign: 'left' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
          <LogIn size={28} style={{ color: 'var(--primary)' }} />
          <h2 style={{ fontSize: '24px', margin: 0, fontWeight: '700' }}>LifeOS Login</h2>
        </div>
        
        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--danger)', color: 'var(--danger)', padding: '12px', borderRadius: '8px', marginBottom: '16px', fontSize: '14px' }}>
            {error}
          </div>
        )}
        
        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label htmlFor="login-username" style={{ fontSize: '13px', fontWeight: '500', color: 'var(--text-muted)' }}>Username or Email</label>
            <input
              id="login-username"
              type="text"
              className="glass-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              placeholder="Enter username"
            />
          </div>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label htmlFor="login-password" style={{ fontSize: '13px', fontWeight: '500', color: 'var(--text-muted)' }}>Password</label>
            <input
              id="login-password"
              type="password"
              className="glass-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
            />
          </div>
          
          <button id="login-submit-btn" type="submit" className="btn-primary" disabled={loading} style={{ width: '100%', marginTop: '8px' }}>
            {loading ? 'Signing In...' : 'Sign In'}
          </button>
        </form>
        
        <div style={{ margin: '20px 0', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ flex: 1, height: '1px', background: 'var(--border-glow)' }}></div>
          <span style={{ fontSize: '12px', color: 'var(--text-dim)' }}>or</span>
          <div style={{ flex: 1, height: '1px', background: 'var(--border-glow)' }}></div>
        </div>
        
        <button id="google-login-btn" onClick={handleGoogleLogin} className="btn-secondary" style={{ width: '100%', display: 'flex', gap: '10px' }}>
          <svg style={{ width: '18px', height: '18px' }} viewBox="0 0 24 24">
            <path fill="#EA4335" d="M12 5.04c1.66 0 3.2.57 4.38 1.69l3.27-3.27C17.68 1.54 14.98 1 12 1 7.35 1 3.37 3.67 1.39 7.56l3.89 3.02C6.22 7.78 8.89 5.04 12 5.04z"/>
            <path fill="#4285F4" d="M23.49 12.27c0-.81-.07-1.59-.2-2.36H12v4.51h6.46c-.29 1.48-1.14 2.73-2.4 3.58l3.76 2.91c2.2-2.03 3.67-5.01 3.67-8.64z"/>
            <path fill="#FBBC05" d="M5.28 14.42c-.25-.76-.39-1.57-.39-2.42s.14-1.66.39-2.42L1.39 6.56C.5 8.34 0 10.32 0 12s.5 3.66 1.39 5.44l3.89-3.02z"/>
            <path fill="#34A853" d="M12 23c3.24 0 5.97-1.07 7.96-2.91l-3.76-2.91c-1.1.74-2.51 1.18-4.2 1.18-3.11 0-5.78-2.74-6.72-6.54L1.39 14.8C3.37 20.33 7.35 23 12 23z"/>
          </svg>
          Sign In with Google
        </button>
        
        <p style={{ marginTop: '24px', fontSize: '14px', color: 'var(--text-muted)', textAlign: 'center' }}>
          Don't have an account? <Link to="/register" style={{ color: 'var(--primary)', textDecoration: 'none', fontWeight: '500' }}>Register here</Link>
        </p>
      </div>
    </div>
  );
}
