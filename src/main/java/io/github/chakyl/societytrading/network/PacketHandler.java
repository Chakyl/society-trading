package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.SocietyTrading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.named(
                    new ResourceLocation(SocietyTrading.MODID, "main"))
            .serverAcceptedVersions((version) -> true)
            .clientAcceptedVersions((version) -> true)
            .networkProtocolVersion(() -> String.valueOf(1))
            .simpleChannel();

    public static void register() {
        INSTANCE.messageBuilder(ServerBoundTradeButtonClickPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundTradeButtonClickPacket::encode)
                .decoder(ServerBoundTradeButtonClickPacket::new)
                .consumerMainThread(ServerBoundTradeButtonClickPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundOpenShopMenuPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundOpenShopMenuPacket::encode)
                .decoder(ServerBoundOpenShopMenuPacket::new)
                .consumerMainThread(ServerBoundOpenShopMenuPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundSearchPacket.class, 2, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundSearchPacket::encode)
                .decoder(ServerBoundSearchPacket::new)
                .consumerMainThread(ServerBoundSearchPacket::handle)
                .add();

        INSTANCE.messageBuilder(ServerBoundTriggerBalanceSyncPacket.class, 3, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerBoundTriggerBalanceSyncPacket::encode)
                .decoder(ServerBoundTriggerBalanceSyncPacket::new)
                .consumerMainThread(ServerBoundTriggerBalanceSyncPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientBoundBalancePacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientBoundBalancePacket::encode)
                .decoder(ClientBoundBalancePacket::new)
                .consumerMainThread(ClientBoundBalancePacket::handle)
                .add();

    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

}