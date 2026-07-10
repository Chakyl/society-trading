package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundTradeButtonClickPacket {
    private final String tradeId;

    public ServerBoundTradeButtonClickPacket(String tradeId) {
        this.tradeId = tradeId;
    }

    public ServerBoundTradeButtonClickPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.tradeId);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                if (menu.clickTradeById(player, this.tradeId)) {
                    menu.broadcastChanges();
                }
            }
        }
        context.get().setPacketHandled(true);
    }
}