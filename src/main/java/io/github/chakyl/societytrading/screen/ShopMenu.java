package io.github.chakyl.societytrading.screen;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.network.ClientBoundBalancePacket;
import io.github.chakyl.societytrading.network.PacketHandler;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.tradelimits.TradeLimitProvider;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import io.github.chakyl.societytrading.util.GeneralUtils;
import io.github.chakyl.societytrading.util.ItemHash;
import io.github.chakyl.societytrading.util.ShopData;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

import static io.github.chakyl.numismaticsutils.utils.CurioUtils.getCardCurio;

public class ShopMenu extends AbstractContainerMenu {
    private final Level level;
    private final int containerId;
    private final Player player;
    private final UUID targetUUID;
    private Shop shop;
    private final ResultContainer result = new ResultContainer();
    private final Slot resultSlot;
    private final Container playerInventory;
    private ShopOffer selectedTrade;
    private ShopOffers trades;
    private int quickSlotIteration = 0;
    private int playerBalance = 0;
    private long lastSoundTime;

    public ShopMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, null, null);
    }

    public ShopMenu(int pContainerId, Inventory pPlayerInventory, String shopID, UUID pTargetUUID) {
        super(ModElements.Menus.SHOP_MENU.get(), pContainerId);
        if (shopID != null) {
            DynamicHolder<Shop> shop = ShopRegistry.INSTANCE.holder(new ResourceLocation("society_trading:" + shopID));
            this.shop = shop.get();
            this.trades = ShopData.getFilteredTrades(this.shop.trades(), this.shop.randomSets(), pPlayerInventory.player, pTargetUUID);
        } else {
            this.shop = null;
            this.trades = null;
        }
        this.player = pPlayerInventory.player;
        this.level = pPlayerInventory.player.level();
        this.targetUUID = pTargetUUID;
        this.playerInventory = pPlayerInventory;
        this.playerBalance = fetchPlayerBalance();
        this.containerId = pContainerId;
        this.resultSlot = this.addSlot(new ShopResultSlot(this.result, 0, 276, 148));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 88 + j * 18, 144 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pPlayerInventory, k, 88 + k * 18, 202));
        }
        this.broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        this.updateResultSlot();
        super.broadcastChanges();
    }

    public ShopOffer getSelectedTrade() {
        return selectedTrade;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button >= 0 && button < this.trades.size()) {
            this.selectedTrade = trades.get(button);
            this.quickSlotIteration = 0;
            this.updateResultSlot();
            return true;
        }
        return false;
    }

    private void updateResultSlot() {
        if (!this.level.isClientSide()) {
            int selectedRecipeIndex = this.trades.indexOf(this.selectedTrade);
            if (selectedRecipeIndex >= 0 && selectedRecipeIndex < this.trades.size()) {
                if (this.canTradeFor(this.selectedTrade)) {
                    ItemStack result = this.selectedTrade.getResult();
                    this.resultSlot.set(result.copy());
                } else {
                    this.resultSlot.set(ItemStack.EMPTY);
                }
            } else {
                this.resultSlot.set(ItemStack.EMPTY);
            }
            super.broadcastChanges();
        }
    }


    public boolean canTradeForSelected() {
        return !this.resultSlot.getItem().isEmpty();
    }

    private boolean canTradeFor(ShopOffer selectedTrade) {
        if (GeneralUtils.atTradeLimit(this.player, selectedTrade)) {
            return false;
        }
        return this.getConsumedMaterialItems(selectedTrade) != null && GeneralUtils.canAffordOrNotRelevant(selectedTrade, this.playerBalance);
    }

    public void trade(ShopOffer offer, Inventory pPlayerInventory) {
        Map<Item, Integer> items = this.getConsumedMaterialItems(offer);
        if (items != null) {
            this.removeItems(items, pPlayerInventory, offer);
        }
        if (this.playerBalance > 0 && offer.hasNumismaticsCost()) {
            this.getPlayerAccount().deduct(offer.getNumismaticsCost());
            this.updateBalance();
        }
        if (offer.getLimit() > 0) {
            this.player.getCapability(TradeLimitProvider.PLAYER_DATA).ifPresent(data -> {
                int existingLimit = data.getData(offer.getTradeId());
                if (existingLimit == 0) {
                    data.setData(offer.getTradeId(), 1);
                } else {
                    data.setData(offer.getTradeId(), 1 + existingLimit);
                }
            });
        }

    }

    private boolean hasItemWithNbt(Inventory pPlayerInventory, Item item, CompoundTag nbt) {
        List<Pair<Container, Runnable>> foundItems = new ArrayList<>();
        IntStream.range(0, pPlayerInventory.getContainerSize()).forEach(slot -> {
            ItemStack stack = pPlayerInventory.getItem(slot);
            if (stack.isEmpty() || stack.isDamaged())
                return;
            if (stack.getItem() == item && stack.getTag() != null && stack.getTag().equals(nbt))
                foundItems.add(Pair.of(pPlayerInventory, () -> stack.setCount(0)));
        });
        return !foundItems.isEmpty();
    }

    private void removeItems(Map<Item, Integer> items, Inventory pPlayerInventory, ShopOffer offer) {
        List<Pair<Container, Runnable>> transactions = new ArrayList<>();
        IntStream.range(0, pPlayerInventory.getContainerSize()).forEach(slot -> {
            boolean offerIsExact = true;
            ItemStack stack = pPlayerInventory.getItem(slot);
            if (stack.isEmpty() || stack.isDamaged())
                return;
            Item item = stack.getItem();
            Integer count = items.get(item);
            if (count != null) {
                if (offer.getCostA().getTag() != null && item == offer.getCostA().getItem()) {
                    if (stack.getTag() == null) offerIsExact = false;
                    else if (!stack.getTag().equals(offer.getCostA().getTag())) offerIsExact = false;
                }
                if (offer.getCostB().getTag() != null && item == offer.getCostB().getItem()) {
                    if (stack.getTag() == null) offerIsExact = false;
                    else if (!stack.getTag().equals(offer.getCostB().getTag())) offerIsExact = false;
                }
                if (offerIsExact) {
                    if (stack.getCount() < count) {
                        count -= stack.getCount();
                        items.put(item, count);
                        transactions.add(Pair.of(pPlayerInventory, () -> stack.setCount(0)));
                    } else {
                        Integer finalCount = count;
                        transactions.add(Pair.of(pPlayerInventory, () -> stack.shrink(finalCount)));
                        items.remove(item);
                    }
                }
            }
        });
        if (items.isEmpty()) {
            transactions.forEach(pair -> {
                pair.right().run();
                pair.left().setChanged();
            });
        }
    }


    public static Map<Item, Integer> countItems(Container pPlayerInventory) {
        Map<Item, Integer> map = new Object2IntOpenCustomHashMap<>(ItemHash.INSTANCE);
        IntStream.range(0, pPlayerInventory.getContainerSize()).forEach(slot -> {
            ItemStack stack = pPlayerInventory.getItem(slot);
            if (stack.isEmpty()) return;
            map.merge(stack.getItem(), stack.getCount(), Integer::sum);
        });

        return map;
    }

    private Map<Item, Integer> addConsumedMaterial(Map<Item, Integer> materials, Map<Item, Integer> counts, ItemStack stack) {
        if (this.playerBalance > 0 && stack.is(NumismaticsTags.AllItemTags.COINS.tag)) return materials;
        int remaining = stack.getCount();
        Item item = stack.getItem();
        int count = counts.getOrDefault(item, 0);
        count -= materials.getOrDefault(item, 0);
        if (count > 0) {
            if (stack.getTag() != null) {
                if (!hasItemWithNbt(this.player.getInventory(), item, stack.getTag())) return null;
            }
            if (count >= remaining) {
                materials.merge(item, remaining, Integer::sum);
                remaining = 0;
            } else {

                materials.merge(item, count, Integer::sum);
                remaining -= count;
            }
        }
        if (remaining > 0) {
            return null;
        }
        return materials;
    }

    @Nullable
    private Map<Item, Integer> getConsumedMaterialItems(ShopOffer offer) {
        Map<Item, Integer> counts = countItems(this.playerInventory);
        Map<Item, Integer> materials = new HashMap<>();
        materials = addConsumedMaterial(materials, counts, offer.getCostA());
        if (materials == null) return null;
        return addConsumedMaterial(materials, counts, offer.getCostB());
    }

    /**
     * Determines whether supplied player can use this container
     */
    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
     * null for the initial slot that was double-clicked.
     */
    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot != this.resultSlot && super.canTakeItemForPickAll(pStack, pSlot);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.getItem().getCount() > 0 && this.quickSlotIteration < slot.getItem().getMaxStackSize() / slot.getItem().getCount()) {
            this.quickSlotIteration++;
            if (slot.hasItem()) {
                ItemStack slotStack = slot.getItem();
                stack = slotStack.copy();
                if (pIndex == this.resultSlot.index) {
                    Item item = slotStack.getItem();
                    item.onCraftedBy(slotStack, pPlayer.level(), pPlayer);
                    if (!this.moveItemStackTo(slotStack, 1, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                    slot.onQuickCraft(slotStack, stack);
                }

                if (slotStack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                }

                slot.setChanged();

                if (slotStack.getCount() == stack.getCount()) {
                    return ItemStack.EMPTY;
                }

                slot.onTake(pPlayer, slotStack);
                this.broadcastChanges();
            }
        } else {
            this.quickSlotIteration = 0;
        }
        return stack;
    }

    /**
     * Called when the container is closed.
     */
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
    }

    /**
     * {@link ClientPacketListener} uses this to set offers for the client side
     * MerchantContainer.
     */
    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public void filterOffers(String searchQuery) {
        ShopOffers availableTrades = ShopData.getFilteredTrades(this.shop.trades(), this.shop.randomSets(), this.player, this.targetUUID);
        if (searchQuery.isEmpty()) this.trades = availableTrades;
        else this.trades = ShopData.getSearchedTrades(availableTrades, searchQuery);
    }

    public ShopOffers getOffers() {
        return this.trades;
    }

    public void syncPlayerBalance() {
        if (!this.level.isClientSide()) {
            PacketHandler.sendToPlayer(new ClientBoundBalancePacket(this.getPlayerBalance()), (ServerPlayer) this.player);
        }
    }

    public void updateBalance() {
        int balance = this.fetchPlayerBalance();
        this.setPlayerBalance(balance);
        if (!this.level.isClientSide()) {
            PacketHandler.sendToPlayer(new ClientBoundBalancePacket(balance), (ServerPlayer) this.player);
        }
    }

    private BankAccount getPlayerAccount() {
        if (this.player == null) return null;
        BankAccount account = null;
        if (SocietyTrading.NUMISMATICS_UTILS_INSTALLED) {
            UUID cardUUID = getCardCurio(player);
            if (cardUUID != null) {
                account = Numismatics.BANK.getAccount(cardUUID);
            } else return null;
        } else {
            Numismatics.BANK.getAccount(this.player.getUUID());
        }
        if (account == null || !account.isAuthorized(this.player.getUUID())) return null;
        return account;
    }

    public int fetchPlayerBalance() {
        if (!SocietyTrading.NUMISMATICS_INSTALLED || this.level.isClientSide()) return 0;
        if (this.player == null) return 0;
        BankAccount account = this.getPlayerAccount();
        if (account == null) return 0;
        return account.getBalance();
    }

    public int getPlayerBalance() {
        return this.playerBalance;
    }

    public void setPlayerBalance(int balance) {
        this.playerBalance = balance;
    }

    public String getTexture() {
        if (this.shop == null) return null;
        return this.shop.texture();
    }

    private class ShopResultSlot extends Slot {
        public ShopResultSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            stack.onCraftedBy(player.level(), player, stack.getCount());
            ShopMenu.this.onTrade(player);
            this.set(stack);
            super.onTake(player, stack);
        }

        @Override
        public void onQuickCraft(ItemStack pOldStack, ItemStack pNewStack) {
            int i = pNewStack.getCount() - pOldStack.getCount();
            if (i > 0) {
                this.onQuickCraft(pNewStack, i);
            }

        }
    }

    private void onTrade(Player player) {
        if (this.selectedTrade != null && this.canTradeFor(this.selectedTrade)) {
            this.trade(this.selectedTrade, player.getInventory());
            ShopMenu.this.updateResultSlot();
            long time = level.getGameTime();
            if (this.lastSoundTime != (this.lastSoundTime = time)) {
                level.playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, Mth.randomBetween(player.level().getRandom(), 1.2F, 0.0F));
            }
        }
    }
}