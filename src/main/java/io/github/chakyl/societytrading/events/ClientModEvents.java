package io.github.chakyl.societytrading.events;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.SelectorScreen;
import io.github.chakyl.societytrading.screen.ShopScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT, modid = SocietyTrading.MODID)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModElements.Menus.SHOP_MENU.get(), ShopScreen::new);
            MenuScreens.register(ModElements.Menus.SELECTOR_MENU.get(), SelectorScreen::new);
            if (SocietyTrading.CONTROLLABLE_INSTALLED) {
                ClientControllableEvents.registerClientTick();
            }
        });


    }

}