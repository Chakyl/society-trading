package io.github.chakyl.societytrading.screen;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.util.ShopData;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;

import java.util.Collection;

public class SelectorMenu extends AbstractContainerMenu {
    private final Collection<Shop> shops;
    private final Level level;

    public SelectorMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, new ClientSideMerchant(pPlayerInventory.player), "");
    }

    public SelectorMenu(int pContainerId, Inventory pPlayerInventory, String customSelectorId) {
        this(pContainerId, pPlayerInventory, new ClientSideMerchant(pPlayerInventory.player), customSelectorId);
    }

    public SelectorMenu(int pContainerId, Inventory pPlayerInventory, Merchant pTrader, String customSelectorId) {
        super(ModElements.Menus.SELECTOR_MENU.get(), pContainerId);
        if (customSelectorId.isEmpty()) {
            this.shops = ShopData.getFilteredShops(ShopRegistry.INSTANCE.getValues(), pPlayerInventory.player, true);
        } else {
            this.shops = ShopData.getFilteredShops(ShopData.getCustomSelectorShops(customSelectorId), pPlayerInventory.player, false);
        }
        this.level = pPlayerInventory.player.level();
    }

    public Collection<Shop> getShops() {
        return this.shops;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }


    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    public Level getLevel() {
        return this.level;
    }
}