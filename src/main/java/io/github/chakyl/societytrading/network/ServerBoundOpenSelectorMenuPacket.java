package io.github.chakyl.societytrading.network;


import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.societytrading.SocietyTrading.loc;
import static io.github.chakyl.societytrading.util.GeneralUtils.openSelectorMenu;

public class ServerBoundOpenSelectorMenuPacket implements CustomPacketPayload {
    public static final Type<ServerBoundOpenSelectorMenuPacket> TYPE = new Type<>(loc("server_bound_open_selector_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundOpenSelectorMenuPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            packet -> packet.selectorID,
            ServerBoundOpenSelectorMenuPacket::new
    );

    private final String selectorID;

    public ServerBoundOpenSelectorMenuPacket(String selectorID) {
        this.selectorID = selectorID;
    }

    public ServerBoundOpenSelectorMenuPacket(RegistryFriendlyByteBuf buffer) {
        this.selectorID = buffer.readUtf();
    }

    @Override
    public Type<ServerBoundOpenSelectorMenuPacket> type() {
        return TYPE;
    }

    public static void handle(ServerBoundOpenSelectorMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                    openSelectorMenu(player, packet.selectorID);
                }
            }
        });
    }
}
