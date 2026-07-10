package io.github.chakyl.societytrading.screen;

import io.github.chakyl.societytrading.registry.ModElements;
import net.minecraft.world.entity.player.Inventory;

import java.util.UUID;

public class ThinShopMenu extends ShopMenu {
    public ThinShopMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, null, null, null);
    }

    public ThinShopMenu(int pContainerId, Inventory pPlayerInventory, String shopID, UUID pTargetUUID, String pPreviousSelector) {
        super(ModElements.Menus.THIN_SHOP_MENU.get(), pContainerId, pPlayerInventory, shopID, pTargetUUID, pPreviousSelector);
    }
}