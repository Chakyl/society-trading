package io.github.chakyl.societytrading.tradelimits;

import io.github.chakyl.societytrading.SocietyTrading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SocietyTrading.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TradeLimitEvents {


    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(TradeLimitCapability.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(SocietyTrading.MODID, "trade_limits"), new TradeLimitProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(TradeLimitProvider.PLAYER_DATA).ifPresent(oldStore -> {
                event.getEntity().getCapability(TradeLimitProvider.PLAYER_DATA).ifPresent(newStore -> {
                    newStore.loadNBT(oldStore.saveNBT());
                });
            });
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.side.isServer() && event.level.getDayTime() % 24000 == 0) {
            for (Player player : event.level.players()) {
                player.getCapability(TradeLimitProvider.PLAYER_DATA).ifPresent(TradeLimitCapability::clear);
            }
        }
    }
}