package io.github.chakyl.societytrading.network;


import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.chakyl.societytrading.util.GeneralUtils.openSelectorMenu;

public class ServerBoundOpenSelectorMenuPacket {
    private final String selectorID;

    public ServerBoundOpenSelectorMenuPacket(String selectorID) {
        this.selectorID = selectorID;
    }

    public ServerBoundOpenSelectorMenuPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.selectorID);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                openSelectorMenu(player, this.selectorID);
            }
        }
        context.get().setPacketHandled(true);
    }
}