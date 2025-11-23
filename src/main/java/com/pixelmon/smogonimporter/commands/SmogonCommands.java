package com.pixelmon.smogonimporter.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.api.SmogonAPI;
import com.pixelmon.smogonimporter.config.SmogonConfig;
import com.pixelmon.smogonimporter.data.PokemonData;
import com.pixelmon.smogonimporter.logic.SetGenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SmogonCommands {

    /**
     * Dynamic suggestion provider for Pokemon names
     * Automatically updates based on loaded generation (Gen8 or Gen9)
     */
    private static final SuggestionProvider<CommandSourceStack> POKEMON_SUGGESTIONS = (context, builder) -> {
        SmogonAPI api = SmogonImporter.getAPI();
        if (api.isReady()) {
            List<String> pokemonNames = api.getAllPokemonNames();
            return SharedSuggestionProvider.suggest(pokemonNames, builder);
        }
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!SmogonConfig.ENABLE_COMMANDS.get()) {
            return;
        }

        dispatcher.register(Commands.literal("smogon")
                .requires(source -> source.hasPermission(2)) // Requires OP

                // Reload data command
                .then(Commands.literal("reload")
                        .executes(context -> {
                            Component message = Component.literal("Reloading Smogon data...")
                                    .withStyle(ChatFormatting.YELLOW);
                            context.getSource().sendSuccess(() -> message, true);

                            SmogonImporter.getAPI().refreshData().thenAccept(success -> {
                                if (success) {
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Successfully reloaded Smogon data!")
                                                    .withStyle(ChatFormatting.GREEN), true);
                                } else {
                                    context.getSource().sendFailure(
                                            Component.literal("Failed to reload Smogon data!")
                                                    .withStyle(ChatFormatting.RED));
                                }
                            });

                            return 1;
                        }))

                // Get Pokemon info command
                .then(Commands.literal("get")
                        .then(Commands.argument("pokemon", StringArgumentType.string())
                                .suggests(POKEMON_SUGGESTIONS)
                                .executes(context -> {
                                    String pokemonName = StringArgumentType.getString(context, "pokemon");
                                    SmogonAPI api = SmogonImporter.getAPI();

                                    Optional<PokemonData> pokemonOpt = api.getPokemon(pokemonName);

                                    if (pokemonOpt.isPresent()) {
                                        PokemonData pokemon = pokemonOpt.get();

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("=== " + pokemon.getName() + " ===")
                                                        .withStyle(ChatFormatting.GOLD), false);

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Level: " + pokemon.getLevel())
                                                        .withStyle(ChatFormatting.AQUA), false);

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Abilities: " + String.join(", ", pokemon.getAbilities()))
                                                        .withStyle(ChatFormatting.GREEN), false);

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Items: " + String.join(", ", pokemon.getItems()))
                                                        .withStyle(ChatFormatting.YELLOW), false);

                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Moves: " + String.join(", ", pokemon.getMoves()))
                                                        .withStyle(ChatFormatting.LIGHT_PURPLE), false);

                                        if (pokemon.getEvs() != null && !pokemon.getEvs().isEmpty()) {
                                            StringBuilder evString = new StringBuilder("EVs: ");
                                            pokemon.getEvs().forEach((stat, value) ->
                                                    evString.append(stat).append(": ").append(value).append(" "));

                                            context.getSource().sendSuccess(() ->
                                                    Component.literal(evString.toString())
                                                            .withStyle(ChatFormatting.GRAY), false);
                                        }
                                    } else {
                                        context.getSource().sendFailure(
                                                Component.literal("Pokemon '" + pokemonName + "' not found!")
                                                        .withStyle(ChatFormatting.RED));
                                    }

                                    return 1;
                                })))

                // Random Pokemon command
                .then(Commands.literal("random")
                        .executes(context -> {
                            return executeRandom(context, 1);
                        })
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 10))
                                .executes(context -> {
                                    int count = IntegerArgumentType.getInteger(context, "count");
                                    return executeRandom(context, count);
                                })))

                // Generate competitive set command
                .then(Commands.literal("generate")
                        .then(Commands.argument("pokemon", StringArgumentType.string())
                                .suggests(POKEMON_SUGGESTIONS)
                                .executes(context -> {
                                    String pokemonName = StringArgumentType.getString(context, "pokemon");
                                    SmogonAPI api = SmogonImporter.getAPI();

                                    if (!api.isReady()) {
                                        context.getSource().sendFailure(
                                                Component.literal("Smogon data not yet loaded!")
                                                        .withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    // For now, use Normal type as placeholder
                                    // TODO: Get actual types from Pixelmon
                                    java.util.List<String> types = java.util.Arrays.asList("Normal");

                                    Optional<SetGenerator.RandomSet> setOpt = api.generateCompetitiveSet(pokemonName, types);

                                    if (setOpt.isEmpty()) {
                                        context.getSource().sendFailure(
                                                Component.literal("Failed to generate competitive set for " + pokemonName)
                                                        .withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    SetGenerator.RandomSet set = setOpt.get();

                                    // Display the competitive set
                                    context.getSource().sendSuccess(() ->
                                            Component.literal("=== COMPETITIVE SET: " + set.getSpecies() + " ===")
                                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

                                    // Show role if Gen9
                                    if (set.hasRole()) {
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("Role: " + set.getRole())
                                                        .withStyle(ChatFormatting.DARK_PURPLE), false);
                                    }

                                    context.getSource().sendSuccess(() ->
                                            Component.literal(set.getSpecies() + " @ " + set.getItem())
                                                    .withStyle(ChatFormatting.YELLOW), false);

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Ability: " + set.getAbility())
                                                    .withStyle(ChatFormatting.GREEN), false);

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Nature: " + set.getNature())
                                                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Level: " + set.getLevel())
                                                    .withStyle(ChatFormatting.AQUA), false);

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("Moves:")
                                                    .withStyle(ChatFormatting.WHITE), false);
                                    for (String move : set.getMoves()) {
                                        context.getSource().sendSuccess(() ->
                                                Component.literal("  - " + move)
                                                        .withStyle(ChatFormatting.GRAY), false);
                                    }

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("EVs: " + set.getEvs())
                                                    .withStyle(ChatFormatting.BLUE), false);

                                    context.getSource().sendSuccess(() ->
                                            Component.literal("IVs: " + set.getIvs())
                                                    .withStyle(ChatFormatting.DARK_AQUA), false);

                                    return 1;
                                })))

                // Statistics command
                .then(Commands.literal("stats")
                        .executes(context -> {
                            SmogonAPI api = SmogonImporter.getAPI();
                            Map<String, Object> stats = api.getStatistics();

                            context.getSource().sendSuccess(() ->
                                    Component.literal("=== Smogon Importer Statistics ===")
                                            .withStyle(ChatFormatting.GOLD), false);

                            stats.forEach((key, value) -> {
                                context.getSource().sendSuccess(() ->
                                        Component.literal(key + ": " + value)
                                                .withStyle(ChatFormatting.GRAY), false);
                            });

                            return 1;
                        }))

                // List command (shows first 10 Pokemon names)
                .then(Commands.literal("list")
                        .executes(context -> {
                            SmogonAPI api = SmogonImporter.getAPI();
                            List<String> names = api.getAllPokemonNames();

                            context.getSource().sendSuccess(() ->
                                    Component.literal("=== Available Pokemon (" + names.size() + " total) ===")
                                            .withStyle(ChatFormatting.GOLD), false);

                            names.stream().limit(10).forEach(name -> {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("- " + name)
                                                .withStyle(ChatFormatting.GRAY), false);
                            });

                            if (names.size() > 10) {
                                context.getSource().sendSuccess(() ->
                                        Component.literal("... and " + (names.size() - 10) + " more")
                                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
                            }

                            return 1;
                        }))
        );
    }

    private static int executeRandom(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, int count) {
        SmogonAPI api = SmogonImporter.getAPI();

        if (!api.isReady()) {
            context.getSource().sendFailure(
                    Component.literal("Smogon data not yet loaded!")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        List<PokemonData> randomPokemon = api.getRandomPokemon(count);

        if (randomPokemon.isEmpty()) {
            context.getSource().sendFailure(
                    Component.literal("No Pokemon available!")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        context.getSource().sendSuccess(() ->
                Component.literal("=== Random Pokemon ===")
                        .withStyle(ChatFormatting.GOLD), false);

        for (PokemonData pokemon : randomPokemon) {
            context.getSource().sendSuccess(() ->
                    Component.literal("- " + pokemon.getName() + " (Level " + pokemon.getLevel() + ")")
                            .withStyle(ChatFormatting.AQUA), false);
        }

        return 1;
    }
}