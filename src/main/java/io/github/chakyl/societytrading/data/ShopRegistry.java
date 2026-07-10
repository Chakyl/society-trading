package io.github.chakyl.societytrading.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.trading.RandomSetShopOffers;
import io.github.chakyl.societytrading.trading.ShopOffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShopRegistry extends DynamicRegistry<Shop> {

    public static final ShopRegistry INSTANCE = new ShopRegistry();

    private Map<String, Shop> shopsByID = new HashMap<>();
    private Set<String> registeredTradeIds = new HashSet<>();

    public ShopRegistry() {
        super(SocietyTrading.LOGGER, "shops", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(new ResourceLocation(SocietyTrading.MODID, "shops"), Shop.CODEC);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.shopsByID = new HashMap<>();
        this.registeredTradeIds = new HashSet<>();
    }

    @Override
    protected void onReload() {
        super.onReload();
        this.shopsByID = ImmutableMap.copyOf(this.shopsByID);
    }

    @Override
    protected void validateItem(ResourceLocation key, Shop shop) {
        shop.validate(key);

        if (this.shopsByID.containsKey(shop.shopID())) {
            String msg = "Attempted to register two shops (%s and %s) with the same shopID: %s!";
            throw new UnsupportedOperationException(String.format(msg, key, this.getKey(this.shopsByID.get(shop.shopID())), shop.shopID()));
        }
        this.shopsByID.put(shop.shopID(), shop);

        if (shop.trades() != null) {
            for (ShopOffer trade : shop.trades()) {
                validateTradeId(trade, key);
            }
        }
        if (shop.randomSets() != null) {
            for (RandomSetShopOffers randomSet : shop.randomSets()) {
                for (ShopOffer trade : randomSet) {
                    validateTradeId(trade, key);
                }
            }
        }
    }

    private void validateTradeId(ShopOffer trade, ResourceLocation shopKey) {
        String tradeId = trade.getTradeId();
        if (this.registeredTradeIds.contains(tradeId)) {
            throw new IllegalStateException(String.format( "Shop '%s': Duplicate or already registered trade ID found: '%s'!", shopKey, tradeId));
        }
        this.registeredTradeIds.add(tradeId);
    }

    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return super.prepare(pResourceManager, pProfiler);
    }
}