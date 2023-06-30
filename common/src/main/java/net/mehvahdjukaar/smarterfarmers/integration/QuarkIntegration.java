package net.mehvahdjukaar.smarterfarmers.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.quark.content.tweaks.module.SimpleHarvestModule;

public class QuarkIntegration {

    public static boolean breakWithAutoReplant(ServerLevel level, BlockPos pos, Entity entity) {
        if (!SimpleHarvestModule.staticEnabled && SimpleHarvestModule.villagersUseSimpleHarvest) {
            BlockState state = level.getBlockState(pos);
            SimpleHarvestModule.harvestAndReplant(level, pos, state, entity, ItemStack.EMPTY);
            return !state.equals(level.getBlockState(pos));
        }
        return false;
    }
}
