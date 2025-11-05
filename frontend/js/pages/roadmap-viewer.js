(function(api, ui){
  const accordion = document.getElementById('roadmapAccordion');
  if (!accordion) return;

  const titleEl = document.getElementById('roadmapTitle');
  const totalWeeksEl = document.getElementById('totalWeeks');
  const dailyHoursEl = document.getElementById('dailyHours');
  const currentLevelEl = document.getElementById('currentLevel');
  const targetCompanyEl = document.getElementById('targetCompany');
  const sourceUrlEl = document.getElementById('sourceUrl');
  const createdAtEl = document.getElementById('createdAt');
  const overallProgressText = document.getElementById('overallProgress');
  const progressBar = document.getElementById('overallProgressBar');
  const saveButton = document.getElementById('saveRoadmapBtn');
  const shareBtn = document.getElementById('shareBtn');

  const urlParams = new URLSearchParams(window.location.search);
  const roadmapId = urlParams.get('id');

  init();

  async function init() {
    let roadmapData = null;
    if (roadmapId) {
      try {
        roadmapData = await api.getJson(`/api/roadmaps/${roadmapId}`);
      } catch (error) {
        console.error(error);
        alert('로드맵을 불러오지 못했습니다.');
      }
    } else {
      const stored = sessionStorage.getItem('job2roadmap.latestRoadmap');
      if (stored) {
        roadmapData = JSON.parse(stored);
      }
    }

    if (!roadmapData) {
      accordion.innerHTML = '<p style="color:var(--muted);">표시할 로드맵이 없습니다. 먼저 로드맵을 생성하세요.</p>';
      if (saveButton) saveButton.disabled = true;
      if (shareBtn) shareBtn.disabled = true;
      return;
    }

    renderRoadmap(roadmapData);
    ui.setupAccordion(accordion);

    if (shareBtn) {
      shareBtn.addEventListener('click', async () => {
        const shareUrl = `${window.location.origin}${window.location.pathname}?id=${roadmapData.id || 'preview'}`;
        try {
          await ui.copyToClipboard(shareUrl);
          shareBtn.textContent = '복사 완료!';
          setTimeout(() => (shareBtn.textContent = '공유 링크 복사'), 2000);
        } catch (error) {
          alert('클립보드 복사에 실패했습니다.');
        }
      });
    }

    if (saveButton) {
      if (!api.getAuthToken()) {
        saveButton.addEventListener('click', () => {
          alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
          window.location.href = 'login.html';
        });
      } else {
        saveButton.addEventListener('click', async () => {
          try {
            const saved = await api.postJson('/api/roadmaps/from-jd', {
              jdUrl: roadmapData.sourceUrl,
              jdText: null,
              targetRole: roadmapData.targetRole,
              targetCompany: roadmapData.targetCompany,
              preparationMonths: Math.max(3, Math.round(roadmapData.totalWeeks / 4)),
              dailyStudyHours: roadmapData.dailyStudyHours,
              currentLevel: roadmapData.level,
            });
            alert('내 로드맵에 저장되었습니다!');
            window.location.href = `roadmap.html?id=${saved.id}`;
          } catch (error) {
            alert(error.message);
          }
        });
      }
    }
  }

  function renderRoadmap(roadmap) {
    const progressState = loadProgressState(roadmap.id);
    titleEl.textContent = roadmap.title;
    totalWeeksEl.textContent = roadmap.totalWeeks;
    dailyHoursEl.textContent = roadmap.dailyStudyHours;
    currentLevelEl.textContent = roadmap.level;
    targetCompanyEl.textContent = roadmap.targetCompany || '미정';
    if (roadmap.sourceUrl) {
      sourceUrlEl.href = roadmap.sourceUrl;
      sourceUrlEl.textContent = '채용 공고 바로가기';
    } else {
      sourceUrlEl.removeAttribute('href');
      sourceUrlEl.textContent = '직접 입력';
    }
    createdAtEl.textContent = new Date().toISOString().slice(0, 10);

    accordion.innerHTML = '';
    let completedWeeks = 0;
    let totalWeeks = 0;

    roadmap.steps.forEach(step => {
      const item = document.createElement('div');
      item.className = 'accordion-item';
      item.innerHTML = `
        <div class="accordion-header">
          <div>
            <h3>${step.title}</h3>
            <p style="color:var(--muted);font-size:0.9rem;">${step.summary || ''}</p>
          </div>
          <span class="badge-pill">${step.durationWeeks}주</span>
        </div>
        <div class="accordion-content"></div>
      `;

      const content = item.querySelector('.accordion-content');
      step.weeks.forEach(week => {
        totalWeeks += 1;
        const checkboxId = `week-${roadmap.id || 'preview'}-${week.weekNumber}`;
        const checked = progressState.completedWeeks.includes(week.weekNumber);
        if (checked) completedWeeks += 1;
        const weekCard = document.createElement('div');
        weekCard.className = 'week-card';
        weekCard.innerHTML = `
          <div class="week-label">Week ${week.weekNumber}</div>
          <div>${week.focusTopics}</div>
          <div class="week-mission">${week.practiceMission}</div>
          <label class="checkbox">
            <input type="checkbox" id="${checkboxId}" ${checked ? 'checked' : ''}>
            <span>완료 표시</span>
          </label>
        `;
        const checkbox = weekCard.querySelector('input[type="checkbox"]');
        checkbox.addEventListener('change', () => {
          toggleWeekCompletion(roadmap.id, week.weekNumber, checkbox.checked);
        });
        content.appendChild(weekCard);
      });

      accordion.appendChild(item);
    });

    updateProgressDisplay(roadmap.id, completedWeeks, totalWeeks);
  }

  function loadProgressState(id) {
    if (!id) {
      return { completedWeeks: [] };
    }
    const raw = localStorage.getItem(`roadmap-progress-${id}`);
    if (!raw) {
      return { completedWeeks: [] };
    }
    try {
      return JSON.parse(raw);
    } catch (e) {
      return { completedWeeks: [] };
    }
  }

  function toggleWeekCompletion(id, weekNumber, completed) {
    if (!id) return;
    const state = loadProgressState(id);
    const index = state.completedWeeks.indexOf(weekNumber);
    if (completed && index === -1) {
      state.completedWeeks.push(weekNumber);
    } else if (!completed && index !== -1) {
      state.completedWeeks.splice(index, 1);
    }
    localStorage.setItem(`roadmap-progress-${id}`, JSON.stringify(state));
    const total = accordion.querySelectorAll('.week-card').length;
    const done = state.completedWeeks.length;
    updateProgressDisplay(id, done, total);
  }

  function updateProgressDisplay(id, completed, total) {
    const percent = total === 0 ? 0 : Math.round((completed / total) * 100);
    overallProgressText.textContent = `진행률 ${percent}%`;
    progressBar.style.width = `${percent}%`;
  }
})(window.Job2RoadmapApi, window.Job2RoadmapUI);
