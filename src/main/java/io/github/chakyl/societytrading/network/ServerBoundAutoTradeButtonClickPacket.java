package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.screen.AutoTraderMenu;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundAutoTradeButtonClickPacket {
    private final byte button;

    public ServerBoundAutoTradeButtonClickPacket(byte button) {
        this.button = button;
    }

    public ServerBoundAutoTradeButtonClickPacket(FriendlyByteBuf buffer) {
        this(buffer.readByte());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeByte(this.button);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof AutoTraderMenu menu && menu.stillValid(player)) {
                if (menu.clickMenuButton(player, this.button)) {
                    menu.broadcastChanges();
                }
            }
        }
        context.get().setPacketHandled(true);

    }
}