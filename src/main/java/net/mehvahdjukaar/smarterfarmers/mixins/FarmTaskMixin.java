package net.mehvahdjukaar.smarterfarmers.mixins;


import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.smarterfarmers.MySortedMap;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.block.*;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.ai.brain.task.FarmTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.stream.Collectors;

@Mixin({FarmTask.class})
public abstract class FarmTaskMixin {

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
    protected abstract BlockPos getValidFarmland(ServerWorld p_212833_1_);


    //TODO: find a better more general solution to max age problem (some mods dont extend cropblock..)
    private boolean canHarvest(BlockState state) {
        Block b = state.getBlock();
        return ((b instanceof CropsBlock && ((CropsBlock) b).isMaxAge(state)) ||
                b instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) == 2 ||
                b instanceof IPlantable && state.hasProperty(BlockStateProperties.AGE_7)
                        && state.getValue(BlockStateProperties.AGE_7) == 7);

    }


    /**
     * @author MehVahdJukaar
     */
    @Overwrite
    protected void tick(ServerWorld world, VillagerEntity villager, long p_212833_3_) {
        if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerThan(villager.position(), 1.0D)) {
            if (this.aboveFarmlandPos != null && p_212833_3_ > this.nextOkStartTime) {
                BlockState toHarvest = world.getBlockState(this.aboveFarmlandPos);
                Block block = toHarvest.getBlock();
                Block farmlandBlock = world.getBlockState(this.aboveFarmlandPos.below()).getBlock();

                Item toReplace = Items.AIR;

                //break crop
                if (this.canHarvest(toHarvest)) {
                    toReplace = block.asItem();
                    world.destroyBlock(this.aboveFarmlandPos, true, villager);
                }
                //if(CaveVines.hasGlowBerries(toHarvest)){
                //    CaveVines.use(toHarvest, world, this.aboveFarmlandPos);
                //}

                //check if block is empty to replant
                if (world.getBlockState(this.aboveFarmlandPos).isAir() && farmlandBlock instanceof FarmlandBlock) {
                    Inventory inventory = villager.getInventory();


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

                        MySortedMap<Block> map = new MySortedMap<>();

                        map.add(world.getBlockState(aboveFarmlandPos.north()).getBlock());
                        map.add(world.getBlockState(aboveFarmlandPos.south()).getBlock());
                        map.add(world.getBlockState(aboveFarmlandPos.east()).getBlock());
                        map.add(world.getBlockState(aboveFarmlandPos.west()).getBlock());
                        List<Block> surroundingBlocks = new ArrayList<>();
                        map.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e ->surroundingBlocks.add(e.getKey()));


                        Map<Block, Integer> availableSeeds = new HashMap<>();
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            Item it = itemStack.getItem();
                            if (SmarterFarmers.isValidSeed(it)) {
                                Block plantBlock = ((BlockItem) it).getBlock();
                                if (((IPlantable)plantBlock).getPlantType(world, aboveFarmlandPos) == PlantType.CROP) {
                                    availableSeeds.put(plantBlock, i);
                                }
                            }
                        }

                        for(Block b : surroundingBlocks){
                            if(availableSeeds.containsKey(b)){
                                ind = availableSeeds.get(b);
                                canPlant = true;
                                itemStack = inventory.getItem(ind);
                                break;
                            }
                        }
                        if(!canPlant) {
                            //gets random
                            Optional<Integer> opt = availableSeeds.values().stream().findAny();
                            if (opt.isPresent()) {
                                ind = opt.get();
                                canPlant = true;
                                itemStack = inventory.getItem(ind);
                            }
                        }
                    }



                    if (canPlant) {
                        world.setBlock(aboveFarmlandPos, ((IPlantable) ((BlockItem) itemStack.getItem()).getBlock()).getPlant(world, aboveFarmlandPos), 3);

                        world.playSound(null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            inventory.setItem(ind, ItemStack.EMPTY);
                        }
                    }

                }

                if (block instanceof CropsBlock && !((CropsBlock) block).isMaxAge(toHarvest)) {
                    this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
                    this.aboveFarmlandPos = this.getValidFarmland(world);
                    if (this.aboveFarmlandPos != null) {
                        this.nextOkStartTime = p_212833_3_ + 20L;
                        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosWrapper(this.aboveFarmlandPos), 0.5F, 1));
                        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(this.aboveFarmlandPos));
                    }
                }
            }

            ++this.timeWorkedSoFar;
        }
    }


}

