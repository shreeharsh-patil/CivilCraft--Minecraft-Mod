package com.civilcraftai;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.civilcraftai.database.DatabaseManager;
import com.civilcraftai.entity.CivilianEntity;
import com.civilcraftai.command.ChatCommand;
import com.civilcraftai.command.TownCommand;
import com.civilcraftai.block.TownHallBlock;
import com.civilcraftai.item.CoinItem;
import com.civilcraftai.claim.ClaimManager;
import com.civilcraftai.network.ClaimBorderSyncPayload;

public class CivilCraftAI implements ModInitializer {
    public static final String MOD_ID = "civilcraftai";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Entity Registry
    public static final EntityType<CivilianEntity> CIVILIAN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "civilian"),
            EntityType.Builder.create(CivilianEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6F, 1.95F)
                    .build("civilian")
    );

    // Block Registry
    public static final Block TOWN_HALL_BLOCK = Registry.register(
            Registries.BLOCK,
            Identifier.of(MOD_ID, "town_hall"),
            new TownHallBlock(AbstractBlock.Settings.create().strength(3.0f).requiresTool())
    );

    // Item Registry
    public static final Item TOWN_HALL_BLOCK_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "town_hall"),
            new BlockItem(TOWN_HALL_BLOCK, new Item.Settings())
    );

    public static final Item COIN = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "coin"),
            new CoinItem(new Item.Settings())
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

        // Register custom entity attributes
        FabricDefaultAttributeRegistry.register(CIVILIAN, CivilianEntity.createCivilianAttributes());

        // Register S2C payload type
        PayloadTypeRegistry.playS2C().register(ClaimBorderSyncPayload.ID, ClaimBorderSyncPayload.CODEC);

        // Register commands
        ChatCommand.register();
        TownCommand.register();

        // Initialize Claim Manager
        ClaimManager.initialize();
        LOGGER.info("CivilCraft AI Claim Manager registered successfully.");

        // Register ticking loop to sync chunk updates to client
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    ChunkPos currentChunk = player.getChunkPos();
                    String dimension = player.getWorld().getRegistryKey().getValue().toString();
                    String ownerTownId = DatabaseManager.getChunkOwnerTownId(dimension, currentChunk.x, currentChunk.z);
                    String townName = ownerTownId != null ? DatabaseManager.getTownName(ownerTownId) : "Wilderness";

                    // Sync packet to client
                    ServerPlayNetworking.send(player, new ClaimBorderSyncPayload(currentChunk.x, currentChunk.z, townName));
                }
            }
        });
    }
}
