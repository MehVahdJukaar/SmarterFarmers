package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.core.BlockPos;
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

@Mixin({Villager.class})
public abstract class VillagerEntityMixin extends AbstractVillager {


    @Shadow
    public static Map<Item, Integer> FOOD_POINTS;

    public VillagerEntityMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
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
            boolean grab = this.getVillagerData().getProfession() == VillagerProfession.FARMER && this.getInventory().canAddItem(stack);
            cir.setReturnValue(grab);
            cir.cancel();
        }
    }

    @Shadow
    public abstract VillagerData getVillagerData();


    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        //prevents trampling
        if (this.getVillagerData().getProfession() == VillagerProfession.FARMER) return false;
        return super.canTrample(state, pos, fallDistance);
    }
}

