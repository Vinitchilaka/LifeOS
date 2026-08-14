import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from './api';
import { UserPlus } from 'lucide-react';

export default function Register() {
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: '',
    mobileNo: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    // Client-side validations
    if (formData.firstName.length < 2 || formData.firstName.length > 50) {
      setError('First name must be between 2 and 50 characters.');
      return;
    }
    
    if (formData.mobileNo && !/^\d{10}$/.test(formData.mobileNo)) {
      setError('Mobile number must be exactly 10 digits.');
      return;
    }

    setLoading(true);
    try {
      await authApi.register(formData);
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err.message || 'Registration failed. Please check your parameters.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh', padding: '20px 0' }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '480px', padding: '40px', textAlign: 'left' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
          <UserPlus size={28} style={{ color: 'var(--primary)' }} />
          <h2 style={{ fontSize: '24px', margin: 0, fontWeight: '700' }}>Create Account</h2>
        </div>
        
        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--danger)', color: 'var(--danger)', padding: '12px', borderRadius: '8px', marginBottom: '16px', fontSize: '14px' }}>
            {error}
          </div>
        )}

        {success && (
          <div style={{ background: 'rgba(16, 185, 129, 0.1)', border: '1px solid var(--secondary)', color: 'var(--secondary)', padding: '12px', borderRadius: '8px', marginBottom: '16px', fontSize: '14px' }}>
            {success}
          </div>
        )}
        
        <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="reg-firstname" style={{ fontSize: '12px', fontWeight: '500', color: 'var(--text-muted)' }}>First Name</label>
              <input
                id="reg-firstname"
                type="text"
                name="firstName"
                className="glass-input"
                value={formData.firstName}
                onChange={handleInputChange}
                required
                placeholder="John"
              />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="reg-lastname" style={{ fontSize: '12px', fontWeight: '500', color: 'var(--text-muted)' }}>Last Name</label>
              <input
                id="reg-lastname"
                type="text"
                name="lastName"
                className="glass-input"
                value={formData.lastName}
                onChange={handleInputChange}
                placeholder="Doe"
              />
            </div>
          </div>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label htmlFor="reg-email" style={{ fontSize: '12px', fontWeight: '500', color: 'var(--text-muted)' }}>Email Address</label>
            <input
              id="reg-email"
              type="email"
              name="email"
              className="glass-input"
              value={formData.email}
              onChange={handleInputChange}
              required
              placeholder="john.doe@example.com"
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label htmlFor="reg-username" style={{ fontSize: '12px', fontWeight: '500', color: 'var(--text-muted)' }}>Username</label>
            <input
              id="reg-username"
              type="text"
              name="username"
              className="glass-input"
              value={formData.username}
              onChange={handleInputChange}
              required
              placeholder="johndoe"
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label htmlFor="reg-password" style={{ fontSize: '12px', fontWeight: '500', color: 'var(--text-muted)' }}>Password</label>
            <input
              id="reg-password"
              type="password"
              name="password"
              className="glass-input"
              value={formData.password}
              onChange={handleInputChange}
              required
              placeholder="Min 6 characters"
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <label htmlFor="reg-mobileno" style={{ fontSize: '12px', fontWeight: '500', color: 'var(--text-muted)' }}>Mobile Number (Optional)</label>
            <input
              id="reg-mobileno"
              type="text"
              name="mobileNo"
              className="glass-input"
              value={formData.mobileNo}
              onChange={handleInputChange}
              placeholder="10 digit number"
            />
          </div>
          
          <button id="register-submit-btn" type="submit" className="btn-primary" disabled={loading} style={{ width: '100%', marginTop: '10px' }}>
            {loading ? 'Creating Account...' : 'Register'}
          </button>
        </form>
        
        <p style={{ marginTop: '20px', fontSize: '14px', color: 'var(--text-muted)', textAlign: 'center' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--primary)', textDecoration: 'none', fontWeight: '500' }}>Login here</Link>
        </p>
      </div>
    </div>
  );
}
