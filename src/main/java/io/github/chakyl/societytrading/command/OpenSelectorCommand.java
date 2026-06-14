package io.github.chakyl.societytrading.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import static io.github.chakyl.societytrading.util.GeneralUtils.openSelectorMenu;

public class OpenSelectorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("openselector")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            openSelectorMenu(EntityArgument.getPlayer(context, "player"), "");
                            return 1;
                        })
                        .then(Commands.argument("custom_selector", StringArgumentType.string())
                                .executes(context -> {
                                    openSelectorMenu(EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "custom_selector"));
                                    return 1;
                                })
                        )
                )
        );
    }
}
