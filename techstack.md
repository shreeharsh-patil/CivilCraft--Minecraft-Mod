# CivilCraft AI - Tech Stack Specification

## ⚙️ 1. Platform & Environment
*   **Game Engine**: Minecraft 1.21.x
*   **Modding Toolchain**: Fabric Loader / Loom Gradle
*   **Programming Language**: Java 21
*   **Server/Client Structure**: Standard dedicated server and client architecture.

---

## 📦 2. Libraries & Dependencies
*   **Fabric API**: Primary library for standard Minecraft block/item registries, event hooks, and packet protocols.
*   **SQLite (JDBC/HikariCP)**: Database engine to store town boundaries, faction standings, transaction ledgers, and AI agent memory.
    *   *Why?* Extremely fast local SQL file database that doesn't require complex installation for the user.
*   **Java HttpClient**: Used for making asynchronous REST requests to LLM endpoints.
*   **Jackson/Gson**: For JSON parsing of agent profiles and LLM request/response body mappings.
*   **Cloth Config / YetAnotherConfigLib**: To create clean configuration menus for server owners.

---

## 🤖 3. AI & Natural Language Processing
The AI Villagers rely on a dual-layer cognitive architecture:
1.  **Macro-Decisions (Rule/Behavior-Based)**: Local behavior trees (using Minecraft's pathfinding tasks and AI memory module) control routine pathing, combat, farming, and resting.
2.  **Micro-Decisions & Conversations (LLM-Based)**: Conversational and diplomatic decision layers communicate asynchronously with LLM backends:
    *   **Local Backend**: Ollama (e.g., Llama 3 / Mistral) for offline, privacy-first single-player sessions.
    *   **Remote Backend**: Gemini API, Vertex AI, or OpenAI API for cloud hosting and server deployments.
