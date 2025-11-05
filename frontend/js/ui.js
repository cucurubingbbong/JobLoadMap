(function() {
  function setupAccordion(container) {
    container.querySelectorAll('.accordion-item').forEach(item => {
      const header = item.querySelector('.accordion-header');
      if (!header) return;
      header.addEventListener('click', () => {
        item.classList.toggle('active');
      });
    });
  }

  function copyToClipboard(text) {
    return navigator.clipboard.writeText(text);
  }

  window.Job2RoadmapUI = {
    setupAccordion,
    copyToClipboard,
  };
})();
