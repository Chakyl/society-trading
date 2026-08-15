package io.github.chakyl.societytrading.events;

import io.github.chakyl.societytrading.SocietyTrading;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = SocietyTrading.MODID)
public class CommonModEvents {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SocietyTrading.CONTROLLABLE_INSTALLED = ModList.get().isLoaded("controllable");
            SocietyTrading.SERENE_SEASONS_INSTALLED = ModList.get().isLoaded("sereneseasons");
            SocietyTrading.KUBEJS_INSTALLED = ModList.get().isLoaded("kubejs");
            SocietyTrading.NUMISMATICS_INSTALLED = ModList.get().isLoaded("numismatics");
            SocietyTrading.NUMISMATICS_UTILS_INSTALLED = ModList.get().isLoaded("numismaticsutils");
        });
    }
}