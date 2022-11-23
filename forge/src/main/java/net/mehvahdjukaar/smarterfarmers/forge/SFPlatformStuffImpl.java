package net.mehvahdjukaar.smarterfarmers.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nullable;

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
        return false;
    }


}
