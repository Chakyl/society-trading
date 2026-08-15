package io.github.chakyl.societytrading.data;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.network.chat.Component;

public sealed interface AbstractShop extends CodecProvider<Shop> permits Shop {
    String shopID();

    Component name();

    String texture();
}