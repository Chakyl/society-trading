package io.github.chakyl.societytrading.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.chakyl.societytrading.screen.ImageShopMenu;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

public class OpenSelectorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("openselector")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            EntityArgument.getPlayer(context, "player").openMenu(new SimpleMenuProvider((containerId, inventory, nPlayer) -> new SelectorMenu(containerId, inventory), Component.translatable("shop.society_trading.selector.name")));
                            return 1;
                        })
                        .then(Commands.argument("custom_selector", StringArgumentType.string())
                                .executes(context -> {
                                    String customSelector = StringArgumentType.getString(context, "custom_selector");
                                    NetworkHooks.openScreen(EntityArgument.getPlayer(context, "player"), new SimpleMenuProvider((containerId, inventory, nPlayer) -> new SelectorMenu(containerId, inventory, customSelector), Component.translatable("shop.society_trading.selector.name")), buffer -> {
                                        buffer.writeUtf(customSelector);
                                    });
                                    return 1;
                                })
                        )
                )
        );
    }
}
