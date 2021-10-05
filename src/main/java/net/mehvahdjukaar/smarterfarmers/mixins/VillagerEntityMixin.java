package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin({VillagerEntity.class})
public abstract class VillagerEntityMixin extends AbstractVillagerEntity {


    @Shadow public static Map<Item, Integer> FOOD_POINTS;

    public VillagerEntityMixin(EntityType<? extends AbstractVillagerEntity> p_i50185_1_, World p_i50185_2_) {
        super(p_i50185_1_, p_i50185_2_);
    }

    @Inject(method = {"wantsToPickUp"}, at = {@At("HEAD")}, cancellable = true)
    private void wantsToPickUp(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Item i = stack.getItem();
        if(FOOD_POINTS.containsKey(i)){
            cir.setReturnValue(true);
            cir.cancel();
        }
        //prevent non farmers from stealing seeds
        else if(SmarterFarmers.isValidSeed(i)){
            boolean grab = this.getVillagerData().getProfession() == VillagerProfession.FARMER && this.getInventory().canAddItem(stack);
            cir.setReturnValue(grab);
            cir.cancel();
        }
    }

    @Shadow
    public abstract VillagerData getVillagerData();


    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        //prevents trampling
        if(this.getVillagerData().getProfession() == VillagerProfession.FARMER) return false;
        return super.canTrample(state, pos, fallDistance);
    }
}

