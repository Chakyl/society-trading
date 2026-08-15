package io.github.chakyl.societytrading.network;


import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static io.github.chakyl.societytrading.SocietyTrading.loc;
import static io.github.chakyl.societytrading.util.GeneralUtils.openShopMenu;

public class ServerBoundOpenShopMenuPacket implements CustomPacketPayload {
    public static final Type<ServerBoundOpenShopMenuPacket> TYPE = new Type<>(loc("server_bound_open_shop_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundOpenShopMenuPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.shopID);
                buf.writeUUID(packet.entityUUID != null ? packet.entityUUID : UUID.randomUUID());
                ByteBufCodecs.STRING_UTF8.encode(buf, packet.selectorID);
            },
            buf -> new ServerBoundOpenShopMenuPacket(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    buf.readUUID(),
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            )
    );

    private final String shopID;
    private final String selectorID;
    private final UUID entityUUID;

    public ServerBoundOpenShopMenuPacket(String shopID, UUID entityUUID, String selectorID) {
        this.shopID = shopID;
        this.entityUUID = entityUUID;
        this.selectorID = selectorID;
    }

    public ServerBoundOpenShopMenuPacket(RegistryFriendlyByteBuf buffer) {
        this.shopID = buffer.readUtf();
        this.entityUUID = buffer.readUUID();
        this.selectorID = buffer.readUtf();
    }

    @Override
    public Type<ServerBoundOpenShopMenuPacket> type() {
        return TYPE;
    }

    public static void handle(ServerBoundOpenShopMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof SelectorMenu menu && menu.stillValid(player)) {
                    DynamicHolder<Shop> shop = ShopRegistry.INSTANCE.holder(ResourceLocation.fromNamespaceAndPath("society_trading", packet.shopID));
                    openShopMenu(shop.get(), player, packet.shopID, packet.entityUUID, packet.selectorID);
                }
            }
        });
    }
}