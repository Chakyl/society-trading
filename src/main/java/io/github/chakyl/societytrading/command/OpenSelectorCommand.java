package io.github.chakyl.societytrading.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.chakyl.societytrading.screen.SelectorMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.OptionalInt;

public class OpenSelectorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("openselector")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
                            OptionalInt optionalint = targetPlayer.openMenu(new SimpleMenuProvider((containerId, inventory, nPlayer) -> new SelectorMenu(containerId, inventory), Component.translatable("shop.society_trading.selector.name")));
                            return 1;
                        })
                )
        );
    }
}
