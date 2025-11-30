// Allow overriding API endpoint at deploy time.
// Render static site build can write window.API_BASE before script.js loads.
window.API_BASE = window.API_BASE || (window.location.origin.includes('localhost') ? 'http://localhost:8080' : '');
