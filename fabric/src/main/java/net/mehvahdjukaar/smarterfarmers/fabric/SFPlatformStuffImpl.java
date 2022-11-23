package net.mehvahdjukaar.smarterfarmers.fabric;

import net.fabricmc.fabric.api.registry.VillagerPlantableRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

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
        var b = VillagerPlantableRegistry.getPlantState(itemStack.getItem());
        if (b != null) return b;
        return ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
    }

    public static boolean isValidSeed(Item i) {
        if (VillagerPlantableRegistry.contains(i)) return true;
        if (i instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            return (b instanceof CropBlock || b.defaultBlockState().is(BlockTags.CROPS));
        }
        return false;
    }

}
