package io.github.chakyl.societytrading;

import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(SocietyTrading.MODID)
public class SocietyTrading {
    public static final String MODID = "society_trading";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static boolean CONTROLLABLE_INSTALLED = false;
    public static boolean SERENE_SEASONS_INSTALLED = false;
    public static boolean KUBEJS_INSTALLED = false;
    public static boolean NUMISMATICS_INSTALLED = false;
    public static boolean NUMISMATICS_UTILS_INSTALLED = false;
    public SocietyTrading() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        ModElements.bootstrap();
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            TabFillingRegistry.register(ModElements.Tabs.TAB_KEY, ModElements.Items.AUTO_TRADER);
        });
        ShopRegistry.INSTANCE.registerToBus();
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }
}