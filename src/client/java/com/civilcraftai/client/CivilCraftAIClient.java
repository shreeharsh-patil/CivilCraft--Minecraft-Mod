package com.civilcraftai.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CivilCraftAIClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("CivilCraft AI Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing CivilCraft AI Client...");
    }
}
