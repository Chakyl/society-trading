package io.github.chakyl.societytrading.registry;

import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.block.AutoTraderBlock;
import io.github.chakyl.societytrading.blockentity.AutoTraderBlockEntity;
import io.github.chakyl.societytrading.screen.*;
import io.github.chakyl.societytrading.tradelimits.TradeLimit;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

public class ModElements {
    private static final DeferredHelper R = DeferredHelper.create(SocietyTrading.MODID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SocietyTrading.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SocietyTrading.MODID);

    public static class Blocks {
        public static final Holder<Block> AUTO_TRADER = R.block("auto_trader", AutoTraderBlock::new, p -> p.strength(4, 3000).noOcclusion().sound(SoundType.METAL).isRedstoneConductor((state, lvl, pos) -> false));

        private static void bootstrap() {
        }
    }

    public static class BlockEntities {
        public static final BlockEntityType<AutoTraderBlockEntity> AUTO_TRADER = R.tickingBlockEntity("auto_trader", AutoTraderBlockEntity::new, TickingBlockEntityType.TickSide.SERVER, Blocks.AUTO_TRADER);

        private static void bootstrap() {
        }
    }

    public static class Items {
        public static final Holder<Item> AUTO_TRADER = R.blockItem("auto_trader", Blocks.AUTO_TRADER);

        private static void bootstrap() {
        }
    }

    public static class Tags {
        public static final TagKey<Block> OPENS_SHOP_SELECTOR = BlockTags.create(loc("opens_shop_selector"));

        private static void bootstrap() {
        }
    }

    public static class Attachments {
        public static final Supplier<AttachmentType<TradeLimit>> TRADE_LIMITS = ATTACHMENT_TYPES.register("trade_limits", () -> AttachmentType.serializable(TradeLimit::new).copyOnDeath().build());

        private static void bootstrap() {
        }
    }

    public static class Menus {
        public static final Supplier<MenuType<ShopMenu>> SHOP_MENU = MENUS.register("shop_menu", () -> IMenuTypeExtension.create((windowId, playerInventory, data) -> new ShopMenu(ModElements.Menus.SHOP_MENU.get(), windowId, playerInventory, data.readUtf(), data.readUUID(), data.readUtf())));
        public static final Supplier<MenuType<ImageShopMenu>> IMAGE_SHOP_MENU = MENUS.register("image_shop_menu", () -> IMenuTypeExtension.create((windowId, playerInventory, data) -> new ImageShopMenu(windowId, playerInventory, data.readUtf(), data.readUUID(), data.readUtf())));
        public static final Supplier<MenuType<ThinShopMenu>> THIN_SHOP_MENU = MENUS.register("thin_shop_menu", () -> IMenuTypeExtension.create((windowId, playerInventory, data) -> new ThinShopMenu(windowId, playerInventory, data.readUtf(), data.readUUID(), data.readUtf())));
        public static final Supplier<MenuType<SelectorMenu>> SELECTOR_MENU = MENUS.register("selector_menu", () -> IMenuTypeExtension.create((windowId, playerInventory, data) -> new SelectorMenu(windowId, playerInventory, data.readUtf())));

        public static final Supplier<MenuType<AutoTraderMenu>> AUTO_TRADER_MENU = MENUS.register("auto_trader_menu", () -> IMenuTypeExtension.create(AutoTraderMenu::new));
        public static final Supplier<MenuType<AutoTraderSelectorMenu>> AUTO_TRADER_SELECTOR_MENU = MENUS.register("auto_trader_selector_menu", () -> IMenuTypeExtension.create(AutoTraderSelectorMenu::new));

        private static void bootstrap() {
        }
    }

    public static class Tabs {
        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, loc("tab"));

        public static final Holder<CreativeModeTab> TAB = R.creativeTab("tab", b -> b.title(Component.translatable("itemGroup." + SocietyTrading.MODID)).icon(() -> Items.AUTO_TRADER.value().getDefaultInstance()));

        private static void bootstrap() {
        }
    }

    public static void bootstrap(IEventBus bus) {
        bus.register(R);
        // I don't fucking know
        ATTACHMENT_TYPES.register(bus);
        MENUS.register(bus);
        Blocks.bootstrap();
        BlockEntities.bootstrap();
        Items.bootstrap();
        Tags.bootstrap();
        Attachments.bootstrap();
        Menus.bootstrap();
        Tabs.bootstrap();
    }
}