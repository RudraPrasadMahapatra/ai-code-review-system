#  AI-Powered Code Review System

> ⚡ **Built with Vibe Coding:** This entire enterprise-grade application was rapidly developed, debugged, and architected purely through autonomous AI-driven prompt engineering, showcasing the blistering speed of AI Pair Programming!

A professional, full-stack AI Code Review tool designed to feel like enterprise developer software (similar to GitHub Copilot or SonarQube). It analyzes your source code across multiple languages, detects bugs and security flaws using a **hybrid analysis approach**, and provides intelligent, actionable suggestions.

![AI Code Reviewer Banner](./ai_code_reviewer_main_page_1773081600559.png) <!-- You can optionally replace this with an actual screenshot path if pushed -->

## ✨ Features

- **Advanced UI/UX:** A robust 3-panel layout featuring a file explorer, an integrated **CodeMirror editor** (with syntax highlighting and line addressing), and a real-time findings assistant pane.
- **Multi-Language Support:** Java, Python, JavaScript, TypeScript, Go, Rust, C++, C#, Kotlin, and Ruby.
- **Hybrid Code Analysis (Cost Optimized):**
  - **Lightweight Static Pre-Analyzer:** Scans locally for SQL injections, division by zeros, out-of-bounds arrays, memory leaks, and hardcoded credentials.
  - **Context-Window Truncating:** If static bugs are found in a massive file, only the relevant logical chunks are sent to the AI, saving enormous LLM token costs.
  - **AI Database Caching:** Automatically computes an MD5 hash of your uploaded snippet. If the exact code is reviewed twice, it hits the cache bypassing the API billing.
- **Developer Scorecard:** Rates your code Quality out of 10, highlights algorithmic Time/Space Complexities, and categorizes findings by `ERROR`, `WARNING`, and `INFO`. 
- **Dashboard & History Tracking:** View all past reviews alongside Token Usage, average AI calls, and estimated financial costs.
- **Instant Deployable:** Fully containerized architecture using Docker, ready for 1-click free deployments via platforms like Render.com.

---

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Spring AI
- **Database:** H2 (In-memory default) / MySQL (Via Profiles)
- **Frontend:** HTML5, Vanilla JS, CSS3, Thymeleaf, CodeMirror
- **LLM Integration:** OpenAI `gpt-4o-mini`

---

## 🚀 Getting Started Locally

### 1. Prerequisites

Make sure you have installed on your local machine:
- **Java 17** or higher
- An **OpenAI API Key**

### 2. Setting up your Environment Variable

You need to inject your API key into your system environment so Spring AI can pick it up.

**For Mac/Linux:**
```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

**For Windows (Command Prompt):**
```cmd
set OPENAI_API_KEY="sk-your-api-key-here"
```

### 3. Run the Application

The repository includes a Maven wrapper, so you don't even need Maven installed globally!

```bash
# Clone the repository
git clone https://github.com/RudraPrasadMahapatra/ai-code-review-system.git
cd ai-code-review-system

# Start the Spring Boot Server
./mvnw clean spring-boot:run
```

### 4. Open in your Browser
- **Main Review App:** [http://localhost:8081/review](http://localhost:8081/review)
- **History Dashboard:** [http://localhost:8081/review/history](http://localhost:8081/review/history)

*(Note: The default port mapped in `application.yml` is `8081`.)*

---

## 🐳 Running with Docker

You can completely bypass installing Java by just using Docker!

```bash
# Provide your API key while running the docker container
docker build -t ai-code-reviewer .

docker run -p 8081:8081 -e OPENAI_API_KEY="sk-your-api-key-here" ai-code-reviewer
```

---

## ☁️ Deploy for Free (Render.com)

Because this application dynamically utilizes an in-memory SQL database (H2) and uses dynamic LLM routing, it is the perfect candidate for a free cloud deployment. 

A `render.yaml` infrastructure-as-code file is included to make this a 1-click process.

1. Create a free account at [Render.com](https://render.com/).
2. In the Render Dashboard, click **New +** > **Blueprint**.
3. Link your GitHub and select this repository.
4. Render will automatically parse the `render.yaml` configuration.
5. In your new Web Service on Render, navigate to **Environment** and add the explicitly required `OPENAI_API_KEY` variable.
6. Click Deploy! Every git push to `main` will automatically trigger a new live deployment for free.

---

## 📁 Repository Structure

- `src/main/java.../controller:` Houses `CodeReviewWebController.java` routing Thymeleaf frontend templates.
- `src/main/java.../service:`
  - `AiCodeReviewService.java:` Business logic dealing with hashing/caching, pre-analysis, and invoking the AI runner.
  - `StaticAnalyzer.java:` Local RegEx-based instant bug scanning.
  - `OpenAiCodeReviewEngine.java:` Constructing AI JSON Prompts and mapping them securely.
- `src/main/resources/templates:` HTML templates for `/review` and `/history`.
- `src/main/resources/static:` Stores `CodeMirror` instances, custom Javascript, and CSS Theme toggles (`review.css`).
