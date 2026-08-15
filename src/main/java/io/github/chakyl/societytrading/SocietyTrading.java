package io.github.chakyl.societytrading;

import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import io.github.chakyl.societytrading.data.CustomSelectorRegistry;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
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

    public SocietyTrading(IEventBus bus) {
        bus.register(this);
        ModElements.bootstrap(bus);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            TabFillingRegistry.register(ModElements.Tabs.TAB_KEY, ModElements.Items.AUTO_TRADER);
        });
        ShopRegistry.INSTANCE.registerToBus();
        CustomSelectorRegistry.INSTANCE.registerToBus();
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}