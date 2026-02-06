package io.github.chakyl.societytrading.registry;

import com.google.common.collect.ImmutableSet;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.block.AutoTraderBlock;
import io.github.chakyl.societytrading.blockentity.AutoTraderBlockEntity;
import io.github.chakyl.societytrading.screen.AutoTraderMenu;
import io.github.chakyl.societytrading.screen.AutoTraderSelectorMenu;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

public class ModElements {
    private static final DeferredHelper R = DeferredHelper.create(SocietyTrading.MODID);


    static BlockBehaviour.Properties defaultBehavior = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK);

    public static class Blocks {

        public static final RegistryObject<Block> AUTO_TRADER = R.block("auto_trader", () -> new AutoTraderBlock(defaultBehavior.strength(4, 3000).noOcclusion()));

        private static void bootstrap() {
        }
    }

    public static class BlockEntities {
        public static final RegistryObject<BlockEntityType<AutoTraderBlockEntity>> AUTO_TRADER = R.blockEntity("auto_trader",
                () -> new TickingBlockEntityType<>(AutoTraderBlockEntity::new, ImmutableSet.of(Blocks.AUTO_TRADER.get()), false, true));

        private static void bootstrap() {
        }
    }

    public static class Items {
        public static final RegistryObject<BlockItem> AUTO_TRADER = R.item("auto_trader", () -> new BlockItem(Blocks.AUTO_TRADER.get(), new Item.Properties()));

        private static void bootstrap() {
        }
    }

    public static class Tags {
        public static final TagKey<Block> OPENS_SHOP_SELECTOR = BlockTags.create(new ResourceLocation(SocietyTrading.MODID, "opens_shop_selector"));

        private static void bootstrap() {
        }
    }

    public static class Menus {
        public static final RegistryObject<MenuType<ShopMenu>> SHOP_MENU = R.menu("shop_menu", () -> MenuUtil.bufType((windowId, playerInventory, data) -> new ShopMenu(windowId, playerInventory, data.readUtf(), data.readUUID())));
        public static final RegistryObject<MenuType<SelectorMenu>> SELECTOR_MENU = R.menu("selector_menu", () -> MenuUtil.type(SelectorMenu::new));
        public static final RegistryObject<MenuType<AutoTraderMenu>> AUTO_TRADER_MENU = R.menu("auto_trader_menu", () -> MenuUtil.bufType(AutoTraderMenu::new));
        public static final RegistryObject<MenuType<AutoTraderSelectorMenu>> AUTO_TRADER_SELECTOR_MENU = R.menu("auto_trader_selector_menu", () -> MenuUtil.bufType(AutoTraderSelectorMenu::new));

        private static void bootstrap() {
        }
    }
    public static class Tabs {
        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(SocietyTrading.MODID, "tab"));

        public static final RegistryObject<CreativeModeTab> AB = R.tab("tab",
                () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + SocietyTrading.MODID)).icon(() -> Items.AUTO_TRADER.get().getDefaultInstance()).build());

        private static void bootstrap() {
        }
    }


    public static void bootstrap() {
        Blocks.bootstrap();
        BlockEntities.bootstrap();
        Items.bootstrap();
        Tags.bootstrap();
        Menus.bootstrap();
        Tabs.bootstrap();
    }
}