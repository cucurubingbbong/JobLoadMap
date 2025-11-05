const API_BASE_URL = window.API_BASE_URL || 'http://localhost:8080';

const storageKeys = {
  token: 'job2roadmap.token',
  displayName: 'job2roadmap.displayName'
};

function getAuthToken() {
  return localStorage.getItem(storageKeys.token);
}

function setAuthToken(token) {
  if (token) {
    localStorage.setItem(storageKeys.token, token);
  } else {
    localStorage.removeItem(storageKeys.token);
  }
}

function setDisplayName(name) {
  if (name) {
    localStorage.setItem(storageKeys.displayName, name);
  } else {
    localStorage.removeItem(storageKeys.displayName);
  }
}

function getDisplayName() {
  return localStorage.getItem(storageKeys.displayName);
}

async function apiRequest(path, options = {}) {
  const headers = options.headers ? { ...options.headers } : {};
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }
  const token = getAuthToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });
  if (!response.ok) {
    let message = '요청에 실패했습니다.';
    try {
      const errorBody = await response.json();
      if (errorBody && errorBody.message) {
        message = errorBody.message;
      }
    } catch (e) {
      // ignore
    }
    throw new Error(message);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

async function postJson(path, body) {
  return apiRequest(path, {
    method: 'POST',
    body: JSON.stringify(body),
  });
}

async function getJson(path) {
  return apiRequest(path, {
    method: 'GET',
  });
}

window.Job2RoadmapApi = {
  API_BASE_URL,
  storageKeys,
  getAuthToken,
  setAuthToken,
  setDisplayName,
  getDisplayName,
  apiRequest,
  postJson,
  getJson,
};
