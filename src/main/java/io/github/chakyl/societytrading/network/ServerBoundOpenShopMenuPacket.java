package io.github.chakyl.societytrading.network;


import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

import static io.github.chakyl.societytrading.util.GeneralUtils.openShopMenu;

public class ServerBoundOpenShopMenuPacket {
    private final String shopID;
    private final String selectorID;
    private final UUID entityUUID;

    public ServerBoundOpenShopMenuPacket(String shopID, UUID entityUUID, String selectorID) {
        this.shopID = shopID;
        this.entityUUID = entityUUID;
        this.selectorID = selectorID;
    }

    public ServerBoundOpenShopMenuPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readUUID(), buffer.readUtf());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.shopID);
        if (this.entityUUID != null) buffer.writeUUID(this.entityUUID);
        else buffer.writeUUID(UUID.randomUUID());
        buffer.writeUtf(this.selectorID);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof SelectorMenu menu && menu.stillValid(player)) {
                DynamicHolder<Shop> shop = ShopRegistry.INSTANCE.holder(new ResourceLocation("society_trading:" + shopID));
                openShopMenu(shop.get(), player, this.shopID, this.entityUUID, this.selectorID);
            }
        }
        context.get().setPacketHandled(true);
    }
}