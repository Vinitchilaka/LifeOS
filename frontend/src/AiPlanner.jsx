import React, { useState } from 'react';
import { aiApi } from './api';
import { BrainCircuit, Play, CheckCircle2, Calendar, Users, Trash } from 'lucide-react';

export default function AiPlanner() {
  const [prioritizedList, setPrioritizedList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handlePrioritize = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await aiApi.prioritize();
      // Expect Response wrapper containing prioritizedTasks list
      setPrioritizedList(response.prioritizedTasks || []);
    } catch (err) {
      setError(err.message || 'AI prioritization call failed. Make sure your OpenAI API Key is valid.');
    } finally {
      setLoading(false);
    }
  };

  // Group tasks by Eisenhower Matrix quadrants
  const doFirstTasks = prioritizedList.filter(t => t.quadrant === 'DO_FIRST');
  const scheduleTasks = prioritizedList.filter(t => t.quadrant === 'SCHEDULE');
  const delegateTasks = prioritizedList.filter(t => t.quadrant === 'DELEGATE');
  const eliminateTasks = prioritizedList.filter(t => t.quadrant === 'ELIMINATE');

  return (
    <div className="animate-fade-in" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <BrainCircuit size={28} style={{ color: 'var(--primary)' }} />
          <h2 style={{ fontSize: '24px', fontWeight: '700' }}>Spring AI Eisenhower Matrix Planner</h2>
        </div>
        
        <button id="ai-prioritize-btn" onClick={handlePrioritize} className="btn-primary" disabled={loading}>
          <Play size={16} /> {loading ? 'Running AI Diagnostics...' : 'Generate AI Plan'}
        </button>
      </div>

      {error && (
        <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--danger)', color: 'var(--danger)', padding: '16px', borderRadius: '12px', textAlign: 'left' }}>
          <p style={{ fontWeight: '600', marginBottom: '4px' }}>AI prioritization engine error</p>
          <span style={{ fontSize: '14px' }}>{error}</span>
        </div>
      )}

      {prioritizedList.length === 0 ? (
        <div className="glass-panel" style={{ padding: '60px 20px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
          <BrainCircuit size={48} style={{ color: 'var(--text-dim)' }} />
          <h3 style={{ fontSize: '18px', fontWeight: '600' }}>No AI Plan Loaded</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '14px', maxWidth: '400px' }}>
            Click the "Generate AI Plan" button above to send your pending tasks to OpenAI. 
            The system will analyze and sort them into the classic Eisenhower Matrix quadrants!
          </p>
        </div>
      ) : (
        <div className="quadrant-grid">
          
          {/* Quadrant 1: DO FIRST */}
          <div className="glass-panel quadrant-card quadrant-do" style={{ textAlign: 'left' }}>
            <h3 style={{ color: 'var(--danger)', fontSize: '18px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CheckCircle2 size={18} />
              Q1: Urgent & Important (Do First)
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {doFirstTasks.length === 0 ? (
                <p style={{ fontSize: '13px', color: 'var(--text-dim)' }}>No urgent tasks classified.</p>
              ) : (
                doFirstTasks.map((t, idx) => <TaskItem key={idx} task={t} />)
              )}
            </div>
          </div>

          {/* Quadrant 2: SCHEDULE */}
          <div className="glass-panel quadrant-card quadrant-schedule" style={{ textAlign: 'left' }}>
            <h3 style={{ color: 'var(--warning)', fontSize: '18px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Calendar size={18} />
              Q2: Important, Not Urgent (Schedule)
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {scheduleTasks.length === 0 ? (
                <p style={{ fontSize: '13px', color: 'var(--text-dim)' }}>No schedule tasks classified.</p>
              ) : (
                scheduleTasks.map((t, idx) => <TaskItem key={idx} task={t} />)
              )}
            </div>
          </div>

          {/* Quadrant 3: DELEGATE */}
          <div className="glass-panel quadrant-card quadrant-delegate" style={{ textAlign: 'left' }}>
            <h3 style={{ color: 'var(--info)', fontSize: '18px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Users size={18} />
              Q3: Urgent, Not Important (Delegate)
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {delegateTasks.length === 0 ? (
                <p style={{ fontSize: '13px', color: 'var(--text-dim)' }}>No delegate tasks classified.</p>
              ) : (
                delegateTasks.map((t, idx) => <TaskItem key={idx} task={t} />)
              )}
            </div>
          </div>

          {/* Quadrant 4: ELIMINATE */}
          <div className="glass-panel quadrant-card quadrant-eliminate" style={{ textAlign: 'left' }}>
            <h3 style={{ color: 'var(--text-muted)', fontSize: '18px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Trash size={18} />
              Q4: Neither (Eliminate)
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {eliminateTasks.length === 0 ? (
                <p style={{ fontSize: '13px', color: 'var(--text-dim)' }}>No tasks classified to eliminate.</p>
              ) : (
                eliminateTasks.map((t, idx) => <TaskItem key={idx} task={t} />)
              )}
            </div>
          </div>

        </div>
      )}

    </div>
  );
}

// Inner Component for Task Item Display
function TaskItem({ task }) {
  return (
    <div style={{ padding: '12px', border: '1px solid var(--border-glow)', borderRadius: '8px', background: 'rgba(255, 255, 255, 0.01)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '14px', fontWeight: '600' }}>{task.taskTitle}</span>
      </div>
      <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>{task.reasoning}</p>
    </div>
  );
}
