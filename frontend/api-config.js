// Allow overriding API endpoint and frontend base at deploy time.
// Render static site build can write window.API_BASE / window.FRONTEND_BASE before script.js loads.
window.API_BASE = window.API_BASE || (window.location.origin.includes('localhost') ? 'http://localhost:8080' : '');
window.FRONTEND_BASE = window.FRONTEND_BASE || window.location.origin;
