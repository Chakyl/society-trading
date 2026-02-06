package io.github.chakyl.societytrading.util;

import io.github.chakyl.societytrading.tradelimits.TradeLimitProvider;
import io.github.chakyl.societytrading.trading.ShopOffer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
}
