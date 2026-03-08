(function () {
  const textarea = document.getElementById("code");
  const messages = document.getElementById("messages");
  const submitBtn = document.getElementById("submit-btn");
  const form = document.getElementById("review-form");

  function scrollToBottom(behavior = 'smooth') {
    if (!messages) return;
    const container = messages.closest('.chat-window');
    if (container) {
      try {
        container.scrollTo({ top: container.scrollHeight, behavior });
      } catch (e) {
        container.scrollTop = container.scrollHeight;
      }
    }
  }

  // On load, focus the textarea and scroll to bottom
  document.addEventListener('DOMContentLoaded', () => {
    if (textarea) textarea.focus();
    // small timeout to allow server-rendered messages to layout
    setTimeout(() => scrollToBottom('auto'), 50);
  });

  // Observe new messages and scroll when they appear
  if (messages && typeof MutationObserver !== 'undefined') {
    const observer = new MutationObserver(() => scrollToBottom());
    observer.observe(messages, { childList: true, subtree: true });
  }

  if (!textarea) return;

  textarea.addEventListener("keydown", (event) => {
    if (event.key === "Tab") {
      event.preventDefault();

      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const before = textarea.value.substring(0, start);
      const after = textarea.value.substring(end);

      textarea.value = before + "  " + after;
      textarea.selectionStart = textarea.selectionEnd = start + 2;
      return;
    }

    const isCtrlEnter = (event.ctrlKey || event.metaKey) && event.key === "Enter";
    if (isCtrlEnter) {
      const formEl = textarea.closest("form");
      if (formEl) {
        event.preventDefault();
        formEl.requestSubmit();
      }
    }
  });

  // Enable/disable submit button based on content
  function updateSubmitState() {
    if (!submitBtn) return;
    submitBtn.disabled = textarea.value.trim() === '';
  }

  textarea.addEventListener('input', updateSubmitState);
  updateSubmitState();

  // Prevent double submits and show a submitting state
  if (form) {
    form.addEventListener('submit', (e) => {
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Reviewing...';
      }
      // allow normal submit to proceed
    });
  }
})();