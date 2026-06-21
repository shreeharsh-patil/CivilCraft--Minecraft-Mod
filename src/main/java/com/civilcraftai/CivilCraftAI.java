package com.civilcraftai;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.civilcraftai.database.DatabaseManager;

public class CivilCraftAI implements ModInitializer {
    public static final String MOD_ID = "civilcraftai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing CivilCraft AI...");
        try {
            DatabaseManager.initialize();
            LOGGER.info("CivilCraft AI SQLite database initialized successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize CivilCraft AI SQLite database!", e);
        }
    }
}
