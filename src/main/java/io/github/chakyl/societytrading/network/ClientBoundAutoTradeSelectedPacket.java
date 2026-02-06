//package io.github.chakyl.societytrading.network;
//
//import io.github.chakyl.societytrading.screen.AutoTraderMenu;
//import io.github.chakyl.societytrading.screen.ShopMenu;
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.world.entity.player.Player;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//public class ClientBoundAutoTradeSelectedPacket {
//    private final String tradeId;
//
//    public ClientBoundAutoTradeSelectedPacket(String tradeId) {
//        this.tradeId = tradeId;
//    }
//
//    public ClientBoundAutoTradeSelectedPacket(FriendlyByteBuf buffer) {
//        this(buffer.readUtf());
//    }
//
//    public void encode(FriendlyByteBuf buffer) {
//        buffer.writeUtf(this.tradeId);
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    protected Player getClientPlayer() {
//        Minecraft mc = Minecraft.getInstance();
//        return mc == null ? null : mc.player;
//    }
//
//    public void handle(Supplier<NetworkEvent.Context> context) {
//        Player player = getClientPlayer();
//        if (player != null) {
//            if (player.containerMenu instanceof AutoTraderMenu menu && menu.stillValid(player)) {
//                menu.setSelectedTrade(this.tradeId);
//                menu.broadcastChanges();
//            }
//        }
//        context.get().setPacketHandled(true);
//
//    }
//}