package com.civilcraftai;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.civilcraftai.database.DatabaseManager;
import com.civilcraftai.entity.CivilianEntity;
import com.civilcraftai.command.ChatCommand;

public class CivilCraftAI implements ModInitializer {
    public static final String MOD_ID = "civilcraftai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final EntityType<CivilianEntity> CIVILIAN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "civilian"),
            EntityType.Builder.create(CivilianEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6F, 1.95F)
                    .build("civilian")
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing CivilCraft AI...");
        try {
            DatabaseManager.initialize();
            LOGGER.info("CivilCraft AI SQLite database initialized successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize CivilCraft AI SQLite database!", e);
        }

        // Register attributes
        FabricDefaultAttributeRegistry.register(CIVILIAN, CivilianEntity.createCivilianAttributes());

        // Register commands
        ChatCommand.register();
    }
}
