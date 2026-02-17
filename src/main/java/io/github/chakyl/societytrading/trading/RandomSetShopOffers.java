package io.github.chakyl.societytrading.trading;

import dev.latvian.mods.kubejs.stages.Stages;
import io.github.chakyl.societytrading.SocietyTrading;
import net.minecraft.world.entity.player.Player;
import sereneseasons.api.season.SeasonHelper;

import java.util.ArrayList;
import java.util.List;

public class RandomSetShopOffers extends ShopOffers {
    private RandomStyle randomStyle;
    private int rolledCount;
    private String stageRequired;
    private String stageOverride;
    private String stageRemoved;
    private List<String> seasonsRequired;

    public RandomSetShopOffers() {
        this(RandomStyle.DEFAULT, 1, "", "", "", new ArrayList<>());
    }

    public RandomSetShopOffers(RandomStyle pRandomStyle, int pRolledCount, String pStageRequired, String pStageOverride, String pStageRemoved, List<String> pSeasonsRequired) {
        this.randomStyle = pRandomStyle;
        this.rolledCount = pRolledCount;
        this.stageRequired = pStageRequired;
        this.stageOverride = pStageOverride;
        this.stageRemoved = pStageRemoved;
        this.seasonsRequired = pSeasonsRequired;
    }

    public RandomStyle getRandomStyle() {
        return randomStyle;
    }

    public int getRolledCount() {
        return this.rolledCount;
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

    public List<String> getSeasonsRequired() {
        return this.seasonsRequired;
    }

    public void setRandomStyle(RandomStyle pRandomStyle) {
        this.randomStyle = pRandomStyle;
    }

    public void setRolledCount(int pRolledCount) {
        this.rolledCount = pRolledCount;
    }

    public void setStageRequired(String pStageRequired) {
        this.stageRequired = pStageRequired;
    }

    public void setStageOverride(String pStageOverride) {
        this.stageOverride = pStageOverride;
    }

    public void setStageRemoved(String pStageRemoved) { this.stageRemoved = pStageRemoved; }

    public void setSeasonsRequired(List<String> pSeasonsRequired) {
        this.seasonsRequired = pSeasonsRequired;
    }

    public boolean playerCanSee(Player player) {
        if (SocietyTrading.KUBEJS_INSTALLED) {
            if (!this.stageOverride.isEmpty() && Stages.get(player).has(this.stageOverride)) {
                return true;
            }
            if (!this.stageRequired.isEmpty() && !Stages.get(player).has(this.stageRequired)) {
                return false;
            }
        }
        if (SocietyTrading.SERENE_SEASONS_INSTALLED) {
            if (!this.seasonsRequired.isEmpty() && !this.seasonsRequired.contains(SeasonHelper.getSeasonState(player.level()).getSubSeason().getSerializedName())) {
                return false;
            }
        }
        return true;
    }
}