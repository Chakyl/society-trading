package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.SocietyTrading;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SocietyTrading.MODID)
public class PacketHandler {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ServerBoundTradeButtonClickPacket.TYPE,
                ServerBoundTradeButtonClickPacket.STREAM_CODEC,
                ServerBoundTradeButtonClickPacket::handle
        );
        registrar.playToServer(
                ServerBoundOpenShopMenuPacket.TYPE,
                ServerBoundOpenShopMenuPacket.STREAM_CODEC,
                ServerBoundOpenShopMenuPacket::handle
        );
        registrar.playToServer(
                ServerBoundSearchPacket.TYPE,
                ServerBoundSearchPacket.STREAM_CODEC,
                ServerBoundSearchPacket::handle
        );
        registrar.playToServer(
                ServerBoundTriggerBalanceSyncPacket.TYPE,
                ServerBoundTriggerBalanceSyncPacket.STREAM_CODEC,
                ServerBoundTriggerBalanceSyncPacket::handle
        );
        registrar.playToClient(
                ClientBoundBalancePacket.TYPE,
                ClientBoundBalancePacket.STREAM_CODEC,
                ClientBoundBalancePacket::handle
        );
        registrar.playToServer(
                ServerBoundAutoTradeButtonClickPacket.TYPE,
                ServerBoundAutoTradeButtonClickPacket.STREAM_CODEC,
                ServerBoundAutoTradeButtonClickPacket::handle
        );
        registrar.playToServer(
                ServerBoundOpenSelectorMenuPacket.TYPE,
                ServerBoundOpenSelectorMenuPacket.STREAM_CODEC,
                ServerBoundOpenSelectorMenuPacket::handle
        );
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void sendToPlayer(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}