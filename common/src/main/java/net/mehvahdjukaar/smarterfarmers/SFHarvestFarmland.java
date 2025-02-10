package net.mehvahdjukaar.smarterfarmers;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.misc.FrequencyOrderedCollection;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.smarterfarmers.integration.QuarkIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

public class SFHarvestFarmland extends HarvestFarmland {

    public int plantTimer;

    // chosen target
    public BlockPos aboveFarmlandPos = null;
    public ItemStack seedToHold = null;

    // debug

    @VisibleForTesting
    public boolean active = false;
    @VisibleForTesting
    public List<Pair<BlockPos, Action>> farmlandAround;
    @VisibleForTesting
    public long lastTriedToStart;


    public SFHarvestFarmland() {
        super();
    }


    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager owner) {
        this.lastTriedToStart = level.getGameTime();
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return false;
        } else if (owner.getVillagerData().getProfession() != VillagerProfession.FARMER) {
            return false;
        }
        this.farmlandAround = getValidFarmlandAround(level, owner);

        if (farmlandAround.isEmpty()) return false;

        var chosen = farmlandAround.get(level.getRandom()
                .nextInt(farmlandAround.size()));


        this.aboveFarmlandPos = chosen.getFirst();
        this.seedToHold = null;

        // if it chose to plant we need to verify we have seeds to plant
        if (chosen.getSecond() == Action.PLANT) {
            ItemStack seed = getSeedToPlantAt(this.aboveFarmlandPos, level, owner);
            if (!seed.isEmpty()) {
                this.seedToHold = seed;
            } else {
                // if not we try getting a non plant action
                farmlandAround.removeIf(e -> e.getSecond() == Action.PLANT);
                if (farmlandAround.isEmpty()) {
                    return false;
                }
                chosen = farmlandAround.get(level.getRandom()
                        .nextInt(farmlandAround.size()));
                this.aboveFarmlandPos = chosen.getFirst();
            }
        }
        return true;
    }

    private List<Pair<BlockPos, Action>> getValidFarmlandAround(ServerLevel level, Villager owner) {
        List<Pair<BlockPos, Action>> validFarmland = new ArrayList<>();
        BlockPos.MutableBlockPos mutableBlockPos = owner.blockPosition().mutable();

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    mutableBlockPos.set(owner.getX() + i, owner.getY() + j, owner.getZ() + k);
                    Action actionForPos = this.getActionForPos(mutableBlockPos, level);
                    if (actionForPos != null) {
                        validFarmland.add(Pair.of(new BlockPos(mutableBlockPos), actionForPos));
                    }
                }
            }
        }
        return validFarmland;
    }


    // Find best seed to plant at position
    protected ItemStack getSeedToPlantAt(BlockPos targetPos, ServerLevel level, Villager villager) {
        // see what's around first
        FrequencyOrderedCollection<Item> blockAsItemAround = new FrequencyOrderedCollection<>();
        BlockPos.MutableBlockPos mutableBlockPos = targetPos.mutable();

        boolean hasAirInNeighbor = false;
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    if (x == 0 && z == 0) continue;
                    mutableBlockPos.set(targetPos.getX() + x, targetPos.getY() + y, targetPos.getZ() + z);
                    BlockState blockState = level.getBlockState(mutableBlockPos);
                    if (!hasAirInNeighbor && y == 0 && Math.abs(x) + Math.abs(z) == 1) {
                        hasAirInNeighbor = blockState.isAir();
                    }

                    Item item = blockState.getBlock().asItem();
                    if (item != Items.AIR) blockAsItemAround.add(item);
                }
            }
        }
        // see what farmer inventory has
        SimpleContainer inventory = villager.getInventory();
        Set<Item> availableSeeds = new HashSet<>();
        Map<Item, ItemStack> villagerSeedsInInventory = new HashMap<>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack itemStack = inventory.getItem(i);
            Item it = itemStack.getItem();
            if (itemStack.isEmpty()) continue;
            //check if its a crop
            if (SFPlatformStuff.isValidSeed(itemStack, villager)) {
                availableSeeds.add(it);
                villagerSeedsInInventory.put(it, itemStack);
            }
        }

        if (availableSeeds.isEmpty()) return ItemStack.EMPTY;

        boolean failedForMelon = false;
        // filter
        for (Item item : blockAsItemAround) {
            if (availableSeeds.contains(item)) {
                if (item.getDefaultInstance().is(SmarterFarmers.STEM_SEEDS) && (targetPos.getX() + targetPos.getZ()) % 2 == 0) {
                    failedForMelon = true;
                    continue;
                }
                return villagerSeedsInInventory.get(item);
            }
        }
        if (failedForMelon) return ItemStack.EMPTY;
        return villagerSeedsInInventory.get(availableSeeds.iterator().next());
    }

    @Override
    protected void start(ServerLevel level, Villager entity, long gameTime) {
        this.active = true;
        this.plantTimer = SmarterFarmers.TIME_TO_HARVEST.get();

        Preconditions.checkNotNull(this.aboveFarmlandPos);

        entity.setItemSlot(EquipmentSlot.MAINHAND, seedToHold != null ? seedToHold.copy() : FarmTaskLogic.getHoe(entity));

        entity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, (new BlockPosTracker(this.aboveFarmlandPos)));
        entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, (new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1)));

        if (PlatHelper.getPhysicalSide() == PlatHelper.Side.CLIENT) {
            FarmTaskDebugRenderer.INSTANCE.trackTask(entity, this);
        }
    }

    @Override
    protected void stop(ServerLevel level, Villager entity, long gameTime) {
        this.active = false;
        entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

        entity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.aboveFarmlandPos = null;
        this.farmlandAround.clear();
    }

    /**
     * If this position contains valid farmland blocks
     */
    @Nullable
    protected Action getActionForPos(BlockPos pos, ServerLevel level) {
        BlockState cropState = level.getBlockState(pos);
        BlockState farmState = level.getBlockState(pos.below());
        boolean validFarmland = FarmTaskLogic.isValidFarmland(farmState.getBlock());
        if (validFarmland) {
            if (FarmTaskLogic.isCropMature(cropState, pos, level)) {
                return Action.HARVEST_AND_REPLANT;
            }
            if (cropState.isAir()) {
                return Action.plantIfNoMelonsAround(pos, level);
            }
        }
        if (cropState.is(SmarterFarmers.HARVESTABLE_ON_DIRT_NO_REPLANT) &&
                farmState.is(SmarterFarmers.FARMER_TILLABLE)) {
            return Action.HARVEST;
        } else if (cropState.is(SmarterFarmers.HARVESTABLE_ON_DIRT) &&
                farmState.is(SmarterFarmers.FARMER_TILLABLE)) {
            return Action.HARVEST_AND_REPLANT;
        } else if (cropState.isAir() && SmarterFarmers.PLANT_ON_DIRT.get() &&
                farmState.is(SmarterFarmers.FARMER_TILLABLE)) {
            return Action.plantIfNoMelonsAround(pos, level);
        }
        return null;
    }


    @Override
    public void tick(ServerLevel level, Villager villager, long tickCount) {
        // if it's close to target pos
        // greater than 1 because this game navigation is shit and will stop before distance 1
        // move to sync target is so dumb and works in block pos. this means it will stop when entity blockpos is 1 manhattan block away from this pos.
        // max dist from center of a block is thus 1.5
        if (!this.aboveFarmlandPos.closerToCenterThan(villager.position(), 2)) return;

        this.plantTimer--;

        // we wait a bit on the block to "work" on it
        if (this.plantTimer > 0) return;
        //if (tickCount <= this.nextOkStartTime) return;
        // now crop at pos might have become invalid. we wait anyways for performance sake as checking every tick would be expensive


        BlockState targetState = level.getBlockState(this.aboveFarmlandPos);
        BlockPos belowPos = this.aboveFarmlandPos.below();

        Item toReplace = Items.AIR;

        // harvest
        if (!targetState.isAir()) {
            //break special crop
            if (targetState.is(SmarterFarmers.SPECIAL_HARVESTABLE) ||
                    targetState.is(SmarterFarmers.HARVESTABLE_ON_DIRT) ||
                    targetState.is(SmarterFarmers.HARVESTABLE_ON_DIRT_NO_REPLANT)) {
                level.destroyBlock(this.aboveFarmlandPos, true, villager);
                BlockState below = level.getBlockState(belowPos);
                if (SFPlatformStuff.tillBlock(below, belowPos, level)) {
                    level.playSound(null, belowPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                if (targetState.is(SmarterFarmers.HARVESTABLE_ON_DIRT_NO_REPLANT)) {
                    this.aboveFarmlandPos = null;
                    //dont replant pumpkins. exit early
                    return;
                }
                //break normal crop
            } else if (FarmTaskLogic.isCropMature(targetState, aboveFarmlandPos, level)) {
                if (SmarterFarmers.QUARK && QuarkIntegration.breakWithAutoReplant(level, this.aboveFarmlandPos, villager)) {
                    this.aboveFarmlandPos = null;
                    //exit as auto replant did job for us
                    return;
                }
                toReplace = targetState.getBlock().asItem();
                level.destroyBlock(this.aboveFarmlandPos, true, villager);
            }
            //if(CaveVines.hasGlowBerries(toHarvest)){
            //    CaveVines.use(toHarvest, level, this.aboveFarmlandPos);
            //}
        }

        // get new target state
        BlockState farmlandBlock = level.getBlockState(belowPos);
        targetState = level.getBlockState(this.aboveFarmlandPos);

        //check if toHarvestBlock is empty to replant
        if (targetState.isAir()) {
            if (FarmTaskLogic.isValidFarmland(farmlandBlock.getBlock())) {
                replant(level, villager, toReplace);
            } else if (farmlandBlock.is(SmarterFarmers.FARMER_TILLABLE)) {
                //till and replant
                if (SFPlatformStuff.tillBlock(farmlandBlock, belowPos, level)) {
                    level.playSound(null, belowPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    replant(level, villager, toReplace);
                }
            }
        }

        // if we reach here, wether we failed or not, we recalculate a new target
        // just ends the task
        this.aboveFarmlandPos = null;
    }

    private void replant(ServerLevel level, Villager villager, Item toReplace) {
        // first try to replant.
        ItemStack itemToPlant = ItemStack.EMPTY;
        if (toReplace != Items.AIR) {
            itemToPlant = findSameItem(villager.getInventory(), toReplace);
        }
        // if we cant replant, or we are planting a new, recompute seed to plant anyways
        // seedToHold is just visual. Most time it should match whats actually planted
        if (itemToPlant.isEmpty()) {
            itemToPlant = getSeedToPlantAt(this.aboveFarmlandPos, level, villager);
        }

        if (!itemToPlant.isEmpty()) {

            boolean success = false;
            if (SFPlatformStuff.trySpecialPlant(level, this.aboveFarmlandPos, itemToPlant, villager)) {
                success = true;
            } else if (itemToPlant.getItem() instanceof BlockItem blockItem) {
                BlockState toPlant = blockItem.getBlock().defaultBlockState();
                level.setBlock(this.aboveFarmlandPos, toPlant, 3);
                success = true;
            }

            if (success) {
                BlockState placed = level.getBlockState(this.aboveFarmlandPos);
                level.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villager, placed));

                level.playSound(null, this.aboveFarmlandPos.getX(), this.aboveFarmlandPos.getY(), this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                itemToPlant.shrink(1);
            } else {
                SmarterFarmers.LOGGER.error("Failed to replant {} from item {}", toReplace, itemToPlant);
            }
        } else {
            SmarterFarmers.LOGGER.error("Failed to replant {}", toReplace);
        }
    }

    @NotNull
    private ItemStack findSameItem(SimpleContainer inventory, Item toReplace) {
        if (toReplace != Items.AIR && toReplace instanceof BlockItem) {
            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                var is = inventory.getItem(i);
                if (is.getItem() == toReplace) {
                    return is;
                }
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    protected boolean canStillUse(ServerLevel level, Villager entity, long gameTime) {
        return aboveFarmlandPos != null;
    }

    public enum Action {
        HARVEST,
        HARVEST_AND_REPLANT,
        PLANT;

        public static @Nullable Action plantIfNoMelonsAround(BlockPos pos, ServerLevel level) {
            // check all 4 neighbors by iterating directions. IF one of them is a melon or pumpkin stem check ITS 3 neighbors. If all of those are NON air, return null
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighbor);
                if (neighborState.getBlock() instanceof StemBlock) {
                    boolean hasOtherSpace = false;
                    for (Direction dir2 : Direction.Plane.HORIZONTAL) {
                        if (dir2 == dir.getOpposite()) continue;
                        BlockPos neighbor2 = neighbor.relative(dir2);
                        if (level.getBlockState(neighbor2).isAir()) {
                            hasOtherSpace = true;
                            break;
                        }
                    }
                    if (!hasOtherSpace) return null;
                }
            }

            return PLANT;
        }

        public boolean harvests() {
            return this == HARVEST || this == HARVEST_AND_REPLANT;
        }
    }

}
