package net.mehvahdjukaar.smarterfarmers.mixins;


import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SweetBerryBushBlock.class})
public abstract class SweetBerryBushMixin {

    @Inject(method = {"entityInside"}, at = {@At("HEAD")}, cancellable = true)
    private void entityInside(BlockState p_57270_, World p_57271_, BlockPos p_57272_, Entity entity, CallbackInfo ci) {
        if(entity instanceof VillagerEntity && ((VillagerEntity) entity).getVillagerData().getProfession() == VillagerProfession.FARMER){
            ci.cancel();
        }
    }

}

