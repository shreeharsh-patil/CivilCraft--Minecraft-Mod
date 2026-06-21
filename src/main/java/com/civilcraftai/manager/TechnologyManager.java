package com.civilcraftai.manager;

import com.civilcraftai.database.DatabaseManager;

public class TechnologyManager {
    public enum Tech {
        AGRICULTURE("agriculture", 50, "Unlocks advanced crop harvesting and farming AI tasks."),
        METALLURGY("metallurgy", 100, "Unlocks steel/iron production and guard combat AI gear."),
        STEAM_POWER("steam", 200, "Unlocks steam machinery and automated woodcutting AI tasks.");

        private final String id;
        private final int cost;
        private final String description;

        Tech(String id, int cost, String description) {
            this.id = id;
            this.cost = cost;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public int getCost() {
            return cost;
        }

        public String getDescription() {
            return description;
        }

        public static Tech fromId(String id) {
            for (Tech tech : values()) {
                if (tech.getId().equalsIgnoreCase(id)) {
                    return tech;
                }
            }
            return null;
        }
    }

    public static boolean purchaseTech(String townId, Tech tech) {
        if (DatabaseManager.isTechUnlocked(townId, tech.getId())) {
            return false; // Already unlocked
        }
        
        boolean success = DatabaseManager.withdrawCoins(townId, tech.getCost());
        if (success) {
            DatabaseManager.unlockTech(townId, tech.getId());
            return true;
        }
        return false; // Insufficient funds
    }
}
