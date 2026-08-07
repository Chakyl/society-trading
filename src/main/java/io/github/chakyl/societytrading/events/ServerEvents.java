package io.github.chakyl.societytrading.events;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.command.OpenSelectorCommand;
import io.github.chakyl.societytrading.command.OpenShopCommand;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.registry.ModElements;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import io.github.chakyl.societytrading.util.ShopData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.UUID;

import static io.github.chakyl.societytrading.util.GeneralUtils.nameTagEntity;
import static io.github.chakyl.societytrading.util.GeneralUtils.openShopMenu;

@EventBusSubscriber(modid = SocietyTrading.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity player = event.getEntity();
        Entity target = event.getTarget();
        if (player instanceof ServerPlayer serverPlayer && target instanceof LivingEntity livingTarget) {
            Shop shop = ShopData.getShopFromEntity(ShopRegistry.INSTANCE.getValues(), livingTarget);
            if (shop != null) {
                if (event.getItemStack().getItem() instanceof NameTagItem) {
                    nameTagEntity(event.getItemStack(), serverPlayer, livingTarget);
                }
                openShopMenu(shop, serverPlayer, shop.shopID(), target.getUUID(), "");
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        Entity player = event.getEntity();
        Entity target = event.getTarget();
        if (player instanceof ServerPlayer serverPlayer && target instanceof Villager villager) {
            VillagerData data = villager.getVillagerData();
            Shop shop = ShopData.getShopFromVillagerProfession(ShopRegistry.INSTANCE.getValues(), data.getProfession().name());
            if (shop != null) {
                if (event.getItemStack().getItem() instanceof NameTagItem) {
                    nameTagEntity(event.getItemStack(), serverPlayer, villager);
                }
                openShopMenu(shop, serverPlayer, shop.shopID(), villager.getUUID(), "");
                int experience = villager.getVillagerXp();
                villager.setVillagerXp(experience > 0 ? experience : 1);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Entity player = event.getEntity();
        BlockState clickedBlock = event.getLevel().getBlockState(event.getPos());
        if (player instanceof ServerPlayer serverPlayer && !player.level().isClientSide) {
            if (clickedBlock.is(ModElements.Tags.OPENS_SHOP_SELECTOR)) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (containerId, inventory, nPlayer) -> new SelectorMenu(containerId, inventory, ""),
                                Component.translatable("shop.society_trading.selector.name")
                        ),
                        buffer -> buffer.writeUtf("")
                );

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            Shop shop = ShopData.getShopFromBlockState(ShopRegistry.INSTANCE.getValues(), clickedBlock);
            if (shop != null) {
                openShopMenu(shop, serverPlayer, shop.shopID(), UUID.randomUUID(), "");
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        OpenShopCommand.register(event.getDispatcher());
        OpenSelectorCommand.register(event.getDispatcher());
    }
}