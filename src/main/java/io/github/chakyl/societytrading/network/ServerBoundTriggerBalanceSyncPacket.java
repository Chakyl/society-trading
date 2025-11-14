package io.github.chakyl.societytrading.network;


import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerBoundTriggerBalanceSyncPacket {

    public ServerBoundTriggerBalanceSyncPacket() {

    }

    public ServerBoundTriggerBalanceSyncPacket(FriendlyByteBuf buffer) {

    }

    public void encode(FriendlyByteBuf buffer) {

    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                menu.syncPlayerBalance();
                menu.broadcastChanges();
            }
        }
        context.get().setPacketHandled(true);
    }
}