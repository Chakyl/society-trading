package io.github.chakyl.societytrading.registry;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.block.ShopBlock;
import io.github.chakyl.societytrading.screen.ShopMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModElements {
    private static final DeferredHelper R = DeferredHelper.create(SocietyTrading.MODID);


    static BlockBehaviour.Properties defaultBehavior = BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.IRON_BLOCK);

    public static class Blocks {

        public static final RegistryObject<Block> SHOP_BLOCK = R.block("shop_block", () -> new ShopBlock(defaultBehavior.strength(4, 3000).noOcclusion()));
        private static void bootstrap() {
        }
    }

    public static class BlockEntities {

        private static void bootstrap() {
        }
    }

    public static class Items {
        public static final RegistryObject<BlockItem> SHOP_BLOCK = R.item("shop_block", () -> new BlockItem(Blocks.SHOP_BLOCK.get(), new Item.Properties()));

        private static void bootstrap() {
        }
    }


    public static class Menus {
        public static final RegistryObject<MenuType<ShopMenu>> SHOP_MENU = R.menu("shop_menu", () -> MenuUtil.type(ShopMenu::new));

        private static void bootstrap() {
        }
    }

    public static class Tabs {
        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(SocietyTrading.MODID, "tab"));

        public static final RegistryObject<CreativeModeTab> AB = R.tab("tab",
                () -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + SocietyTrading.MODID)).icon(() -> Items.SHOP_BLOCK.get().getDefaultInstance()).build());

        private static void bootstrap() {
        }
    }

    public static void bootstrap() {
        Blocks.bootstrap();
        BlockEntities.bootstrap();
        Items.bootstrap();
        Menus.bootstrap();
        Tabs.bootstrap();
    }
}