package io.github.chakyl.societytrading.data;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.trading.RandomSetShopOffers;
import io.github.chakyl.societytrading.trading.RandomStyle;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * Stores all of the information representing a Shop.
 *
 * @param shopID             The unique ID of the shop
 * @param name               The display name of shop
 * @param texture            The resource location of the texture used for the shopkeeper
 * @param villagerProfession Villager Profession that enables the shop screen to show when interacted
 * @param entity             Living entity that opens the shop screen to show when interacted
 * @param entityData         Data on the passed entity that must exist to open the shop
 * @param blockTag           Block tag that opens the scree
 * @param hiddenFromSelector Shops is never shown on the Shop Selector list
 * @param hiddenFromAutoTrader Shops is never shown on the Shop Selector list on Auto Trader
 * @param selectorWeight     Weight of shop in Shop Selector list for order
 * @param jeiCatalyst        Item used as a catalyst in JEI
 * @param stageRequired      KubeJs stage the player needs to have to see the shop
 * @param stageOverride      KubeJs stage that always allows the player to see the shop
 * @param seasonsRequired    Serene Seasons season to display the shop
 * @param trades             Trade object
 * @param randomSets         Sets of randomly rolled trades
 */
public record Shop(String shopID, MutableComponent name, String texture, String villagerProfession,
                   EntityType<? extends LivingEntity> entity, String entityData, TagKey<Block> blockTag,
                   Boolean hiddenFromSelector, Boolean hiddenFromAutoTrader,int selectorWeight, ItemStack jeiCatalyst,
                   String stageRequired, String stageOverride, List<String> seasonsRequired,
                   ShopOffers trades, List<RandomSetShopOffers> randomSets) implements CodecProvider<Shop> {

    public static final Codec<Shop> CODEC = new ShopCodec();
    public static final List<String> POSSIBLE_SEASONS = Arrays.asList("early_spring", "mid_spring", "late_spring", "early_summer", "mid_summer", "late_summer", "early_autumn", "mid_autumn", "late_autumn", "early_winter", "mid_winter", "late_winter");
    public static final List<String> POSSIBLE_RANDOM_STYLES = Arrays.asList("per_day", "per_player", "per_entity", "default");
    public static List<String> registeredIds = new ArrayList<>();

    public Shop(Shop other) {
        this(other.shopID, other.name, other.texture, other.villagerProfession, other.entity, other.entityData, other.blockTag, other.hiddenFromSelector, other.hiddenFromAutoTrader, other.selectorWeight, other.jeiCatalyst, other.stageRequired, other.stageOverride, other.seasonsRequired, other.trades, other.randomSets);
    }

    public int getColor() {
        return this.name.getStyle().getColor().getValue();
    }


    public Shop validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.shopID, "Invalid shop ID!");
        Preconditions.checkNotNull(this.name, "Invalid shop name!");
        Preconditions.checkNotNull(this.texture, "Missing texture!");
        // This doesn't really work how I want it to rip
//        for (ShopOffer trade : this.trades) {
//            if (registeredIds.contains(trade.getTradeId())) {
//                throw new NullPointerException("Trade given a duplicate ID " + trade.getTradeId());
//            } else {
//                registeredIds.add(trade.getTradeId());
//            }
//        }
        if (this.seasonsRequired != null) {
            this.seasonsRequired.forEach((season) -> {
                // Why is Java like that????
                if (!POSSIBLE_SEASONS.contains(season.replace("\"", ""))) {
                    throw new NullPointerException("Season " + season + " that doesn't exist! Possible values: " + POSSIBLE_SEASONS);
                }
            });
        }
        if (this.randomSets != null) {
            this.randomSets.forEach((rSet) -> {
                // Why is Java like that????
                String randomStyle = rSet.getRandomStyle().toString().toLowerCase().replace("\"", "");
                if (!POSSIBLE_RANDOM_STYLES.contains(randomStyle)) {
                    throw new NullPointerException("RandomSet given random style " + randomStyle + " that doesn't exist! Possible values: " + POSSIBLE_RANDOM_STYLES);
                }
            });
        }
        return this;
    }

    @Override
    public Codec<? extends Shop> getCodec() {
        return CODEC;
    }

    private static JsonArray encodeTradesArray(ShopOffers shopOffersTrades, String shopID) {
        ResourceLocation key = new ResourceLocation(SocietyTrading.MODID, shopID);
        JsonArray trades = new JsonArray();
        for (ShopOffer trade : shopOffersTrades) {
            JsonObject tradeObj = new JsonObject();
            JsonElement request = ItemAdapter.ITEM_READER.toJsonTree(trade.getCostA());
            JsonObject requestJson = request.getAsJsonObject();
            ResourceLocation requestItemName = new ResourceLocation(requestJson.get("item").getAsString());
            JsonElement secondRequest = ItemAdapter.ITEM_READER.toJsonTree(trade.getCostB());
            JsonObject secondRequestJson = secondRequest.getAsJsonObject();
            ResourceLocation secondRequestItemName = new ResourceLocation(secondRequestJson.get("item").getAsString());
            JsonElement offer = ItemAdapter.ITEM_READER.toJsonTree(trade.getResult());
            JsonObject offerJson = offer.getAsJsonObject();
            ResourceLocation offerItemName = new ResourceLocation(offerJson.get("item").getAsString());
            if (!"minecraft".equals(offerItemName.getNamespace()) && !key.getNamespace().equals(offerItemName.getNamespace())) {
                offerJson.addProperty("optional", true);
            }
            if (!"minecraft".equals(requestItemName.getNamespace()) && !key.getNamespace().equals(requestItemName.getNamespace())) {
                requestJson.addProperty("optional", true);
            }
            if (!"minecraft".equals(secondRequestItemName.getNamespace()) && !key.getNamespace().equals(secondRequestItemName.getNamespace())) {
                secondRequestJson.addProperty("optional", true);
            }

            JsonArray tradeSeasonsRequired = new JsonArray();
            for (String season : trade.getSeasonsRequired()) {
                tradeSeasonsRequired.add(season.replace("\"", ""));
            }
            tradeObj.add("request", requestJson);
            tradeObj.add("second_request", secondRequestJson);
            tradeObj.add("offer", offerJson);
            if (!Objects.equals(trade.getUnlockDescription(), Component.empty())) {
                tradeObj.addProperty("unlock_description", ((TranslatableContents) trade.getUnlockDescription().getContents()).getKey());
            }
            tradeObj.addProperty("stage_required", trade.getStageRequired());
            tradeObj.addProperty("stage_override", trade.getStageOverride());
            tradeObj.addProperty("stage_removed", trade.getStageRemoved());
            tradeObj.add("seasons_required", tradeSeasonsRequired);
            tradeObj.addProperty("numismatics_cost", trade.getNumismaticsCost());
            tradeObj.addProperty("trade_id", trade.getTradeId());
            tradeObj.addProperty("limit", trade.getLimit());
            trades.add(tradeObj);
        }
        return trades;

    }

    private static ShopOffers decodeTradesArray(JsonObject obj, String shopID) {
        ShopOffers trades = new ShopOffers();
        if (obj.has("trades")) {
            for (JsonElement json : GsonHelper.getAsJsonArray(obj, "trades")) {
                if (json.getAsJsonObject().has("offer") && json.getAsJsonObject().has("request")) {
                    ItemStack request = ItemAdapter.ITEM_READER.fromJson(json.getAsJsonObject().getAsJsonObject("request"), ItemStack.class);
                    ItemStack offer = ItemAdapter.ITEM_READER.fromJson(json.getAsJsonObject().getAsJsonObject("offer"), ItemStack.class);
                    String tradeStage = "";
                    String tradeStageOverride = "";
                    String tradeStageRemoved = "";
                    int numismaticsCost = 0;
                    int limit = -1;
                    String tradeId = shopID + ":" + offer.getItem();
                    List<String> tradeSeasonsRequired = new ArrayList<>();
                    if (json.getAsJsonObject().has("seasons_required")) {
                        for (JsonElement arrayJson : GsonHelper.getAsJsonArray(json.getAsJsonObject(), "seasons_required")) {
                            tradeSeasonsRequired.add(String.valueOf(arrayJson).replace("\"", ""));
                        }
                    }
                    MutableComponent tradeUnlockDescription = Component.empty();
                    if (json.getAsJsonObject().has("unlock_description")) {
                        tradeUnlockDescription = Component.translatable(GsonHelper.getAsString(json.getAsJsonObject(), "unlock_description"));
                    }
                    if (json.getAsJsonObject().has("stage_required"))
                        tradeStage = GsonHelper.getAsString(json.getAsJsonObject(), "stage_required");
                    if (json.getAsJsonObject().has("stage_override"))
                        tradeStageOverride = GsonHelper.getAsString(json.getAsJsonObject(), "stage_override");
                    if (json.getAsJsonObject().has("stage_removed"))
                        tradeStageRemoved = GsonHelper.getAsString(json.getAsJsonObject(), "stage_removed");
                    if (json.getAsJsonObject().has("numismatics_cost"))
                        numismaticsCost = GsonHelper.getAsInt(json.getAsJsonObject(), "numismatics_cost");
                    if (json.getAsJsonObject().has("trade_id")) {
                        tradeId = GsonHelper.getAsString(json.getAsJsonObject(), "trade_id");
                    }
                    if (json.getAsJsonObject().has("limit")) {
                        limit = GsonHelper.getAsInt(json.getAsJsonObject(), "limit");
                    }
                    if (json.getAsJsonObject().has("second_request")) {
                        ItemStack secondRequest = ItemAdapter.ITEM_READER.fromJson(json.getAsJsonObject().getAsJsonObject("second_request"), ItemStack.class);
                        trades.add(new ShopOffer(request, secondRequest, offer, tradeUnlockDescription, tradeStage, tradeStageOverride, tradeStageRemoved, tradeSeasonsRequired, numismaticsCost, limit, tradeId));
                    } else {
                        trades.add(new ShopOffer(request, offer, tradeUnlockDescription, tradeStage, tradeStageOverride,tradeStageRemoved, tradeSeasonsRequired, numismaticsCost, limit, tradeId));
                    }
                }

            }
        }
        return trades;
    }

    public static class ShopCodec implements Codec<Shop> {

        @Override
        public <T> DataResult<T> encode(Shop input, DynamicOps<T> ops, T prefix) {
            JsonObject obj = new JsonObject();
            obj.addProperty("shop_id", input.shopID);
            obj.addProperty("name", ((TranslatableContents) input.name.getContents()).getKey());
            obj.addProperty("texture", input.texture);
            obj.addProperty("villager_profession", input.villagerProfession);
            obj.addProperty("entity", EntityType.getKey(input.entity).toString());
            obj.addProperty("entity_data", input.entityData);
            if (input.blockTag != null) obj.addProperty("block_tag", input.blockTag.location().toString());
            obj.addProperty("hidden_from_selector", input.hiddenFromSelector);
            obj.addProperty("hidden_from_auto_trader", input.hiddenFromAutoTrader);
            obj.addProperty("hidden_from_selector", input.hiddenFromSelector);
            obj.addProperty("selector_weight", input.selectorWeight);
            obj.add("jei_catalyst", ItemAdapter.ITEM_READER.toJsonTree(input.jeiCatalyst));
            obj.addProperty("stage_required", input.stageRequired);
            obj.addProperty("stage_override", input.stageOverride);
            JsonArray seasonsRequired = new JsonArray();
            obj.add("seasons_required", seasonsRequired);
            for (String season : input.seasonsRequired) {
                seasonsRequired.add(season.replace("\"", ""));
            }
            obj.add("trades", encodeTradesArray(input.trades, input.shopID));
            JsonArray randomizedTrades = new JsonArray();
            for (RandomSetShopOffers randomSetShopOffers : input.randomSets) {
                JsonObject rsObject = new JsonObject();
                rsObject.addProperty("stage_required", randomSetShopOffers.getStageRequired());
                rsObject.addProperty("stage_override", randomSetShopOffers.getStageOverride());
                rsObject.addProperty("stage_removed", randomSetShopOffers.getStageRemoved());
                JsonArray rsSeasonsRequired = new JsonArray();
                for (String season : randomSetShopOffers.getSeasonsRequired()) {
                    rsSeasonsRequired.add(season.replace("\"", ""));
                }
                rsObject.add("seasons_required", rsSeasonsRequired);
                String randomStyle;
                switch (randomSetShopOffers.getRandomStyle()) {
                    case PER_DAY -> randomStyle = "per_day";
                    case PER_PLAYER -> randomStyle = "per_player";
                    case PER_ENTITY -> randomStyle = "per_entity";
                    default -> randomStyle = "default";
                }
                rsObject.addProperty("random_style", randomStyle);
                rsObject.addProperty("rolled_count", randomSetShopOffers.getRolledCount());
                rsObject.add("trades", encodeTradesArray(randomSetShopOffers, input.shopID));
                randomizedTrades.add(rsObject);
            }
            obj.add("random_sets", randomizedTrades);
            return DataResult.success(JsonOps.INSTANCE.convertTo(ops, obj));
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> DataResult<Pair<Shop, T>> decode(DynamicOps<T> ops, T input) {
            JsonObject obj = ops.convertTo(JsonOps.INSTANCE, input).getAsJsonObject();
            String shopId = GsonHelper.getAsString(obj, "shop_id");
            MutableComponent name = Component.translatable(GsonHelper.getAsString(obj, "name"));
            String texture = GsonHelper.getAsString(obj, "texture");
            String villagerProfession = "";
            if (obj.has("villager_profession")) {
                villagerProfession = GsonHelper.getAsString(obj, "villager_profession");
            }
            EntityType<? extends LivingEntity> entity = null;
            if (obj.has("entity")) {
                String entityStr = GsonHelper.getAsString(obj, "entity");
                entity = (EntityType) ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityStr));
                if (entity == EntityType.PIG && !"minecraft:pig".equals(entityStr))
                    throw new JsonParseException("Shop has entity type " + entityStr);
            }
            String entityData = "";
            if (obj.has("entity_data")) {
                entityData = GsonHelper.getAsString(obj, "entity_data");
            }
            TagKey<Block> blockTag = null;
            if (obj.has("block_tag")) {
                blockTag = TagKey.create(Registries.BLOCK, new ResourceLocation(obj.get("block_tag").getAsString()));
            }
            boolean hiddenFromSelector = false;
            if (obj.has("hidden_from_selector")) {
                JsonElement el = obj.get("hidden_from_selector");

                if (el != null && !el.isJsonNull()) {
                    if (el.isJsonPrimitive()) {
                        JsonPrimitive p = el.getAsJsonPrimitive();
                        if (p.isBoolean()) {
                            hiddenFromSelector = p.getAsBoolean();
                        } else if (p.isNumber()) {
                            hiddenFromSelector = p.getAsInt() != 0;
                        }
                    }
                }
            }
            boolean hiddenFromAutoTrader = false;
            if (obj.has("hidden_from_auto_trader")) {
                JsonElement el = obj.get("hidden_from_auto_trader");

                if (el != null && !el.isJsonNull()) {
                    if (el.isJsonPrimitive()) {
                        JsonPrimitive p = el.getAsJsonPrimitive();
                        if (p.isBoolean()) {
                            hiddenFromAutoTrader = p.getAsBoolean();
                        } else if (p.isNumber()) {
                            hiddenFromAutoTrader = p.getAsInt() != 0;
                        }
                    }
                }
            }
            int selectorWeight = 1;
            if (obj.has("selector_weight")) {
                selectorWeight = (GsonHelper.getAsInt(obj, "selector_weight"));
            }
            ItemStack jeiCatalyst = Items.VILLAGER_SPAWN_EGG.getDefaultInstance();
            if (obj.has("jei_catalyst")) {
                jeiCatalyst = ItemAdapter.ITEM_READER.fromJson(obj.getAsJsonObject("jei_catalyst"), ItemStack.class);
            }
            String stageRequired = "";
            if (obj.has("stage_required")) {
                stageRequired = GsonHelper.getAsString(obj, "stage_required");
            }
            String stageOverride = "";
            if (obj.has("stage_override")) {
                stageOverride = GsonHelper.getAsString(obj, "stage_override");
            }
            List<String> seasonsRequired = new ArrayList<>();
            if (obj.has("seasons_required")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "seasons_required")) {
                    seasonsRequired.add(String.valueOf(json).replace("\"", ""));
                }
            }
            ShopOffers trades = decodeTradesArray(obj, shopId);
            List<RandomSetShopOffers> randomSets = new ArrayList<>();
            if (obj.has("random_sets")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "random_sets")) {
                    JsonObject objJson = json.getAsJsonObject();
                    RandomSetShopOffers rsTrades = decodeTradesArray(objJson, shopId).toRandomShopOffers();
                    if (objJson.has("stage_required")) {
                        rsTrades.setStageRequired(GsonHelper.getAsString(objJson, "stage_required"));
                    }
                    if (objJson.has("stage_override")) {
                        rsTrades.setStageOverride(GsonHelper.getAsString(objJson, "stage_override"));
                    }
                    if (objJson.has("stage_removed")) {
                        rsTrades.setStageRemoved(GsonHelper.getAsString(objJson, "stage_removed"));
                    }
                    List<String> rsSeasonsRequired = new ArrayList<>();
                    if (objJson.has("seasons_required")) {
                        for (JsonElement sjson : GsonHelper.getAsJsonArray(objJson, "seasons_required")) {
                            rsSeasonsRequired.add(String.valueOf(sjson).replace("\"", ""));
                        }
                    }
                    if (objJson.has("rolled_count")) {
                        rsTrades.setRolledCount(GsonHelper.getAsInt(objJson, "rolled_count"));
                    }
                    if (objJson.has("random_style")) {
                        RandomStyle foundStyle = switch (GsonHelper.getAsString(objJson, "random_style")) {
                            case "per_day" -> RandomStyle.PER_DAY;
                            case "per_player" -> RandomStyle.PER_PLAYER;
                            case "per_entity" -> RandomStyle.PER_ENTITY;
                            default -> RandomStyle.DEFAULT;
                        };
                        rsTrades.setRandomStyle(foundStyle);
                    }
                    rsTrades.setSeasonsRequired(rsSeasonsRequired);
                    randomSets.add(rsTrades);
                }
            }
            return DataResult.success(Pair.of(new Shop(shopId, name, texture, villagerProfession, entity, entityData, blockTag, hiddenFromSelector, hiddenFromAutoTrader,  selectorWeight, jeiCatalyst, stageRequired, stageOverride, seasonsRequired, trades, randomSets), input));
        }

    }

}