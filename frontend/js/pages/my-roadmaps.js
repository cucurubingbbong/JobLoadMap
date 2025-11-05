(function(api){
  const listEl = document.getElementById('roadmapList');
  if (!listEl) return;

  async function init() {
    if (!api.getAuthToken()) {
      listEl.innerHTML = '<p style="color:var(--muted);">로그인 후 내 로드맵을 확인할 수 있습니다.</p>';
      return;
    }
    try {
      const roadmaps = await api.getJson('/api/roadmaps');
      if (roadmaps.length === 0) {
        listEl.innerHTML = '<p style="color:var(--muted);">아직 저장된 로드맵이 없습니다. 새로운 로드맵을 생성해보세요.</p>';
        return;
      }
      roadmaps.forEach(renderCard);
    } catch (error) {
      listEl.innerHTML = `<p class="error-message">${error.message}</p>`;
    }
  }

  function renderCard(roadmap) {
    const card = document.createElement('div');
    card.className = 'card';
    const storedProgress = localStorage.getItem(`roadmap-progress-${roadmap.id}`);
    let percent = 0;
    if (storedProgress) {
      try {
        const parsed = JSON.parse(storedProgress);
        percent = Math.round((parsed.completedWeeks.length / roadmap.totalWeeks) * 100);
      } catch (e) {
        percent = 0;
      }
    }
    card.innerHTML = `
      <div class="card-header">
        <div>
          <h3>${roadmap.title}</h3>
          <p style="color:var(--muted);font-size:0.9rem;">${roadmap.targetCompany || '목표 회사 미정'} • 총 ${roadmap.totalWeeks}주</p>
        </div>
        <span class="badge-pill">하루 ${roadmap.dailyStudyHours}시간</span>
      </div>
      <div class="progress-bar" style="margin-bottom:0.75rem;">
        <span style="width:${percent}%"></span>
      </div>
      <button class="button secondary" data-id="${roadmap.id}">로드맵 열기</button>
    `;
    card.querySelector('button').addEventListener('click', () => {
      window.location.href = `roadmap.html?id=${roadmap.id}`;
    });
    listEl.appendChild(card);
  }

  init();
})(window.Job2RoadmapApi);
