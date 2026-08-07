package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

public class ServerBoundTradeButtonClickPacket implements CustomPacketPayload {
    public static final Type<ServerBoundTradeButtonClickPacket> TYPE = new Type<>(loc("server_bound_trade_button_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundTradeButtonClickPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            packet -> packet.tradeId,
            ServerBoundTradeButtonClickPacket::new
    );

    private final String tradeId;

    public ServerBoundTradeButtonClickPacket(String tradeId) {
        this.tradeId = tradeId;
    }

    public ServerBoundTradeButtonClickPacket(RegistryFriendlyByteBuf buffer) {
        this.tradeId = buffer.readUtf();
    }

    @Override
    public Type<ServerBoundTradeButtonClickPacket> type() {
        return TYPE;
    }

    public static void handle(ServerBoundTradeButtonClickPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                    if (menu.clickTradeById(player, packet.tradeId)) {
                        menu.broadcastChanges();
                    }
                }
            }
        });
    }
}