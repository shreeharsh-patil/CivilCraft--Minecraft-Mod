package com.civilcraftai.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import com.civilcraftai.database.DatabaseManager;
import com.civilcraftai.manager.TechnologyManager;
import com.civilcraftai.CivilCraftAI;
import net.minecraft.util.math.ChunkPos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TownCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("town")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    showTownMenu(player);
                    return 1;
                })
                .then(CommandManager.literal("claim")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        claimCurrentChunk(player);
                        return 1;
                    })
                )
                .then(CommandManager.literal("deposit")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        depositToTown(player);
                        return 1;
                    })
                )
                .then(CommandManager.literal("borders")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        showClaimBorders(player);
                        return 1;
                    })
                )
                .then(CommandManager.literal("research")
                    .then(CommandManager.argument("tech", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String techId = StringArgumentType.getString(context, "tech");
                            researchTechnology(player, techId);
                            return 1;
                        })
                    )
                )
            );

            // Register short alias '/t'
            dispatcher.register(CommandManager.literal("t")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    showTownMenu(player);
                    return 1;
                })
                .then(CommandManager.literal("claim")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        claimCurrentChunk(player);
                        return 1;
                    })
                )
                .then(CommandManager.literal("deposit")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        depositToTown(player);
                        return 1;
                    })
                )
                .then(CommandManager.literal("borders")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        showClaimBorders(player);
                        return 1;
                    })
                )
                .then(CommandManager.literal("research")
                    .then(CommandManager.argument("tech", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            String techId = StringArgumentType.getString(context, "tech");
                            researchTechnology(player, techId);
                            return 1;
                        })
                    )
                )
            );
        });
    }

    private static void showTownMenu(ServerPlayerEntity player) {
        String playerUuid = player.getUuidAsString();
        String townId = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM towns WHERE founder_uuid = ?")) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    townId = rs.getString("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (townId == null) {
            player.sendMessage(Text.literal("§c[CivilCraft] You do not own a town yet. Place a Town Hall block to start!"), false);
            return;
        }

        String townName = DatabaseManager.getTownName(townId);
        int balance = DatabaseManager.getTownBalance(townId);

        boolean unlockedAg = DatabaseManager.isTechUnlocked(townId, "agriculture");
        boolean unlockedMet = DatabaseManager.isTechUnlocked(townId, "metallurgy");
        boolean unlockedSteam = DatabaseManager.isTechUnlocked(townId, "steam");

        player.sendMessage(Text.literal("§6========================================"), false);
        player.sendMessage(Text.literal("§e             TOWN MANAGEMENT - " + townName.toUpperCase()), false);
        player.sendMessage(Text.literal("§6========================================"), false);
        player.sendMessage(Text.literal("§7 Treasury Balance: §f" + balance + " §7coins"), false);
        player.sendMessage(Text.literal("§7 Tech Unlocks: " +
                (unlockedAg ? "§a[Agriculture] " : "§8[Agriculture] ") +
                (unlockedMet ? "§a[Metallurgy] " : "§8[Metallurgy] ") +
                (unlockedSteam ? "§a[Steam]" : "§8[Steam]")), false);
        player.sendMessage(Text.literal(""), false);

        MutableText claimBtn = Text.literal("§a§l[CLAIM CHUNK] ").styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t claim"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Claim current chunk for 16 coins"))));

        MutableText depositBtn = Text.literal("§b§l[DEPOSIT 10 COINS] ").styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t deposit"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Deposit 10 coins into treasury"))));

        MutableText borderBtn = Text.literal("§d§l[SHOW BORDERS] ").styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t borders"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("View particle borders of current chunk"))));

        player.sendMessage(claimBtn.append(depositBtn).append(borderBtn), false);
        player.sendMessage(Text.literal("§6========================================"), false);
    }

    private static void claimCurrentChunk(ServerPlayerEntity player) {
        String playerUuid = player.getUuidAsString();
        String townId = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM towns WHERE founder_uuid = ?")) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    townId = rs.getString("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (townId == null) {
            player.sendMessage(Text.literal("§cYou must own a town to claim chunks."), false);
            return;
        }

        ChunkPos pos = player.getChunkPos();
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        String owner = DatabaseManager.getChunkOwnerTownId(dimension, pos.x, pos.z);

        if (owner != null) {
            if (owner.equals(townId)) {
                player.sendMessage(Text.literal("§cThis chunk is already claimed by your town."), false);
            } else {
                player.sendMessage(Text.literal("§cThis chunk is owned by another town."), false);
            }
            return;
        }

        int cost = 16;
        boolean success = DatabaseManager.withdrawCoins(townId, cost);
        if (success) {
            DatabaseManager.claimChunk(dimension, pos.x, pos.z, townId);
            player.sendMessage(Text.literal("§aSuccessfully claimed chunk (" + pos.x + ", " + pos.z + ") for 16 coins!"), false);
        } else {
            player.sendMessage(Text.literal("§cInsufficient treasury funds! Cost is 16 coins, balance is " + DatabaseManager.getTownBalance(townId) + "."), false);
        }
    }

    private static void depositToTown(ServerPlayerEntity player) {
        String playerUuid = player.getUuidAsString();
        String townId = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM towns WHERE founder_uuid = ?")) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    townId = rs.getString("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (townId == null) {
            player.sendMessage(Text.literal("§cYou must own a town to deposit coins."), false);
            return;
        }

        boolean hasCoin = false;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == CivilCraftAI.COIN && stack.getCount() >= 10) {
                stack.decrement(10);
                hasCoin = true;
                break;
            }
        }

        if (hasCoin) {
            DatabaseManager.depositCoins(townId, 10);
            player.sendMessage(Text.literal("§aDeposited 10 coins from inventory to town treasury! New balance: " + DatabaseManager.getTownBalance(townId)), false);
        } else {
            DatabaseManager.depositCoins(townId, 20);
            player.sendMessage(Text.literal("§e[Sandbox Mode] Deposited 20 mock coins to town treasury! New balance: " + DatabaseManager.getTownBalance(townId)), false);
        }
    }

    private static void showClaimBorders(ServerPlayerEntity player) {
        ChunkPos pos = player.getChunkPos();
        int minX = pos.getStartX();
        int maxX = pos.getEndX();
        int minZ = pos.getStartZ();
        int maxZ = pos.getEndZ();
        double y = player.getY() + 0.1D;

        net.minecraft.server.world.ServerWorld world = player.getServerWorld();
        for (int x = minX; x <= maxX; x++) {
            world.spawnParticles(player, net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, true, x, y, minZ, 1, 0, 0, 0, 0);
            world.spawnParticles(player, net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, true, x, y, maxZ, 1, 0, 0, 0, 0);
        }
        for (int z = minZ; z <= maxZ; z++) {
            world.spawnParticles(player, net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, true, minX, y, z, 1, 0, 0, 0, 0);
            world.spawnParticles(player, net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, true, maxX, y, z, 1, 0, 0, 0, 0);
        }

        player.sendMessage(Text.literal("§aShowing borders of chunk (" + pos.x + ", " + pos.z + ") with happy villager particles!"), false);
    }

    private static void researchTechnology(ServerPlayerEntity player, String techId) {
        String playerUuid = player.getUuidAsString();
        String townId = null;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM towns WHERE founder_uuid = ?")) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    townId = rs.getString("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (townId == null) {
            player.sendMessage(Text.literal("§cYou must own a town to research tech."), false);
            return;
        }

        TechnologyManager.Tech tech = TechnologyManager.Tech.fromId(techId);
        if (tech == null) {
            player.sendMessage(Text.literal("§cInvalid technology. Available: agriculture, metallurgy, steam"), false);
            return;
        }

        boolean success = TechnologyManager.purchaseTech(townId, tech);
        if (success) {
            player.sendMessage(Text.literal("§aSuccessfully unlocked technology: " + tech.name() + "!"), false);
        } else {
            player.sendMessage(Text.literal("§cFailed to unlock technology. Check if you have enough balance (" + tech.getCost() + " coins) or if it's already unlocked."), false);
        }
    }
}
