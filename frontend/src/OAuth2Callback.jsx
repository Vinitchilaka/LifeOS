import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from './api';
import { Loader2 } from 'lucide-react';

export default function OAuth2Callback({ onLoginSuccess }) {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('Authenticating with Google...');

  useEffect(() => {
    const code = searchParams.get('code');
    if (code) {
      exchangeCode(code);
    } else {
      setStatus('No OAuth2 authorization code found.');
      setTimeout(() => navigate('/login'), 2000);
    }
  }, [searchParams]);

  const exchangeCode = async (code) => {
    try {
      const redirectUri = 'http://localhost:5173/oauth2/callback';
      await authApi.loginWithGoogle(code, redirectUri);
      onLoginSuccess();
      navigate('/');
    } catch (err) {
      console.error(err);
      setStatus('Google authentication failed. Redirecting...');
      setTimeout(() => navigate('/login'), 2000);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh', gap: '16px' }}>
      <Loader2 size={36} className="animate-spin" style={{ color: 'var(--primary)' }} />
      <p style={{ fontSize: '16px', color: 'var(--text-muted)' }}>{status}</p>
    </div>
  );
}
