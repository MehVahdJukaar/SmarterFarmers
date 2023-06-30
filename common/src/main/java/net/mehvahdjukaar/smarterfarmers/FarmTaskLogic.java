package net.mehvahdjukaar.smarterfarmers;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FarmTaskLogic {


    public static ItemStack getHoe(Villager pEntity) {
        return switch (pEntity.getVillagerData().getLevel()) {
            default -> Items.IRON_HOE.getDefaultInstance();
            case 1 -> Items.WOODEN_HOE.getDefaultInstance();
            case 2 -> Items.STONE_HOE.getDefaultInstance();
            case 4 -> Items.GOLDEN_HOE.getDefaultInstance();
            case 5 -> Items.DIAMOND_HOE.getDefaultInstance();
            case 6 -> Items.NETHERITE_HOE.getDefaultInstance();
        };
    }

    public static boolean isCropMature(BlockState state) {
        Block b = state.getBlock();
        if (state.isAir()) return false;
        return ((b instanceof CropBlock crop && crop.isMaxAge(state)) ||
                b instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) == 2 ||
                hardcodedCheckMaxAge(state, b)); //if previous didnt catch it (some mods dont extend crop block)
    }

    private static boolean hardcodedCheckMaxAge(BlockState state, Block b) {
        return SFPlatformStuff.isPlantable(state) && (
                checkAge(state, BlockStateProperties.AGE_1, 1) ||
                        checkAge(state, BlockStateProperties.AGE_2, 2) ||
                        checkAge(state, BlockStateProperties.AGE_3, 3) ||
                        checkAge(state, BlockStateProperties.AGE_4, 4) ||
                        checkAge(state, BlockStateProperties.AGE_5, 5) ||
                        checkAge(state, BlockStateProperties.AGE_7, 7)
        );
    }

    private static boolean checkAge(BlockState state, IntegerProperty property, int max) {
        return state.hasProperty(property) && state.getValue(property) == max;
    }

    public static boolean canSpecialBreak(BlockState state) {
        return state.is(SmarterFarmers.SPECIAL_HARVESTABLE) || canBreakNoReplant(state);
    }

    public static boolean canBreakNoReplant(BlockState state) {
        return state.is(SmarterFarmers.NO_REPLANT);
    }

    public static boolean isValidFarmland(BlockState state) {
        return state.getBlock() instanceof FarmBlock ||
                state.is(SmarterFarmers.VALID_FARMLAND);
    }

}
