package net.mehvahdjukaar.smarterfarmers;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

public class SFPlatformStuff {

    @Contract
    @ExpectPlatform
    public static Block getCropFromSeed(ServerLevel world, BlockPos pos, Item it) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isPlantable(BlockState state) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockState getPlant(ServerLevel world, BlockPos pos, ItemStack itemStack) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static boolean isValidSeed(Item i) {
        throw new AssertionError();
    }
}
