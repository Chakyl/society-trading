package io.github.chakyl.societytrading.registry;

import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

public class ModElements {
    private static final DeferredHelper R = DeferredHelper.create(SocietyTrading.MODID);


    static BlockBehaviour.Properties defaultBehavior = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK);
//
//    public static class Blocks {
//
//        public static final RegistryObject<Block> SHOP_BLOCK = R.block("shop_block", () -> new ShopBlock(defaultBehavior.strength(4, 3000).noOcclusion()));
//
//        private static void bootstrap() {
//        }
//    }

    public static class BlockEntities {

        private static void bootstrap() {
        }
    }

//    public static class Items {
//        public static final RegistryObject<BlockItem> SHOP_BLOCK = R.item("shop_block", () -> new BlockItem(Blocks.SHOP_BLOCK.get(), new Item.Properties()));
//
//        private static void bootstrap() {
//        }
//    }

    public static class Tags {
        public static final TagKey<Block> OPENS_SHOP_SELECTOR = BlockTags.create(new ResourceLocation(SocietyTrading.MODID, "opens_shop_selector"));

        private static void bootstrap() {
        }
    }

    public static class Menus {
        public static final RegistryObject<MenuType<ShopMenu>> SHOP_MENU = R.menu("shop_menu", () -> MenuUtil.bufType((windowId, playerInventory, data) -> new ShopMenu(windowId, playerInventory, data.readUtf(), data.readUUID())));
        public static final RegistryObject<MenuType<SelectorMenu>> SELECTOR_MENU = R.menu("selector_menu", () -> MenuUtil.type(SelectorMenu::new));

        private static void bootstrap() {
        }
    }
//    public static class Tabs {
//        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(SocietyTrading.MODID, "tab"));
//
//        public static final RegistryObject<CreativeModeTab> AB = R.tab("tab",
//                () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + SocietyTrading.MODID)).icon(() -> Items.SHOP_BLOCK.get().getDefaultInstance()).build());
//
//        private static void bootstrap() {
//        }
//    }


    public static void bootstrap() {
//        Blocks.bootstrap();
        BlockEntities.bootstrap();
//        Items.bootstrap();
        Tags.bootstrap();
        Menus.bootstrap();
//        Tabs.bootstrap();
    }
}