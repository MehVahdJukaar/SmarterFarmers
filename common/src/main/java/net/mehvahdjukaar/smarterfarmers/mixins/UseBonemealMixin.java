package net.mehvahdjukaar.smarterfarmers.mixins;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
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

import java.util.Map;

@Mixin({UseBonemeal.class})
public abstract class UseBonemealMixin extends Behavior<Villager> {


    protected UseBonemealMixin(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
        super(pEntryCondition);
    }

    @ModifyReturnValue(method = "validPos", at = {@At("RETURN")})
    private boolean validPos(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
        if (original) {
            BlockState state = level.getBlockState(pos);
            if (state.is(SmarterFarmers.HARVEST_ON_TILLABLE_NO_REPLANT)) {
                return false;
            }

        }
        return original;
    }

}

