(function(api){
  const form = document.getElementById('authForm');
  if (!form) return;

  const loginTab = document.getElementById('loginTab');
  const registerTab = document.getElementById('registerTab');
  const displayNameGroup = document.getElementById('displayNameGroup');
  const confirmPasswordGroup = document.getElementById('confirmPasswordGroup');
  const submitBtn = document.getElementById('submitBtn');
  const errorBox = document.getElementById('authError');

  let mode = 'login';

  loginTab.addEventListener('click', () => setMode('login'));
  registerTab.addEventListener('click', () => setMode('register'));

  function setMode(nextMode) {
    mode = nextMode;
    loginTab.classList.toggle('active', mode === 'login');
    registerTab.classList.toggle('active', mode === 'register');
    displayNameGroup.style.display = mode === 'register' ? 'grid' : 'none';
    confirmPasswordGroup.style.display = mode === 'register' ? 'grid' : 'none';
    submitBtn.textContent = mode === 'login' ? '로그인' : '회원가입';
    errorBox.style.display = 'none';
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    errorBox.style.display = 'none';
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();

    if (mode === 'register') {
      const displayName = document.getElementById('displayName').value.trim();
      const confirmPassword = document.getElementById('confirmPassword').value.trim();
      if (!displayName) {
        showError('닉네임을 입력해주세요.');
        return;
      }
      if (password !== confirmPassword) {
        showError('비밀번호가 일치하지 않습니다.');
        return;
      }
      await authenticate('/api/auth/register', { email, password, displayName });
    } else {
      await authenticate('/api/auth/login', { email, password });
    }
  });

  async function authenticate(path, payload) {
    submitBtn.disabled = true;
    submitBtn.textContent = mode === 'login' ? '로그인 중...' : '가입 중...';
    try {
      const response = await api.postJson(path, payload);
      api.setAuthToken(response.token);
      api.setDisplayName(response.displayName);
      window.location.href = 'my-roadmaps.html';
    } catch (error) {
      showError(error.message);
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = mode === 'login' ? '로그인' : '회원가입';
    }
  }

  function showError(message) {
    errorBox.textContent = message;
    errorBox.style.display = 'block';
  }
})(window.Job2RoadmapApi);
