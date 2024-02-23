package net.mehvahdjukaar.smarterfarmers.forge;

import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

/**
 * Author: MehVahdJukaar
 */
@Mod(SmarterFarmers.MOD_ID)
public class SmarterFarmersForge {

    public SmarterFarmersForge() {
        SmarterFarmers.commonInit();


        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (ver, remote) -> true));


        FMLJavaModLoadingContext.get().getModEventBus().addListener(SmarterFarmersForge::setup);


        MinecraftForge.EVENT_BUS.register(this);
    }


    public static void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(SmarterFarmers::setup);
    }


    @SubscribeEvent
    public void mobGriefing(EntityMobGriefingEvent event) {
        if(event.getEntity() instanceof Villager && SmarterFarmers.PICKUP_FOOD.get()){
            event.setResult(EntityMobGriefingEvent.Result.ALLOW);
        }
    }
}

