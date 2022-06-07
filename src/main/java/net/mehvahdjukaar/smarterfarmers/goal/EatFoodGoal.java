package net.mehvahdjukaar.smarterfarmers.goal;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class EatFoodGoal extends Behavior<Villager> {

    private int eatingTime;

    private int cooldown = 0;
    private int buffer = 40;

    public EatFoodGoal(int minDur, int maxDur) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_HOSTILE, MemoryStatus.VALUE_ABSENT
        ), minDur, maxDur);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
        if (cooldown > 0){
            cooldown--;
            return false;
        }
        if (pOwner.tickCount - pOwner.getLastHurtByMobTimestamp() < 400) return false;
        if (pOwner.getBrain().isActive(Activity.PANIC)) return false;

        if (pOwner.getHealth() < pOwner.getMaxHealth() && pOwner.getInventory().hasAnyOf(Villager.FOOD_POINTS.keySet())) {
            buffer--;
            return buffer <= 0;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        super.start(pLevel, pEntity, pGameTime);
        this.buffer = 40;
        //stay still
        pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        //hax
        pEntity.getNavigation().stop();

        this.eatingTime = 80;
        SimpleContainer inventory = pEntity.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemstack = inventory.getItem(i);
            if (Villager.FOOD_POINTS.get(itemstack.getItem()) != null) {
                ItemStack s = itemstack.split(1);
                if (itemstack.getCount() == 0) inventory.setItem(i, ItemStack.EMPTY);
                pEntity.setItemInHand(InteractionHand.MAIN_HAND, s);
            }
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        if (pEntity.getBrain().isActive(Activity.PANIC)) return false;
        if (pEntity.tickCount - pEntity.getLastHurtByMobTimestamp() < 200) return false;
        return eatingTime > 0 && Villager.FOOD_POINTS.get(pEntity.getMainHandItem().getItem()) != null;
    }

    @Override
    protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
        if (this.eatingTime-- < 50) {
            ItemStack stack = pOwner.getMainHandItem();

            if (stack.isEmpty()) return;

            if (eatingTime % 2 == 0) {
                pLevel.broadcastEntityEvent(pOwner, EntityEvent.FOX_EAT);

            }
            if (eatingTime % 5 == 0) {
                pOwner.playSound(pOwner.getEatingSound(stack), 0.3F + 0.4F * (float) pLevel.random.nextInt(2),
                        (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.2F + 1.3F);
            }
        }
    }

    @Override
    protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        super.stop(pLevel, pEntity, pGameTime);
        ItemStack stack = pEntity.getMainHandItem();
        Item item = stack.getItem();
        if (item != Items.AIR && eatingTime <= 0) {
            Integer i = Villager.FOOD_POINTS.get(item);
            if (i != null) {
                pEntity.heal(i);

                item.finishUsingItem(stack, pLevel, pEntity);
                this.cooldown = 20 * (4 + pLevel.random.nextInt(14)) + pLevel.random.nextInt(20);
            }
        }
        pEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }
}
