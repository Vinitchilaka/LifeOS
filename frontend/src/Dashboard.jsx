import React, { useState, useEffect } from 'react';
import { projectApi, goalApi, taskApi } from './api';
import { Plus, Trash2, Edit, CheckSquare, ListTodo, Target, FolderHeart, ArrowUpDown, ChevronLeft, ChevronRight, Search } from 'lucide-react';

export default function Dashboard() {
  const [projects, setProjects] = useState([]);
  const [goals, setGoals] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Pagination & Filters State
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('id');
  const [sortDir, setSortDir] = useState('ASC');

  // Creation States
  const [newProject, setNewProject] = useState({ title: '', description: '' });
  const [newGoal, setNewGoal] = useState({ title: '', description: '', targetDate: '' });
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    dueDate: '',
    priority: 'MEDIUM',
    status: 'PENDING',
    projectId: '',
    goalId: ''
  });

  const [activeTab, setActiveTab] = useState('tasks'); // 'tasks' | 'goals' | 'projects'

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    fetchTasksList();
  }, [page, sortBy, sortDir]);

  const fetchInitialData = async () => {
    setLoading(true);
    setError('');
    try {
      const projs = await projectApi.getProjects();
      setProjects(projs);

      const gls = await goalApi.getGoals();
      setGoals(gls);

      await fetchTasksList();
    } catch (err) {
      setError(err.message || 'Error loading dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  const fetchTasksList = async () => {
    try {
      const data = await taskApi.getTasks(page - 1, 10, sortBy, sortDir, search);
      // Backend returns PaginatedTaskResponse containing content and paging metadata
      setTasks(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err) {
      console.error('Failed to load tasks:', err);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(1);
    fetchTasksList();
  };

  // Creation triggers
  const handleCreateProject = async (e) => {
    e.preventDefault();
    if (!newProject.title.trim()) return;
    try {
      const created = await projectApi.createProject(newProject.title, newProject.description);
      setProjects(prev => [...prev, created]);
      setNewProject({ title: '', description: '' });
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCreateGoal = async (e) => {
    e.preventDefault();
    if (!newGoal.title.trim()) return;
    try {
      const created = await goalApi.createGoal(newGoal.title, newGoal.description, newGoal.targetDate);
      setGoals(prev => [...prev, created]);
      setNewGoal({ title: '', description: '', targetDate: '' });
    } catch (err) {
      setError(err.message);
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    if (!newTask.title.trim()) return;
    
    const taskPayload = {
      ...newTask,
      projectId: newTask.projectId ? parseInt(newTask.projectId) : null,
      goalId: newTask.goalId ? parseInt(newTask.goalId) : null
    };

    try {
      await taskApi.createTask(taskPayload);
      setNewTask({
        title: '',
        description: '',
        dueDate: '',
        priority: 'MEDIUM',
        status: 'PENDING',
        projectId: '',
        goalId: ''
      });
      fetchTasksList();
      
      // Auto-progress trigger: if task is linked to a goal, reload goals to show updated progress bar
      if (taskPayload.goalId) {
        const gls = await goalApi.getGoals();
        setGoals(gls);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  const handleStatusChange = async (task, newStatus) => {
    try {
      await taskApi.updateTask(task.id, {
        title: task.title,
        description: task.description,
        dueDate: task.dueDate,
        priority: task.priority,
        status: newStatus,
        projectId: task.projectId,
        goalId: task.goalId
      });
      fetchTasksList();
      
      // Reload goals since progress percentages change when task status is set to COMPLETED
      const gls = await goalApi.getGoals();
      setGoals(gls);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteTask = async (taskId, linkedGoalId) => {
    if (!window.confirm('Are you sure you want to delete this task?')) return;
    try {
      await taskApi.deleteTask(taskId);
      fetchTasksList();
      
      if (linkedGoalId) {
        const gls = await goalApi.getGoals();
        setGoals(gls);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="animate-fade-in" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Overview Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px' }}>
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ background: 'rgba(99, 102, 241, 0.1)', padding: '12px', borderRadius: '12px' }}>
            <ListTodo size={24} style={{ color: 'var(--primary)' }} />
          </div>
          <div>
            <h4 style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 500 }}>Tasks Loaded</h4>
            <p style={{ fontSize: '24px', fontWeight: 700 }}>{tasks.length}</p>
          </div>
        </div>
        
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ background: 'rgba(16, 185, 129, 0.1)', padding: '12px', borderRadius: '12px' }}>
            <Target size={24} style={{ color: 'var(--secondary)' }} />
          </div>
          <div>
            <h4 style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 500 }}>Active Goals</h4>
            <p style={{ fontSize: '24px', fontWeight: 700 }}>{goals.length}</p>
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ background: 'rgba(6, 182, 212, 0.1)', padding: '12px', borderRadius: '12px' }}>
            <FolderHeart size={24} style={{ color: 'var(--info)' }} />
          </div>
          <div>
            <h4 style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 500 }}>Total Projects</h4>
            <p style={{ fontSize: '24px', fontWeight: 700 }}>{projects.length}</p>
          </div>
        </div>
      </div>

      {error && (
        <div style={{ background: 'rgba(239, 68, 68, 0.1)', border: '1px solid var(--danger)', color: 'var(--danger)', padding: '12px', borderRadius: '8px' }}>
          {error}
        </div>
      )}

      {/* Main Content Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '24px' }}>
        
        {/* Left Side: Tasks Operations */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: 700 }}>Tasks Planner</h3>
            
            {/* Search Filter */}
            <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '8px' }}>
              <div style={{ position: 'relative' }}>
                <input
                  id="task-search-input"
                  type="text"
                  placeholder="Search tasks..."
                  className="glass-input"
                  style={{ paddingLeft: '36px', height: '40px', width: '200px' }}
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
                <Search size={16} style={{ position: 'absolute', left: '12px', top: '12px', color: 'var(--text-dim)' }} />
              </div>
              <button id="task-search-btn" type="submit" className="btn-secondary" style={{ height: '40px', padding: '0 16px' }}>Go</button>
            </form>
          </div>

          {/* Add Task Quick Form */}
          <form id="create-task-form" onSubmit={handleCreateTask} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px', marginBottom: '24px', background: 'rgba(255, 255, 255, 0.02)', padding: '16px', borderRadius: '12px', border: '1px solid var(--border-glow)' }}>
            <input
              id="new-task-title"
              type="text"
              placeholder="Task title"
              className="glass-input"
              style={{ gridColumn: 'span 3' }}
              value={newTask.title}
              onChange={(e) => setNewTask(prev => ({ ...prev, title: e.target.value }))}
              required
            />
            <input
              id="new-task-description"
              type="text"
              placeholder="Description (Optional)"
              className="glass-input"
              style={{ gridColumn: 'span 3' }}
              value={newTask.description}
              onChange={(e) => setNewTask(prev => ({ ...prev, description: e.target.value }))}
            />
            <input
              id="new-task-duedate"
              type="date"
              className="glass-input"
              value={newTask.dueDate}
              onChange={(e) => setNewTask(prev => ({ ...prev, dueDate: e.target.value }))}
            />
            <select
              id="new-task-priority"
              className="glass-input"
              value={newTask.priority}
              onChange={(e) => setNewTask(prev => ({ ...prev, priority: e.target.value }))}
            >
              <option value="LOW">Low Priority</option>
              <option value="MEDIUM">Medium Priority</option>
              <option value="HIGH">High Priority</option>
            </select>
            <select
              id="new-task-status"
              className="glass-input"
              value={newTask.status}
              onChange={(e) => setNewTask(prev => ({ ...prev, status: e.target.value }))}
            >
              <option value="PENDING">Pending</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
              <option value="PARKED">Parked</option>
            </select>
            <select
              id="new-task-project"
              className="glass-input"
              value={newTask.projectId}
              onChange={(e) => setNewTask(prev => ({ ...prev, projectId: e.target.value }))}
            >
              <option value="">No Project</option>
              {projects.map(p => <option key={p.id} value={p.id}>{p.title}</option>)}
            </select>
            <select
              id="new-task-goal"
              className="glass-input"
              value={newTask.goalId}
              onChange={(e) => setNewTask(prev => ({ ...prev, goalId: e.target.value }))}
            >
              <option value="">No Goal</option>
              {goals.map(g => <option key={g.id} value={g.id}>{g.title}</option>)}
            </select>
            <button id="add-task-btn" type="submit" className="btn-primary" style={{ display: 'flex', gap: '6px' }}>
              <Plus size={16} /> Add Task
            </button>
          </form>

          {/* Sorter Config controls */}
          <div style={{ display: 'flex', gap: '16px', alignItems: 'center', marginBottom: '16px', fontSize: '13px', color: 'var(--text-muted)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <ArrowUpDown size={14} />
              <span>Sort By:</span>
              <select id="task-sort-by" className="glass-input" style={{ padding: '4px 8px', fontSize: '12px' }} value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                <option value="id">Creation ID</option>
                <option value="title">Title</option>
                <option value="dueDate">Due Date</option>
                <option value="priority">Priority</option>
                <option value="status">Status</option>
              </select>
            </div>
            <button id="task-sort-dir-btn" className="btn-secondary" style={{ padding: '6px 12px', fontSize: '12px' }} onClick={() => setSortDir(prev => prev === 'ASC' ? 'DESC' : 'ASC')}>
              Order: {sortDir}
            </button>
          </div>

          {/* Task List */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {tasks.length === 0 ? (
              <p style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '20px 0' }}>No tasks found. Add a task to get started!</p>
            ) : (
              tasks.map(task => (
                <div key={task.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px', border: '1px solid var(--border-glow)', borderRadius: '12px', background: 'rgba(255,255,255,0.01)' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', textAlign: 'left' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <span style={{ fontWeight: '600', fontSize: '15px', color: 'var(--text-main)' }}>{task.title}</span>
                      <span style={{
                        fontSize: '11px',
                        fontWeight: '700',
                        padding: '2px 8px',
                        borderRadius: '12px',
                        background: task.priority === 'HIGH' ? 'rgba(239, 68, 68, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                        color: task.priority === 'HIGH' ? 'var(--danger)' : 'var(--warning)'
                      }}>{task.priority}</span>
                    </div>
                    {task.description && <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>{task.description}</p>}
                    <div style={{ display: 'flex', gap: '12px', fontSize: '12px', color: 'var(--text-dim)' }}>
                      {task.dueDate && <span>Due: {task.dueDate}</span>}
                      {task.projectTitle && <span>Project: {task.projectTitle}</span>}
                      {task.goalTitle && <span>Goal: {task.goalTitle}</span>}
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <select
                      id={`task-status-${task.id}`}
                      className="glass-input"
                      style={{ padding: '6px 12px', fontSize: '13px' }}
                      value={task.status}
                      onChange={(e) => handleStatusChange(task, e.target.value)}
                    >
                      <option value="PENDING">Pending</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="PARKED">Parked</option>
                    </select>
                    <button
                      id={`delete-task-btn-${task.id}`}
                      className="btn-danger"
                      style={{ padding: '8px' }}
                      onClick={() => handleDeleteTask(task.id, task.goalId)}
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', marginTop: '24px', alignItems: 'center' }}>
              <button
                id="task-prev-page"
                className="btn-secondary"
                disabled={page === 1}
                onClick={() => setPage(prev => Math.max(1, prev - 1))}
                style={{ padding: '8px' }}
              >
                <ChevronLeft size={16} />
              </button>
              <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>Page {page} of {totalPages}</span>
              <button
                id="task-next-page"
                className="btn-secondary"
                disabled={page === totalPages}
                onClick={() => setPage(prev => Math.min(totalPages, prev + 1))}
                style={{ padding: '8px' }}
              >
                <ChevronRight size={16} />
              </button>
            </div>
          )}
        </div>

        {/* Right Side: Goals & Projects Info */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Active Goals Section */}
          <div className="glass-panel" style={{ padding: '20px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Target size={18} style={{ color: 'var(--secondary)' }} />
              Goals Progress
            </h3>

            {/* Quick Goal Creation */}
            <form id="create-goal-form" onSubmit={handleCreateGoal} style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '20px' }}>
              <input
                id="new-goal-title"
                type="text"
                placeholder="New Goal Title..."
                className="glass-input"
                style={{ padding: '8px 12px' }}
                value={newGoal.title}
                onChange={(e) => setNewGoal(prev => ({ ...prev, title: e.target.value }))}
                required
              />
              <button id="add-goal-btn" type="submit" className="btn-secondary" style={{ padding: '8px' }}>
                <Plus size={14} /> Add Goal
              </button>
            </form>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {goals.length === 0 ? (
                <p style={{ fontSize: '12px', color: 'var(--text-dim)' }}>No active goals configured.</p>
              ) : (
                goals.map(goal => (
                  <div key={goal.id} style={{ display: 'flex', flexDirection: 'column', gap: '6px', textAlign: 'left' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                      <span style={{ fontWeight: '600' }}>{goal.title}</span>
                      <span style={{ color: 'var(--secondary)', fontWeight: '700' }}>{Math.round(goal.progressPercentage)}%</span>
                    </div>
                    {/* Progress Bar */}
                    <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                      <div style={{
                        width: `${goal.progressPercentage}%`,
                        height: '100%',
                        background: 'linear-gradient(90deg, var(--secondary) 0%, #34d399 100%)',
                        transition: 'width 0.4s ease'
                      }}></div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Projects Folder Section */}
          <div className="glass-panel" style={{ padding: '20px' }}>
            <h3 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <FolderHeart size={18} style={{ color: 'var(--info)' }} />
              Projects
            </h3>

            {/* Quick Project Creation */}
            <form id="create-project-form" onSubmit={handleCreateProject} style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '20px' }}>
              <input
                id="new-project-title"
                type="text"
                placeholder="New Project Title..."
                className="glass-input"
                style={{ padding: '8px 12px' }}
                value={newProject.title}
                onChange={(e) => setNewProject(prev => ({ ...prev, title: e.target.value }))}
                required
              />
              <button id="add-project-btn" type="submit" className="btn-secondary" style={{ padding: '8px' }}>
                <Plus size={14} /> Add Project
              </button>
            </form>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', textAlign: 'left' }}>
              {projects.length === 0 ? (
                <p style={{ fontSize: '12px', color: 'var(--text-dim)' }}>No active projects.</p>
              ) : (
                projects.map(proj => (
                  <div key={proj.id} style={{ padding: '8px 12px', border: '1px solid var(--border-glow)', borderRadius: '8px', background: 'rgba(255,255,255,0.01)' }}>
                    <span style={{ fontSize: '14px', fontWeight: '500' }}>{proj.title}</span>
                  </div>
                ))
              )}
            </div>
          </div>

        </div>

      </div>

    </div>
  );
}
