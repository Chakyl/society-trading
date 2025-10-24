package io.github.chakyl.societytrading.util;

import dev.latvian.mods.kubejs.stages.Stages;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import sereneseasons.api.season.SeasonHelper;

import java.util.*;

public class ShopData {

    public static Collection<Shop> getFilteredShops(Collection<Shop> shops, Player player) {
        List<Shop> newShops = new ArrayList<>();
        for (Shop shop : shops) {
            boolean flag = !shop.hiddenFromSelector();
            if (SocietyTrading.SERENE_SEASONS_INSTALLED) {
                if (!shop.seasonsRequired().isEmpty() && !shop.seasonsRequired().contains(SeasonHelper.getSeasonState(player.level()).getSubSeason().getSerializedName())) {
                    flag = false;
                }
            }
            if (SocietyTrading.KUBEJS_INSTALLED) {
                if (!shop.stageRequired().isEmpty() && !Stages.get(player).has(shop.stageRequired())) {
                    flag = false;
                }
                if (!shop.stageOverride().isEmpty() && Stages.get(player).has(shop.stageRequired())) {
                    flag = true;
                }
            }

            if (flag) {
                newShops.add(shop);
            }
        }
        ShopComparator comparator = new ShopComparator();
        comparator.setSortingBy(ShopComparator.Order.ID);
        Collections.sort(newShops, comparator);
        return newShops;
    }

    public static Shop getShopFromEntity(Collection<Shop> shops, LivingEntity entity) {
        for (Shop shop : shops) {
            if (shop.entity() == entity.getType()) return shop;
        }
        return null;
    }

    public static Shop getShopFromBlockState(Collection<Shop> shops, BlockState blockState) {
        for (Shop shop : shops) {
            if (shop.blockTag() != null && blockState.is(shop.blockTag())) return shop;
        }
        return null;
    }

    public static Shop getShopFromVillagerProfession(Collection<Shop> shops, String profession) {
        for (Shop shop : shops) {
            if (shop.villagerProfession().equals(profession)) return shop;
        }
        return null;
    }

    public static ShopOffers getFilteredTrades(ShopOffers trades, Player player) {
        ShopOffers newTrades = new ShopOffers();
        for (ShopOffer trade : trades) {
            if (trade.playerCanSee(player)) {
                newTrades.add(trade);
            }
        }
        return newTrades;
    }
    public static ShopOffers getSearchedTrades(ShopOffers trades, String searchQuery) {
        ShopOffers newTrades = new ShopOffers();
        for (ShopOffer trade : trades) {
            if (trade.getResult().getDisplayName().getString().toLowerCase().contains(searchQuery.toLowerCase())) newTrades.add(trade);
        }
        return newTrades;
    }
    public static String formatPrice(String number) {
        return formatPrice(number, true);
    }

    public static String formatPrice(String number, boolean truncateMillionsBillions) {
        if (truncateMillionsBillions) {
            if (number.length() < 4) return number;
            if (number.length() > 9) return number.charAt(0) + "." + number.charAt(1) + "B";
            if (number.length() > 6) {
                StringBuilder out = new StringBuilder(3);
                for (int i = 0; i < number.length() - 6; i++) {
                    out.append(number.charAt(i));
                }
                if (number.length() == 7) {
                    out.append('.');
                    out.append(number.charAt(1));
                }
                out.append("M");
                return out.toString();
            }
        }
        int start = number.length() % 3;
        StringBuilder out = new StringBuilder(number.length() + (number.length() / 3));
        out.append(number, 0, start);
        for (int i = 0; i < number.length() / 3; i++) {
            if (i != 0 || start != 0) out.append(",");
            out.append(number, i * 3 + start, i * 3 + start + 3);
        }
        return out.toString();
    }

}