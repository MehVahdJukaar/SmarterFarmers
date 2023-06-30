package net.mehvahdjukaar.smarterfarmers.fabric;

import net.fabricmc.fabric.mixin.content.registry.HoeItemAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SFPlatformStuffImpl {
    //Crappier version

    public static boolean isPlantable(BlockState state) {
        return true;
    }

    @Nullable
    public static Block getCropFromSeed(ServerLevel level, BlockPos pos, Item item) {
        if (item instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            if (b instanceof CropBlock || b.defaultBlockState().is(BlockTags.CROPS)) {
                return b;
            }
        }
        return null;
    }

    public static BlockState getPlant(ServerLevel world, BlockPos pos, ItemStack itemStack) {
        return ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
    }

    public static boolean isValidSeed(Item i) {
        if (i instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            return (b instanceof CropBlock || b.defaultBlockState().is(BlockTags.CROPS));
        }
        return i.getDefaultInstance().is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    public static boolean tillBlock(BlockState state, BlockPos belowPos, ServerLevel level) {
        UseOnContext c = new UseOnContext(level, null, InteractionHand.MAIN_HAND,
                Items.IRON_HOE.getDefaultInstance(),
                new BlockHitResult(belowPos.getCenter(), Direction.UP, belowPos, false));
        var a = HoeItemAccessor.getTillingActions().get(state.getBlock());
        if (a != null && a.getFirst().test(c)) {
            a.getSecond().accept(c);
            return level.getBlockState(belowPos) != state;
        }
        return false;
    }

}
