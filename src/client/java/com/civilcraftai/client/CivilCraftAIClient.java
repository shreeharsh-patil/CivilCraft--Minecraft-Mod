package com.civilcraftai.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.entity.EntityRendererFactories;
import net.minecraft.text.Text;
import com.civilcraftai.CivilCraftAI;
import com.civilcraftai.client.render.CivilianEntityRenderer;
import com.civilcraftai.network.ClaimBorderSyncPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CivilCraftAIClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("CivilCraft AI Client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing CivilCraft AI Client...");

        // Register entity renderer
        EntityRendererFactories.register(CivilCraftAI.CIVILIAN, CivilianEntityRenderer::new);

        // Register client packet receiver to show overlay/actionbar claiming boundary info
        ClientPlayNetworking.registerGlobalReceiver(ClaimBorderSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().inGameHud != null) {
                    context.client().inGameHud.setOverlayMessage(
                            Text.literal("§6Entered Chunk: " + payload.townName()),
                            false
                    );
                }
            });
        });
    }
}
