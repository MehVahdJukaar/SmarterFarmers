package net.mehvahdjukaar.smarterfarmers;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Author: MehVahdJukaar
 */
public class SmarterFarmers {

    public static final String MOD_ID = "smarterfarmers";
    public static final Logger LOGGER = LogManager.getLogger();

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public static final boolean QUARK = PlatHelper.isModLoaded("quark");

    public static final TagKey<Block> SPECIAL_HARVESTABLE = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "harvestable_plant"));
    public static final TagKey<Block> NO_REPLANT = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "harvestable_plant_no_replant"));
    public static final TagKey<Block> VALID_FARMLAND = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "farmer_plantable_on"));
    public static final TagKey<Item> MEAT = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "food/meat"));

    public static final Supplier<Boolean> PICKUP_FOOD;
    public static final Supplier<Boolean> EAT_FOOD;

    static{
        ConfigBuilder builder = ConfigBuilder.create(MOD_ID, ConfigType.COMMON);

        builder.push("general");
        PICKUP_FOOD = builder.comment("If true, villagers will pick up food items from the regardless of mob griefing gamerule. Needed since with mob griefing on they wont be able to breed.")
                .define("pickup_food_override", true);
        EAT_FOOD = builder.comment("If true, villagers will eat food items they pick up. Eating food will heal them")
                .define("eat_food", true);

        builder.pop();

        builder.buildAndRegister();
    }

    public static void commonInit() {
        MoonlightEventsHelper.addListener(SmarterFarmers::onVillagerBrainInitialize, IVillagerBrainEvent.class);
        PlatHelper.addCommonSetup(SmarterFarmers::setup);
    }

    public static void setup() {
        //TODO: use quark recipe crawl to convert crop->seed or crop->food
        try {
            Map<Item, Integer> newMap = new HashMap<>(Villager.FOOD_POINTS);

            for (Item i : BuiltInRegistries.ITEM) {
                if (i.isEdible() && i.getRarity(new ItemStack(i)) == Rarity.COMMON && !
                        i.builtInRegistryHolder().is(MEAT)
                        //ignore container items
                        && !(i instanceof BowlFoodItem) && !(i instanceof HoneyBottleItem)) {
                    FoodProperties foodProperties = i.getFoodProperties();
                    if (foodProperties != null) {
                        newMap.put(i, (int) Math.max(1, foodProperties.getNutrition() * 2 / 3f));
                    }
                }
            }
            Villager.FOOD_POINTS = newMap;
        } catch (Exception e) {
            LOGGER.warn("Failed to add custom foods to villagers");
        }
    }


    public static void onVillagerBrainInitialize(IVillagerBrainEvent event) {
        //babies do not eat
        // this also mean they will need a reload after they grown up...
        if (!event.getVillager().isBaby() && EAT_FOOD.get()) {
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
        Level level = villager.level();
        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
                pos.x + Mth.randomBetween(level.random, -0.05f, 0.05f),
                pos.y - 0.4 + Mth.randomBetween(level.random, -0.05f, 0.05f),
                pos.z + Mth.randomBetween(level.random, -0.05f, 0.05f),
                0.03, 0.05, 0.03);

    }
}
