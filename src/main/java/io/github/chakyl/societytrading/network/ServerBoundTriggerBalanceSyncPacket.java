package io.github.chakyl.societytrading.network;


import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

public record ServerBoundTriggerBalanceSyncPacket() implements CustomPacketPayload {
    public static final Type<ServerBoundTriggerBalanceSyncPacket> TYPE = new Type<>(loc("server_bound_trigger_balance_sync"));

    public static final ServerBoundTriggerBalanceSyncPacket INSTANCE = new ServerBoundTriggerBalanceSyncPacket();

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerBoundTriggerBalanceSyncPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public ServerBoundTriggerBalanceSyncPacket(RegistryFriendlyByteBuf buffer) {
        this();
    }

    @Override
    public Type<ServerBoundTriggerBalanceSyncPacket> type() {
        return TYPE;
    }

    public static void handle(ServerBoundTriggerBalanceSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                    menu.syncPlayerBalance();
                    menu.broadcastChanges();
                }
            }
        });
    }
}