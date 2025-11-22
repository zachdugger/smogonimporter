package com.pixelmon.smogonimporter.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class SmogonConfig {
    public static final ModConfigSpec SPEC;
    public static final SmogonConfig CONFIG;

    // Data URLs
    public static ModConfigSpec.ConfigValue<String> PRIMARY_DATA_URL;
    public static ModConfigSpec.ConfigValue<List<? extends String>> BACKUP_DATA_URLS;
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

        PRIMARY_DATA_URL = builder
                .comment("Primary URL for fetching Pokemon data")
                .define("primary_url", "https://data.pkmn.cc/randbats/gen8randombattle.json");

        BACKUP_DATA_URLS = builder
                .comment("Backup URLs to try if primary fails")
                .define("backup_urls",
                        java.util.Arrays.asList(
                                "https://raw.githubusercontent.com/pkmn/randbats/main/data/gen8randombattle.json"
                        ),
                        obj -> obj instanceof String);

        GENERATION = builder
                .comment("Pokemon generation to use (gen1-gen9)")
                .define("generation", "gen8");

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
}