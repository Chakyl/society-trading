package io.github.chakyl.societytrading.network;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

public class ClientBoundBalancePacket implements CustomPacketPayload {
    public static final Type<ClientBoundBalancePacket> TYPE = new Type<>(loc( "client_bound_balance"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientBoundBalancePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            packet -> packet.balance,
            ClientBoundBalancePacket::new
    );

    private final int balance;

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

    @OnlyIn(Dist.CLIENT)
    protected LocalPlayer getClientPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null ? null : mc.player;
    }

    public static void handle(ClientBoundBalancePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                if (player.containerMenu instanceof ShopMenu menu && menu.stillValid(player)) {
                    menu.setPlayerBalance(packet.balance);
                    menu.broadcastChanges();
                }
            }
        });
    }
}