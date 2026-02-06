package io.github.chakyl.societytrading.events;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.network.PacketHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = SocietyTrading.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonModEvents {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
        });
        event.enqueueWork(() -> SocietyTrading.CONTROLLABLE_INSTALLED = ModList.get().isLoaded("controllable"));
        event.enqueueWork(() -> SocietyTrading.SERENE_SEASONS_INSTALLED = ModList.get().isLoaded("sereneseasons"));
        event.enqueueWork(() -> SocietyTrading.KUBEJS_INSTALLED = ModList.get().isLoaded("kubejs"));
        event.enqueueWork(() -> SocietyTrading.NUMISMATICS_INSTALLED = ModList.get().isLoaded("numismatics"));
        event.enqueueWork(() -> SocietyTrading.NUMISMATICS_UTILS_INSTALLED = ModList.get().isLoaded("numismatics_utils"));
    }
}