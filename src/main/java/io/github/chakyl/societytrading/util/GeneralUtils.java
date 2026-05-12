package io.github.chakyl.societytrading.util;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.ImageShopMenu;
import io.github.chakyl.societytrading.screen.ShopMenu;
import io.github.chakyl.societytrading.tradelimits.TradeLimitProvider;
import io.github.chakyl.societytrading.trading.ShopOffer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class GeneralUtils {
    public static boolean atTradeLimit(Player pPlayer, ShopOffer trade) {
        if (trade.getLimit() == -1) return false;
        return pPlayer.getCapability(TradeLimitProvider.PLAYER_DATA).map(data -> {
            int limitData = data.getData(trade.getTradeId());
            return trade.getLimit() - limitData <= 0;
        }).orElse(false);
    }

    public static void nameTagEntity(ItemStack pStack, Player pPlayer, LivingEntity pTarget) {
        if (pStack.hasCustomHoverName() && !(pTarget instanceof Player)) {
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

    public static void openShopMenu(Shop shop, ServerPlayer player, String shopID) {
        if (shop.displayType().equals("image")) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ImageShopMenu(containerId, inventory, shopID, null), shop.name()), buffer -> {
                buffer.writeUtf(shopID);
            });
        } else {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ShopMenu(ModElements.Menus.SHOP_MENU.get(), containerId, inventory, shopID, null), shop.name()), buffer -> {
                buffer.writeUtf(shopID);
            });
        }
    }

    public static void openShopMenu(Shop shop, ServerPlayer player, String shopID, UUID entityUUID) {
        if (shop.displayType().equals("image")) {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ImageShopMenu(containerId, inventory, shopID, entityUUID), shop.name()), buffer -> {
                buffer.writeUtf(shopID);
                buffer.writeUUID(entityUUID);
            });
        } else {
            NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ShopMenu(ModElements.Menus.SHOP_MENU.get(), containerId, inventory, shopID, entityUUID), shop.name()), buffer -> {
                buffer.writeUtf(shopID);
                buffer.writeUUID(entityUUID);
            });
        }
    }
}
