package net.mehvahdjukaar.smarterfarmers.mixins;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.smarterfarmers.FarmTaskLogic;
import net.minecraft.world.entity.ai.sensing.SecondaryPoiSensor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SecondaryPoiSensor.class)
public class SecondaryPoiSensorMixin {

    @WrapOperation(method = "doTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;)V",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableSet;contains(Ljava/lang/Object;)Z"))
    public boolean smarterfarmers$addMoreFarmland(ImmutableSet<Block> instance, Object o, Operation<Boolean> original,
                                                  @Local(argsOnly = true) Villager villager) {
        if (villager.getVillagerData().getProfession() == VillagerProfession.FARMER) {
            return original.call(instance, o) || FarmTaskLogic.isFarmland(((Block) o).defaultBlockState());
        } else {

            return original.call(instance, o);
        }
    }
}
