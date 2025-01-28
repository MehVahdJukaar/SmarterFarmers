package net.mehvahdjukaar.smarterfarmers.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SFPlatformStuffImpl {

    public static boolean isValidSeed(ItemStack item, Villager villager) {
        return item.is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    public static boolean tillBlock(BlockState state, BlockPos belowPos, ServerLevel level) {
        UseOnContext c = new UseOnContext(level, null, InteractionHand.MAIN_HAND,
                Items.IRON_HOE.getDefaultInstance(),
                new BlockHitResult(belowPos.getCenter(), Direction.UP, belowPos, false));
        var ret = Items.IRON_HOE.useOn(c);

        return ret.consumesAction();
    }

    public static boolean trySpecialPlant(ServerLevel level, BlockPos aboveFarmlandPos, ItemStack itemToPlant, Villager villager) {
        return false;
    }

}
