package com.civilcraftai.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathfinderMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.EntityData;
import net.minecraft.text.Text;
import com.civilcraftai.database.DatabaseManager;
import com.civilcraftai.agent.behavior.BehaviorNode;
import com.civilcraftai.agent.behavior.SelectorNode;
import com.google.gson.JsonObject;

public class CivilianEntity extends PathfinderMob {
    private String npcName = "Citizen";
    private String personality = "Hardworking and curious";
    private BehaviorNode behaviorTree;
    private int behaviorTickCooldown = 0;
    private boolean dbInitialized = false;

    public CivilianEntity(EntityType<? extends PathfinderMob> entityType, World world) {
        super(entityType, world);
        this.setupBehaviorTree();
    }

    public static DefaultAttributeContainer.Builder createCivilianAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    private void setupBehaviorTree() {
        this.behaviorTree = new SelectorNode(
                entity -> {
                    if (entity.getRandom().nextFloat() < 0.05f) {
                        double rx = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                        double ry = entity.getY();
                        double rz = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                        entity.getNavigation().startMovingTo(rx, ry, rz, 1.0D);
                    }
                    return BehaviorNode.Status.SUCCESS;
                }
        );
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData);
        
        String[] firstNames = {"John", "Alden", "Garrick", "Elisa", "Lyra", "Rowan"};
        String[] lastNames = {"Stone", "Ironwood", "Rivers", "Miller", "Baker", "Smith"};
        String[] traits = {"friendly", "suspicious", "optimistic", "greedy", "anxious", "stoic"};
        
        this.npcName = firstNames[this.random.nextInt(firstNames.length)] + " " + lastNames[this.random.nextInt(lastNames.length)];
        this.personality = "An NPC who is " + traits[this.random.nextInt(traits.length)] + " and focused on survival.";
        
        this.setCustomName(Text.literal(this.npcName));
        this.setCustomNameVisible(true);

        if (!this.getWorld().isClient()) {
            DatabaseManager.registerAgent(this.getUuidAsString(), this.npcName, this.personality);
        }
        this.dbInitialized = true;
        return data;
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (!this.getWorld().isClient()) {
            if (!dbInitialized) {
                DatabaseManager.registerAgent(this.getUuidAsString(), this.npcName, this.personality);
                dbInitialized = true;
            }

            if (behaviorTickCooldown++ >= 20) {
                behaviorTickCooldown = 0;
                if (this.behaviorTree != null) {
                    this.behaviorTree.tick(this);
                }
            }
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient()) {
            // Register target for chat
            com.civilcraftai.command.ChatCommand.ACTIVE_CONVERSATIONS.put(player.getUuid(), this.getUuid());
            player.sendMessage(Text.literal("§6[CivilCraft] Speaking with " + this.npcName + ". Use /c <message> to talk."), false);
            
            JsonObject info = DatabaseManager.getAgentInfo(this.getUuidAsString());
            if (info != null) {
                this.npcName = info.get("name").getAsString();
                this.personality = info.get("personality").getAsString();
            }
            player.sendMessage(Text.literal("§7" + this.npcName + " looks at you: \"" + this.personality + "\""), false);
        }
        return ActionResult.success(this.getWorld().isClient());
    }

    public String getNpcName() {
        return npcName;
    }

    public String getPersonality() {
        return personality;
    }
}
