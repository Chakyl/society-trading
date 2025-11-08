package io.github.chakyl.societytrading.trading;

import java.util.ArrayList;

public class ShopOffers extends ArrayList<ShopOffer> {
    public ShopOffers() {
    }

    public RandomSetShopOffers toRandomShopOffers() {
        RandomSetShopOffers randomSetShopOffers = new RandomSetShopOffers();
        randomSetShopOffers.addAll(this);
        return randomSetShopOffers;
    }
}