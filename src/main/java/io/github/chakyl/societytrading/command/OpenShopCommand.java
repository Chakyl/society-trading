package io.github.chakyl.societytrading.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import static io.github.chakyl.societytrading.util.GeneralUtils.openShopMenu;

public class OpenShopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("openshop")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("shop_id", StringArgumentType.string())
                                .executes(context -> {
                                    ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
                                    String commandShopId = StringArgumentType.getString(context, "shop_id");
                                    DynamicHolder<Shop> shop = ShopRegistry.INSTANCE.holder(new ResourceLocation("society_trading:" + commandShopId));
                                    if (shop.isBound()) {
                                        UUID randomUUID = UUID.randomUUID();
                                        openShopMenu(shop.get(), targetPlayer, commandShopId, randomUUID, "");
                                        return 1;
                                    } else {
                                        context.getSource().sendFailure(Component.translatable("command.society_trading.wrong_id", commandShopId));
                                        return -1;
                                    }
                                })
                        )
                )
        );
    }
}
