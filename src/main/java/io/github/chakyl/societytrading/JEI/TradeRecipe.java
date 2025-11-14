package io.github.chakyl.societytrading.JEI;

import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.trading.ShopOffer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class TradeRecipe {
    final MutableComponent shopName;
    final ItemStack costA;
    final ItemStack costB;
    final ItemStack result;
    final ItemStack catalyst;
    final int numismaticsCost;
    final MutableComponent unlockDescription;
    final boolean hideCostA;
    final boolean hideCostB;

    public TradeRecipe(ShopOffer shop, ItemStack catalyst, MutableComponent shopName) {
        this.shopName = shopName;
        this.costA = shop.getCostA();
        this.costB = shop.getCostB();
        this.result = shop.getResult();
        this.catalyst = catalyst;
        this.numismaticsCost = shop.getNumismaticsCost();
        this.unlockDescription = shop.getUnlockDescription();
        this.hideCostA = (SocietyTrading.NUMISMATICS_INSTALLED && this.costA.is(NumismaticsTags.AllItemTags.COINS.tag) && this.numismaticsCost > 0);
        this.hideCostB = this.costB == ItemStack.EMPTY || (SocietyTrading.NUMISMATICS_INSTALLED && this.costB.is(NumismaticsTags.AllItemTags.COINS.tag) && this.numismaticsCost > 0);

    }
}
