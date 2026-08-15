package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.societytrading.SocietyTrading.loc;
import static io.github.chakyl.societytrading.network.ClientNetworkUtils.handleBalanceSyncClient;

public class ClientBoundBalancePacket implements CustomPacketPayload {
    public static final Type<ClientBoundBalancePacket> TYPE = new Type<>(loc("client_bound_balance"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundBalancePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            packet -> packet.balance,
            ClientBoundBalancePacket::new
    );

    final int balance;

    public ClientBoundBalancePacket(int balance) {
        this.balance = balance;
    }

    public ClientBoundBalancePacket(RegistryFriendlyByteBuf buffer) {
        this.balance = buffer.readInt();
    }

    @Override
    public Type<ClientBoundBalancePacket> type() {
        return TYPE;
    }

    public static void handle(ClientBoundBalancePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            handleBalanceSyncClient(packet);
        });
    }
}