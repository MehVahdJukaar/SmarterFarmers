package net.mehvahdjukaar.smarterfarmers.forge;

import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Author: MehVahdJukaar
 */
@Mod(SmarterFarmers.MOD_ID)
public class SmarterFarmersForge {

    public SmarterFarmersForge(IEventBus bus) {
        SmarterFarmers.commonInit();
    }

}

