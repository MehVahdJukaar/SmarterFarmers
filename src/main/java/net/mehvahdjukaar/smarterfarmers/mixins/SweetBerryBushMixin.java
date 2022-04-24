package net.mehvahdjukaar.smarterfarmers.mixins;


import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({SweetBerryBushBlock.class})
public abstract class SweetBerryBushMixin {

    @Inject(method = {"entityInside"}, at = {@At("HEAD")}, cancellable = true)
    private void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof Villager villager && villager.getVillagerData().getProfession() == VillagerProfession.FARMER) {
            ci.cancel();
        }
    }

}

