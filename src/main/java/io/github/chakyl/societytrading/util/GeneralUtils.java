package io.github.chakyl.societytrading.util;

import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.ImageShopMenu;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import io.github.chakyl.societytrading.screen.ShopMenu;
import io.github.chakyl.societytrading.screen.ThinShopMenu;
import io.github.chakyl.societytrading.tradelimits.TradeLimit;
import io.github.chakyl.societytrading.trading.ShopOffer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.UUID;

public class GeneralUtils {
    public static boolean atTradeLimit(Player pPlayer, ShopOffer trade) {
        if (trade.getLimit() == -1) return false;
        return trade.getLimit() - pPlayer.getData(ModElements.Attachments.TRADE_LIMITS.get()).getData(trade.getTradeId()) <= 0;
    }

    public static void nameTagEntity(ItemStack pStack, Player pPlayer, LivingEntity pTarget) {
        if (pStack.has(DataComponents.CUSTOM_NAME) && !(pTarget instanceof Player)) {
            if (!pPlayer.level().isClientSide && pTarget.isAlive()) {
                pTarget.setCustomName(pStack.getHoverName());
                if (pTarget instanceof Mob) {
                    ((Mob) pTarget).setPersistenceRequired();
                }
                pStack.shrink(1);
            }
        }
    }

    public static boolean canAffordOrNotRelevant(ShopOffer trade, int balance) {
        return balance <= 0 || !trade.hasNumismaticsCost() || balance >= trade.getNumismaticsCost();
    }

    public static void openSelectorMenu(ServerPlayer player, String customSelector) {
        player.openMenu(new SimpleMenuProvider((containerId, inventory, nPlayer) -> new SelectorMenu(containerId, inventory, customSelector), Component.translatable("shop.society_trading.selector.name")),
                buffer -> {
                    buffer.writeUtf(customSelector);
                }
        );
    }

    public static void openShopMenu(Shop shop, ServerPlayer player, String shopID, UUID entityUUID, @Nonnull String previousSelector) {
        if (shop.displayType().equals("image")) {
            player.openMenu(
                    new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ImageShopMenu(containerId, inventory, shopID, entityUUID, previousSelector), shop.name()),
                    buffer -> {
                        buffer.writeUtf(shopID);
                        buffer.writeUUID(entityUUID);
                        buffer.writeUtf(previousSelector);
                    }
            );
        } else if (shop.displayType().equals("thin")) {
            player.openMenu(
                    new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ThinShopMenu(containerId, inventory, shopID, entityUUID, previousSelector), shop.name()),
                    buffer -> {
                        buffer.writeUtf(shopID);
                        buffer.writeUUID(entityUUID);
                        buffer.writeUtf(previousSelector);
                    }
            );
        } else {
            player.openMenu(
                    new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ShopMenu(ModElements.Menus.SHOP_MENU.get(), containerId, inventory, shopID, entityUUID, previousSelector), shop.name()),
                    buffer -> {
                        buffer.writeUtf(shopID);
                        buffer.writeUUID(entityUUID);
                        buffer.writeUtf(previousSelector);
                    }
            );
        }
    }
}
