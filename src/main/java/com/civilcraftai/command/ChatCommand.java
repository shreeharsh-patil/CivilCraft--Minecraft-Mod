package com.civilcraftai.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.civilcraftai.database.DatabaseManager;
import com.civilcraftai.agent.AgentScheduler;
import com.civilcraftai.agent.llm.LLMClient;
import com.civilcraftai.entity.CivilianEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatCommand {
    public static final Map<UUID, UUID> ACTIVE_CONVERSATIONS = new ConcurrentHashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("c")
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        String playerMessage = StringArgumentType.getString(context, "message");

                        UUID targetNpcUuid = ACTIVE_CONVERSATIONS.get(player.getUuid());
                        if (targetNpcUuid == null) {
                            player.sendMessage(Text.literal("§c[CivilCraft] You are not currently speaking to an NPC. Right-click a citizen first."), false);
                            return 0;
                        }

                        ServerWorld world = player.getServerWorld();
                        Entity entity = world.getEntity(targetNpcUuid);

                        if (!(entity instanceof CivilianEntity npc)) {
                            player.sendMessage(Text.literal("§c[CivilCraft] The NPC is no longer in range or has despawned."), false);
                            ACTIVE_CONVERSATIONS.remove(player.getUuid());
                            return 0;
                        }

                        String npcName = npc.getNpcName();
                        String personality = npc.getPersonality();
                        String uuidStr = targetNpcUuid.toString();

                        player.sendMessage(Text.literal("§dYou: " + playerMessage), false);

                        DatabaseManager.saveDialogueLog(uuidStr, player.getName().getString(), playerMessage);

                        AgentScheduler.supplyAsync(() -> {
                            String history = DatabaseManager.getRecentDialogueHistory(uuidStr, 6);
                            return LLMClient.generateResponse(npcName, personality, history, playerMessage);
                        }).thenAccept(response -> {
                            player.getServer().execute(() -> {
                                DatabaseManager.saveDialogueLog(uuidStr, npcName, response);
                                player.sendMessage(Text.literal("§a" + npcName + ": " + response), false);
                            });
                        });

                        return 1;
                    })
                )
            );
        });
    }
}
