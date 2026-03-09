document.addEventListener('DOMContentLoaded', () => {
  const textarea = document.getElementById("code");
  const form = document.getElementById("review-form");
  const submitBtn = document.getElementById("submit-btn");
  const langSelect = document.getElementById("language");
  const loadingIndicator = document.getElementById("loading-indicator");
  const chatWindow = document.getElementById("chat-window");
  const fileInput = document.getElementById("file-upload");
  const fileNameDisplay = document.getElementById("file-name");
  const themeToggle = document.getElementById("theme-toggle");
  const lintBadge = document.getElementById("lint-badge");
  const resizer = document.getElementById("resizer");
  const editorPane = document.querySelector(".editor-pane");

  // Resizable panes logic
  if (resizer && chatWindow && editorPane) {
    let isResizing = false;

    resizer.addEventListener("mousedown", (e) => {
      isResizing = true;
      document.body.style.cursor = 'col-resize';
      // Prevent text selection while dragging
      document.body.style.userSelect = 'none';
    });

    document.addEventListener("mousemove", (e) => {
      if (!isResizing) return;
      
      // Calculate new width of the chat window based on mouse position
      // Mouse X from the right edge gives the new chat window width
      const newWidth = document.body.clientWidth - e.clientX;
      
      // Constraints: min 300px, max 800px or up to a limit so editor is visible
      if (newWidth >= 300 && newWidth <= 800 && newWidth < document.body.clientWidth - 300) {
        chatWindow.style.width = `${newWidth}px`;
      }
    });

    document.addEventListener("mouseup", () => {
      if (isResizing) {
        isResizing = false;
        document.body.style.cursor = '';
        document.body.style.userSelect = '';
        // Signal CodeMirror to resize since container size changed
        if (typeof editor !== 'undefined' && editor) {
          editor.refresh();
        }
      }
    });
  }

  // Load theme from localStorage
  const currentTheme = localStorage.getItem("theme") || "dark";
  if (currentTheme === "light") {
    document.documentElement.setAttribute("data-theme", "light");
  }

  // Theme Toggler
  if (themeToggle) {
    themeToggle.addEventListener("click", () => {
      let theme = document.documentElement.getAttribute("data-theme");
      if (theme === "light") {
        document.documentElement.removeAttribute("data-theme");
        localStorage.setItem("theme", "dark");
        if (editor) editor.setOption("theme", "material-ocean");
      } else {
        document.documentElement.setAttribute("data-theme", "light");
        localStorage.setItem("theme", "light");
        if (editor) editor.setOption("theme", "eclipse");
      }
    });
  }

  // Generic syntax linter (bracket matching) for all languages
  if (typeof CodeMirror !== 'undefined') {
    CodeMirror.registerHelper("lint", "generic", function(text) {
      const found = [];
      const stack = [];
      const pairs = { '{': '}', '[': ']', '(': ')' };
      const revPairs = { '}': '{', ']': '[', ')': '(' };
      const lines = text.split('\n');

      let inString = false;
      let stringChar = '';

      for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        for (let j = 0; j < line.length; j++) {
          const ch = line[j];
          
          // Basic string ignoring
          if (!inString && (ch === '"' || ch === "'" || ch === '`')) {
            inString = true;
            stringChar = ch;
            continue;
          } else if (inString && ch === stringChar && line[j-1] !== '\\') {
            inString = false;
            continue;
          }

          if (inString) continue;

          if (pairs[ch]) {
            stack.push({ch: ch, line: i, chPos: j});
          } else if (revPairs[ch]) {
            if (stack.length === 0 || stack[stack.length - 1].ch !== revPairs[ch]) {
              found.push({
                message: "Unmatched or unexpected '" + ch + "'",
                severity: "error",
                from: CodeMirror.Pos(i, j),
                to: CodeMirror.Pos(i, j + 1)
              });
              // To prevent cascading errors, clear stack if mismatched closing brace
              if (stack.length > 0) stack.pop(); 
            } else {
              stack.pop();
            }
          }
        }
      }

      while (stack.length > 0) {
        const item = stack.pop();
        found.push({
          message: "Unclosed '" + item.ch + "'",
          severity: "error",
          from: CodeMirror.Pos(item.line, item.chPos),
          to: CodeMirror.Pos(item.line, item.chPos + 1)
        });
      }
      return found;
    });
  }

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
      theme: document.documentElement.getAttribute("data-theme") === "light" ? "eclipse" : "material-ocean",
      lineNumbers: true,
      matchBrackets: true,
      autoCloseBrackets: true,
      indentUnit: 4,
      tabSize: 4,
      indentWithTabs: false,
      lineWrapping: true,
      gutters: ["CodeMirror-lint-markers", "CodeMirror-linenumbers"],
      lint: {
        getAnnotations: CodeMirror.lint.generic,
        async: false
      },
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

    // Update lint badge based on syntax errors
    function updateLintBadge(errors) {
      if (!lintBadge) return;
      if (errors.length > 0) {
        lintBadge.className = "lint-status has-errors";
        lintBadge.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> ${errors.length} error(s)`;
      } else {
        lintBadge.className = "lint-status no-errors";
        lintBadge.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg> No syntax errors`;
      }
    }

    // Capture lint updates
    editor.on("update", () => {
       setTimeout(() => {
          const state = editor.state.lint;
          if (state && state.marked) {
              updateLintBadge(state.marked);
          }
       }, 500);
    });

    // Auto-focus
    setTimeout(() => {
        editor.focus();
        editor.performLint(); // trigger linting on load
    }, 100);

    // Highlight lines with issues from AI findings
    setTimeout(() => {
      document.querySelectorAll('.finding').forEach(finding => {
        const lineStartAttr = finding.getAttribute('data-line-start');
        const severity = finding.getAttribute('data-severity');
        if (lineStartAttr && lineStartAttr !== 'null' && severity) {
          const lineNum = parseInt(lineStartAttr, 10);
          if (!isNaN(lineNum) && lineNum > 0) {
            editor.addLineClass(lineNum - 1, "background", "cm-highlight-" + severity);
          }
        }
      });
    }, 200);
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

  // --- Left Sidebar History Fetch ---
  const recentList = document.getElementById("recent-reviews-list");
  if (recentList) {
    fetch('/review/history')
      .then(res => res.text())
      .then(html => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const rows = doc.querySelectorAll('.history-table tbody tr');
        let hasData = false;
        
        recentList.innerHTML = '';
        
        rows.forEach(row => {
            if (!row.querySelector('.empty-state')) {
                hasData = true;
                const idCell = row.cells[0]?.textContent.trim() || '';
                const langCell = row.cells[1]?.textContent.trim() || '';
                
                const item = document.createElement('a');
                item.className = 'explorer-item';
                item.href = '/review/history';
                item.innerHTML = `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z"/></svg> ${idCell} (${langCell})`;
                recentList.appendChild(item);
            }
        });
        
        if (!hasData) {
            recentList.innerHTML = '<div class="text-muted" style="padding: 4px 8px; font-size: 11px;">No history found.</div>';
        }
      })
      .catch(err => {
         recentList.innerHTML = '<div class="text-muted" style="padding: 4px 8px; font-size: 11px;">Could not load history.</div>';
      });
  }

  // --- Category Filtering ---
  const categoryTabs = document.getElementById("category-tabs");
  const findingsList = document.getElementById("findings-list");
  if (categoryTabs && findingsList) {
    const tabs = categoryTabs.querySelectorAll(".category-tab");
    const findings = findingsList.querySelectorAll(".finding");

    tabs.forEach(tab => {
      tab.addEventListener("click", () => {
        // Update active class
        tabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");

        const selectedCategory = tab.getAttribute("data-category");

        // Filter findings
        findings.forEach(finding => {
          if (selectedCategory === "ALL" || finding.getAttribute("data-category") === selectedCategory) {
            finding.style.display = "flex";
          } else {
            finding.style.display = "none";
          }
        });
      });
    });
  }

  // --- Export Markdown ---
  const exportBtn = document.getElementById("export-btn");
  if (exportBtn) {
    exportBtn.addEventListener("click", () => {
      let markdown = "# AI Code Review Report\n\n";
      
      const lang = langSelect ? langSelect.value : 'java';
      const file = document.getElementById("file-name")?.textContent || "Code Snippet";
      markdown += `**Language:** ${lang}\n`;
      markdown += `**File:** ${file}\n\n`;

      const gauge = document.querySelector(".score-value span");
      if (gauge) {
        markdown += `## Quality Score: ${gauge.textContent}/10\n\n`;
      }

      const metrics = document.querySelectorAll(".metric-pill");
      if (metrics.length > 0) {
        markdown += `## Complexity\n`;
        metrics.forEach(pill => {
          markdown += `- **${pill.textContent.trim()}**\n`;
        });
        markdown += `\n`;
      }

      const summary = document.getElementById("raw-summary");
      if (summary) {
        markdown += `## Summary\n${summary.textContent.trim()}\n\n`;
      }

      if (findingsList) {
        const findings = findingsList.querySelectorAll(".finding");
        if (findings.length > 0) {
          markdown += `## Findings (${findings.length})\n\n`;
          findings.forEach(finding => {
            const sev = finding.querySelector(".severity-badge")?.textContent || "";
            const cat = finding.querySelector(".category-badge")?.textContent || "";
            const rule = finding.querySelector(".rule")?.textContent || "";
            const lines = finding.querySelector(".lines")?.textContent || "";
            const msg = finding.querySelector(".msg")?.textContent || "";
            const sug = finding.querySelector(".sug")?.textContent || "";
            
            markdown += `### ${sev} | ${cat} | ${rule} ${lines}\n`;
            markdown += `**Issue:** ${msg}\n\n`;
            if (sug) {
              markdown += `**Suggestion:** ${sug}\n\n`;
            }
          });
        }
      }

      const optimized = document.getElementById("raw-optimized");
      if (optimized) {
        markdown += `## Optimized Code\n\`\`\`${lang}\n${optimized.textContent.trim()}\n\`\`\`\n`;
      }

      // Download
      const blob = new Blob([markdown], { type: 'text/markdown' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `code_review_${new Date().getTime()}.md`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    });
  }
});