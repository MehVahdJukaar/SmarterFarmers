package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {


    @Shadow
    public static Map<Item, Integer> FOOD_POINTS;

    @Shadow public abstract VillagerData getVillagerData();

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = {"wantsToPickUp"}, at = {@At("HEAD")}, cancellable = true)
    private void wantsToPickUp(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Item i = stack.getItem();
        if (FOOD_POINTS.containsKey(i)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
        //prevent non farmers from stealing seeds
        else if (SmarterFarmers.isValidSeed(i)) {
            boolean grab = isFarmer() && this.getInventory().canAddItem(stack);
            cir.setReturnValue(grab);
            cir.cancel();
        }
    }

    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        //prevents trampling
        if (isFarmer()) return false;
        return super.canTrample(state, pos, fallDistance);
    }

    private boolean isFarmer() {
        return this.getVillagerData().getProfession() == VillagerProfession.FARMER;
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource pSource) {
        if (pSource == DamageSource.SWEET_BERRY_BUSH && isFarmer()) return true;
        return super.isInvulnerableTo(pSource);
    }

    @Override
    public void handleEntityEvent(byte pId) {
        super.handleEntityEvent(pId);
        if (pId == EntityEvent.FOX_EAT) { //using this one
            if (this.level.isClientSide) {
                //copied from haunted harvest
                SmarterFarmers.spawnEatingParticles(this);
            }
        }
    }


}

