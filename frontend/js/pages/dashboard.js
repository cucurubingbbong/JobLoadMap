(function(api){
  const keywordItems = document.getElementById('keywordItems');
  const roleFilter = document.getElementById('roleFilter');
  const periodFilter = document.getElementById('periodFilter');
  const refreshBtn = document.getElementById('refreshKeywords');
  const totalPosts = document.getElementById('totalPosts');

  if (!keywordItems) return;

  refreshBtn.addEventListener('click', loadKeywords);
  loadKeywords();

  async function loadKeywords() {
    const role = roleFilter.value;
    const period = periodFilter.value;
    keywordItems.innerHTML = '<p style="color:var(--muted);">데이터를 불러오는 중입니다...</p>';

    try {
      // 아직 백엔드 API가 준비되지 않았으므로 임시 데이터를 사용합니다.
      const mock = buildMockKeywords(role, period);
      renderKeywordList(mock);
    } catch (error) {
      keywordItems.innerHTML = `<p class="error-message">${error.message}</p>`;
    }
  }

  function renderKeywordList(data) {
    totalPosts.textContent = `총 ${data.totalPosts}건 분석`;
    keywordItems.innerHTML = '';
    data.keywords.forEach((item, index) => {
      const div = document.createElement('div');
      div.className = 'keyword-item';
      div.innerHTML = `
        <div>
          <strong>#${index + 1} ${item.keyword}</strong>
          <p style="color:var(--muted);font-size:0.85rem;">주요 포지션: ${item.relatedRole}</p>
        </div>
        <span class="badge-pill">${item.frequency}회</span>
      `;
      keywordItems.appendChild(div);
    });
  }

  function buildMockKeywords(role, period) {
    const baseKeywords = {
      backend: [
        { keyword: 'Java', relatedRole: '백엔드', frequency: 82 },
        { keyword: 'Spring Boot', relatedRole: '백엔드', frequency: 76 },
        { keyword: 'MySQL', relatedRole: 'DB', frequency: 58 },
        { keyword: 'AWS', relatedRole: '클라우드', frequency: 44 },
        { keyword: 'Docker', relatedRole: 'DevOps', frequency: 38 },
        { keyword: 'JUnit', relatedRole: '테스트', frequency: 34 },
        { keyword: 'Redis', relatedRole: '캐시', frequency: 29 },
        { keyword: 'Kotlin', relatedRole: '백엔드', frequency: 22 }
      ],
      frontend: [
        { keyword: 'React', relatedRole: '프론트엔드', frequency: 74 },
        { keyword: 'TypeScript', relatedRole: '프론트엔드', frequency: 62 },
        { keyword: 'Next.js', relatedRole: '프론트엔드', frequency: 48 },
        { keyword: 'REST API', relatedRole: '공통', frequency: 43 },
        { keyword: 'TailwindCSS', relatedRole: 'UI', frequency: 39 },
        { keyword: 'WebSocket', relatedRole: '실시간', frequency: 27 },
        { keyword: 'Jest', relatedRole: '테스트', frequency: 26 },
        { keyword: 'Storybook', relatedRole: '디자인 시스템', frequency: 21 }
      ]
    };

    const selected = role === 'frontend' ? baseKeywords.frontend : baseKeywords.backend;
    const keywords = [...selected];

    if (role === 'all') {
      keywords.push(...baseKeywords.frontend);
    }

    const factor = period === '7' ? 0.5 : 1;
    const scaled = keywords.map(item => ({
      ...item,
      frequency: Math.max(10, Math.round(item.frequency * factor))
    })).slice(0, 12);

    return {
      totalPosts: period === '7' ? 128 : 412,
      keywords: scaled
    };
  }
})(window.Job2RoadmapApi);
