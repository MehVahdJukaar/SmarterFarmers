package net.mehvahdjukaar.smarterfarmers;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.villager_ai.VillagerBrainEvent;
import net.mehvahdjukaar.smarterfarmers.goal.EatFoodGoal;
import net.mehvahdjukaar.smarterfarmers.mixins.FarmTaskMixin;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

//TODO: make villagers eat food
@Mod(SmarterFarmers.MOD_ID)
public class SmarterFarmers {
    public static final String MOD_ID = "smarterfarmers";

    private static final Logger LOGGER = LogManager.getLogger();

    public static final TagKey<Block> CROP_REPLACEABLE = BlockTags.create(new ResourceLocation(MOD_ID, "crop_replaceable"));
    public static final TagKey<Block> CROP_PLANTABLE = BlockTags.create(new ResourceLocation(MOD_ID, "crop_plantable"));
    public static final TagKey<Item> MEAT = ItemTags.create(new ResourceLocation("forge", "food/meat"));

    public SmarterFarmers() {

        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (ver, remote) -> true));


        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);

    }


    private void setup(final FMLCommonSetupEvent event) {
        try {
            Map<Item, Integer> newMap = new HashMap<>(Villager.FOOD_POINTS);

            for (Item i : ForgeRegistries.ITEMS) {
                if (i.isEdible() && i.getRarity(new ItemStack(i)) == Rarity.COMMON && !
                        i.builtInRegistryHolder().is(MEAT)
                        //ignore container items
                        && !(i instanceof BowlFoodItem) && !(i instanceof HoneyBottleItem)) {
                    newMap.put(i, (int) Math.max(1, i.getFoodProperties().getNutrition() * 2 / 3f));
                }
            }
            Villager.FOOD_POINTS = newMap;
        } catch (Exception e) {
            LOGGER.warn("Failed to add custom foods to villagers");
        }
    }

    public static boolean isValidSeed(Item item) {
        if (item instanceof BlockItem blockItem) {
            Block b = blockItem.getBlock();
            return b instanceof IPlantable && !(b instanceof StemBlock);
        }
        return false;
    }


    @SubscribeEvent
    public void onVillagerBrainInitialize(VillagerBrainEvent event) {
        //babies do not eat
        if(!event.getVillager().isBaby())
        event.addTaskToActivity(Activity.MEET, Pair.of(7, new EatFoodGoal(100, 140)));
    }

    public static void spawnEatingParticles(AbstractVillager villager) {
        Vec3 pos = new Vec3(0, 0, 0.4);
        //pos = pos.xRot(pOwner.getXRot() * ((float) Math.PI / 180F));
        //particle accuracy is shit because yRot isnt synced properly. being a server side mod we cant do better
        pos = pos.yRot((-villager.yBodyRot) * ((float) Math.PI / 180F));
        pos = pos.add(villager.getX(), villager.getEyeY(), villager.getZ());
        ItemStack stack = villager.getMainHandItem();
        Level level = villager.getLevel();
        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
                pos.x + Mth.randomBetween(level.random, -0.05f,0.05f),
                pos.y - 0.4 + Mth.randomBetween(level.random, -0.05f,0.05f),
                pos.z + Mth.randomBetween(level.random, -0.05f,0.05f),
                0.03, 0.05, 0.03);
    }

}
