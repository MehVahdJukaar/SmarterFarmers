package net.mehvahdjukaar.smarterfarmers.mixins;


import net.mehvahdjukaar.smarterfarmers.SmarterFarmers;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin({HarvestFarmland.class})
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
    protected abstract BlockPos getValidFarmland(ServerLevel p_212833_1_);


    //TODO: find a better more general solution to max age problem (some mods dont extend cropblock..)
    private boolean canHarvest(BlockState state) {
        Block b = state.getBlock();
        return ((b instanceof CropBlock && ((CropBlock) b).isMaxAge(state)) ||
                b instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) == 2 ||
                b instanceof IPlantable && state.hasProperty(BlockStateProperties.AGE_7)
                        && state.getValue(BlockStateProperties.AGE_7) == 7);

    }


    /**
     * @author MehVahdJukaar
     */
    @Overwrite
    protected void tick(ServerLevel world, Villager villager, long p_212833_3_) {
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
                if(CaveVines.hasGlowBerries(toHarvest)){
                    CaveVines.use(toHarvest, world, this.aboveFarmlandPos);
                }

                //check if block is empty to replant
                if (world.getBlockState(this.aboveFarmlandPos).isAir() && farmlandBlock instanceof FarmBlock) {
                    SimpleContainer inventory = villager.getInventory();


                    ItemStack itemStack = ItemStack.EMPTY;
                    boolean canPlant = false;
                    int ind = 0;
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
                        for (int i = 0; i < inventory.getContainerSize(); ++i) {
                            itemStack = inventory.getItem(i);
                            Item it = itemStack.getItem();
                            if (SmarterFarmers.isValidSeed(it)) {
                                if (((IPlantable) ((BlockItem) it).getBlock()).getPlantType(world, aboveFarmlandPos) == PlantType.CROP) {
                                    canPlant = true;
                                    ind = i;
                                    break;
                                }
                            }
                        }
                    }

                    if (canPlant) {
                        world.setBlock(aboveFarmlandPos, ((IPlantable) ((BlockItem) itemStack.getItem()).getBlock()).getPlant(world, aboveFarmlandPos), 3);

                        world.playSound(null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            inventory.setItem(ind, ItemStack.EMPTY);
                        }
                    }

                }

                if (block instanceof CropBlock && !((CropBlock) block).isMaxAge(toHarvest)) {
                    this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
                    this.aboveFarmlandPos = this.getValidFarmland(world);
                    if (this.aboveFarmlandPos != null) {
                        this.nextOkStartTime = p_212833_3_ + 20L;
                        villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
                        villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
                    }
                }
            }

            ++this.timeWorkedSoFar;
        }
    }


}

