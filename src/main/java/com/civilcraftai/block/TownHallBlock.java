package com.civilcraftai.block;

import com.civilcraftai.database.DatabaseManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class TownHallBlock extends Block {
    public TownHallBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient() && placer instanceof PlayerEntity player) {
            String townId = UUID.randomUUID().toString();
            String townName = player.getName().getString() + "'s Town";
            String founderUuid = player.getUuidAsString();

            // Save to DB
            DatabaseManager.createTown(townId, townName, founderUuid);

            // Claim placed chunk
            ChunkPos chunkPos = world.getChunk(pos).getPos();
            DatabaseManager.claimChunk(world.getRegistryKey().getValue().toString(), chunkPos.x, chunkPos.z, townId);

            player.sendMessage(Text.literal("§a[CivilCraft] Placed Town Hall! Established '" + townName + "' and claimed this chunk."), false);
        }
        super.onPlaced(world, pos, state, placer, itemStack);
    }
}
