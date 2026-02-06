package io.github.chakyl.societytrading.blockentity;

import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.block.AutoTraderBlock;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.AutoTraderMenu;
import io.github.chakyl.societytrading.screen.AutoTraderSelectorMenu;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.util.ShopData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class AutoTraderBlockEntity extends BlockEntity implements TickingBlockEntity, MenuProvider {
    protected final ContainerData data;
    private int selectedShopIndex = -1;
    private int selectedTradeIndex = -1;
    private int progress = 0;
    private int TRADING_TIME = 200;
    private ShopOffer selectedOffer;
    /**
     * TODO:
     * - Numismatics card support
     * - Craft using materials
     *   - When the craft is done, check if the shop ID of the indexed equals the shop id of the result
     * - Cache IDs if they don't exist
     */
    private final ItemStackHandler inputInventoryA = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    private final ItemStackHandler inputInventoryB = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    private final ItemStackHandler cardInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };
    private final ItemStackHandler outputInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    private final LazyOptional<ItemStackHandler> inputInventoryAOptional = LazyOptional.of(() -> this.inputInventoryA);
    private final LazyOptional<ItemStackHandler> inputInventoryBOptional = LazyOptional.of(() -> this.inputInventoryB);
    private final LazyOptional<ItemStackHandler> cardInventoryOptional = LazyOptional.of(() -> this.cardInventory);
    private final LazyOptional<ItemStackHandler> outputInventoryOptional = LazyOptional.of(() -> this.outputInventory);

    public AutoTraderBlockEntity(BlockPos pos, BlockState state) {
        super(ModElements.BlockEntities.AUTO_TRADER.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> AutoTraderBlockEntity.this.progress;
                    case 1 -> AutoTraderBlockEntity.this.TRADING_TIME;
                    case 2 -> AutoTraderBlockEntity.this.selectedShopIndex;
                    case 3 -> AutoTraderBlockEntity.this.selectedTradeIndex;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> AutoTraderBlockEntity.this.progress = pValue;
                    case 1 -> AutoTraderBlockEntity.this.TRADING_TIME = pValue;
                    case 2 -> AutoTraderBlockEntity.this.selectedShopIndex = pValue;
                    case 3 -> AutoTraderBlockEntity.this.selectedTradeIndex = pValue;
                }

            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % 10 == 0 && this.selectedShopIndex != -1 && this.selectedTradeIndex != -1) {
            if (selectedOffer != null && isTradable()) {
                if (this.progress == 0) {
                    BlockState newState = state.setValue(AutoTraderBlock.WORKING, true);
                    level.setBlockAndUpdate(pos, newState);
                }
                this.progress = this.progress + 10;
                setChanged(level, pos, state);
                if (this.progress >= TRADING_TIME) {
                    craftItem();
                    BlockState newState = state.setValue(AutoTraderBlock.WORKING, false);
                    level.setBlockAndUpdate(pos, newState);
                    this.progress = 0;
                }
            } else {
                if (this.selectedOffer == null) this.selectedOffer = getSelectedShopOfferByIndex();
                this.progress = 0;
            }
        }
    }

    private ShopOffer getSelectedShopOfferByIndex() {
        return getSelectedShopByIndex().trades().get(this.selectedTradeIndex);
    }

    private Shop getSelectedShopByIndex() {
        return ShopData.getAutoTraderShops(ShopRegistry.INSTANCE.getValues()).get(this.selectedShopIndex);
    }

    public void setSelectedTrade(int tradeNum) {
        this.selectedTradeIndex = tradeNum;
    }
    private void craftItem() {
        ItemStack outputSlot = this.outputInventory.getStackInSlot(0);
        ItemStack result = this.selectedOffer.getResult();
        int resultCount = result.getCount();
        if (outputSlot.getCount() > 0 && canInsertItemIntoOutputSlot(outputSlot, result) && canInsertAmountIntoOutputSlot(outputSlot, resultCount)) {
            outputSlot.setCount(outputSlot.getCount() + resultCount);
        } else {
            this.outputInventory.setStackInSlot(0, this.selectedOffer.getResult().copy());
        }
        this.inputInventoryA.getStackInSlot(0).shrink(this.selectedOffer.getCostA().getCount());
        if (!this.selectedOffer.getCostB().isEmpty()) this.inputInventoryB.getStackInSlot(0).shrink(this.selectedOffer.getCostB().getCount());
    }

    private boolean isTradable() {
        if (this.selectedOffer == null) return false;
        ItemStack inputA = selectedOffer.getCostA();
        ItemStack inputB = selectedOffer.getCostB();
        ItemStack result = selectedOffer.getResult();
        ItemStack inputASlot = this.inputInventoryA.getStackInSlot(0);
        ItemStack inputBSlot = this.inputInventoryB.getStackInSlot(0);
        ItemStack outputSlot = this.outputInventory.getStackInSlot(0);
        if (!(slotMatches(inputA, inputASlot) && inputASlot.getCount() >= inputA.getCount() && slotMatches(inputB, inputBSlot) && inputBSlot.getCount() >= inputB.getCount())) return false;
        return outputSlot.isEmpty() || (slotMatches(outputSlot, result)) && (canInsertAmountIntoOutputSlot(outputSlot, result.getCount()) && canInsertItemIntoOutputSlot(outputSlot, result));
    }
    private boolean canInsertItemIntoOutputSlot(ItemStack output, ItemStack result) {
        return output.isEmpty() || (output.is(result.getItem()) && output.areShareTagsEqual(result));
    }

    private boolean canInsertAmountIntoOutputSlot(ItemStack output, int addedCount) {
        return output.getCount() + addedCount <= output.getMaxStackSize();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN) return this.outputInventoryOptional.cast();
            if (side == Direction.UP) return this.inputInventoryAOptional.cast();
            else return this.inputInventoryBOptional.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.inputInventoryAOptional.invalidate();
        this.inputInventoryBOptional.invalidate();
        this.cardInventoryOptional.invalidate();
        this.outputInventoryOptional.invalidate();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (this.level != null && this.level.isClientSide())
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public LazyOptional<ItemStackHandler> getInputInventoryAOptional() {
        return this.inputInventoryAOptional;
    }

    public LazyOptional<ItemStackHandler> getInputInventoryBOptional() {
        return this.inputInventoryBOptional;
    }

    public LazyOptional<ItemStackHandler> getCardInventoryOptional() {
        return this.cardInventoryOptional;
    }

    public LazyOptional<ItemStackHandler> getOutputInventoryOptional() {
        return this.outputInventoryOptional;
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(4);
        inventory.setItem(0, inputInventoryA.getStackInSlot(0));
        inventory.setItem(1, inputInventoryB.getStackInSlot(0));
        inventory.setItem(2, cardInventory.getStackInSlot(0));
        inventory.setItem(4, outputInventory.getStackInSlot(0));
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (this.selectedShopIndex == -1)
            return new AutoTraderSelectorMenu(pContainerId, pPlayerInventory, this, this.data);
        return new AutoTraderMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    private boolean slotMatches(ItemStack slot, ItemStack recipe) {
        return slot.is(recipe.getItem()) && slot.areShareTagsEqual(recipe);
    }


    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag traderData = new CompoundTag();
        traderData.put("InputInventoryA", this.inputInventoryA.serializeNBT());
        traderData.put("InputInventoryB", this.inputInventoryB.serializeNBT());
        traderData.put("CardInventory", this.cardInventory.serializeNBT());
        traderData.put("OutputInventory", this.outputInventory.serializeNBT());
        traderData.putInt("Progress", this.progress);
        traderData.putInt("SelectedTradeIndex", this.selectedTradeIndex);
        traderData.putInt("SelectedShopIndex", this.selectedShopIndex);
        tag.put(SocietyTrading.MODID, traderData);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        CompoundTag traderData = pTag.getCompound(SocietyTrading.MODID);
        if (traderData.contains("InputInventoryA", Tag.TAG_COMPOUND)) {
            this.inputInventoryA.deserializeNBT(traderData.getCompound("InputInventoryA"));
        }
        if (traderData.contains("InputInventoryB", Tag.TAG_COMPOUND)) {
            this.inputInventoryB.deserializeNBT(traderData.getCompound("InputInventoryB"));
        }
        if (traderData.contains("CardInventory", Tag.TAG_COMPOUND)) {
            this.outputInventory.deserializeNBT(traderData.getCompound("CardInventory"));
        }
        if (traderData.contains("OutputInventory", Tag.TAG_COMPOUND)) {
            this.outputInventory.deserializeNBT(traderData.getCompound("OutputInventory"));
        }
        this.progress = traderData.getInt("Progress");
        this.selectedTradeIndex = traderData.getInt("SelectedTradeIndex");
        this.selectedShopIndex = traderData.getInt("SelectedShopIndex");
    }


    @Override
    public Component getDisplayName() {
        return Component.translatable("block.society_trading.auto_trader");
    }

}