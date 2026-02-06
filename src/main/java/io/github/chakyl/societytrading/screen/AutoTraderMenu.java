package io.github.chakyl.societytrading.screen;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.blockentity.AutoTraderBlockEntity;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import io.github.chakyl.societytrading.util.ShopData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class AutoTraderMenu extends AbstractContainerMenu {
    public final AutoTraderBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    public final int containerId;
    private Shop shop;
    private ShopOffer selectedTrade;
    private ShopOffers trades;
    private int quickSlotIteration = 0;
    private int playerBalance = 0;
    private long lastSoundTime;
    private final Player player;

    public AutoTraderMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public AutoTraderMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModElements.Menus.AUTO_TRADER_MENU.get(), pContainerId);
        checkContainerSize(inv, 2);
        blockEntity = ((AutoTraderBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;
        addDataSlots(data);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 209, 18));
        });
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.SOUTH).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 231, 18));
        });
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 220, 64));
        });
        String shopID = ShopData.getAutoTraderShops(ShopRegistry.INSTANCE.getValues()).get(this.data.get(2)).shopID();
        SocietyTrading.LOGGER.info(shopID + " " + this.data.get(2));
        if (shopID != null) {
            DynamicHolder<Shop> shop = ShopRegistry.INSTANCE.holder(new ResourceLocation("society_trading:" + shopID));
            this.shop = shop.get();
            this.trades = ShopData.getAutoTraderTrades(this.shop.trades());
            this.selectedTrade = getSelectedTrade();
        } else {
            this.shop = null;
            this.trades = null;
            this.selectedTrade = null;
        }

        this.player = inv.player;
        this.containerId = pContainerId;
        this.broadcastChanges();
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int progressArrowSize = 12;

        return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
    }

    public String getTexture() {
        if (this.shop == null) return null;
        return this.shop.texture();
    }

    public void setSelectedTradeIndex(int index) {
        this.data.set(3, index);
    }

    public int getSelectedShopIndex() {
        int index = this.data.get(2);
        if (index == -1) Minecraft.getInstance().setScreen(null);
        return index;
    }

    public void syncShopData() {
        this.shop = this.getSelectedShop();
        if (this.shop == null) return;
        this.trades = ShopData.getAutoTraderTrades(this.shop.trades());
    }

    public Shop getSelectedShop() {
        if (this.getSelectedShopIndex() == -1) return null;
        return ShopData.getAutoTraderShops(ShopRegistry.INSTANCE.getValues()).get(this.getSelectedShopIndex());
    }

    public void resetSelectedShop() {
        this.data.set(2, -1);
    }

    public int getSelectedTradeIndex() {
        return this.data.get(3);
    }

    public ShopOffer getSelectedTrade() {
        if (this.shop == null || this.getSelectedTradeIndex() == -1) return null;
        return this.shop.trades().get(this.getSelectedTradeIndex());
    }

    public ShopOffers getOffers() {
        return this.trades;
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        if (pId == -1) {
            this.resetSelectedShop();
        } else {
            this.setSelectedTradeIndex(pId);
        }
        return true;
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 3;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 88 + l * 18, 98 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 88 + i * 18, 156));
        }
    }


    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModElements.Blocks.AUTO_TRADER.get());
    }

    public boolean costASlotEmpty() {
        return this.slots.get(35 + 1).getItem().isEmpty();
    }

    public boolean costBSlotEmpty() {
        return this.slots.get(35 + 2).getItem().isEmpty();
    }



    public boolean resultSlotEmpty() {
        return this.slots.get(35 + 3).getItem().isEmpty();
    }
}