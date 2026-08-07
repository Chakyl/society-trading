package io.github.chakyl.societytrading.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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



import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all of the information representing a Custom Shop Selector.
 *
 * @param selectorId The unique ID of the Selector
 * @param name       The display name of Selector
 * @param shopIds    List of shop IDs associated with this selector
 */
public record CustomSelector(String selectorId, Component name, List<String> shopIds) implements AbstractCustomSelector {

    public static final Codec<CustomSelector> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("selector_id").forGetter(CustomSelector::selectorId),
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(CustomSelector::name),
                    Codec.STRING.listOf().fieldOf("shop_ids").forGetter(CustomSelector::shopIds)
            )
            .apply(inst, CustomSelector::new));

    public static List<String> registeredIds = new ArrayList<>();

    public CustomSelector(CustomSelector other) {
        this(other.selectorId, other.name, other.shopIds);
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
}