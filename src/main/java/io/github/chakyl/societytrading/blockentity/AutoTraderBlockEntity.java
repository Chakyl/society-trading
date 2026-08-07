package io.github.chakyl.societytrading.blockentity;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.registry.NumismaticsDataComponents;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.block.AutoTraderBlock;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.AutoTraderMenu;
import io.github.chakyl.societytrading.screen.AutoTraderSelectorMenu;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import io.github.chakyl.societytrading.util.ShopData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Objects;

import static io.github.chakyl.societytrading.util.GeneralUtils.canAffordOrNotRelevant;

public class AutoTraderBlockEntity extends BlockEntity implements TickingBlockEntity, MenuProvider {
    protected final ContainerData data;
    private int selectedShopIndex = -1;
    private int selectedTradeIndex = -1;
    private String selectedShopId = "";
    private String selectedTradeId = "";
    private int progress = 0;
    private int TRADING_TIME = 120;
    private ShopOffer selectedOffer;

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

    public AutoTraderBlockEntity(BlockPos pos, BlockState state) {
        super(ModElements.BlockEntities.AUTO_TRADER, pos, state);
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
            if (level.hasNeighborSignal(pos)) {
                return;
            }
            if (this.selectedOffer != null && (isTradable() || (shouldAttemptPurchase() && autoTraderCanPurchase()))) {
                if (this.progress == 0) {
                    BlockState newState = state.setValue(AutoTraderBlock.WORKING, true);
                    level.setBlockAndUpdate(pos, newState);
                }
                this.progress = this.progress + 10;
                if (this.progress == 10) checkAndCacheIds();
                setChanged(level, pos, state);
                if (this.progress >= TRADING_TIME) {
                    craftItem();
                    this.progress = 0;
                }
            } else {
                if (this.selectedOffer == null && this.selectedTradeIndex != -1 && this.selectedShopIndex != -1) {
                    this.selectedOffer = getSelectedShopOfferByIndex();
                }
                BlockState newState = state.setValue(AutoTraderBlock.WORKING, false);
                level.setBlockAndUpdate(pos, newState);
                this.progress = 0;
            }
        }
    }

    private ShopOffer getSelectedShopOfferByIndex() {
        ShopOffers offers = ShopData.getAutoTraderTrades(getSelectedShopByIndex().trades());
        if (this.selectedTradeIndex == -1 || offers.size() <= this.selectedTradeIndex) return null;
        return offers.get(this.selectedTradeIndex);
    }

    private Shop getSelectedShopByIndex() {
        if (this.selectedShopIndex == -1) return null;
        return ShopData.getAutoTraderShops(ShopRegistry.INSTANCE.getValues()).get(this.selectedShopIndex);
    }

    public void refreshSelectedTrade() {
        this.selectedOffer = null;
        this.selectedTradeId = "";
        this.progress = 0;
    }

    public void refreshSelectedShop() {
        this.selectedShopId = "";
        this.selectedOffer = null;
        this.selectedTradeIndex = -1;
        refreshSelectedTrade();
    }

    private void craftItem() {
        this.checkAndCacheIds();
        ItemStack outputSlot = this.outputInventory.getStackInSlot(0);
        ItemStack result = this.selectedOffer.getResult();
        int resultCount = result.getCount();

        if (!outputSlot.isEmpty() && canInsertItemIntoOutputSlot(outputSlot, result) && canInsertAmountIntoOutputSlot(outputSlot, resultCount)) {
            outputSlot.setCount(outputSlot.getCount() + resultCount);
        } else {
            this.outputInventory.setStackInSlot(0, this.selectedOffer.getResult().copy());
        }
        if (shouldAttemptPurchase() && autoTraderCanPurchase()) {
            BankAccount cardAccount = getCardAccount();
            if (cardAccount != null) cardAccount.deduct(this.selectedOffer.getNumismaticsCost());
        } else if (!this.selectedOffer.getCostB().isEmpty()) {
            this.inputInventoryB.getStackInSlot(0).shrink(this.selectedOffer.getCostB().getCount());
        }
        this.inputInventoryA.getStackInSlot(0).shrink(this.selectedOffer.getCostA().getCount());
    }

    private BankAccount getCardAccount() {
        ItemStack card = this.cardInventory.getStackInSlot(0);
        if (!SocietyTrading.NUMISMATICS_INSTALLED || card.is(Items.AIR)) return null;
        if (!card.is(NumismaticsTags.AllItemTags.CARDS.tag)) return null;

        if (card.getComponents().isEmpty() && card.getComponents().has(NumismaticsDataComponents.CARD_ACCOUNT_ID)) {
            return Numismatics.BANK.getAccount(card.getComponents().get(NumismaticsDataComponents.CARD_ACCOUNT_ID));
        }
        return null;
    }

    private void checkAndCacheIds() {
        if (getSelectedShopByIndex() == null) return;
        if (getSelectedShopOfferByIndex() == null) return;
        String indexedShop = getSelectedShopByIndex().shopID();
        String indexedTrade = getSelectedShopOfferByIndex().getTradeId();
        if (this.selectedShopId.isEmpty()) this.selectedShopId = indexedShop;
        if (this.selectedTradeId.isEmpty()) this.selectedTradeId = indexedTrade;
        if (!this.selectedShopId.equals(indexedShop)) {
            this.selectedShopIndex = -1;
            refreshSelectedTrade();
        }
        if (!this.selectedTradeId.equals(indexedTrade) && getSelectedShopByIndex() != null) {
            ShopOffers trades = getSelectedShopByIndex().trades();
            boolean foundFlag = false;
            for (int i = 0; i < trades.size(); i++) {
                if (!foundFlag && trades.get(i).getTradeId().equals(this.selectedTradeId)) {
                    this.selectedTradeIndex = i;
                    refreshSelectedTrade();
                    foundFlag = true;
                }
            }
            if (!foundFlag) this.selectedTradeIndex = -1;
        }
    }

    private boolean accountHasCash() {
        return Objects.requireNonNull(getCardAccount()).getBalance() > 0;
    }

    private boolean shouldAttemptPurchase() {
        return this.selectedOffer.hasNumismaticsCost() && getCardAccount() != null && accountHasCash();
    }

    /**
     * This method is extremely important! Yes the logic is very grug developer but it needs to ensure the transaction is valid perfectly
     * @return whether or not the autotrader processes
     */
    private boolean autoTraderCanPurchase() {
        int bankAccountBalance = Objects.requireNonNull(getCardAccount()).getBalance();
        if (!canOutputCraft()) return false;
        if (!canAffordOrNotRelevant(this.selectedOffer, bankAccountBalance)) return false;
        ItemStack inputA = selectedOffer.getCostA();
        ItemStack inputB = selectedOffer.getCostB();
        boolean hasCoinA = inputA.is(NumismaticsTags.AllItemTags.COINS.tag);
        boolean hasCoinB = inputB.is(NumismaticsTags.AllItemTags.COINS.tag);
        ItemStack inputASlot = this.inputInventoryA.getStackInSlot(0);
        ItemStack inputBSlot = this.inputInventoryB.getStackInSlot(0);
        boolean inputACanTrade = slotMatches(inputA, inputASlot) && inputASlot.getCount() >= inputA.getCount();
        boolean inputBCanTrade = slotMatches(inputB, inputBSlot) && inputBSlot.getCount() >= inputB.getCount();
        boolean emptyA = inputA.isEmpty();
        boolean emptyB = inputB.isEmpty();

        if ((hasCoinA || emptyA) && (hasCoinB || emptyB) && (hasCoinA || hasCoinB)) {
            return true;
        }
        if (!hasCoinA && !hasCoinB) {
            return inputACanTrade && inputBCanTrade;
        }
        if (hasCoinA) return inputBCanTrade;
        if (hasCoinB) return inputACanTrade;

        return true;
    }

    private boolean isTradable() {
        if (this.selectedOffer == null) return false;
        ItemStack inputA = this.selectedOffer.getCostA();
        ItemStack inputB = this.selectedOffer.getCostB();
        ItemStack inputASlot = this.inputInventoryA.getStackInSlot(0);
        ItemStack inputBSlot = this.inputInventoryB.getStackInSlot(0);
        if (!(slotMatches(inputA, inputASlot) && inputASlot.getCount() >= inputA.getCount() && slotMatches(inputB, inputBSlot) && inputBSlot.getCount() >= inputB.getCount()))
            return false;
        return canOutputCraft();
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output, ItemStack result) {
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, result);
    }

    private boolean canInsertAmountIntoOutputSlot(ItemStack output, int addedCount) {
        return output.getCount() + addedCount <= output.getMaxStackSize();
    }

    private boolean canOutputCraft() {
        ItemStack result = this.selectedOffer.getResult();
        ItemStack outputSlot = this.outputInventory.getStackInSlot(0);
        return outputSlot.isEmpty() || (slotMatches(outputSlot, result)) && (canInsertAmountIntoOutputSlot(outputSlot, result.getCount()) && canInsertItemIntoOutputSlot(outputSlot, result));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide())
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public ItemStackHandler getInputInventoryA() {
        return inputInventoryA;
    }

    public ItemStackHandler getInputInventoryB() {
        return inputInventoryB;
    }

    public ItemStackHandler getCardInventory() {
        return cardInventory;
    }

    public ItemStackHandler getOutputInventory() {
        return outputInventory;
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(4);
        inventory.setItem(0, inputInventoryA.getStackInSlot(0));
        inventory.setItem(1, inputInventoryB.getStackInSlot(0));
        inventory.setItem(2, cardInventory.getStackInSlot(0));
        inventory.setItem(3, outputInventory.getStackInSlot(0));
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
        return ItemStack.isSameItemSameComponents(slot, recipe);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag traderData = new CompoundTag();
        traderData.put("InputInventoryA", this.inputInventoryA.serializeNBT(registries));
        traderData.put("InputInventoryB", this.inputInventoryB.serializeNBT(registries));
        traderData.put("CardInventory", this.cardInventory.serializeNBT(registries));
        traderData.put("OutputInventory", this.outputInventory.serializeNBT(registries));
        traderData.putInt("Progress", this.progress);
        traderData.putInt("SelectedTradeIndex", this.selectedTradeIndex);
        traderData.putString("SelectedTradeId", this.selectedTradeId);
        traderData.putInt("SelectedShopIndex", this.selectedShopIndex);
        traderData.putString("SelectedShopId", this.selectedShopId);
        tag.put(SocietyTrading.MODID, traderData);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        CompoundTag traderData = pTag.getCompound(SocietyTrading.MODID);
        if (traderData.contains("InputInventoryA", Tag.TAG_COMPOUND)) {
            this.inputInventoryA.deserializeNBT(registries, traderData.getCompound("InputInventoryA"));
        }
        if (traderData.contains("InputInventoryB", Tag.TAG_COMPOUND)) {
            this.inputInventoryB.deserializeNBT(registries, traderData.getCompound("InputInventoryB"));
        }
        if (traderData.contains("CardInventory", Tag.TAG_COMPOUND)) {
            this.cardInventory.deserializeNBT(registries, traderData.getCompound("CardInventory"));
        }
        if (traderData.contains("OutputInventory", Tag.TAG_COMPOUND)) {
            this.outputInventory.deserializeNBT(registries, traderData.getCompound("OutputInventory"));
        }
        this.progress = traderData.getInt("Progress");
        this.selectedTradeIndex = traderData.getInt("SelectedTradeIndex");
        this.selectedTradeId = traderData.getString("SelectedTradeId");
        this.selectedShopIndex = traderData.getInt("SelectedShopIndex");
        this.selectedShopId = traderData.getString("SelectedShopId");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.society_trading.auto_trader");
    }
}