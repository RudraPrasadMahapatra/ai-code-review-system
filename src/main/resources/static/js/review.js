document.addEventListener('DOMContentLoaded', () => {
  const textarea = document.getElementById("code");
  const form = document.getElementById("review-form");
  const submitBtn = document.getElementById("submit-btn");
  const langSelect = document.getElementById("language");
  const loadingIndicator = document.getElementById("loading-indicator");
  const chatWindow = document.getElementById("chat-window");
  const messagesList = document.getElementById("messages");

  // Keep chat scrolled to bottom
  function scrollToBottom() {
    if (chatWindow) {
      chatWindow.scrollTop = chatWindow.scrollHeight;
    }
  }
  setTimeout(scrollToBottom, 50);

  // Initialize CodeMirror 6 (which is actually exposed globally as CodeMirror in the legacy v5 cdn link used, 
  // but we included the v5 scripts per standard non-module setups to avoid build tools).
  // Setup CodeMirror instance
  let editor = null;
  if (textarea && typeof CodeMirror !== 'undefined') {
    editor = CodeMirror(document.getElementById("editor-container"), {
      value: textarea.value || "",
      mode: getCodeMirrorMode(langSelect ? langSelect.value : 'Java'),
      theme: "material-ocean",
      lineNumbers: true,
      matchBrackets: true,
      autoCloseBrackets: true,
      indentUnit: 4,
      tabSize: 4,
      indentWithTabs: false,
      lineWrapping: true,
      extraKeys: {
        "Ctrl-Enter": function() {
          if (form) form.requestSubmit();
        },
        "Cmd-Enter": function() {
          if (form) form.requestSubmit();
        }
      }
    });

    // Sync CodeMirror changes to hidden textarea
    editor.on("change", () => {
      textarea.value = editor.getValue();
      updateSubmitState();
    });
    
    // Focus automatically
    setTimeout(() => editor.focus(), 100);
  }

  // Handle language change for syntax highlighting
  if (langSelect && editor) {
    langSelect.addEventListener("change", (e) => {
      editor.setOption("mode", getCodeMirrorMode(e.target.value));
    });
  }

  // Enable/disable submit button based on content
  function updateSubmitState() {
    if (!submitBtn || !textarea) return;
    const isEmpty = textarea.value.trim() === '';
    submitBtn.disabled = isEmpty;
    if (isEmpty) {
        submitBtn.style.opacity = '0.5';
        submitBtn.style.cursor = 'not-allowed';
    } else {
        submitBtn.style.opacity = '1';
        submitBtn.style.cursor = 'pointer';
    }
  }

  if (textarea) updateSubmitState();

  // Form Submission & Loading State
  if (form) {
    form.addEventListener('submit', (e) => {
      // Ensure sync one last time
      if (editor) textarea.value = editor.getValue();

      if (textarea.value.trim() === '') {
        e.preventDefault();
        return;
      }

      // Show loading state
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `
            <span>Analyzing...</span>
            <svg class="spinner" viewBox="0 0 50 50" style="width:16px; height:16px; border:none; animation: spin 1s linear infinite;"><circle cx="25" cy="25" r="20" fill="none" stroke="currentColor" stroke-width="4" stroke-dasharray="31.4 31.4" stroke-linecap="round"></circle></svg>
        `;
      }
      
      // Hide empty state if exists
      const emptyState = document.querySelector('.empty-state');
      if (emptyState) emptyState.style.display = 'none';

      // Show loading indicator in chat
      if (loadingIndicator) {
          loadingIndicator.style.display = 'flex';
          scrollToBottom();
      }
    });
  }

  // map java/spring languages to codemirror modes
  function getCodeMirrorMode(lang) {
    const l = (lang || "").toLowerCase();
    if (l.includes("java")) return "text/x-java";
    if (l.includes("python")) return "python";
    if (l.includes("javascript") || l.includes("ts") || l.includes("node")) return "javascript";
    if (l.includes("xml") || l.includes("html")) return "xml";
    return "text/x-java"; // default
  }
});