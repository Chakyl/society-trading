package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.screen.AutoTraderMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

public class ServerBoundAutoTradeButtonClickPacket implements CustomPacketPayload {
    public static final Type<ServerBoundAutoTradeButtonClickPacket> TYPE = new Type<>(loc("server_bound_auto_trade_button_click"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundAutoTradeButtonClickPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            packet -> packet.button,
            ServerBoundAutoTradeButtonClickPacket::new
    );

    private final byte button;

    public ServerBoundAutoTradeButtonClickPacket(byte button) {
        this.button = button;
    }

    public ServerBoundAutoTradeButtonClickPacket(RegistryFriendlyByteBuf buffer) {
        this.button = buffer.readByte();
    }

    @Override
    public Type<ServerBoundAutoTradeButtonClickPacket> type() {
        return TYPE;
    }

    public static void handle(ServerBoundAutoTradeButtonClickPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof AutoTraderMenu menu && menu.stillValid(player)) {
                    if (menu.clickMenuButton(player, packet.button)) {
                        menu.broadcastChanges();
                    }
                }
            }
        });
    }
}