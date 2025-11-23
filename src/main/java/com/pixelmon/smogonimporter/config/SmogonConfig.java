package com.pixelmon.smogonimporter.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class SmogonConfig {
    public static final ModConfigSpec SPEC;
    public static final SmogonConfig CONFIG;

    // Generation selection (ONLY gen8 or gen9 allowed)
    public static ModConfigSpec.ConfigValue<String> GENERATION;

    // Cache settings
    public static ModConfigSpec.BooleanValue USE_CACHE;
    public static ModConfigSpec.IntValue CACHE_DURATION_HOURS;
    public static ModConfigSpec.BooleanValue AUTO_UPDATE;
    public static ModConfigSpec.IntValue UPDATE_INTERVAL_HOURS;

    // Performance settings
    public static ModConfigSpec.IntValue CONNECTION_TIMEOUT_SECONDS;
    public static ModConfigSpec.IntValue READ_TIMEOUT_SECONDS;
    public static ModConfigSpec.IntValue MAX_RETRIES;

    // Features
    public static ModConfigSpec.BooleanValue ENABLE_COMMANDS;
    public static ModConfigSpec.BooleanValue DEBUG_MODE;
    public static ModConfigSpec.BooleanValue ALLOW_CUSTOM_SETS;

    static {
        final Pair<SmogonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder()
                .configure(SmogonConfig::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    private SmogonConfig(ModConfigSpec.Builder builder) {
        builder.comment("Smogon Importer Configuration")
                .push("data_sources");

        GENERATION = builder
                .comment("Pokemon generation to use",
                         "ONLY gen8 or gen9 allowed!",
                         "",
                         "gen8 = Uses Gen8 random battles data with dynamic competitive set generation",
                         "       Source: github.com/smogon/pokemon-showdown/data/random-battles/gen8/data.json",
                         "gen9 = Uses Gen9 pre-built competitive sets from random battles",
                         "       Source: github.com/smogon/pokemon-showdown/data/random-battles/gen9/sets.json",
                         "",
                         "Note: URLs are HARDCODED in code - do not modify!",
                         "Note: Gen9 Tera types are read but not used (Pixelmon doesn't support them yet)")
                .define("generation", "gen8",
                        obj -> obj != null && (obj.equals("gen8") || obj.equals("gen9")));

        builder.pop();

        builder.push("cache");

        USE_CACHE = builder
                .comment("Enable local caching of Pokemon data")
                .define("use_cache", true);

        CACHE_DURATION_HOURS = builder
                .comment("How long to keep cached data before refreshing (in hours)")
                .defineInRange("cache_duration", 24, 1, 168);

        AUTO_UPDATE = builder
                .comment("Automatically update data periodically")
                .define("auto_update", true);

        UPDATE_INTERVAL_HOURS = builder
                .comment("How often to check for updates (in hours)")
                .defineInRange("update_interval", 6, 1, 24);

        builder.pop();

        builder.push("performance");

        CONNECTION_TIMEOUT_SECONDS = builder
                .comment("Connection timeout for web requests (in seconds)")
                .defineInRange("connection_timeout", 10, 1, 60);

        READ_TIMEOUT_SECONDS = builder
                .comment("Read timeout for web requests (in seconds)")
                .defineInRange("read_timeout", 30, 1, 120);

        MAX_RETRIES = builder
                .comment("Maximum number of retries for failed requests")
                .defineInRange("max_retries", 3, 0, 10);

        builder.pop();

        builder.push("features");

        ENABLE_COMMANDS = builder
                .comment("Enable admin commands for managing the data")
                .define("enable_commands", true);

        DEBUG_MODE = builder
                .comment("Enable debug logging")
                .define("debug_mode", false);

        ALLOW_CUSTOM_SETS = builder
                .comment("Allow adding custom Pokemon sets to the database")
                .define("allow_custom_sets", true);

        builder.pop();
    }

    // ==================== HARDCODED DATA URLS ====================
    // URLs are NOT configurable to prevent incompatible data sources

    // Base GitHub URL for Smogon Pokemon Showdown repository
    private static final String GITHUB_BASE = "https://raw.githubusercontent.com/smogon/pokemon-showdown/master/data";

    /**
     * Get primary data URL for the configured generation
     * HARDCODED - Gen8 vs Gen9 use different endpoints
     */
    public static String getPrimaryDataURL() {
        String gen = GENERATION.get();

        if ("gen9".equals(gen)) {
            // Gen9 uses sets.json (pre-built competitive sets)
            return GITHUB_BASE + "/random-battles/gen9/sets.json";
        } else {
            // Gen8 uses data.json (for dynamic generation)
            return GITHUB_BASE + "/random-battles/gen8/data.json";
        }
    }

    /**
     * Get backup URLs for the configured generation
     * HARDCODED - Different backups per generation via CDN
     */
    public static java.util.List<String> getBackupDataURLs() {
        String gen = GENERATION.get();
        String cdnBase = "https://cdn.jsdelivr.net/gh/smogon/pokemon-showdown@master/data";

        if ("gen9".equals(gen)) {
            // Gen9 backups - sets.json from CDN
            return java.util.Arrays.asList(
                cdnBase + "/random-battles/gen9/sets.json"
            );
        } else {
            // Gen8 backups - data.json from CDN
            return java.util.Arrays.asList(
                cdnBase + "/random-battles/gen8/data.json"
            );
        }
    }

    /**
     * Get moves data URL (TypeScript file)
     * Contains move names, types, categories, power, accuracy, etc.
     */
    public static String getMovesDataURL() {
        return GITHUB_BASE + "/moves.ts";
    }

    /**
     * Get abilities data URL (TypeScript file)
     * Contains ability names, ratings, and effects
     */
    public static String getAbilitiesDataURL() {
        return GITHUB_BASE + "/abilities.ts";
    }

    /**
     * Get items data URL (TypeScript file)
     * Contains item names, categories, and effects
     */
    public static String getItemsDataURL() {
        return GITHUB_BASE + "/items.ts";
    }

    /**
     * Get pokedex data URL (TypeScript file)
     * Contains Pokemon species data: types, base stats, abilities, etc.
     */
    public static String getPokedexDataURL() {
        return GITHUB_BASE + "/pokedex.ts";
    }

    /**
     * Get type chart data URL (TypeScript file)
     * Contains type effectiveness and list of all Pokemon types
     */
    public static String getTypeChartDataURL() {
        return GITHUB_BASE + "/typechart.ts";
    }

    /**
     * Get natures data URL (TypeScript file)
     * Contains nature stat modifications
     */
    public static String getNaturesDataURL() {
        return GITHUB_BASE + "/natures.ts";
    }

    /**
     * Get rulesets data URL (TypeScript file)
     * Contains battle format rules and restrictions
     */
    public static String getRulesetsDataURL() {
        return GITHUB_BASE + "/rulesets.ts";
    }

    /**
     * Get tags data URL (TypeScript file)
     * Contains Pokemon tags and categories
     */
    public static String getTagsDataURL() {
        return GITHUB_BASE + "/tags.ts";
    }

    /**
     * Get teams helper file URL for the configured generation
     * Contains team building logic and strategies
     */
    public static String getTeamsDataURL() {
        String gen = GENERATION.get();
        return GITHUB_BASE + "/random-battles/" + gen + "/teams.ts";
    }

    /**
     * Check if current generation is Gen9
     */
    public static boolean isGen9() {
        return "gen9".equals(GENERATION.get());
    }

    /**
     * Check if current generation is Gen8
     */
    public static boolean isGen8() {
        return "gen8".equals(GENERATION.get());
    }
}