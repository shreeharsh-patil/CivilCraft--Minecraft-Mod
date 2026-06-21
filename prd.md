# CivilCraft AI - Product Requirements Document (PRD)

## 🎯 1. Target Audience
This mod is built for:
*   Minecraft players who enjoy geopolitical servers, roleplay, and nation-building.
*   Enthusiasts of AI agents who want to see LLMs interact dynamically within simulated virtual environments.
*   Strategy gamers who want first-person 4X strategy mechanics.

---

## 🛠️ 2. Core Feature Requirements

### A. Settlement Claiming & Town Management
*   **Town Hall Block**: Placing this block establishes a Settlement core.
*   **Grid Claims**: Claims are chunks (16x16). Upkeep is charged daily (in-game time) in coins.
*   **Roles & Permissions**: Players can invite other players or assign tasks to AI villagers (e.g., Builder, Farmer, Guard).

### B. LLM-Powered Agent Framework
*   **Personality & Identity**: Every AI villager has a unique JSON-defined profile: Name, Profession, Culture, Traits (e.g., *Greedy*, *Honorable*, *Fearful*), and relationships mapping.
*   **Dialogue Interface**: Right-clicking an agent opens a chat canvas. Players type messages, and the agent responds using an asynchronous LLM API caller.
*   **Long-Term Memory**: Agents write key summaries of interactions to SQLite database tables.

### C. Geopolitics & Factions
*   **Factions**: Multiple towns can merge to form a Nation.
*   **Diplomatic States**: Neutral, Friendly (Alliance), or Hostile (War).
*   **Wars**: Declaring war requires a cassus belli or reputation penalty. Wars are won by capturing the enemy's Town Hall block or forcing a surrender treaty via dialogue.

### D. Economy & Trade
*   **Ledger**: SQLite database tracks coin balances for players and towns.
*   **Merchant Caravans**: Custom entities that travel along pathways between towns.
*   **Local Markets**: Chest shops managed by player or AI merchants.

### E. Technology & Progression Tree
*   **Tech Node Registry**: Unlocking *Agriculture* enables automated farms; unlocking *Metallurgy* enables iron weapons; unlocking *Steam/Redstone* enables automated miners.
*   **Era System**: Advances server-wide capabilities.

---

## 🎚️ 3. Non-Functional Requirements
*   **Zero Main-Thread Blocking**: All LLM API calls, SQLite queries, and pathfinding tasks must execute asynchronously on secondary threads.
*   **Compatability**: Target Fabric Loader for Minecraft 1.21.x to ensure excellent client performance and developer ease.
*   **Configurability**: Allow server hosts to configure LLM API provider keys (Ollama, OpenAI, Gemini, Vertex AI).
