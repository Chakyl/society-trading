package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundSearchPacket {
    private final String query;

    public ServerBoundSearchPacket(String query) {
        this.query = query;
    }

    public ServerBoundSearchPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.query);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                menu.filterOffers(this.query);
                menu.broadcastChanges();
            }
        }
        context.get().setPacketHandled(true);

    }
}