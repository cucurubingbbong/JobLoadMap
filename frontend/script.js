// JobRoadMap Frontend Helpers
// 화이트/블루 UI를 유지하면서 페이지별 초기화 로직을 분리했습니다.
// 각 섹션의 주석을 따라가면 담당자가 쉽게 수정할 수 있습니다.

const API_BASE = (window.API_BASE && window.API_BASE.trim()) || 'http://localhost:8080';
window.API_BASE = API_BASE;
const FRONTEND_BASE = (window.FRONTEND_BASE && window.FRONTEND_BASE.trim()) || window.location.origin;
const page = document.body.dataset.page;

// 샘플 JD 텍스트는 데모/오프라인 환경에서 즉시 실행하도록 제공합니다.
const sampleJD = `주요업무\n- Java/Spring 기반 백엔드 신규 개발 및 성능 개선\n- AWS 환경에서 서비스 운영 경험 보유자 우대\n자격요건\n- OOP 기반 개발 경험\n- MySQL, Redis, Kafka 경험 우대`;

// ----- 공통 네비게이션 & 인증 상태 표시 -----
attachNavHandlers();

// 페이지별 엔트리 포인트 분기
if (page === 'builder') initBuilder();
if (page === 'my-roadmaps') initDashboard();
if (page === 'community') initCommunity();
if (page === 'community-edit') initCommunityEdit();
if (page === 'login') initLoginPage();
if (page === 'signup') initSignupPage();
if (page === 'hiring') initHiring();
if (page === 'attendance') initAttendancePage();
if (page === 'profile') initProfilePage();

function attachNavHandlers() {
    const loginBtn = document.getElementById('navLogin');
    const signupBtn = document.getElementById('navSignup');
    const logoutBtn = document.getElementById('navLogout');
    if (loginBtn) loginBtn.onclick = () => window.location.href = 'login.html';
    if (signupBtn) signupBtn.onclick = () => window.location.href = 'signup.html';
    if (logoutBtn) logoutBtn.onclick = fakeLogout;
    updateAuthButtons();
    setActiveNavLinks();
}

// Attendance page
function initAttendancePage() {
    const grid = document.getElementById('attendanceGrid');
    const status = document.getElementById('attendanceStatus');
    const btn = document.getElementById('attendanceCheck');
    const rewards = [
        { day: 1, name: '출석 보상 x1' },
        { day: 2, name: '출석 보상 x1' },
        { day: 3, name: '출석 보상 x1' },
        { day: 4, name: '출석 보상 x1' },
        { day: 5, name: '출석 보상 x1' },
        { day: 6, name: '출석 보상 x1' },
        { day: 7, name: '출석 보상 x1' },
        { day: 8, name: '출석 보상 x1' },
        { day: 9, name: '출석 보상 x1' },
        { day: 10, name: '출석 보상 x1' },
        { day: 11, name: '출석 보상 x1' },
        { day: 12, name: '출석 보상 x1' },
        { day: 13, name: '출석 보상 x1' },
        { day: 14, name: '출석 보상 x1' },
    ];
    btn.addEventListener('click', async () => {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 이용해주세요.');
            return;
        }
        await fetch(`${API_BASE}/api/attendance/check`, { method: 'POST', headers: authHeaders(token) });
        load();
    });
    load();

    async function load() {
        const token = localStorage.getItem('token');
        if (!token) {
            grid.innerHTML = '<p class="empty">로그인 후 이용해주세요.</p>';
            return;
        }
        const res = await fetch(`${API_BASE}/api/attendance`, { headers: authHeaders(token) });
        const data = await res.json();
        const checked = new Set(data.checkedDates || []);
        status.textContent = `출석 ${data.streak || 0}일 연속`;
        grid.classList.remove('empty');
        grid.innerHTML = rewards.map(r => {
            const claimed = checked.size >= r.day;
            return `<div class="reward-card${claimed ? ' active' : ''}">
                <div class="reward-day">${r.day}일째</div>
                <div class="reward-name">${r.name}</div>
                <div class="muted">${claimed ? '수령 가능/수령됨' : '미수령'}</div>
            </div>`;
        }).join('');
    }
}

// Profile page
function initProfilePage() {
    const profileInfo = document.getElementById('profileInfo');
    const usernameInput = document.getElementById('newUsername');
    const usernameBtn = document.getElementById('updateUsernameBtn');
    const usernameStatus = document.getElementById('usernameStatus');
    const currentPassword = document.getElementById('currentPassword');
    const newPassword = document.getElementById('newPassword');
    const passwordBtn = document.getElementById('updatePasswordBtn');
    const passwordStatus = document.getElementById('passwordStatus');
    loadProfile();

    if (usernameBtn) {
        usernameBtn.addEventListener('click', async () => {
            const token = localStorage.getItem('token');
            if (!token) return alert('로그인 후 이용하세요.');
            const username = usernameInput.value.trim();
            if (!username) return;
            try {
                const res = await fetch(`${API_BASE}/api/profile/username`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
                    body: JSON.stringify({ username })
                });
                const data = await res.json();
                if (!res.ok) throw new Error(data.error || '실패');
                usernameStatus.textContent = '아이디가 변경되었습니다.';
                loadProfile();
            } catch (e) {
                usernameStatus.textContent = e.message;
            }
        });
    }

    if (passwordBtn) {
        passwordBtn.addEventListener('click', async () => {
            const token = localStorage.getItem('token');
            if (!token) return alert('로그인 후 이용하세요.');
            const cur = currentPassword.value.trim();
            const next = newPassword.value.trim();
            if (!cur || !next) return;
            try {
                const res = await fetch(`${API_BASE}/api/profile/password`, {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
                    body: JSON.stringify({ currentPassword: cur, newPassword: next })
                });
                const data = await res.json();
                if (!res.ok) throw new Error(data.error || '실패');
                passwordStatus.textContent = '비밀번호가 변경되었습니다.';
                currentPassword.value = '';
                newPassword.value = '';
            } catch (e) {
                passwordStatus.textContent = e.message;
            }
        });
    }

    async function loadProfile() {
        const token = localStorage.getItem('token');
        const securityContent = document.getElementById('accountSecurityContent');
        const securityMsg = document.getElementById('accountSecurityMsg');

        if (!token) {
            profileInfo.textContent = '로그인 후 확인 가능합니다.';
            if (securityContent) securityContent.style.display = 'none';
            if (securityMsg) {
                securityMsg.style.display = 'block';
                securityMsg.textContent = '로그인 후 변경 가능합니다.';
            }
            return;
        }

        if (securityContent) securityContent.style.display = 'grid';
        if (securityMsg) securityMsg.style.display = 'none';

        try {
            const res = await fetch(`${API_BASE}/api/profile/me`, { headers: authHeaders(token) });
            if (!res.ok) throw new Error('정보를 불러오지 못했습니다.');
            const data = await res.json();
            profileInfo.classList.remove('empty');
            profileInfo.innerHTML = `
                <p class="eyebrow">이메일</p>
                <h3>${data.email}</h3>
                <p class="muted">아이디: ${data.username || '-'}</p>
            `;
        } catch (e) {
            profileInfo.textContent = e.message;
        }
    }
}

function fakeLogout() {
    const token = localStorage.getItem('token');
    if (!token) return;
    fetch(`${API_BASE}/api/auth/logout`, { method: 'POST', headers: authHeaders(token) }).finally(() => {
        localStorage.removeItem('token');
        updateAuthButtons();
        const status = document.getElementById('formStatus');
        if (status) status.textContent = '로그아웃되었습니다.';
    });
}

function updateAuthButtons() {
    const token = localStorage.getItem('token');
    const loginBtn = document.getElementById('navLogin');
    const signupBtn = document.getElementById('navSignup');
    const logoutBtn = document.getElementById('navLogout');

    if (loginBtn) loginBtn.style.display = token ? 'none' : 'inline-block';
    if (signupBtn) signupBtn.style.display = token ? 'none' : 'inline-block';
    if (logoutBtn) logoutBtn.style.display = token ? 'inline-block' : 'none';

    const saveBtn = document.getElementById('saveRoadmap');
    if (saveBtn) {
        saveBtn.textContent = token ? '내 로드맵 저장' : '로그인 후 저장';
        saveBtn.disabled = false;
    }
}

function authHeaders(token) {
    return { 'X-Auth-Token': token };
}

function setActiveNavLinks() {
    const page = document.body.dataset.page || '';
    const pageMap = {
        home: 'index.html', // Not in menu, but default
        builder: 'builder.html',
        'my-roadmaps': 'my-roadmaps.html',
        profile: 'profile.html',
        hiring: 'hiring.html',
        community: 'community.html',
        'community-post': 'community.html',
        'community-edit': 'community.html',
        login: 'login.html', // Not in main menu
        signup: 'signup.html' // Not in main menu
    };
    const target = pageMap[page];
    const current = target || location.pathname.split('/').pop() || 'index.html';

    // Updated selector for new header structure
    document.querySelectorAll('.nav-container a').forEach(a => {
        const href = a.getAttribute('href');
        // Simple check: if the link's href matches the current page target
        if (href === current) {
            a.classList.add('active');
        } else {
            a.classList.remove('active');
        }
    });
}

// ==============================================
// 1) 로드맵 생성 페이지 (builder)
// ==============================================
function initBuilder() {
    // 주요 DOM 참조를 한곳에 모아둠
    const roadmapDiv = document.getElementById('roadmap');
    const postsDiv = document.getElementById('posts');
    const roadmapMeta = document.getElementById('roadmapMeta');
    const statusEl = document.getElementById('formStatus');
    attachRangeDisplay('duration', 'durationValue', '개월');
    attachRangeDisplay('hours', 'hoursValue', '시간');

    // 탭 전환: URL 입력 / 텍스트 입력
    document.querySelectorAll('#jdTabs button').forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn));
    });

    // 샘플 JD 채우기 버튼
    const fillSampleBtn = document.getElementById('fillSample');
    if (fillSampleBtn) {
        fillSampleBtn.addEventListener('click', () => {
            document.getElementById('jdText').value = sampleJD;
        });
    }

    // 로드맵 생성 버튼
    document.getElementById('generate').addEventListener('click', async () => {
        const payload = buildJdPayload();
        if (!payload.jdText && !payload.jdUrl) payload.jdText = sampleJD;

        statusEl.textContent = '로드맵 생성 중...';
        setLoading(roadmapDiv, true);
        try {
            const res = await fetch(`${API_BASE}/api/roadmap/from-jd`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error('로드맵 생성 실패');
            const data = await res.json();
            renderRoadmap(data, roadmapDiv, roadmapMeta);
            statusEl.textContent = '완료! 저장 버튼으로 내 로드맵에 담을 수 있어요.';
            document.getElementById('saveRoadmap').onclick = () => saveRoadmap(data);
        } catch (e) {
            statusEl.textContent = e.message;
            roadmapDiv.innerHTML = '<p class="empty">로드맵 생성에 실패했습니다.</p>';
        } finally {
            setLoading(roadmapDiv, false);
        }
    });

    // 크롤링된 샘플 공고 보기 (헤더/섹션 버튼 모두 연결)
    document.querySelectorAll('#fetchPostsHero, #fetchPostsRefresh').forEach(btn => {
        btn.addEventListener('click', async () => {
            setLoading(postsDiv, true);
            try {
                const res = await fetch(`${API_BASE}/api/crawl/daily`);
                const data = await res.json();
                renderPosts(data, postsDiv);
            } catch (e) {
                postsDiv.innerHTML = `<p class="empty">${e.message}</p>`;
            } finally {
                setLoading(postsDiv, false);
            }
        });
    });
}

function buildJdPayload() {
    const level = document.getElementById('level').value || '신입 준비';
    return {
        jdText: document.getElementById('jdText').value.trim(),
        jdUrl: document.getElementById('jdUrl').value.trim(),
        durationMonths: Number(document.getElementById('duration').value),
        dailyHours: Number(document.getElementById('hours').value),
        level
    };
}

function switchTab(btn) {
    document.querySelectorAll('#jdTabs button').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    btn.classList.add('active');
    document.querySelector(`.tab-content[data-tab="${btn.dataset.tab}"]`).classList.add('active');
}

function switchDetailTab(tabName) {
    const tabs = document.getElementById('detailTabs');
    if (!tabs) return;

    tabs.querySelectorAll('button').forEach(btn => {
        if (btn.dataset.tab === tabName) btn.classList.add('active');
        else btn.classList.remove('active');
    });

    const container = tabs.closest('.panel');
    if (!container) return;

    container.querySelectorAll('.tab-content').forEach(content => {
        if (content.dataset.tab === tabName) content.classList.add('active');
        else content.classList.remove('active');
    });
}

async function saveRoadmap(data) {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('로그인 후 저장할 수 있습니다.');
        return;
    }
    const titleInput = document.getElementById('saveTitle');
    const title = (titleInput && titleInput.value.trim()) ? titleInput.value.trim() : '나의 맞춤 로드맵';
    const payload = { title, progress: 0, roadmap: data };
    const res = await fetch(`${API_BASE}/api/profile/roadmaps`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const msg = await res.text().catch(() => '');
        alert(`저장 실패 (code ${res.status}${msg ? `, ${msg}` : ''})`);
        return;
    }
    if (titleInput) titleInput.value = '';
    const statusEl = document.getElementById('formStatus');
    if (statusEl) statusEl.textContent = '저장되었습니다! 내 로드맵 페이지에서 확인하세요.';
}

function renderRoadmap(data, container, metaEl) {
    container.classList.remove('empty');
    if (metaEl) metaEl.textContent = `총 ${data.totalWeeks}주 · 하루 ${data.dailyHours}시간 권장${data.aiGenerated ? ' · Gemini 생성' : ''}`;

    const rows = data.steps.map((step, index) => {
        const stepNumber = index + 1;
        const pills = step.weeks.map(week => `
            <div class="timeline-pill">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                    <h4 style="margin:0; color:var(--primary-strong);">${week.weekNumber}주차</h4>
                    <span class="badge" style="background:#f0f0f0; color:#666;">${week.topics.length}개 토픽</span>
                </div>
                <div class="pill-topics">${week.topics.map(t => `<span class="badge">${t}</span>`).join('')}</div>
                <div style="margin-top:10px; padding:10px; background:#f9f9f9; border-radius:8px;">
                    <p style="margin:0; font-weight:bold;">🎯 핵심 미션</p>
                    <p style="margin:4px 0 0; color:#444;">${week.mission}</p>
                </div>
                ${week.detail ? `<p class="pill-detail" style="margin-top:8px;">${week.detail}</p>` : ''}
            </div>
        `).join('');

        return `
            <div class="timeline-row" style="margin-bottom: 30px;">
                <div class="timeline-row-label" style="font-size:18px; margin-bottom:15px; padding-bottom:10px; border-bottom:2px solid var(--primary);">
                    <span style="color:var(--primary); margin-right:8px;">Step ${stepNumber}</span> ${step.title} 
                    <span style="font-size:14px; color:#888; font-weight:normal; margin-left:10px;">(${step.estimatedWeeks}주 완성)</span>
                </div>
                <div class="timeline-track" style="display:grid; grid-template-columns:repeat(auto-fill, minmax(300px, 1fr)); gap:15px;">
                    ${pills}
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = `
        <div class="timeline-container">
            <div class="timeline">
                ${rows}
            </div>
        </div>
    `;
}

function renderChecklistInline(week) {
    const checklist = week.checklist || [];
    if (!checklist.length) return '';
    return `<div class="pill-detail">${checklist.map(item => `• ${item}`).join('<br>')}</div>`;
}

function renderPosts(posts, container) {
    container.classList.remove('empty');
    if (!posts.length) {
        container.innerHTML = '<p class="empty">크롤링된 공고가 없습니다.</p>';
        return;
    }
    container.innerHTML = posts.map(post => `
        <div class="card">
            <p class="eyebrow">${post.company}</p>
            <h3>${post.role}</h3>
            <p>${post.location} · 마감 ${post.deadline}</p>
            <div class="badges">${post.keywords.map(k => `<span class="badge">${k}</span>`).join('')}</div>
            <a href="${post.url}" target="_blank" rel="noreferrer">공고 보기 →</a>
        </div>
    `).join('');
}

// ==============================================
// 2) 내 로드맵 페이지 (dashboard)
// ==============================================
function initDashboard() {
    const savedDiv = document.getElementById('savedRoadmaps');
    const detailDiv = document.getElementById('savedDetail');
    const detailTitle = document.getElementById('savedDetailTitle');
    const progressValue = document.getElementById('progressValue');
    const progressSave = document.getElementById('progressSave');
    const floatingSave = document.getElementById('floatingSave');
    const weekChecklist = document.getElementById('weekChecklist');
    const chatInput = document.getElementById('chatInput');
    const chatSend = document.getElementById('chatSend');
    const chatAnswer = document.getElementById('chatAnswer');
    const chatStatus = document.getElementById('chatStatus');
    const tabs = document.getElementById('detailTabs');
    const chatContext = document.getElementById('chatContext');
    const chatHistoryBox = document.getElementById('chatHistory');
    let selectedRoadmap = null;
    let savedData = [];
    const chatHistory = {};

    document.getElementById('refreshRoadmaps').onclick = loadSaved;
    if (progressSave) progressSave.addEventListener('click', updateProgress);
    if (floatingSave) floatingSave.addEventListener('click', updateProgress);
    if (chatSend) {
        chatSend.addEventListener('click', sendChat);
    }
    if (tabs) {
        tabs.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', () => switchDetailTab(btn.dataset.tab));
        });
    }

    loadSaved();

    async function loadSaved() {
        const token = localStorage.getItem('token');
        if (!token) {
            savedDiv.innerHTML = '<p class="empty">로그인 후 이용해주세요.</p>';
            return;
        }
        setLoading(savedDiv, true);
        try {
            const res = await fetch(`${API_BASE}/api/profile/roadmaps`, { headers: authHeaders(token) });
            if (res.status === 401) {
                localStorage.removeItem('token');
                savedDiv.innerHTML = '<p class="empty">로그인 세션이 만료되었습니다. 다시 로그인해주세요.</p>';
                setLoading(savedDiv, false);
                return;
            }
            if (!res.ok) {
                const msg = await res.text().catch(() => '');
                savedDiv.innerHTML = `<p class="empty">로드맵을 불러오지 못했습니다. (code ${res.status}${msg ? `, ${msg}` : ''})</p>`;
                setLoading(savedDiv, false);
                return;
            }
            const json = await res.json();
            savedData = Array.isArray(json) ? json : [];
            if (!Array.isArray(json)) {
                savedDiv.innerHTML = '<p class="empty">로드맵 데이터를 불러오지 못했습니다.</p>';
                setLoading(savedDiv, false);
                return;
            }
            if (savedData.length === 0) {
                savedDiv.innerHTML = '<p class="empty">저장된 로드맵이 없습니다. 먼저 생성하고 저장해주세요.</p>';
                setLoading(savedDiv, false);
                return;
            }
            savedDiv.classList.remove('empty');
            savedDiv.innerHTML = savedData.map(r => `
                <div class="card saved-card${selectedRoadmap && selectedRoadmap.id === r.id ? ' active' : ''}" data-id="${r.id}">
                    <p class="eyebrow">${r.createdAt}</p>
                    <h3>${r.title}</h3>
                    <p>총 ${r.roadmap.totalWeeks}주 · 진행률 ${r.progress}% ${r.roadmap.aiGenerated ? '· Gemini' : ''}</p>
                    <div style="display:flex; gap:8px; flex-wrap:wrap; margin-top:8px;">
                        <button class="ghost share-roadmap" data-id="${r.id}">공유 링크</button>
                        <button class="ghost delete-roadmap" data-id="${r.id}">삭제</button>
                    </div>
                </div>
            `).join('');
            savedDiv.querySelectorAll('.saved-card').forEach(card => {
                card.addEventListener('click', () => {
                    const id = card.dataset.id;
                    const found = savedData.find(r => r.id === id);
                    if (found) {
                        selectedRoadmap = found;
                        detailTitle.textContent = found.title;
                        renderWeeks(found);
                        progressValue.textContent = found.progress;
                        renderRoadmap(found.roadmap, detailDiv, document.createElement('p'));
                        detailDiv.classList.add('roadmap-grid');
                        updateChatContext(found);
                        renderChatHistory();
                        switchDetailTab('weeks');
                    }
                });
            });
            // 선택 유지
            if (selectedRoadmap) {
                const again = savedData.find(r => r.id === selectedRoadmap.id);
                if (again) {
                    selectedRoadmap = again;
                    detailTitle.textContent = again.title;
                    renderWeeks(again);
                    progressValue.textContent = again.progress;
                    renderRoadmap(again.roadmap, detailDiv, document.createElement('p'));
                    detailDiv.classList.add('roadmap-grid');
                    updateChatContext(again);
                    renderChatHistory();
                    switchDetailTab('weeks');
                }
            }
            savedDiv.querySelectorAll('.delete-roadmap').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    console.log('Delete button clicked', btn.dataset.id);
                    if (confirm('정말 삭제하시겠습니까?')) {
                        deleteRoadmap(btn.dataset.id);
                    }
                });
            });
            savedDiv.querySelectorAll('.share-roadmap').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    createShareLink(btn.dataset.id);
                });
            });
        } catch (e) {
            savedDiv.innerHTML = `<p class="empty">로드맵을 불러오지 못했습니다. (네트워크 오류)</p>`;
            console.error('loadSaved error', e);
        }
    }

    async function updateProgress() {
        if (!selectedRoadmap) {
            alert('로드맵을 먼저 선택하세요.');
            return;
        }
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 이용해주세요.');
            return;
        }
        const progress = computeProgress();

        // Save checklist state to localStorage
        const checklistState = {};
        weekChecklist.querySelectorAll('.week-card').forEach((card, weekIdx) => {
            const boxes = card.querySelectorAll('input[type="checkbox"]');
            checklistState[weekIdx] = Array.from(boxes).map(box => box.checked);
        });
        localStorage.setItem(`checklist_${selectedRoadmap.id}`, JSON.stringify(checklistState));

        await fetch(`${API_BASE}/api/profile/roadmaps/${selectedRoadmap.id}/progress`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
            body: JSON.stringify({ progress })
        });
        selectedRoadmap.progress = progress;
        alert('달성도가 저장되었습니다.');
        // loadSaved(); // Removed to prevent reset of UI state
    }

    function computeProgress() {
        const weekCards = Array.from(weekChecklist.querySelectorAll('.week-card'));
        if (!weekCards.length) return 0;
        let done = 0;
        weekCards.forEach(card => {
            const boxes = card.querySelectorAll('input[type="checkbox"]');
            const checked = card.querySelectorAll('input[type="checkbox"]:checked').length;
            const isComplete = boxes.length > 0 && checked === boxes.length;

            if (isComplete) {
                done++;
                card.classList.add('completed');
                // Auto-collapse only if not already interacted with? 
                // For now, let's just mark it completed. Collapse is manual or initial load.
            } else {
                card.classList.remove('completed');
            }
        });
        const pct = Math.round((done / weekCards.length) * 100);
        progressValue.textContent = pct;
        return pct;
    }

    // 삭제/공유 관련 헬퍼
    async function deleteRoadmap(id) {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 삭제할 수 있습니다.');
            return;
        }
        try {
            const res = await fetch(`${API_BASE}/api/profile/roadmaps/${id}`, {
                method: 'DELETE',
                headers: authHeaders(token)
            });
            if (!res.ok) {
                if (res.status === 401) {
                    alert('로그인이 만료되었습니다. 다시 로그인 해주세요.');
                } else {
                    const msg = await res.text();
                    alert(`삭제에 실패했습니다.\n${msg}`);
                }
                return;
            }
        } catch (err) {
            alert(`삭제 중 오류가 발생했습니다: ${err.message}`);
            return;
        }
        // 즉시 UI에서 제거
        savedData = savedData.filter(r => r.id !== id);
        if (selectedRoadmap && selectedRoadmap.id === id) {
            selectedRoadmap = null;
            detailTitle.textContent = '선택된 로드맵';
            detailDiv.innerHTML = '<p class="empty">로드맵을 선택하면 상세가 표시됩니다.</p>';
            weekChecklist.innerHTML = '<p class="empty">로드맵을 선택하세요.</p>';
            if (chatContext) chatContext.textContent = '로드맵을 선택하면 제목과 진행률이 표시됩니다.';
            if (chatHistoryBox) chatHistoryBox.textContent = '이전 질문/답변이 여기에 누적됩니다.';
            switchDetailTab('weeks');
        }
        await loadSaved();
    }

    async function createShareLink(id) {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 이용해주세요.');
            return;
        }
        try {
            const res = await fetch(`${API_BASE}/api/profile/roadmaps/${id}/share`, {
                method: 'POST',
                headers: authHeaders(token)
            });
            if (!res.ok) {
                if (res.status === 401) throw new Error('로그인이 만료되었습니다. 다시 로그인 해주세요.');
                const msg = await res.text();
                throw new Error(msg || '공유 링크 생성 실패');
            }
            const data = await res.json();
            // 프론트 전용 공유 URL로 변환 (정적 파일 + 토큰)
            const fullUrl = `${FRONTEND_BASE}/shared-roadmap.html?token=${data.token || data.url?.split('/').pop()}`;
            if (navigator.clipboard) {
                navigator.clipboard.writeText(fullUrl);
                alert(`공유 링크를 클립보드에 복사했습니다.\n${fullUrl}`);
            } else {
                alert(`공유 링크: ${fullUrl}`);
            }
        } catch (e) {
            alert(e.message);
        }
    }

    function renderWeeks(record) {
        if (!record || !record.roadmap) return;
        weekChecklist.classList.remove('empty');
        weekChecklist.innerHTML = '';
        const weeks = (record.roadmap.steps || []).flatMap(step => step.weeks || []);

        // Restore checklist state
        const savedState = JSON.parse(localStorage.getItem(`checklist_${record.id}`) || '{}');

        weeks.forEach((week, weekIdx) => {
            const wrapper = document.createElement('div');
            wrapper.classList.add('card', 'week-card');
            wrapper.style.border = '1px solid var(--line)';
            wrapper.style.boxShadow = 'none';

            const checklist = week.checklist || [];
            const derived = checklist.length ? checklist : deriveChecklist(week);

            // Header for collapse toggle
            const header = document.createElement('div');
            header.style.display = 'flex';
            header.style.justifyContent = 'space-between';
            header.style.alignItems = 'center';
            header.style.cursor = 'pointer';
            header.style.marginBottom = '10px';
            header.onclick = () => {
                if (wrapper.classList.contains('completed')) {
                    wrapper.classList.toggle('collapsed');
                }
            };

            const title = document.createElement('h4');
            title.textContent = `${week.weekNumber}주차`;
            title.style.margin = '0';

            const toggleIcon = document.createElement('span');
            toggleIcon.className = 'toggle-icon';
            toggleIcon.innerHTML = '&#9660;'; // Down arrow
            toggleIcon.style.fontSize = '12px';
            toggleIcon.style.color = '#999';

            header.appendChild(title);
            header.appendChild(toggleIcon);
            wrapper.appendChild(header);

            const ul = document.createElement('div');
            ul.classList.add('checklist-box');

            const weekState = savedState[weekIdx] || [];
            let allChecked = true;

            derived.forEach((item, itemIdx) => {
                const row = document.createElement('label');
                row.classList.add('check-row');
                const cb = document.createElement('input');
                cb.type = 'checkbox';
                cb.checked = weekState[itemIdx] || false;
                if (!cb.checked) allChecked = false;

                cb.onchange = () => {
                    computeProgress();
                };
                row.appendChild(cb);
                const span = document.createElement('span');
                span.textContent = item;
                row.appendChild(span);
                ul.appendChild(row);
            });

            wrapper.appendChild(ul);
            weekChecklist.appendChild(wrapper);

            if (derived.length > 0 && allChecked) {
                wrapper.classList.add('completed', 'collapsed');
            }
        });

        computeProgress();
    }

    function deriveChecklist(week) {
        const list = [];
        (week.topics || []).forEach(t => list.push(`${t} 복습/실습`));
        if (week.mission) list.push(week.mission);
        if (week.detail) list.push(`세부: ${week.detail}`);
        return list.slice(0, 5);
    }

    async function sendChat() {
        if (!selectedRoadmap) {
            alert('로드맵을 먼저 선택하세요.');
            return;
        }
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 이용해주세요.');
            return;
        }
        const msg = (chatInput && chatInput.value.trim()) || '';
        if (!msg) {
            alert('메시지를 입력하세요.');
            return;
        }
        if (chatStatus) chatStatus.textContent = 'AI 응답 중...';
        try {
            const res = await fetch(`${API_BASE}/api/roadmap/chat`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
                body: JSON.stringify({ message: msg, roadmap: selectedRoadmap.roadmap, progress: selectedRoadmap.progress })
            });
            if (!res.ok) throw new Error('AI 응답 실패');
            const data = await res.json();
            if (chatAnswer) chatAnswer.textContent = data.answer || '응답이 비었습니다.';
            storeChat(msg, data.answer || '');
            renderChatHistory();
        } catch (e) {
            if (chatAnswer) chatAnswer.textContent = '실패했습니다.';
        } finally {
            if (chatStatus) chatStatus.textContent = '';
        }
    }

    function updateChatContext(record) {
        if (!chatContext || !record) return;
        chatContext.textContent = `선택한 로드맵: ${record.title} · 진행률 ${record.progress}% · 총 ${record.roadmap.totalWeeks}주`;
    }

    function storeChat(question, answer) {
        if (!selectedRoadmap) return;
        if (!chatHistory[selectedRoadmap.id]) chatHistory[selectedRoadmap.id] = [];
        chatHistory[selectedRoadmap.id].unshift({ question, answer, ts: new Date().toLocaleString() });
        chatHistory[selectedRoadmap.id] = chatHistory[selectedRoadmap.id].slice(0, 10);
    }

    function renderChatHistory() {
        if (!chatHistoryBox) return;
        if (!selectedRoadmap || !chatHistory[selectedRoadmap.id] || chatHistory[selectedRoadmap.id].length === 0) {
            chatHistoryBox.textContent = '이전 질문/답변이 여기에 누적됩니다.';
            return;
        }
        chatHistoryBox.innerHTML = chatHistory[selectedRoadmap.id]
            .map(item => `<div style="margin-bottom:8px;"><strong>Q:</strong> ${item.question}<br><strong>A:</strong> ${item.answer}</div>`)
            .join('');
    }
}

// ==============================================
// 3) 커뮤니티 페이지 (community)
// ==============================================
function initCommunity() {
    const postsDiv = document.getElementById('communityPosts');
    const categoryTabs = document.getElementById('communityTabs');
    const categorySelect = document.getElementById('postCategory');
    let filter = 'all';
    let myEmail = null;

    (async () => {
        const token = localStorage.getItem('token');
        if (!token) return;
        try {
            const res = await fetch(`${API_BASE}/api/profile/me`, { headers: authHeaders(token) });
            const data = await res.json();
            if (data.email) myEmail = data.email;
        } catch { }
    })();

    const refreshBtn = document.getElementById('refreshPosts');
    if (refreshBtn) refreshBtn.onclick = loadCommunity;
    document.getElementById('postSubmit').onclick = submitPost;

    if (categoryTabs) {
        categoryTabs.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', () => {
                categoryTabs.querySelectorAll('button').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                filter = btn.dataset.tab;
                loadCommunity();
            });
        });
    }

    loadCommunity();

    async function toBase64(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
    }

    async function submitPost() {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 글을 작성할 수 있습니다.');
            return;
        }
        const title = document.getElementById('postTitle').value;
        const content = document.getElementById('postContent').value;
        const category = categorySelect ? categorySelect.value : '일반';
        const fileInput = document.getElementById('postFile');
        let attachmentName = null;
        let attachmentData = null;
        if (fileInput && fileInput.files && fileInput.files[0]) {
            attachmentName = fileInput.files[0].name;
            attachmentData = await toBase64(fileInput.files[0]);
        }
        try {
            await fetch(`${API_BASE}/api/community/posts`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
                body: JSON.stringify({ title, content, category, attachmentName, attachmentData })
            });
            document.getElementById('postStatus').textContent = '등록 완료';
            // 입력값 초기화
            document.getElementById('postTitle').value = '';
            document.getElementById('postContent').value = '';
            if (fileInput) fileInput.value = '';
            if (categorySelect) categorySelect.value = '일반';
            loadCommunity();
        } catch (e) {
            document.getElementById('postStatus').textContent = '등록 실패';
        }
    }

    async function loadCommunity() {
        setLoading(postsDiv, true);
        const res = await fetch(`${API_BASE}/api/community/posts`);
        const data = await res.json();
        const filtered = data.filter(p => {
            if (filter === 'all') return true;
            if (filter === 'notice') return p.category === '공지';
            if (filter === 'general') return p.category === '일반';
            if (filter === 'data') return p.category === '자료';
            return true;
        });
        postsDiv.classList.remove('empty');
        if (!filtered.length) {
            postsDiv.innerHTML = '<p class="empty">등록된 글이 없습니다.</p>';
            setLoading(postsDiv, false);
            return;
        }
        postsDiv.innerHTML = filtered.map((p, idx) => `
            <div class="board-row">
                <span class="col num">${idx + 1}</span>
                <span class="col category"><span class="badge cat">${p.category || '일반'}</span></span>
                <span class="col title"><a href="community-post.html?id=${p.id}" target="_blank" data-id="${p.id}">${p.title}</a>${p.attachmentName ? ' 📎' : ''}</span>
                <span class="col author">${p.author || p.authorEmail || '익명'}</span>
                <span class="col date">${(p.createdAt || '').toString().split('T')[0] || ''}</span>
                <span class="col actions">
                    ${myEmail && p.authorEmail === myEmail ? `
                        <div class="action-buttons">
                            <a class="button ghost edit-post" href="community-edit.html?id=${p.id}">수정</a>
                            <button class="ghost delete-post" data-id="${p.id}">삭제</button>
                        </div>
                    ` : ''}
                </span>
            </div>
        `).join('');
        // 제목을 클릭하면 새 탭에서 글과 댓글을 볼 수 있도록 안내만 표시
        const detailBox = document.getElementById('postDetailContent');
        if (detailBox) {
            detailBox.innerHTML = '<p class="muted">제목을 클릭하면 새 탭에서 글과 댓글을 볼 수 있습니다.</p>';
        }
        postsDiv.querySelectorAll('.delete-post').forEach(btn => {
            btn.addEventListener('click', () => deletePost(btn.dataset.id));
        });
        setLoading(postsDiv, false);
    }

    async function deletePost(id) {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 삭제할 수 있습니다.');
            return;
        }
        await fetch(`${API_BASE}/api/community/posts/${id}`, { method: 'DELETE', headers: authHeaders(token) });
        loadCommunity();
    }

    function renderPostDetail(post) {
        const box = document.getElementById('postDetailContent');
        if (!box) return;
        if (!post) {
            box.textContent = '글을 선택하면 내용이 표시됩니다.';
            return;
        }
        box.innerHTML = `
            <h3>${post.title}</h3>
            <p class="muted">${post.author || post.authorEmail || '익명'} · ${(post.createdAt || '').toString().split('T')[0] || ''} · ${post.category || '일반'}</p>
            <p>${post.content || ''}</p>
            ${post.attachmentName ? `<a href="${post.attachmentData}" download="${post.attachmentName}">첨부 다운로드</a>` : ''}
        `;
    }
}

// 커뮤니티 수정 페이지 (community-edit)
function initCommunityEdit() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');
    const titleInput = document.getElementById('editTitle');
    const contentInput = document.getElementById('editContent');
    const categorySelect = document.getElementById('editCategory');
    const statusEl = document.getElementById('editStatus');
    const heading = document.getElementById('editHeading');
    const saveBtn = document.getElementById('editSave');

    if (!id) {
        if (statusEl) statusEl.textContent = '잘못된 접근입니다.';
        return;
    }

    loadPost();

    async function loadPost() {
        try {
            const res = await fetch(`${API_BASE}/api/community/posts/${id}`);
            if (!res.ok) throw new Error('글을 불러오지 못했습니다.');
            const data = await res.json();
            if (heading) heading.textContent = data.title || '게시글 수정';
            if (titleInput) titleInput.value = data.title || '';
            if (contentInput) contentInput.value = data.content || '';
            if (categorySelect) categorySelect.value = data.category || '일반';
        } catch (e) {
            if (statusEl) statusEl.textContent = e.message;
        }
    }

    async function save() {
        const token = localStorage.getItem('token');
        if (!token) {
            alert('로그인 후 수정할 수 있습니다.');
            return;
        }
        const payload = {
            title: titleInput ? titleInput.value.trim() : '',
            content: contentInput ? contentInput.value.trim() : '',
            category: categorySelect ? categorySelect.value : '일반'
        };
        if (!payload.title || !payload.content) {
            if (statusEl) statusEl.textContent = '제목과 내용을 입력하세요.';
            return;
        }
        if (statusEl) statusEl.textContent = '저장 중...';
        const res = await fetch(`${API_BASE}/api/community/posts/${id}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', ...authHeaders(token) },
            body: JSON.stringify(payload)
        });
        if (!res.ok) {
            const msg = await res.text();
            if (statusEl) statusEl.textContent = msg || '수정에 실패했습니다.';
            return;
        }
        if (statusEl) statusEl.textContent = '저장되었습니다. 목록으로 이동합니다.';
        setTimeout(() => window.location.href = 'community.html', 500);
    }

    if (saveBtn) saveBtn.addEventListener('click', save);
}

// ==============================================
// 5) 채용공고 페이지 (hiring)
// ==============================================
function initHiring() {
    const listEl = document.getElementById('hiringList');
    const refreshBtn = document.getElementById('refreshHiring');
    const load = async () => {
        setLoading(listEl, true);
        try {
            const res = await fetch(`${API_BASE}/api/hiring`);
            const data = await res.json();
            listEl.classList.remove('empty');
            listEl.innerHTML = data.map(cat => `
                <div class="hiring-card">
                    <p class="eyebrow">${cat.name}</p>
                    ${cat.posts.map(p => `
                        <div class="card" style="box-shadow:none; border:1px solid ${'var(--line)'}; margin-bottom:8px;">
                            <p class="eyebrow">${p.company} · ${p.location}</p>
                            <h3>${p.role}</h3>
                            <p>마감 ${p.deadline}</p>
                            <div class="badges">${(p.keywords || []).map(k => `<span class="badge">${k}</span>`).join('')}</div>
                            <a href="${p.url}" target="_blank" rel="noreferrer">공고 보기 →</a>
                        </div>
                    `).join('')}
                </div>
            `).join('');
        } catch (e) {
            listEl.innerHTML = `<p class="empty">${e.message}</p>`;
        }
    };
    if (refreshBtn) refreshBtn.onclick = load;
    load();
}

// ----- 공통 UI 유틸 -----
function setLoading(el, isLoading) {
    el.classList.toggle('loading', isLoading);
    if (isLoading) el.innerHTML = '<p class="empty">잠시만 기다려주세요...</p>';
}

function attachRangeDisplay(rangeId, displayId, suffix) {
    const input = document.getElementById(rangeId);
    const display = document.getElementById(displayId);
    if (!input || !display) return;

    const update = () => display.textContent = input.value;
    input.addEventListener('input', update);
    update();
}

// ==============================================
// 4) 로그인 / 회원가입 페이지
// ==============================================
if (page === 'ai-check') initAiCheck();

function initLoginPage() {
    const form = document.getElementById('loginForm');
    const statusEl = document.getElementById('loginStatus');
    const resetBtn = document.getElementById('resetPasswordBtn');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value || 'pass';
        statusEl.textContent = '로그인 중...';
        try {
            const res = await fetch(`${API_BASE}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            if (!res.ok) throw new Error('로그인 실패');
            const data = await res.json();
            localStorage.setItem('token', data.token);
            updateAuthButtons();
            statusEl.textContent = '완료! 메인 페이지로 이동합니다.';
            setTimeout(() => { window.location.href = 'index.html'; }, 600);
        } catch (err) {
            statusEl.textContent = err.message;
        }
    });
    if (resetBtn) {
        resetBtn.addEventListener('click', async () => {
            const email = prompt('가입한 이메일을 입력하세요');
            if (!email) return;
            try {
                const res = await fetch(`${API_BASE}/api/auth/request-reset`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email })
                });
                const data = await res.json();
                alert(`재설정 코드: ${data.code || '이메일을 확인하세요.'}`);
                const code = prompt('받은 코드를 입력하세요');
                const newPass = prompt('새 비밀번호를 입력하세요');
                if (!code || !newPass) return;
                await fetch(`${API_BASE}/api/auth/reset-password`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, code, newPassword: newPass })
                });
                alert('비밀번호가 재설정되었습니다.');
            } catch (e) {
                alert('재설정에 실패했습니다.');
            }
        });
    }
}

function initAiCheck() {
    const btn = document.getElementById('checkAi');
    const statusEl = document.getElementById('aiStatus');
    if (!btn || !statusEl) return;
    btn.addEventListener('click', async () => {
        statusEl.textContent = '확인 중...';
        statusEl.style.color = '';
        try {
            const res = await fetch(`${API_BASE}/api/health/ai`);
            const data = await res.json();
            if (data.aiConnected) {
                statusEl.textContent = '연결되었습니다';
                statusEl.style.color = '#16a34a';
            } else {
                statusEl.textContent = '실패했습니다';
                statusEl.style.color = '#b91c1c';
            }
        } catch (e) {
            statusEl.textContent = '실패했습니다';
            statusEl.style.color = '#b91c1c';
        }
    });
}

function initSignupPage() {
    const form = document.getElementById('signupForm');
    const statusEl = document.getElementById('signupStatus');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('signupEmail').value;
        const username = document.getElementById('signupId') ? document.getElementById('signupId').value : '';
        const password = document.getElementById('signupPassword').value || 'pass';
        const passwordConfirm = document.getElementById('signupPasswordConfirm') ? document.getElementById('signupPasswordConfirm').value : password;
        if (password !== passwordConfirm) {
            statusEl.textContent = '비밀번호가 일치하지 않습니다.';
            return;
        }
        statusEl.textContent = '가입 중...';
        try {
            // 단순 가입 (이메일 인증 없음)
            const regRes = await fetch(`${API_BASE}/api/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password, username })
            });
            const regData = await regRes.json();
            if (!regRes.ok) throw new Error(regData.error || '가입 처리 중 오류');
            // 가입 후 자동 로그인
            const loginRes = await fetch(`${API_BASE}/api/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            if (!loginRes.ok) throw new Error('로그인 실패');
            const data = await loginRes.json();
            localStorage.setItem('token', data.token);
            updateAuthButtons();
            statusEl.textContent = '가입 및 인증 완료! 로그인되었습니다.';
            setTimeout(() => { window.location.href = 'builder.html'; }, 600);
        } catch (err) {
            statusEl.textContent = err.message;
        }
    });
}
