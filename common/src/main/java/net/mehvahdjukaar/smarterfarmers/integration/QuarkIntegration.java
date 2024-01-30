package net.mehvahdjukaar.smarterfarmers.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.violetmoon.quark.content.tweaks.module.SimpleHarvestModule;

public class QuarkIntegration {

    public static boolean breakWithAutoReplant(ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (!SimpleHarvestModule.staticEnabled && SimpleHarvestModule.villagersUseSimpleHarvest) {
            return SimpleHarvestModule.tryHarvestOrClickCrop(level, pos, entity, InteractionHand.MAIN_HAND, false);
        }
        return false;
    }
}
