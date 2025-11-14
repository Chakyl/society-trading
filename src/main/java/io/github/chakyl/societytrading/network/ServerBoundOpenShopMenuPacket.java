package io.github.chakyl.societytrading.network;


import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerBoundOpenShopMenuPacket {
    private final String shopID;
    private final UUID entityUUID;

    public ServerBoundOpenShopMenuPacket(String shopID, UUID entityUUID) {
        this.shopID = shopID;
        this.entityUUID = entityUUID;
    }

    public ServerBoundOpenShopMenuPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readUUID());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.shopID);
        if (this.entityUUID != null) buffer.writeUUID(this.entityUUID);
        else buffer.writeUUID(UUID.randomUUID());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            if (player.containerMenu instanceof SelectorMenu menu && menu.stillValid(player)) {
                DynamicHolder<Shop> shop = ShopRegistry.INSTANCE.holder(new ResourceLocation("society_trading:" + shopID));
                NetworkHooks.openScreen(player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ShopMenu(containerId, inventory, this.shopID, this.entityUUID), shop.get().name()), buffer -> {
                    buffer.writeUtf(this.shopID);
                    buffer.writeUUID(this.entityUUID);
                });
            }
        }
        context.get().setPacketHandled(true);
    }
}