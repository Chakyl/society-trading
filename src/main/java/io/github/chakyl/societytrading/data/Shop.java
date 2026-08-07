package io.github.chakyl.societytrading.data;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.chakyl.societytrading.trading.RandomSetShopOffers;
import io.github.chakyl.societytrading.trading.RandomStyle;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Stores all of the information representing a Shop.
 *
 * @param shopID               The unique ID of the shop
 * @param name                 The display name of shop
 * @param texture              The resource location of the texture used for the shopkeeper
 * @param villagerProfession   Villager Profession that enables the shop screen to show when interacted
 * @param entity               Living entity that opens the shop screen to show when interacted
 * @param entityData           Data on the passed entity that must exist to open the shop
 * @param blockTag             Block tag that opens the scree
 * @param hiddenFromSelector   Shops is never shown on the Shop Selector list
 * @param hiddenFromAutoTrader Shops is never shown on the Shop Selector list on Auto Trader
 * @param selectorWeight       Weight of shop in Shop Selector list for order
 * @param jeiCatalyst          Item used as a catalyst in JEI
 * @param stageRequired        KubeJs stage the player needs to have to see the shop
 * @param stageOverride        KubeJs stage that always allows the player to see the shop
 * @param displayType          Display type of the shop
 * @param seasonsRequired      Serene Seasons season to display the shop
 * @param trades               Trade object
 * @param randomSets           Sets of randomly rolled trades
 */
public record Shop(String shopID, MutableComponent name, String texture, String villagerProfession,
                   EntityType<? extends LivingEntity> entity, String entityData, TagKey<Block> blockTag,
                   Boolean hiddenFromSelector, Boolean hiddenFromAutoTrader, int selectorWeight, ItemStack jeiCatalyst,
                   String stageRequired, String stageOverride, String displayType, List<String> seasonsRequired,
                   ShopOffers trades, List<RandomSetShopOffers> randomSets) implements AbstractShop {
    public static final List<String> POSSIBLE_SEASONS = Arrays.asList("early_spring", "mid_spring", "late_spring", "early_summer", "mid_summer", "late_summer", "early_autumn", "mid_autumn", "late_autumn", "early_winter", "mid_winter", "late_winter");
    public static final List<String> POSSIBLE_RANDOM_STYLES = Arrays.asList("per_day", "per_player", "per_entity", "default");
    public static final Codec<EntityType<? extends LivingEntity>> LIVING_ENTITY_CODEC = (Codec) BuiltInRegistries.ENTITY_TYPE.byNameCodec();

    public static final Codec<ShopOffer> SHOP_OFFER_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ItemStack.CODEC.fieldOf("request").forGetter(ShopOffer::getCostA),
            ItemStack.CODEC.optionalFieldOf("second_request", ItemStack.EMPTY).forGetter(ShopOffer::getCostB),
            ItemStack.CODEC.fieldOf("offer").forGetter(ShopOffer::getResult),
            ComponentSerialization.CODEC.optionalFieldOf("unlock_description", Component.empty()).forGetter(ShopOffer::getUnlockDescription),
            Codec.STRING.optionalFieldOf("stage_required", "").forGetter(ShopOffer::getStageRequired),
            Codec.STRING.optionalFieldOf("stage_override", "").forGetter(ShopOffer::getStageOverride),
            Codec.STRING.optionalFieldOf("stage_removed", "").forGetter(ShopOffer::getStageRemoved),
            Codec.STRING.optionalFieldOf("image", "").forGetter(ShopOffer::getImage),
            ComponentSerialization.CODEC.optionalFieldOf("image_description", Component.empty()).forGetter(ShopOffer::getImageDescription),
            Codec.STRING.listOf().optionalFieldOf("seasons_required", List.of()).forGetter(ShopOffer::getSeasonsRequired),
            Codec.INT.optionalFieldOf("numismatics_cost", 0).forGetter(ShopOffer::getNumismaticsCost),
            Codec.INT.optionalFieldOf("limit", -1).forGetter(ShopOffer::getLimit),
            Codec.STRING.optionalFieldOf("trade_id", "").forGetter(ShopOffer::getTradeId)
    ).apply(inst, (request, secondRequest, offer, unlockDescription, stageRequired, stageOverride, stageRemoved, img, imgDescription, seasonsRequired, cost, limit, tradeId) ->
            new ShopOffer(request, secondRequest, offer, unlockDescription instanceof MutableComponent mutableComponent ? mutableComponent : unlockDescription.copy(), stageRequired, stageOverride, stageRemoved, img, imgDescription instanceof MutableComponent mutableComponent ? mutableComponent : imgDescription.copy(), seasonsRequired, cost, limit, tradeId)
    ));

    public static final Codec<ShopOffers> SHOP_OFFERS_CODEC = SHOP_OFFER_CODEC.listOf().xmap(
            list -> {
                ShopOffers offers = new ShopOffers();
                offers.addAll(list);
                return offers;
            },
            offers -> new ArrayList<>(offers)
    );

    public static final Codec<RandomSetShopOffers> RANDOM_SET_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("stage_required", "").forGetter(RandomSetShopOffers::getStageRequired),
            Codec.STRING.optionalFieldOf("stage_override", "").forGetter(RandomSetShopOffers::getStageOverride),
            Codec.STRING.optionalFieldOf("stage_removed", "").forGetter(RandomSetShopOffers::getStageRemoved),
            Codec.STRING.listOf().optionalFieldOf("seasons_required", List.of()).forGetter(RandomSetShopOffers::getSeasonsRequired),
            Codec.INT.optionalFieldOf("rolled_count", 0).forGetter(RandomSetShopOffers::getRolledCount),
            Codec.STRING.optionalFieldOf("random_style", "default").xmap(
                    s -> switch (s.toLowerCase()) {
                        case "per_day" -> RandomStyle.PER_DAY;
                        case "per_player" -> RandomStyle.PER_PLAYER;
                        case "per_entity" -> RandomStyle.PER_ENTITY;
                        default -> RandomStyle.DEFAULT;
                    },
                    style -> switch (style) {
                        case PER_DAY -> "per_day";
                        case PER_PLAYER -> "per_player";
                        case PER_ENTITY -> "per_entity";
                        default -> "default";
                    }
            ).forGetter(RandomSetShopOffers::getRandomStyle),
            SHOP_OFFER_CODEC.listOf().optionalFieldOf("trades", List.of()).forGetter(ArrayList::new)
    ).apply(inst, (stageReqired, stageOverride, stageRemoved, seasonsRequired, rolledCount, randomStyle, trades) -> {
        RandomSetShopOffers rs = new RandomSetShopOffers();
        rs.addAll(trades);
        rs.setStageRequired(stageReqired);
        rs.setStageOverride(stageOverride);
        rs.setStageRemoved(stageRemoved);
        rs.setSeasonsRequired(seasonsRequired);
        rs.setRolledCount(rolledCount);
        rs.setRandomStyle(randomStyle);
        return rs;
    }));

    // Why 2? apparently you can only have 16 different fields. I know what I'm doing.
    private record ShopData1(String shopID, Component name, String texture, String villagerProfession,
                             Optional<EntityType<? extends LivingEntity>> entity, String entityData,
                             Optional<TagKey<Block>> blockTag, Boolean hiddenFromSelector, Boolean hiddenFromAutoTrader,
                             int selectorWeight, ItemStack jeiCatalyst) {
    }

    private record ShopData2(String stageRequired, String stageOverride, String displayType,
                             List<String> seasonsRequired, ShopOffers trades, List<RandomSetShopOffers> randomSets) {
    }

    private static final Codec<ShopData1> DATA1_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("shop_id").forGetter(ShopData1::shopID),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ShopData1::name),
            Codec.STRING.fieldOf("texture").forGetter(ShopData1::texture),
            Codec.STRING.optionalFieldOf("villager_profession", "").forGetter(ShopData1::villagerProfession),
            LIVING_ENTITY_CODEC.optionalFieldOf("entity").forGetter(ShopData1::entity),
            Codec.STRING.optionalFieldOf("entity_data", "").forGetter(ShopData1::entityData),
            TagKey.codec(Registries.BLOCK).optionalFieldOf("block_tag").forGetter(ShopData1::blockTag),
            Codec.BOOL.optionalFieldOf("hidden_from_selector", false).forGetter(ShopData1::hiddenFromSelector),
            Codec.BOOL.optionalFieldOf("hidden_from_auto_trader", false).forGetter(ShopData1::hiddenFromAutoTrader),
            Codec.INT.optionalFieldOf("selector_weight", 1).forGetter(ShopData1::selectorWeight),
            ItemStack.CODEC.optionalFieldOf("jei_catalyst", Items.VILLAGER_SPAWN_EGG.getDefaultInstance()).forGetter(ShopData1::jeiCatalyst)
    ).apply(inst, ShopData1::new));

    private static final Codec<ShopData2> DATA2_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("stage_required", "").forGetter(ShopData2::stageRequired),
            Codec.STRING.optionalFieldOf("stage_override", "").forGetter(ShopData2::stageOverride),
            Codec.STRING.optionalFieldOf("display_type", "default").forGetter(ShopData2::displayType),
            Codec.STRING.listOf().optionalFieldOf("seasons_required", List.of()).forGetter(ShopData2::seasonsRequired),
            SHOP_OFFERS_CODEC.optionalFieldOf("trades", new ShopOffers()).forGetter(ShopData2::trades),
            RANDOM_SET_CODEC.listOf().optionalFieldOf("random_sets", List.of()).forGetter(ShopData2::randomSets)
    ).apply(inst, ShopData2::new));


    // This is so cursed
    public static final Codec<Shop> CODEC = new Codec<Shop>() {
        @Override
        public <T> DataResult<Pair<Shop, T>> decode(DynamicOps<T> dynamicOps, T input) {
            return DATA1_CODEC.decode(dynamicOps, input).flatMap(p1 ->
                    DATA2_CODEC.decode(dynamicOps, input).map(p2 -> {
                        ShopData1 shopData1 = p1.getFirst();
                        ShopData2 shopData2 = p2.getFirst();
                        MutableComponent mutableName = shopData1.name() instanceof MutableComponent mutableComponent ? mutableComponent : shopData1.name().copy();

                        return Pair.of(new Shop(
                                shopData1.shopID(), mutableName, shopData1.texture(), shopData1.villagerProfession(),
                                shopData1.entity().orElse(null), shopData1.entityData(), shopData1.blockTag().orElse(null),
                                shopData1.hiddenFromSelector(), shopData1.hiddenFromAutoTrader(), shopData1.selectorWeight(), shopData1.jeiCatalyst(),
                                shopData2.stageRequired(), shopData2.stageOverride(), shopData2.displayType(), shopData2.seasonsRequired(),
                                shopData2.trades(), shopData2.randomSets()
                        ), p2.getSecond());
                    })
            );
        }

        @Override
        public <T> DataResult<T> encode(Shop input, DynamicOps<T> dynamicOps, T prefix) {
            ShopData1 shopData1 = new ShopData1( input.shopID(), input.name(), input.texture(), input.villagerProfession(),Optional.ofNullable(input.entity()), input.entityData(), Optional.ofNullable(input.blockTag()), input.hiddenFromSelector(), input.hiddenFromAutoTrader(), input.selectorWeight(), input.jeiCatalyst());
            ShopData2 shopData2 = new ShopData2(input.stageRequired(), input.stageOverride(), input.displayType(), input.seasonsRequired(),  input.trades(), input.randomSets());
            DataResult<T> dataResult = DATA1_CODEC.encode(shopData1, dynamicOps, prefix);
            return dataResult.flatMap(t -> DATA2_CODEC.encode(shopData2, dynamicOps, t));
        }
    };

    public Shop(Shop other) {
        this(other.shopID, other.name, other.texture, other.villagerProfession, other.entity, other.entityData, other.blockTag, other.hiddenFromSelector, other.hiddenFromAutoTrader, other.selectorWeight, other.jeiCatalyst, other.stageRequired, other.stageOverride, other.displayType, other.seasonsRequired, other.trades, other.randomSets);
    }

    public Shop validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.shopID, "Invalid shop ID!");
        Preconditions.checkNotNull(this.name, "Invalid shop name!");
        Preconditions.checkNotNull(this.texture, "Missing texture!");
        if (this.seasonsRequired != null) {
            this.seasonsRequired.forEach((season) -> {
                if (!POSSIBLE_SEASONS.contains(season.replace("\"", ""))) {
                    throw new NullPointerException("Season " + season + " that doesn't exist! Possible values: " + POSSIBLE_SEASONS);
                }
            });
        }
        if (this.randomSets != null) {
            this.randomSets.forEach((rSet) -> {
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
}