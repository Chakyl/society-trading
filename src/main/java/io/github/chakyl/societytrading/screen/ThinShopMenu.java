package io.github.chakyl.societytrading.screen;

import io.github.chakyl.societytrading.registry.ModElements;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;

import java.util.UUID;

public class ThinShopMenu extends ShopMenu {
    private final ResultContainer result = new ResultContainer();
    private final Slot resultSlot;
    public ThinShopMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, null, null, null);
    }

    public ThinShopMenu(int pContainerId, Inventory pPlayerInventory, String shopID, UUID pTargetUUID, String pPreviousSelector) {
        super(ModElements.Menus.THIN_SHOP_MENU.get(), pContainerId, pPlayerInventory, shopID, pTargetUUID, pPreviousSelector);
        this.resultSlot = this.addSlot(new ShopResultSlot(this.result, 0, 196, 148));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 144 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 202));
        }
        this.addDataSlot(selectedTradeSlot);
        this.broadcastChanges();
    }
}