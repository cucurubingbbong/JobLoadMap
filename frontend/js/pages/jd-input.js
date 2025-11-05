(function(api){
  const tabs = document.getElementById('inputTabs');
  if (!tabs) return;

  const urlGroup = document.getElementById('urlInputGroup');
  const textGroup = document.getElementById('textInputGroup');
  const dailyHours = document.getElementById('dailyHours');
  const dailyHoursValue = document.getElementById('dailyHoursValue');
  const generateBtn = document.getElementById('generateBtn');
  const errorBox = document.getElementById('jdError');

  tabs.querySelectorAll('.radio-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      tabs.querySelectorAll('.radio-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      const input = pill.querySelector('input');
      if (input.value === 'url') {
        urlGroup.style.display = 'grid';
        textGroup.style.display = 'none';
      } else {
        urlGroup.style.display = 'none';
        textGroup.style.display = 'grid';
      }
    });
  });

  document.querySelectorAll('#levelGroup .radio-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('#levelGroup .radio-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      pill.querySelector('input').checked = true;
    });
  });

  dailyHours.addEventListener('input', () => {
    dailyHoursValue.textContent = `${dailyHours.value}시간`;
  });

  generateBtn.addEventListener('click', async () => {
    errorBox.style.display = 'none';
    const inputType = tabs.querySelector('.radio-pill.active input').value;
    const payload = {
      jdUrl: inputType === 'url' ? document.getElementById('jdUrl').value.trim() : null,
      jdText: inputType === 'text' ? document.getElementById('jdText').value.trim() : null,
      targetRole: document.getElementById('targetRole').value.trim(),
      targetCompany: document.getElementById('targetCompany').value.trim(),
      preparationMonths: parseInt(document.getElementById('preparationMonths').value, 10),
      dailyStudyHours: parseInt(dailyHours.value, 10),
      currentLevel: document.querySelector('#levelGroup input:checked').value,
    };

    if (!payload.targetRole) {
      errorBox.textContent = '목표 직무를 입력해주세요.';
      errorBox.style.display = 'block';
      return;
    }

    if (!payload.jdUrl && !payload.jdText) {
      errorBox.textContent = '채용 공고 URL 또는 텍스트를 입력해주세요.';
      errorBox.style.display = 'block';
      return;
    }

    generateBtn.disabled = true;
    generateBtn.textContent = '생성 중...';

    try {
      const response = await api.postJson('/api/roadmaps/from-jd', payload);
      sessionStorage.setItem('job2roadmap.latestRoadmap', JSON.stringify(response));
      window.location.href = 'roadmap.html';
    } catch (error) {
      errorBox.textContent = error.message;
      errorBox.style.display = 'block';
    } finally {
      generateBtn.disabled = false;
      generateBtn.textContent = '로드맵 생성하기';
    }
  });
})(window.Job2RoadmapApi);
