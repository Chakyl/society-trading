package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundTradeButtonClickPacket {
    private final short button;
    
    public ServerBoundTradeButtonClickPacket(short button) {
        this.button = button;
    }

    public ServerBoundTradeButtonClickPacket(FriendlyByteBuf buffer) {
        this(buffer.readShort());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeShort(this.button);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                if (menu.clickMenuButton(player, this.button)) {
                    menu.broadcastChanges();
                }
            }
        }
        context.get().setPacketHandled(true);

    }
}