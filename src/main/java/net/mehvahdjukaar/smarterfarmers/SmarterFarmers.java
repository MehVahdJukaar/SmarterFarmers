package net.mehvahdjukaar.smarterfarmers;

import net.minecraft.block.Block;
import net.minecraft.block.StemBlock;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.*;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Mod(SmarterFarmers.MOD_ID)
public class SmarterFarmers {
    public static final String MOD_ID = "smarterfarmers";

    private static final Logger LOGGER = LogManager.getLogger();

    public SmarterFarmers() {

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        try {
            Map<Item, Integer> newMap = new HashMap<>(VillagerEntity.FOOD_POINTS);

            for (Item i : ForgeRegistries.ITEMS) {
                if (i.isEdible() && i.getRarity(new ItemStack(i)) == Rarity.COMMON
                        //ignore container items
                        && !(i instanceof SoupItem) && !(i instanceof HoneyBottleItem)) {
                    newMap.put(i, i.getFoodProperties().getNutrition());
                }
            }
            VillagerEntity.FOOD_POINTS = newMap;
        } catch (Exception e) {
            LOGGER.warn("Failed to add custom foods to villagers");
        }
    }

    public static boolean isValidSeed(Item item) {
        if (item instanceof BlockItem) {
            Block b = ((BlockItem) item).getBlock();
            return b instanceof IPlantable && !(b instanceof StemBlock);
        }
        return false;
    }
}
