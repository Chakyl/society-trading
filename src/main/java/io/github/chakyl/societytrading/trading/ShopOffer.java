package io.github.chakyl.societytrading.trading;

import dev.latvian.mods.kubejs.stages.Stages;
import io.github.chakyl.societytrading.SocietyTrading;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import sereneseasons.api.season.SeasonHelper;

import java.util.List;

public class ShopOffer {
    private final String tradeId;
    private final ItemStack costA;
    private final ItemStack costB;
    private final ItemStack result;
    private final String stageRequired;
    private final String stageOverride;
    private final String stageRemoved;
    private final List<String> seasonsRequired;
    private final int numismaticsCost;
    private final int limit;
    MutableComponent unlockDescription;

    public ShopOffer(ItemStack pCostA, ItemStack pResult, MutableComponent pUnlockDescription, String pStageRequired, String pStageOverride, String pStageRemoved, List<String> pSeasonsRequired, int pNumismaticsCost, int limit, String tradeId) {
        this(pCostA, ItemStack.EMPTY, pResult, pUnlockDescription, pStageRequired, pStageOverride, pStageRemoved, pSeasonsRequired, pNumismaticsCost, limit, tradeId);
    }

    public ShopOffer(ItemStack pCostA, ItemStack pCostB, ItemStack pResult, MutableComponent pUnlockDescription, String pStageRequired, String pStageOverride, String pStageRemoved, List<String> pSeasonsRequired, int pNumismaticsCost, int limit, String tradeId) {
        this.costA = pCostA;
        this.costB = pCostB;
        this.result = pResult;
        this.stageRequired = pStageRequired;
        this.stageOverride = pStageOverride;
        this.stageRemoved = pStageRemoved;
        this.seasonsRequired = pSeasonsRequired;
        this.numismaticsCost = pNumismaticsCost;
        this.unlockDescription = pUnlockDescription;
        this.tradeId = tradeId;
        this.limit = limit;
    }

    public String getTradeId() {
        return tradeId;
    }

    public int getLimit() {
        return limit;
    }

    public ItemStack getCostA() {
        return costA;
    }

    public ItemStack getCostB() {
        return this.costB;
    }

    public ItemStack getResult() {
        return this.result;
    }

    public String getStageRequired() {
        return this.stageRequired;
    }

    public String getStageOverride() {
        return this.stageOverride;
    }

    public String getStageRemoved() {
        return this.stageRemoved;
    }

    public MutableComponent getUnlockDescription() {
        return this.unlockDescription;
    }

    public List<String> getSeasonsRequired() {
        return this.seasonsRequired;
    }

    public boolean hasNumismaticsCost() {
        return this.numismaticsCost > 0;
    }

    public int getNumismaticsCost() {
        return this.numismaticsCost;
    }

    public boolean playerCanSee(Player player) {
        if (player != null && SocietyTrading.KUBEJS_INSTALLED) {
            if (!this.stageOverride.isEmpty() && Stages.get(player).has(this.stageOverride)) {
                return true;
            }
            if (!this.stageRemoved.isEmpty() && Stages.get(player).has(this.stageRemoved)) {
                return false;
            }
            if (!this.stageRequired.isEmpty() && !Stages.get(player).has(this.stageRequired)) {
                return false;
            }
        }
        if (player != null && SocietyTrading.SERENE_SEASONS_INSTALLED) {
            if (!this.seasonsRequired.isEmpty() && !this.seasonsRequired.contains(SeasonHelper.getSeasonState(player.level()).getSubSeason().getSerializedName())) {
                return false;
            }
        }
        return true;
    }

    public boolean satisfiedBy(ItemStack pPlayerOfferA, ItemStack pPlayerOfferB) {
        return this.isRequiredItem(pPlayerOfferA, this.getCostA()) && pPlayerOfferA.getCount() >= this.getCostA().getCount() && this.isRequiredItem(pPlayerOfferB, this.costB) && pPlayerOfferB.getCount() >= this.costB.getCount();
    }

    private boolean isRequiredItem(ItemStack pOffer, ItemStack pCost) {
        if (pCost.isEmpty() && pOffer.isEmpty()) {
            return true;
        } else {
            ItemStack itemstack = pOffer.copy();
            if (itemstack.getItem().isDamageable(itemstack)) {
                itemstack.setDamageValue(itemstack.getDamageValue());
            }

            return ItemStack.isSameItem(itemstack, pCost) && (!pCost.hasTag() || itemstack.hasTag() && NbtUtils.compareNbt(pCost.getTag(), itemstack.getTag(), false));
        }
    }

    public boolean take(ItemStack pPlayerOfferA, ItemStack pPlayerOfferB) {
        if (!this.satisfiedBy(pPlayerOfferA, pPlayerOfferB)) {
            return false;
        } else {
            pPlayerOfferA.shrink(this.getCostA().getCount());
            if (!this.getCostB().isEmpty()) {
                pPlayerOfferB.shrink(this.getCostB().getCount());
            }

            return true;
        }
    }
}
