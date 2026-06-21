# CivilCraft AI - Technical Requirements Document (TRD)

## 🏗️ 1. Architecture Overview
The mod is divided into three distinct modules to separate concerns:
1.  **Core Mod Module**: Registers blocks, items, custom PathfinderMob entities (AI Villagers), client screen handlers, and standard gameplay logic.
2.  **State Database Module**: Persists claims, relations, and agent dialogue logs to SQLite.
3.  **Asynchronous Agent Brain Module**: Manages the thread pool that dispatches prompt tasks, processes responses, updates agent long-term memory structures, and queues task commands back to the main Minecraft tick thread.

---

## 🗄️ 2. Database Schema (SQLite)

```sql
-- Towns & Claims
CREATE TABLE IF NOT EXISTS towns (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    founder_uuid TEXT NOT NULL,
    balance INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS claims (
    dimension TEXT NOT NULL,
    chunk_x INTEGER NOT NULL,
    chunk_z INTEGER NOT NULL,
    town_id TEXT,
    PRIMARY KEY (dimension, chunk_x, chunk_z),
    FOREIGN KEY (town_id) REFERENCES towns(id)
);

-- AI Memory System
CREATE TABLE IF NOT EXISTS agent_memory (
    agent_id TEXT NOT NULL,
    entity_uuid TEXT PRIMARY KEY,
    personality_json TEXT NOT NULL,
    summary TEXT,
    last_interaction INTEGER
);

CREATE TABLE IF NOT EXISTS dialogue_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_uuid TEXT NOT NULL,
    speaker_name TEXT NOT NULL,
    message TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    FOREIGN KEY (entity_uuid) REFERENCES agent_memory(entity_uuid)
);
```

---

## 🔄 3. Threading Model
*   **Main Server Thread**: Handles entity ticking, player movement, physics, blocks, and rendering.
*   **Worker Thread Pool**: Handles REST requests to LLM APIs, sqlite read/write queries, and pathfinder generation.
*   **Tick Sync Queue**: The Worker Thread Pool schedules actions back onto the Main Server Thread via `MinecraftServer#execute` to prevent race conditions or crashes when editing world blocks or modifying entity states.

---

## 📡 4. Network Protocols (Custom Packets)
*   **Claim Boundary Sync**: Server sends `s2c_sync_borders` packet containing boundary coordinates to allow client-side particle rendering of claimed land borders.
*   **Town UI Sync**: Server sends `s2c_open_town_screen` containing town statistics, treasury ledger, and member tables when players interact with the Town Hall block.
