document.addEventListener('DOMContentLoaded', () => {
  const textarea = document.getElementById("code");
  const form = document.getElementById("review-form");
  const submitBtn = document.getElementById("submit-btn");
  const langSelect = document.getElementById("language");
  const loadingIndicator = document.getElementById("loading-indicator");
  const chatWindow = document.getElementById("chat-window");
  const fileInput = document.getElementById("file-upload");
  const fileNameDisplay = document.getElementById("file-name");

  // File upload display
  if (fileInput && fileNameDisplay) {
    fileInput.addEventListener("change", (e) => {
      if (e.target.files.length > 0) {
        fileNameDisplay.textContent = e.target.files[0].name;
      } else {
        fileNameDisplay.textContent = "";
      }
      updateSubmitState();
    });
  }

  // Scroll chat to bottom
  function scrollToBottom() {
    if (chatWindow) {
      chatWindow.scrollTop = chatWindow.scrollHeight;
    }
  }
  setTimeout(scrollToBottom, 50);

  // Initialize CodeMirror
  let editor = null;
  if (textarea && typeof CodeMirror !== 'undefined') {
    editor = CodeMirror(document.getElementById("editor-container"), {
      value: textarea.value || "",
      mode: getCodeMirrorMode(langSelect ? langSelect.value : 'java'),
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

    // Sync CodeMirror → hidden textarea
    editor.on("change", () => {
      textarea.value = editor.getValue();
      updateSubmitState();
    });

    // Auto-focus
    setTimeout(() => editor.focus(), 100);
  }

  // Language change → update syntax highlighting
  if (langSelect && editor) {
    langSelect.addEventListener("change", (e) => {
      editor.setOption("mode", getCodeMirrorMode(e.target.value));
    });
  }

  // Enable/disable submit
  function updateSubmitState() {
    if (!submitBtn) return;
    const hasCode = textarea && textarea.value.trim() !== '';
    const hasFile = fileInput && fileInput.files.length > 0;
    const canSubmit = hasCode || hasFile;
    submitBtn.disabled = !canSubmit;
  }

  if (textarea) updateSubmitState();

  // Form submission with loading state
  if (form) {
    form.addEventListener('submit', (e) => {
      if (editor) textarea.value = editor.getValue();

      const hasCode = textarea && textarea.value.trim() !== '';
      const hasFile = fileInput && fileInput.files.length > 0;

      if (!hasCode && !hasFile) {
        e.preventDefault();
        return;
      }

      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `
            <div class="spinner" style="width:14px;height:14px;border-width:2px;"></div>
            <span>Analyzing...</span>
        `;
      }

      if (loadingIndicator) {
        loadingIndicator.style.display = 'flex';
        scrollToBottom();
      }
    });
  }

  /**
   * Map language names to CodeMirror mode identifiers.
   * Covers all 10 languages supported by the app.
   */
  function getCodeMirrorMode(lang) {
    const l = (lang || "").toLowerCase();
    const modes = {
      'java':       'text/x-java',
      'kotlin':     'text/x-kotlin',
      'csharp':     'text/x-csharp',
      'cpp':        'text/x-c++src',
      'javascript': 'javascript',
      'typescript': 'application/typescript',
      'python':     'python',
      'go':         'go',
      'rust':       'rust',
      'ruby':       'ruby',
    };
    return modes[l] || 'text/x-java';
  }
});