package com.srcpolygot.mobswaponhit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MobSwapOnHitMod implements ModInitializer {
    private static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();

    @Override
    public void onInitialize() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(world instanceof ServerWorld serverWorld)
                    || !(player instanceof ServerPlayerEntity serverPlayer)
                    || player.isSpectator()
                    || !(entity instanceof MobEntity mob)) {
                return ActionResult.PASS;
            }

            if (mob.getCommandTags().contains(MobSwapConfig.MINI_TAG)) {
                return ActionResult.PASS;
            }

            long currentTick = serverWorld.getTime();
            Long lastTick = COOLDOWN_MAP.get(serverPlayer.getUuid());
            if (lastTick != null && currentTick - lastTick < MobSwapConfig.COOLDOWN_TICKS) {
                return ActionResult.PASS;
            }
            COOLDOWN_MAP.put(serverPlayer.getUuid(), currentTick);

            float roll = serverWorld.getRandom().nextFloat();
            if (roll < MobSwapConfig.SWAP_CHANCE) {
                double playerX = serverPlayer.getX();
                double playerY = serverPlayer.getY();
                double playerZ = serverPlayer.getZ();
                float playerYaw = serverPlayer.getYaw();
                float playerPitch = serverPlayer.getPitch();

                double mobX = mob.getX();
                double mobY = mob.getY();
                double mobZ = mob.getZ();
                float mobYaw = mob.getYaw();
                float mobPitch = mob.getPitch();

                serverPlayer.teleport(serverWorld, mobX, mobY, mobZ, mobYaw, mobPitch);
                mob.refreshPositionAndAngles(playerX, playerY, playerZ, playerYaw, playerPitch);

                serverWorld.spawnParticles(ParticleTypes.PORTAL, playerX, playerY + 1.0, playerZ, 20, 0.4, 0.6, 0.4, 0.1);
                serverWorld.spawnParticles(ParticleTypes.PORTAL, mobX, mobY + 1.0, mobZ, 20, 0.4, 0.6, 0.4, 0.1);
                serverWorld.playSound(null, mobX, mobY, mobZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                serverWorld.playSound(null, playerX, playerY, playerZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                serverPlayer.sendMessage(Text.literal("Swapped places with " + mob.getName().getString() + "!"), true);
            } else if (roll < MobSwapConfig.SWAP_CHANCE + MobSwapConfig.MINI_SPAWN_CHANCE) {
                int nearbyMinis = serverWorld.getEntitiesByClass(
                        MobEntity.class,
                        Box.of(mob.getPos(), 32.0, 16.0, 32.0),
                        e -> e.getCommandTags().contains(MobSwapConfig.MINI_TAG)
                ).size();

                if (nearbyMinis <= MobSwapConfig.MAX_NEARBY_MINIS) {
                    Entity created = mob.getType().create(serverWorld);
                    if (created instanceof MobEntity mini) {
                        double offsetX = (serverWorld.getRandom().nextDouble() - 0.5) * 1.5;
                        double offsetZ = (serverWorld.getRandom().nextDouble() - 0.5) * 1.5;
                        mini.refreshPositionAndAngles(mob.getX() + offsetX, mob.getY(), mob.getZ() + offsetZ, mob.getYaw(), mob.getPitch());

                        EntityAttributeInstance scaleAttr = mini.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
                        if (scaleAttr != null) {
                            scaleAttr.setBaseValue(MobSwapConfig.MINI_SCALE);
                        }

                        EntityAttributeInstance attackAttr = mini.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                        if (attackAttr != null) {
                            attackAttr.setBaseValue(0.0);
                        }

                        EntityAttributeInstance followAttr = mini.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
                        if (followAttr != null) {
                            followAttr.setBaseValue(0.0);
                        }

                        EntityAttributeInstance healthAttr = mini.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                        if (healthAttr != null) {
                            healthAttr.setBaseValue(4.0);
                        }

                        mini.setHealth(4.0f);
                        mini.setTarget(null);
                        mini.addCommandTag(MobSwapConfig.MINI_TAG);
                        mini.setCustomName(Text.literal("Mini " + mob.getName().getString()));
                        mini.setCustomNameVisible(true);

                        serverWorld.spawnEntity(mini);
                        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, mini.getX(), mini.getY() + 0.5, mini.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
                        serverWorld.playSound(null, mini.getX(), mini.getY(), mini.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 1.0f, 1.2f);
                        serverPlayer.sendMessage(Text.literal("Spawned a Mini " + mob.getName().getString() + "!"), true);
                    }
                }
            }

            return ActionResult.PASS;
        });
    }
}
