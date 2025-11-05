(function(api){
  const questionList = document.getElementById('questionList');
  const chatLog = document.getElementById('chatLog');
  const chatForm = document.getElementById('chatForm');
  const chatMessage = document.getElementById('chatMessage');
  const newQuestionBtn = document.getElementById('newQuestionBtn');

  if (!questionList) return;

  const mockQuestions = [
    {
      title: 'Spring MVC 구조가 잘 이해되지 않아요',
      step: 'Step Spring 핵심 - 핵심기술',
      week: 'Week 5',
      answers: 3,
      createdAt: '2024-03-01'
    },
    {
      title: 'REST API 에러 응답을 어떻게 표준화하나요?',
      step: 'Step REST API 설계 - 실무스택',
      week: 'Week 9',
      answers: 1,
      createdAt: '2024-02-28'
    },
    {
      title: 'Docker compose로 로컬 개발환경 구성 팁 있을까요?',
      step: 'Step Docker - 보너스',
      week: 'Week 13',
      answers: 2,
      createdAt: '2024-02-27'
    }
  ];

  function renderQuestions() {
    questionList.innerHTML = '';
    mockQuestions.forEach(q => {
      const card = document.createElement('div');
      card.className = 'question-card';
      card.innerHTML = `
        <h3>${q.title}</h3>
        <div class="meta">
          <span>${q.step}</span>
          <span>${q.week}</span>
          <span>답변 ${q.answers}개</span>
          <span>${q.createdAt}</span>
        </div>
      `;
      questionList.appendChild(card);
    });
  }

  renderQuestions();

  newQuestionBtn.addEventListener('click', () => {
    if (!api.getAuthToken()) {
      alert('질문을 등록하려면 로그인해주세요.');
      window.location.href = 'login.html';
      return;
    }
    const title = prompt('질문 제목을 입력해주세요');
    if (title) {
      mockQuestions.unshift({
        title,
        step: 'Step 커스텀',
        week: 'Week ?',
        answers: 0,
        createdAt: new Date().toISOString().slice(0, 10)
      });
      renderQuestions();
    }
  });

  chatForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const message = chatMessage.value.trim();
    if (!message) return;

    appendChat(message, true);
    chatMessage.value = '';

    setTimeout(() => {
      appendChat('좋은 질문이에요! Step 2 주차 학습 내용을 복습하고, 공식 문서 예제를 직접 구현해보세요.', false);
    }, 600);
  });

  function appendChat(text, isUser) {
    const bubble = document.createElement('div');
    bubble.className = 'chat-message' + (isUser ? ' user' : '');
    bubble.textContent = text;
    chatLog.appendChild(bubble);
    chatLog.scrollTop = chatLog.scrollHeight;
  }
})(window.Job2RoadmapApi);
