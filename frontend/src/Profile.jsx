import React, { useState, useEffect } from 'react';
import { userApi } from './api';
import { User, Settings, Save, AlertCircle } from 'lucide-react';

export default function Profile() {
  const [profile, setProfile] = useState({ firstName: '', lastName: '', mobileNo: '', email: '', username: '' });
  const [preferences, setPreferences] = useState({ theme: 'DARK', language: 'en', timezone: 'UTC', emailNotifications: true });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchProfileAndPrefs();
  }, []);

  const fetchProfileAndPrefs = async () => {
    setLoading(true);
    setError('');
    try {
      const prof = await userApi.getProfile();
      setProfile(prof);
      
      const prefs = await userApi.getPreferences();
      setPreferences(prefs);
    } catch (err) {
      setError('Failed to load settings data.');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    // Validations
    if (profile.firstName.length < 2 || profile.firstName.length > 50) {
      setError('First name must be between 2 and 50 characters.');
      return;
    }
    if (profile.mobileNo && !/^\d{10}$/.test(profile.mobileNo)) {
      setError('Mobile number must be exactly 10 digits.');
      return;
    }

    try {
      const updated = await userApi.updateProfile({
        firstName: profile.firstName,
        lastName: profile.lastName,
        mobileNo: profile.mobileNo
      });
      setProfile(updated);
      setSuccess('Profile updated successfully!');
    } catch (err) {
      setError(err.message || 'Failed to update profile.');
    }
  };

  const handlePreferencesSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const updated = await userApi.updatePreferences(preferences);
      setPreferences(updated);
      setSuccess('Preferences updated successfully!');
      
      // Update body background or theme class globally based on selected theme
      if (updated.theme === 'LIGHT') {
        document.documentElement.style.setProperty('--bg-main', '#f9fafb');
        document.documentElement.style.setProperty('--text-main', '#111827');
        document.documentElement.style.setProperty('--bg-card', '#ffffff');
      } else {
        document.documentElement.style.setProperty('--bg-main', '#0b0c10');
        document.documentElement.style.setProperty('--text-main', '#f3f4f6');
        document.documentElement.style.setProperty('--bg-card', 'rgba(22, 28, 45, 0.4)');
      }
    } catch (err) {
      setError(err.message || 'Failed to update preferences.');
    }
  };

  return (
    <div className="animate-fade-in" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '800px', margin: '0 auto' }}>
      
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <Settings size={28} style={{ color: 'var(--primary)' }} />
        <h2 style={{ fontSize: '24px', fontWeight: '700' }}>Account Settings</h2>
      </div>

      {error && (
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--danger)', color: 'var(--danger)', padding: '12px', borderRadius: '8px' }}>
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div style={{ background: 'rgba(16, 185, 129, 0.1)', border: '1px solid var(--secondary)', color: 'var(--secondary)', padding: '12px', borderRadius: '8px' }}>
          {success}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        
        {/* Profile Card */}
        <div className="glass-panel" style={{ padding: '24px', textAlign: 'left' }}>
          <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '20px', display: 'flex', gap: '8px', alignItems: 'center' }}>
            <User size={18} style={{ color: 'var(--primary)' }} />
            Profile Details
          </h3>
          
          <form onSubmit={handleProfileSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="profile-firstname" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>First Name</label>
              <input
                id="profile-firstname"
                type="text"
                className="glass-input"
                value={profile.firstName}
                onChange={(e) => setProfile(prev => ({ ...prev, firstName: e.target.value }))}
                required
              />
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="profile-lastname" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Last Name</label>
              <input
                id="profile-lastname"
                type="text"
                className="glass-input"
                value={profile.lastName || ''}
                onChange={(e) => setProfile(prev => ({ ...prev, lastName: e.target.value }))}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="profile-mobileno" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Mobile Number</label>
              <input
                id="profile-mobileno"
                type="text"
                className="glass-input"
                value={profile.mobileNo || ''}
                onChange={(e) => setProfile(prev => ({ ...prev, mobileNo: e.target.value }))}
                placeholder="10 digit number"
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', opacity: 0.6 }}>
              <label htmlFor="profile-email" style={{ fontSize: '12px', color: 'var(--text-dim)' }}>Email (Read Only)</label>
              <input id="profile-email" type="text" className="glass-input" value={profile.email} disabled />
            </div>

            <button id="save-profile-btn" type="submit" className="btn-primary" style={{ marginTop: '8px' }}>
              <Save size={16} /> Save Profile
            </button>
          </form>
        </div>

        {/* Preferences Card */}
        <div className="glass-panel" style={{ padding: '24px', textAlign: 'left' }}>
          <h3 style={{ fontSize: '18px', fontWeight: '700', marginBottom: '20px', display: 'flex', gap: '8px', alignItems: 'center' }}>
            <Settings size={18} style={{ color: 'var(--secondary)' }} />
            System Preferences
          </h3>
          
          <form onSubmit={handlePreferencesSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="pref-theme" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Theme Mode</label>
              <select
                id="pref-theme"
                className="glass-input"
                value={preferences.theme}
                onChange={(e) => setPreferences(prev => ({ ...prev, theme: e.target.value }))}
              >
                <option value="DARK">Dark Glow</option>
                <option value="LIGHT">Clean Light</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="pref-lang" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Language</label>
              <select
                id="pref-lang"
                className="glass-input"
                value={preferences.language}
                onChange={(e) => setPreferences(prev => ({ ...prev, language: e.target.value }))}
              >
                <option value="en">English (US)</option>
                <option value="es">Español</option>
                <option value="fr">Français</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <label htmlFor="pref-timezone" style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Timezone</label>
              <input
                id="pref-timezone"
                type="text"
                className="glass-input"
                value={preferences.timezone}
                onChange={(e) => setPreferences(prev => ({ ...prev, timezone: e.target.value }))}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', margin: '8px 0' }}>
              <input
                id="pref-notifications"
                type="checkbox"
                style={{ width: '18px', height: '18px', cursor: 'pointer' }}
                checked={preferences.emailNotifications}
                onChange={(e) => setPreferences(prev => ({ ...prev, emailNotifications: e.target.checked }))}
              />
              <label htmlFor="pref-notifications" style={{ fontSize: '14px', color: 'var(--text-muted)', cursor: 'pointer' }}>Enable email summaries</label>
            </div>

            <button id="save-preferences-btn" type="submit" className="btn-primary" style={{ marginTop: '8px' }}>
              <Save size={16} /> Save Preferences
            </button>
          </form>
        </div>

      </div>

    </div>
  );
}
