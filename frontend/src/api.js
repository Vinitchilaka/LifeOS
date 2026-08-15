const BASE_URL = 'http://localhost:9091';

// In-memory or localStorage token storage helpers
export const getAccessToken = () => localStorage.getItem('access_token');
export const getRefreshToken = () => localStorage.getItem('refresh_token');

export const setTokens = (accessToken, refreshToken) => {
  localStorage.setItem('access_token', accessToken);
  localStorage.setItem('refresh_token', refreshToken);
};

export const clearTokens = () => {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  localStorage.removeItem('user_info');
};

// Generic fetch API wrapper with automatic token refresh on 401
export async function apiRequest(endpoint, options = {}) {
  const url = `${BASE_URL}${endpoint}`;
  
  // Set default headers
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  
  // Inject access token if available
  const token = getAccessToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  const fetchOptions = {
    ...options,
    headers,
  };
  
  let response = await fetch(url, fetchOptions);
  
  // If response is 401 (Unauthorized) and we have a refresh token, try to rotate tokens
  if (response.status === 401 && getRefreshToken()) {
    console.log('[API Interceptor] Access Token expired. Attempting token rotation...');
    const refreshSuccess = await attemptTokenRotation();
    
    if (refreshSuccess) {
      // Retry the original request with the fresh access token
      headers['Authorization'] = `Bearer ${getAccessToken()}`;
      response = await fetch(url, fetchOptions);
    } else {
      // Refresh token failed (expired or invalid), log out
      clearTokens();
      window.dispatchEvent(new Event('auth-logout'));
    }
  }
  
  return response;
}

// Function to call Auth Refresh endpoint
async function attemptTokenRotation() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;
  
  try {
    const response = await fetch(`${BASE_URL}/api/v1/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    
    if (response.ok) {
      const data = await response.json();
      // Expect token rotation schema: data.accessToken, data.refreshToken
      const accessToken = data.accessToken || data.token;
      if (accessToken && data.refreshToken) {
        console.log('[API Interceptor] Token rotation successful!');
        setTokens(accessToken, data.refreshToken);
        return true;
      }
    }
  } catch (error) {
    console.error('[API Interceptor ERROR] Token rotation call failed:', error);
  }
  return false;
}

// Mapped API Endpoints
export const authApi = {
  login: async (username, password) => {
    const res = await apiRequest('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
    
    if (!res.ok) throw new Error('Invalid login credentials');
    const data = await res.json();
    const accessToken = data.accessToken || data.token;
    setTokens(accessToken, data.refreshToken);
    localStorage.setItem('user_info', JSON.stringify({ username: data.username }));
    return data;
  },
  
  register: async (userData) => {
    const res = await apiRequest('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || 'Registration failed');
    }
    return await res.json();
  },
  
  loginWithGoogle: async (code, redirectUri) => {
    const res = await apiRequest('/api/v1/auth/oauth2/google', {
      method: 'POST',
      body: JSON.stringify({ code, redirectUri })
    });
    if (!res.ok) throw new Error('Google authentication failed');
    const data = await res.json();
    const accessToken = data.accessToken || data.token;
    setTokens(accessToken, data.refreshToken);
    localStorage.setItem('user_info', JSON.stringify({ username: data.username }));
    return data;
  }
};

export const userApi = {
  getProfile: async () => {
    const res = await apiRequest('/api/v1/users/me');
    if (!res.ok) throw new Error('Failed to load profile');
    return await res.json();
  },
  
  updateProfile: async (profileData) => {
    const res = await apiRequest('/api/v1/users/me', {
      method: 'PUT',
      body: JSON.stringify(profileData)
    });
    if (!res.ok) throw new Error('Failed to update profile');
    return await res.json();
  },
  
  getPreferences: async () => {
    const res = await apiRequest('/api/v1/users/me/preferences');
    if (!res.ok) throw new Error('Failed to load preferences');
    return await res.json();
  },
  
  updatePreferences: async (preferenceData) => {
    const res = await apiRequest('/api/v1/users/me/preferences', {
      method: 'PUT',
      body: JSON.stringify(preferenceData)
    });
    if (!res.ok) throw new Error('Failed to update preferences');
    return await res.json();
  }
};

export const projectApi = {
  getProjects: async () => {
    const res = await apiRequest('/api/v1/projects');
    if (!res.ok) throw new Error('Failed to load projects');
    return await res.json();
  },
  
  createProject: async (title, description) => {
    const res = await apiRequest('/api/v1/projects', {
      method: 'POST',
      body: JSON.stringify({ title, description })
    });
    if (!res.ok) throw new Error('Failed to create project');
    return await res.json();
  }
};

const formatLocalDateTime = (dateStr) => {
  if (!dateStr) return null;
  if (dateStr.includes('T')) return dateStr;
  return `${dateStr}T00:00:00`;
};

export const goalApi = {
  getGoals: async () => {
    const res = await apiRequest('/api/v1/goals');
    if (!res.ok) throw new Error('Failed to load goals');
    return await res.json();
  },
  
  createGoal: async (title, description, targetDate, status = 'PENDING') => {
    const res = await apiRequest('/api/v1/goals', {
      method: 'POST',
      body: JSON.stringify({
        title,
        description,
        targetDate: formatLocalDateTime(targetDate),
        status
      })
    });
    if (!res.ok) throw new Error('Failed to create goal');
    return await res.json();
  }
};

export const taskApi = {
  getTasks: async (page = 1, size = 10, sortBy = 'id', sortDir = 'ASC', search = '') => {
    // Note: page is 0-indexed on Spring Boot, page query parameter matches UserService mappings
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
      search
    });
    const res = await apiRequest(`/api/v1/tasks?${params.toString()}`);
    if (!res.ok) throw new Error('Failed to load tasks');
    return await res.json();
  },
  
  createTask: async (taskData) => {
    const payload = {
      ...taskData,
      dueDate: formatLocalDateTime(taskData.dueDate)
    };
    const res = await apiRequest('/api/v1/tasks', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Failed to create task');
    return await res.json();
  },
  
  updateTask: async (taskId, taskData) => {
    const payload = {
      ...taskData,
      dueDate: formatLocalDateTime(taskData.dueDate)
    };
    const res = await apiRequest(`/api/v1/tasks/${taskId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Failed to update task');
    return await res.json();
  },
  
  deleteTask: async (taskId) => {
    const res = await apiRequest(`/api/v1/tasks/${taskId}`, {
      method: 'DELETE'
    });
    if (!res.ok) throw new Error('Failed to delete task');
    return true;
  }
};

export const aiApi = {
  prioritize: async () => {
    const res = await apiRequest('/api/v1/ai/prioritize', {
      method: 'POST'
    });
    if (!res.ok) throw new Error('Failed to prioritize tasks');
    return await res.json();
  }
};

export const actuatorApi = {
  getHealth: async () => {
    // Note: health actuator endpoint is public
    const res = await fetch(`${BASE_URL}/actuator/health`);
    if (!res.ok && res.status !== 503) throw new Error('Failed to load actuator health');
    return await res.json();
  },
  
  getInfo: async () => {
    const res = await fetch(`${BASE_URL}/actuator/info`);
    if (!res.ok) throw new Error('Failed to load actuator info');
    return await res.json();
  }
};
