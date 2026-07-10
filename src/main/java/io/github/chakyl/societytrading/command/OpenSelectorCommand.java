package io.github.chakyl.societytrading.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.chakyl.societytrading.data.CustomSelectorRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import static io.github.chakyl.societytrading.util.GeneralUtils.openSelectorMenu;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceLocation;

public class OpenSelectorCommand {
    private static final SuggestionProvider<CommandSourceStack> SELECTOR_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
            CustomSelectorRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::getPath),
            builder
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("openselector")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            openSelectorMenu(EntityArgument.getPlayer(context, "player"), "");
                            return 1;
                        })
                        .then(Commands.argument("custom_selector", StringArgumentType.string())
                                .suggests(SELECTOR_SUGGESTIONS)
                                .executes(context -> {
                                    openSelectorMenu(EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "custom_selector"));
                                    return 1;
                                })
                        )
                )
        );
    }
}