package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

public class ServerBoundSearchPacket implements CustomPacketPayload {
    public static final Type<ServerBoundSearchPacket> TYPE = new Type<>(loc("server_bound_search"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundSearchPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            packet -> packet.query,
            ServerBoundSearchPacket::new
    );

    private final String query;

    public ServerBoundSearchPacket(String query) {
        this.query = query;
    }

    public ServerBoundSearchPacket(RegistryFriendlyByteBuf buffer) {
        this.query = buffer.readUtf();
    }

    @Override
    public Type<ServerBoundSearchPacket> type() {
        return TYPE;
    }

    public static void handle(ServerBoundSearchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                    menu.filterOffers(packet.query);
                    menu.broadcastChanges();
                }
            }
        });
    }
}