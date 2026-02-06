package io.github.chakyl.societytrading.screen;

import io.github.chakyl.societytrading.blockentity.AutoTraderBlockEntity;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.util.ShopData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collection;

public class AutoTraderSelectorMenu extends AbstractContainerMenu {
    private final Collection<Shop> shops;
    public final AutoTraderBlockEntity blockEntity;
    public final int containerId;
    private final Level level;
    private final ContainerData data;

    public AutoTraderSelectorMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public AutoTraderSelectorMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModElements.Menus.AUTO_TRADER_SELECTOR_MENU.get(), pContainerId);
        blockEntity = ((AutoTraderBlockEntity) entity);
        this.shops = ShopData.getAutoTraderShops(ShopRegistry.INSTANCE.getValues());
        this.level = inv.player.level();
        this.data = data;
        this.containerId = pContainerId;
        addDataSlots(data);
        this.broadcastChanges();
    }

    public void setSelectedShopIndex(int index) {
        this.data.set(2, index);
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        this.setSelectedShopIndex(pId);
        return true;
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
