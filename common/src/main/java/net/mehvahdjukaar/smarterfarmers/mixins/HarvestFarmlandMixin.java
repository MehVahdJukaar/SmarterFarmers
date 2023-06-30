package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.CountOrderedSortedMap;
import net.mehvahdjukaar.smarterfarmers.FarmTaskLogic;
import net.mehvahdjukaar.smarterfarmers.SFPlatformStuff;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.mehvahdjukaar.smarterfarmers.integration.QuarkIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = {HarvestFarmland.class}, priority = 500)
public abstract class HarvestFarmlandMixin {


    @Shadow
    private BlockPos aboveFarmlandPos;
    @Shadow
    private long nextOkStartTime;
    @Final
    @Shadow
    private List<BlockPos> validFarmlandAroundVillager;
    @Shadow
    private int timeWorkedSoFar;

    @Shadow
    protected abstract BlockPos getValidFarmland(ServerLevel serverLevel);

    @Inject(method = {"start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V"}, at = {@At("HEAD")})
    public void start(ServerLevel level, Villager villager, long pGameTime, CallbackInfo ci) {
        if (pGameTime > this.nextOkStartTime && this.aboveFarmlandPos != null) {
            villager.setItemSlot(EquipmentSlot.MAINHAND, FarmTaskLogic.getHoe(villager));
        }
    }

    @Inject(method = {"stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V"}, at = {@At("HEAD")})
    public void stop(ServerLevel level, Villager villager, long pGameTime, CallbackInfo ci) {
        villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }


    /**
     * @author MehVahdJukaar
     * @reason Smarter Farmers Mod, overhauled farm task logic
     */
    @Overwrite
    protected boolean validPos(BlockPos pPos, ServerLevel level) {
        BlockState cropState = level.getBlockState(pPos);
        BlockState farmState = level.getBlockState(pPos.below());
        if(cropState.isAir() || FarmTaskLogic.isCropMature(cropState)){
            return FarmTaskLogic.isValidFarmland(farmState);
        }else if(FarmTaskLogic.canSpecialBreak(cropState)){
            return (FarmTaskLogic.isValidFarmland(farmState) || farmState.is(BlockTags.DIRT));
        }
        return false;
    }

    //TODO:  redo from scratch (?)

    /**
     * Basically an overwrite. Not using that cause of fabric api mixins
     *
     * @author MehVahdJukaar
     * @reason Smarter Farmers Mod, overhauled farm task logic
     */
    //@Overwrite
    @Inject(method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V",
            at = @At("HEAD"), cancellable = true)
    public void tick(ServerLevel level, Villager villager, long tickCount, CallbackInfo ci) {
        if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerToCenterThan(villager.position(), 1.0D)) {
            if (this.aboveFarmlandPos != null && tickCount > this.nextOkStartTime) {
                BlockState toHarvest = level.getBlockState(this.aboveFarmlandPos);
                BlockPos belowPos = this.aboveFarmlandPos.below();

                Item toReplace = Items.AIR;

                if(!toHarvest.isAir()) {
                    //break special crop
                    if (FarmTaskLogic.canSpecialBreak(toHarvest)) {
                        level.destroyBlock(this.aboveFarmlandPos, true, villager);
                        BlockState below = level.getBlockState(belowPos);
                        if (SFPlatformStuff.tillBlock(below, belowPos, level)) {
                            level.playSound(null, belowPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                        if (FarmTaskLogic.canBreakNoReplant(toHarvest)) {
                            this.timeWorkedSoFar++;
                            //dont replant pumpkins. exit early
                            ci.cancel();
                            return;
                        }
                        //break normal crop
                    } else if (FarmTaskLogic.isCropMature(toHarvest)) {
                        if(SmarterFarmers.QUARK && QuarkIntegration.breakWithAutoReplant(level, this.aboveFarmlandPos,villager)){
                            this.timeWorkedSoFar++;
                            //exit as auto replant did job for us
                            ci.cancel();
                            return;
                        }
                        toReplace = toHarvest.getBlock().asItem();
                        level.destroyBlock(this.aboveFarmlandPos, true, villager);
                    }
                    //if(CaveVines.hasGlowBerries(toHarvest)){
                    //    CaveVines.use(toHarvest, level, this.aboveFarmlandPos);
                    //}
                }

                BlockState farmlandBlock = level.getBlockState(belowPos);

                //check if toHarvestBlock is empty to replant
                if (level.getBlockState(this.aboveFarmlandPos).isAir() && canPlantOn(farmlandBlock)) {
                    SimpleContainer inventory = villager.getInventory();


                    ItemStack itemStack = ItemStack.EMPTY;
                    boolean canPlant = false;
                    int ind = -1;
                    if (toReplace != Items.AIR && toReplace instanceof BlockItem) {
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            if (itemStack.getItem() == toReplace) {
                                canPlant = true;
                                ind = i;
                                break;
                            }
                        }
                    }

                    //normal behavior
                    if (!canPlant) {

                        CountOrderedSortedMap<Block> map = new CountOrderedSortedMap<>();

                        map.add(level.getBlockState(aboveFarmlandPos.north()).getBlock());
                        map.add(level.getBlockState(aboveFarmlandPos.south()).getBlock());
                        map.add(level.getBlockState(aboveFarmlandPos.east()).getBlock());
                        map.add(level.getBlockState(aboveFarmlandPos.west()).getBlock());
                        List<Block> surroundingBlocks = new ArrayList<>();
                        map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e -> surroundingBlocks.add(e.getKey()));


                        Map<Block, Integer> availableSeeds = new HashMap<>();
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            Item it = itemStack.getItem();
                            var cc = SFPlatformStuff.getCropFromSeed(level, aboveFarmlandPos, it);
                            if (cc != null) {
                                availableSeeds.put(cc, i);
                            }
                        }

                        for (Block b : surroundingBlocks) {
                            if (availableSeeds.containsKey(b)) {
                                ind = availableSeeds.get(b);
                                canPlant = true;
                                itemStack = inventory.getItem(ind);
                                break;
                            }
                        }
                        if (!canPlant) {
                            Optional<Integer> opt = availableSeeds.values().stream().findFirst();
                            if (opt.isPresent()) {
                                ind = opt.get();
                                canPlant = true;
                                itemStack = inventory.getItem(ind);
                            }
                        }
                    }


                    if (canPlant) {

                        level.setBlock(aboveFarmlandPos, SFPlatformStuff.getPlant(level, aboveFarmlandPos, itemStack), 3);

                        level.playSound(null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            inventory.setItem(ind, ItemStack.EMPTY);
                        }
                    }

                }

                if (toHarvest.getBlock() instanceof CropBlock cropBlock && !cropBlock.isMaxAge(toHarvest)) {
                    this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
                    this.aboveFarmlandPos = this.getValidFarmland(level);
                    if (this.aboveFarmlandPos != null) {
                        this.nextOkStartTime = tickCount + 20L;
                        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
                        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
                    }
                }
            }

            ++this.timeWorkedSoFar;
        }
        ci.cancel();
    }


}

