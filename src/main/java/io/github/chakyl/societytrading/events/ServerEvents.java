package io.github.chakyl.societytrading.events;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import io.github.chakyl.societytrading.screen.ShopMenu;
import io.github.chakyl.societytrading.util.ShopData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

import java.util.OptionalInt;

import static io.github.chakyl.societytrading.util.GeneralUtils.nameTagEntity;

public class ServerEvents {
    @Mod.EventBusSubscriber(modid = SocietyTrading.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onEntityInteract(EntityInteract event) {
            Entity player = event.getEntity();
            Entity target = event.getTarget();
            if (player instanceof ServerPlayer && target instanceof LivingEntity) {
                Shop shop = ShopData.getShopFromEntity(ShopRegistry.INSTANCE.getValues(), (LivingEntity) target);
                if (shop != null) {
                    if (event.getItemStack().getItem() instanceof NameTagItem) {
                        nameTagEntity(event.getItemStack(), (Player) player, (LivingEntity) target);
                    }
                    NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ShopMenu(containerId, inventory, shop.shopID()), shop.name()), buffer -> {
                        buffer.writeUtf(shop.shopID());
                    });
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onVillagerInteract(EntityInteract event) {
            Entity player = event.getEntity();
            Entity villager = event.getTarget();
            if (player instanceof ServerPlayer && villager instanceof Villager) {
                VillagerData data = ((Villager) villager).getVillagerData();
                Shop shop = ShopData.getShopFromVillagerProfession(ShopRegistry.INSTANCE.getValues(), data.getProfession().name());
                if (shop != null) {
                    if (event.getItemStack().getItem() instanceof NameTagItem) {
                        nameTagEntity(event.getItemStack(), (Player) player, (LivingEntity) villager);
                    }
                    NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ShopMenu(containerId, inventory, shop.shopID()), shop.name()), buffer -> {
                        buffer.writeUtf(shop.shopID());
                    });
                    int experience = ((Villager) villager).getVillagerXp();
                    ((Villager) villager).setVillagerXp(experience > 0 ? experience : 1);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onBlockInteract(RightClickBlock event) {
            Entity player = event.getEntity();
            BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());
            if (player instanceof ServerPlayer && !player.level().isClientSide) {
                if (clickedBlock.is(ModElements.Tags.OPENS_SHOP_SELECTOR)) {
                    OptionalInt optionalint = ((ServerPlayer) player).openMenu(new SimpleMenuProvider((containerId, inventory, nPlayer) -> new SelectorMenu(containerId, inventory), Component.translatable("shop.society_trading.selector.name")));
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
                Shop shop = ShopData.getShopFromBlockState(ShopRegistry.INSTANCE.getValues(), clickedBlock);
                if (shop != null) {
                    NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((containerId, inventory, nPlayer) -> new ShopMenu(containerId, inventory, shop.shopID()), shop.name()), buffer -> {
                        buffer.writeUtf(shop.shopID());
                    });
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }
}