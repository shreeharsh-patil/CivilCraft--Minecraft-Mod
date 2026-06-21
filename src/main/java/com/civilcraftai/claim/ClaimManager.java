package com.civilcraftai.claim;

import com.civilcraftai.database.DatabaseManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.ChunkPos;

public class ClaimManager {
    public static void initialize() {
        // Enforce block breaking permissions
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClient()) {
                ChunkPos chunkPos = world.getChunk(pos).getPos();
                String dimension = world.getRegistryKey().getValue().toString();
                String ownerTownId = DatabaseManager.getChunkOwnerTownId(dimension, chunkPos.x, chunkPos.z);
                
                if (ownerTownId != null) {
                    boolean isMember = DatabaseManager.isTownMember(ownerTownId, player.getUuidAsString());
                    if (!isMember) {
                        player.sendMessage(Text.literal("§cThis chunk is claimed by " + DatabaseManager.getTownName(ownerTownId)), true);
                        return false; // Cancel block break
                    }
                }
            }
            return true;
        });

        // Enforce block placing/interaction permissions
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient()) {
                ChunkPos chunkPos = world.getChunk(hitResult.getBlockPos()).getPos();
                String dimension = world.getRegistryKey().getValue().toString();
                String ownerTownId = DatabaseManager.getChunkOwnerTownId(dimension, chunkPos.x, chunkPos.z);

                if (ownerTownId != null) {
                    boolean isMember = DatabaseManager.isTownMember(ownerTownId, player.getUuidAsString());
                    if (!isMember) {
                        player.sendMessage(Text.literal("§cThis chunk is claimed by " + DatabaseManager.getTownName(ownerTownId)), true);
                        return ActionResult.FAIL; // Block interaction
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}
