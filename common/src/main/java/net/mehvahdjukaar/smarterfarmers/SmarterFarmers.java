package net.mehvahdjukaar.smarterfarmers;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.smarterfarmers.goal.EatFoodGoal;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MehVahdJukaar
 */
public class SmarterFarmers {

    public static final String MOD_ID = "smarterfarmers";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }


    public static final TagKey<Block> SPECIAL_HARVESTABLE = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "harvestable_plant"));
    public static final TagKey<Block> NO_REPLANT = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "harvestable_plant_no_replant"));
    public static final TagKey<Block> VALID_FARMLAND = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(MOD_ID, "farmer_plantable_on"));
    public static final TagKey<Item> MEAT = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "food/meat"));


    public static void commonInit() {

        MoonlightEventsHelper.addListener(SmarterFarmers::onVillagerBrainInitialize, IVillagerBrainEvent.class);

    }

    public static void setup() {
        try {
            Map<Item, Integer> newMap = new HashMap<>(Villager.FOOD_POINTS);

            for (Item i : Registry.ITEM) {
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


    public static void onVillagerBrainInitialize(IVillagerBrainEvent event) {
        //babies do not eat
        if (!event.getVillager().isBaby()) {
            event.addTaskToActivity(Activity.MEET, Pair.of(7, new EatFoodGoal(100, 140)));
        }
    }

    public static void spawnEatingParticles(AbstractVillager villager) {
        Vec3 pos = new Vec3(0, 0, 0.4);
        //pos = pos.xRot(pOwner.getXRot() * ((float) Math.PI / 180F));
        //particle accuracy is shit because yRot isn't synced properly. being a server side mod we can't do better
        pos = pos.yRot((-villager.yBodyRot) * ((float) Math.PI / 180F));
        pos = pos.add(villager.getX(), villager.getEyeY(), villager.getZ());
        ItemStack stack = villager.getMainHandItem();
        Level level = villager.getLevel();
        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
                pos.x + Mth.randomBetween(level.random, -0.05f, 0.05f),
                pos.y - 0.4 + Mth.randomBetween(level.random, -0.05f, 0.05f),
                pos.z + Mth.randomBetween(level.random, -0.05f, 0.05f),
                0.03, 0.05, 0.03);

    }
}