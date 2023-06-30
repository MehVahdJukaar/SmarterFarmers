package net.mehvahdjukaar.smarterfarmers.fabric;

import net.fabricmc.api.ModInitializer;
import net.mehvahdjukaar.moonlight.fabric.MLFabricSetupCallbacks;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;

public class SmarterFarmersFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        SmarterFarmers.commonInit();

        MLFabricSetupCallbacks.COMMON_SETUP.add(SmarterFarmers::setup);

    }
}
