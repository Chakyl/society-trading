package io.github.chakyl.societytrading.events;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = SocietyTrading.MODID)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModElements.Menus.SHOP_MENU.get(), ShopScreen::new);
        event.register(ModElements.Menus.IMAGE_SHOP_MENU.get(), ImageShopScreen::new);
        event.register(ModElements.Menus.THIN_SHOP_MENU.get(), ThinShopScreen::new);
        event.register(ModElements.Menus.SELECTOR_MENU.get(), SelectorScreen::new);
        event.register(ModElements.Menus.AUTO_TRADER_MENU.get(), AutoTraderScreen::new);
        event.register(ModElements.Menus.AUTO_TRADER_SELECTOR_MENU.get(), AutoTraderSelectorScreen::new);
    }

//    @SubscribeEvent
//    public static void onClientSetup(FMLClientSetupEvent event) {
//        event.enqueueWork(() -> {
//            if (SocietyTrading.CONTROLLABLE_INSTALLED) {
//                ClientControllableEvents.registerClientTick();
//            }
//        });
//    }
}