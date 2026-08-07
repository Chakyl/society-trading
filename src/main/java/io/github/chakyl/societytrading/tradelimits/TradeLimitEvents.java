package io.github.chakyl.societytrading.tradelimits;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.registry.ModElements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = SocietyTrading.MODID)
public class TradeLimitEvents {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.getDayTime() % 24000 == 10) {
                for (Player player : serverLevel.players()) {
                    TradeLimit limits = player.getData(ModElements.Attachments.TRADE_LIMITS);
                    limits.clear();
                }
            }
        }
    }
}