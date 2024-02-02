package net.mehvahdjukaar.smarterfarmers.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.IPlantable;
import net.neoforged.neoforge.common.PlantType;
import net.neoforged.neoforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class SFPlatformStuffImpl {

    @Nullable
    public static Block getCropFromSeed(ServerLevel level, BlockPos pos, Item item) {
        if (item instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            if (b instanceof IPlantable pl && !(b instanceof StemBlock)) {
                if (pl.getPlantType(level, pos) == PlantType.CROP) {
                    return b;
                }
            }
        }
        return null;
    }

    public static boolean isPlantable(BlockState state) {
        return state.getBlock() instanceof IPlantable;
    }

    public static BlockState getPlant(ServerLevel world, BlockPos pos, ItemStack itemStack) {
        return ((IPlantable) ((BlockItem) itemStack.getItem()).getBlock())
                .getPlant(world, pos);
    }


    public static boolean isValidSeed(Item i) {
        if (i instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            return (b instanceof IPlantable pl && !(b instanceof StemBlock));
        }
        return i.getDefaultInstance().is(ItemTags.VILLAGER_PLANTABLE_SEEDS);
    }

    public static boolean tillBlock(BlockState state, BlockPos belowPos, ServerLevel level) {
        UseOnContext c = new UseOnContext(level, null, InteractionHand.MAIN_HAND,
                Items.IRON_HOE.getDefaultInstance(),
                new BlockHitResult(belowPos.getCenter(), Direction.UP, belowPos, false));
        BlockState newState = state.getToolModifiedState(c, ToolActions.HOE_TILL, false);
        if(newState != null && newState != state){
            level.setBlock(belowPos, newState, 11);
            return true;
        }
        return false;
    }


}
