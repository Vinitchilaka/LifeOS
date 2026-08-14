import React, { useState, useEffect } from 'react';
import { actuatorApi } from './api';
import { Activity, ShieldCheck, Database, HardDrive, Mail, Server } from 'lucide-react';

export default function ActuatorMonitor() {
  const [healthData, setHealthData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchHealthStatus();
  }, []);

  const fetchHealthStatus = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await actuatorApi.getHealth();
      setHealthData(data);
    } catch (err) {
      setError('Could not connect to Spring Boot Actuator. Make sure backend is running.');
    } finally {
      setLoading(false);
    }
  };

  const components = healthData?.components || {};

  return (
    <div className="animate-fade-in" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '800px', margin: '0 auto' }}>
      
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Activity size={28} style={{ color: 'var(--primary)' }} />
          <h2 style={{ fontSize: '24px', fontWeight: '700' }}>System Health Monitor</h2>
        </div>
        <button id="refresh-health-btn" onClick={fetchHealthStatus} className="btn-secondary" style={{ padding: '8px 16px' }}>Refresh</button>
      </div>

      {error && (
        <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--danger)', color: 'var(--danger)', padding: '16px', borderRadius: '12px', textAlign: 'left' }}>
          {error}
        </div>
      )}

      {healthData && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Main Status Panel */}
          <div className="glass-panel" style={{ padding: '32px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
            <Server size={48} style={{ color: healthData.status === 'UP' ? 'var(--secondary)' : 'var(--danger)' }} />
            <div>
              <h3 style={{ fontSize: '16px', color: 'var(--text-muted)', fontWeight: 500 }}>Overall Application Status</h3>
              <p style={{
                fontSize: '36px',
                fontWeight: 800,
                color: healthData.status === 'UP' ? 'var(--secondary)' : 'var(--danger)',
                textShadow: healthData.status === 'UP' ? '0 0 20px rgba(16,185,129,0.3)' : '0 0 20px rgba(239,68,68,0.3)'
              }}>{healthData.status}</p>
            </div>
          </div>

          {/* Subcomponents Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
            
            {/* Database indicator */}
            {components.db && (
              <div className="glass-panel" style={{ padding: '20px', textAlign: 'left', display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
                <Database size={24} style={{ color: components.db.status === 'UP' ? 'var(--secondary)' : 'var(--danger)', marginTop: '2px' }} />
                <div>
                  <h4 style={{ fontSize: '16px', fontWeight: '700' }}>Database Connection</h4>
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px' }}>
                    Status: <span style={{ fontWeight: '600', color: components.db.status === 'UP' ? 'var(--secondary)' : 'var(--danger)' }}>{components.db.status}</span>
                  </p>
                  {components.db.details && (
                    <p style={{ fontSize: '12px', color: 'var(--text-dim)', marginTop: '2px' }}>
                      Dialect: {components.db.details.database}
                    </p>
                  )}
                </div>
              </div>
            )}

            {/* DiskSpace indicator */}
            {components.diskSpace && (
              <div className="glass-panel" style={{ padding: '20px', textAlign: 'left', display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
                <HardDrive size={24} style={{ color: components.diskSpace.status === 'UP' ? 'var(--secondary)' : 'var(--danger)', marginTop: '2px' }} />
                <div>
                  <h4 style={{ fontSize: '16px', fontWeight: '700' }}>Disk Space</h4>
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px' }}>
                    Status: <span style={{ fontWeight: '600', color: components.diskSpace.status === 'UP' ? 'var(--secondary)' : 'var(--danger)' }}>{components.diskSpace.status}</span>
                  </p>
                  {components.diskSpace.details && (
                    <p style={{ fontSize: '12px', color: 'var(--text-dim)', marginTop: '2px' }}>
                      Free: {Math.round(components.diskSpace.details.free / (1024*1024*1024))} GB
                    </p>
                  )}
                </div>
              </div>
            )}

            {/* Custom Mail indicators */}
            {components.mailService && (
              <div className="glass-panel" style={{ padding: '20px', textAlign: 'left', display: 'flex', gap: '16px', alignItems: 'flex-start', gridColumn: 'span 2' }}>
                <Mail size={24} style={{ color: components.mailService.status === 'UP' ? 'var(--secondary)' : 'var(--danger)', marginTop: '2px' }} />
                <div style={{ flex: 1 }}>
                  <h4 style={{ fontSize: '16px', fontWeight: '700' }}>SMTP Email Service</h4>
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px' }}>
                    Status: <span style={{ fontWeight: '600', color: components.mailService.status === 'UP' ? 'var(--secondary)' : 'var(--danger)' }}>{components.mailService.status}</span>
                  </p>
                  {components.mailService.details && (
                    <div style={{ marginTop: '8px', padding: '10px', background: 'rgba(0,0,0,0.2)', borderRadius: '6px', fontSize: '12px', border: '1px solid var(--border-glow)' }}>
                      <p style={{ color: 'var(--text-muted)' }}>Reason: {components.mailService.details.reason}</p>
                      <p style={{ color: 'var(--text-dim)', marginTop: '2px' }}>Configured User: {components.mailService.details.configuredUser}</p>
                    </div>
                  )}
                </div>
              </div>
            )}

          </div>

        </div>
      )}

    </div>
  );
}
