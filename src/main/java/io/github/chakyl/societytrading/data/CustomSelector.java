package io.github.chakyl.societytrading.data;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Stores all of the information representing a Custom Shop Selector.
 *
 * @param selectorId           The unique ID of the Selector
 * @param name                 The display name of Selector
 * @param shopIds              List of
 */
public record CustomSelector(String selectorId, MutableComponent name,  List<String> shopIds) implements CodecProvider<CustomSelector> {

    public static final Codec<CustomSelector> CODEC = new CustomSelectorCodec();
    public static List<String> registeredIds = new ArrayList<>();

    public CustomSelector(CustomSelector other) {
        this(other.selectorId, other.name, other.shopIds);
    }

    public int getColor() {
        return this.name.getStyle().getColor().getValue();
    }


    public CustomSelector validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.selectorId, "Invalid selector ID!");
        Preconditions.checkNotNull(this.name, "Invalid selector name!");

        return this;
    }

    @Override
    public Codec<? extends CustomSelector> getCodec() {
        return CODEC;
    }

    public static class CustomSelectorCodec implements Codec<CustomSelector> {

        @Override
        public <T> DataResult<T> encode(CustomSelector input, DynamicOps<T> ops, T prefix) {
            JsonObject obj = new JsonObject();
            obj.addProperty("selector_id", input.selectorId);
            obj.addProperty("name", ((TranslatableContents) input.name.getContents()).getKey());
            JsonArray shopIds = new JsonArray();
            obj.add("shop_ids", shopIds);
            for (String season : input.shopIds) {
                shopIds.add(season.replace("\"", ""));
            }
            return DataResult.success(JsonOps.INSTANCE.convertTo(ops, obj));
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> DataResult<Pair<CustomSelector, T>> decode(DynamicOps<T> ops, T input) {
            JsonObject obj = ops.convertTo(JsonOps.INSTANCE, input).getAsJsonObject();
            String shopId = GsonHelper.getAsString(obj, "selector_id");
            MutableComponent name = Component.translatable(GsonHelper.getAsString(obj, "name"));
            List<String> shopIds = new ArrayList<>();
            if (obj.has("shop_ids")) {
                for (JsonElement json : GsonHelper.getAsJsonArray(obj, "shop_ids")) {
                    shopIds.add(String.valueOf(json).replace("\"", ""));
                }
            }
            return DataResult.success(Pair.of(new CustomSelector(shopId, name, shopIds), input));
        }

    }

}