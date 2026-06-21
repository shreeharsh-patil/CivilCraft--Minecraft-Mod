package com.civilcraftai.client.render;

import com.civilcraftai.entity.CivilianEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public class CivilianEntityRenderer extends BipedEntityRenderer<CivilianEntity, BipedEntityModel<CivilianEntity>> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/entity/steve.png");

    public CivilianEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(CivilianEntity entity) {
        return TEXTURE;
    }
}
