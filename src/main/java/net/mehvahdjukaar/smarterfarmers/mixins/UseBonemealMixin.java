package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.UseBonemeal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin({UseBonemeal.class})
public abstract class UseBonemealMixin extends Behavior<Villager> {


    public UseBonemealMixin(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition);
    }

    @Inject(method = {"validPos"}, at = {@At("RETURN")}, cancellable = true)
    private void validPos(BlockPos pPos, ServerLevel pLevel, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            BlockState state = pLevel.getBlockState(pPos);
            if (state.is(SmarterFarmers.CROP_REPLACEABLE)) cir.setReturnValue(false);
        }
    }

}

