package com.pixelmon.smogonimporter.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.config.SmogonConfig;
import com.pixelmon.smogonimporter.data.PokemonData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DataManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Map<String, PokemonData> pokemonDatabase = new ConcurrentHashMap<>();
    private final List<String> pokemonNames = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Path cacheDir;
    private final Path cacheFile;

    private volatile boolean initialized = false;
    private volatile long lastUpdateTime = 0;

    // Central registry for moves, items, abilities, pokedex
    private final SmogonDataRegistry dataRegistry = new SmogonDataRegistry();

    public DataManager() {
        this.cacheDir = FMLPaths.CONFIGDIR.get().resolve("smogonimporter");
        this.cacheFile = cacheDir.resolve("pokemon_cache.json");

        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            SmogonImporter.LOGGER.error("Failed to create cache directory", e);
        }
    }

    /**
     * Initialize the data manager by loading cached data or fetching fresh data
     */
    public void initialize() {
        if (initialized) {
            return;
        }

        SmogonImporter.LOGGER.info("Initializing Smogon data manager...");

        // Gen9 mode: Always fetch from web (cache doesn't store Gen9SetSelector data)
        // Gen8 mode: Use cache if available
        boolean canUseCache = SmogonConfig.USE_CACHE.get() && !SmogonConfig.isGen9();

        // Try to load from cache first
        if (canUseCache && loadFromCache()) {
            SmogonImporter.LOGGER.info("Loaded {} Pokemon from cache", pokemonDatabase.size());

            // Check if cache needs updating
            long cacheAge = System.currentTimeMillis() - lastUpdateTime;
            long maxAge = SmogonConfig.CACHE_DURATION_HOURS.get() * 3600000L;

            if (cacheAge > maxAge) {
                SmogonImporter.LOGGER.info("Cache is outdated, scheduling update...");
                scheduleDataUpdate();
            }
        } else {
            // No valid cache, fetch fresh data
            if (SmogonConfig.isGen9()) {
                SmogonImporter.LOGGER.info("Gen9 mode: Fetching data from web (cache not supported for Gen9)");
            }
            fetchDataFromWeb();
        }

        // Initialize comprehensive data registry (moves, items, abilities, pokedex)
        SmogonImporter.LOGGER.info("Loading comprehensive Smogon data (moves, items, abilities, pokedex)...");
        dataRegistry.initialize();

        initialized = true;
    }

    /**
     * Get the data registry (for moves, items, abilities, pokedex)
     */
    public SmogonDataRegistry getDataRegistry() {
        return dataRegistry;
    }

    /**
     * Fetch Pokemon data from the web
     */
    public CompletableFuture<Boolean> fetchDataFromWeb() {
        return CompletableFuture.supplyAsync(() -> {
            String generation = SmogonConfig.GENERATION.get();
            SmogonImporter.LOGGER.info("Fetching {} Pokemon data from web...", generation.toUpperCase());

            String dataUrl = SmogonConfig.getPrimaryDataURL();
            int retries = 0;

            while (retries <= SmogonConfig.MAX_RETRIES.get()) {
                try {
                    String jsonData = fetchFromUrl(dataUrl);
                    if (jsonData != null) {
                        parseAndStoreData(jsonData);
                        saveCache();
                        SmogonImporter.LOGGER.info("Successfully fetched and stored {} Pokemon", pokemonDatabase.size());
                        return true;
                    }
                } catch (Exception e) {
                    SmogonImporter.LOGGER.warn("Failed to fetch from primary URL, attempt {}/{}",
                            retries + 1, SmogonConfig.MAX_RETRIES.get(), e);
                }

                retries++;

                // Try backup URLs
                if (retries <= SmogonConfig.MAX_RETRIES.get()) {
                    List<String> backupUrls = SmogonConfig.getBackupDataURLs();
                    for (String backupUrl : backupUrls) {
                        try {
                            String jsonData = fetchFromUrl(backupUrl);
                            if (jsonData != null) {
                                parseAndStoreData(jsonData);
                                saveCache();
                                SmogonImporter.LOGGER.info("Successfully fetched from backup URL");
                                return true;
                            }
                        } catch (Exception e) {
                            SmogonImporter.LOGGER.warn("Failed to fetch from backup URL: {}", backupUrl, e);
                        }
                    }
                }
            }

            SmogonImporter.LOGGER.error("Failed to fetch Pokemon data after all retries");
            return false;
        });
    }

    /**
     * Fetch data from a specific URL
     */
    private String fetchFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "SmogonImporter/1.0");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(SmogonConfig.CONNECTION_TIMEOUT_SECONDS.get() * 1000);
            connection.setReadTimeout(SmogonConfig.READ_TIMEOUT_SECONDS.get() * 1000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Parse JSON data and store in database
     * Branches based on generation (gen8 vs gen9)
     */
    private void parseAndStoreData(String jsonData) {
        String generation = SmogonConfig.GENERATION.get();

        if (SmogonConfig.isGen9()) {
            SmogonImporter.LOGGER.info("Parsing Gen9 sets.json...");
            parseGen9Data(jsonData);
        } else {
            SmogonImporter.LOGGER.info("Parsing Gen8 data.json...");
            parseGen8Data(jsonData);
        }

        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Parse Gen8 data.json format from GitHub
     * Format: { "species": { "level": 84, "moves": [...] } }
     * Uses dynamic generation - stores base Pokemon with movepool
     * Abilities are populated from pokedex data
     */
    private void parseGen8Data(String jsonData) {
        pokemonDatabase.clear();
        pokemonNames.clear();

        JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();
        String generation = SmogonConfig.GENERATION.get();

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String pokemonName = entry.getKey();
            JsonObject pokemonJson = entry.getValue().getAsJsonObject();

            PokemonData pokemon = GSON.fromJson(pokemonJson, PokemonData.class);
            pokemon.setName(pokemonName);
            pokemon.setGeneration(generation);

            // Gen8 data doesn't include abilities - get them from pokedex
            if (pokemon.getAbilities() == null || pokemon.getAbilities().isEmpty()) {
                PokedexEntry pokedexEntry = dataRegistry.getPokedexEntry(pokemonName);
                if (pokedexEntry != null && pokedexEntry.getAbilities() != null) {
                    List<String> abilities = pokedexEntry.getAbilities().getAllAbilities();
                    if (!abilities.isEmpty()) {
                        pokemon.setAbilities(abilities);
                    } else {
                        // Fallback to avoid null pointer
                        pokemon.setAbilities(java.util.Collections.singletonList("Unknown"));
                        SmogonImporter.LOGGER.warn("Pokemon '{}' has no abilities in pokedex", pokemonName);
                    }
                } else {
                    // Fallback to avoid null pointer
                    pokemon.setAbilities(java.util.Collections.singletonList("Unknown"));
                    SmogonImporter.LOGGER.warn("Pokemon '{}' not found in pokedex", pokemonName);
                }
            }

            pokemonDatabase.put(pokemonName.toLowerCase(), pokemon);
            pokemonNames.add(pokemonName);
        }

        if (SmogonConfig.DEBUG_MODE.get()) {
            SmogonImporter.LOGGER.debug("Parsed {} Gen8 Pokemon entries", pokemonDatabase.size());
        }
    }

    /**
     * Parse Gen9 sets.json format from GitHub
     * Format: { "species": { "level": 84, "sets": [{"role": "...", "movepool": [...], "abilities": [...]}] } }
     * Creates one PokemonData per role/set - allows role-specific generation
     */
    private void parseGen9Data(String jsonData) {
        pokemonDatabase.clear();
        pokemonNames.clear();

        JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();
        String generation = SmogonConfig.GENERATION.get();
        int totalSetsLoaded = 0;

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String pokemonName = entry.getKey();
            JsonObject pokemonJson = entry.getValue().getAsJsonObject();

            try {
                // Parse the Gen9 format
                int level = pokemonJson.has("level") ? pokemonJson.get("level").getAsInt() : 100;

                if (!pokemonJson.has("sets") || pokemonJson.getAsJsonArray("sets").isEmpty()) {
                    SmogonImporter.LOGGER.warn("Pokemon '{}' has no sets, skipping", pokemonName);
                    continue;
                }

                // Parse ALL sets (not just the first one!)
                JsonArray setsArray = pokemonJson.getAsJsonArray("sets");

                for (JsonElement setElement : setsArray) {
                    if (!setElement.isJsonObject()) continue;

                    JsonObject setData = setElement.getAsJsonObject();

                    // Extract role
                    String role = setData.has("role") ? setData.get("role").getAsString() : "Unknown";

                    // Extract movepool
                    List<String> moves = new ArrayList<>();
                    if (setData.has("movepool")) {
                        JsonArray movepoolArray = setData.getAsJsonArray("movepool");
                        for (JsonElement moveElement : movepoolArray) {
                            moves.add(moveElement.getAsString());
                        }
                    }

                    // Extract abilities
                    List<String> abilities = new ArrayList<>();
                    if (setData.has("abilities")) {
                        JsonArray abilitiesArray = setData.getAsJsonArray("abilities");
                        for (JsonElement abilityElement : abilitiesArray) {
                            abilities.add(abilityElement.getAsString());
                        }
                    }

                    // If no abilities in set, get from pokedex
                    if (abilities.isEmpty()) {
                        PokedexEntry pokedexEntry = dataRegistry.getPokedexEntry(pokemonName);
                        if (pokedexEntry != null && pokedexEntry.getAbilities() != null) {
                            abilities = pokedexEntry.getAbilities().getAllAbilities();
                        }
                    }

                    // Create PokemonData object for this role
                    PokemonData pokemon = new PokemonData();
                    pokemon.setName(pokemonName);
                    pokemon.setGeneration(generation);
                    pokemon.setLevel(level);
                    pokemon.setRole(role);
                    pokemon.setMoves(moves);
                    pokemon.setAbilities(abilities.isEmpty() ?
                        java.util.Collections.singletonList("Unknown") : abilities);

                    // Items will be selected by ItemSelector based on moveset and role
                    pokemon.setItems(null);  // SetGenerator doesn't use this field

                    // Store with composite key: "species|role"
                    String compositeKey = pokemonName.toLowerCase() + "|" + role;
                    pokemonDatabase.put(compositeKey, pokemon);

                    // Also add species name to list (deduplicated)
                    if (!pokemonNames.contains(pokemonName)) {
                        pokemonNames.add(pokemonName);
                    }

                    totalSetsLoaded++;
                }

            } catch (Exception e) {
                SmogonImporter.LOGGER.warn("Failed to parse Gen9 Pokemon '{}': {}", pokemonName, e.getMessage());
            }
        }

        SmogonImporter.LOGGER.info("Loaded {} Gen9 sets for {} unique Pokemon (avg {:.1f} roles per Pokemon)",
            totalSetsLoaded, pokemonNames.size(), (double) totalSetsLoaded / Math.max(1, pokemonNames.size()));

        if (SmogonConfig.DEBUG_MODE.get()) {
            SmogonImporter.LOGGER.debug("Parsed {} Gen9 Pokemon entries with {} total role variations",
                pokemonNames.size(), totalSetsLoaded);
        }
    }

    /**
     * Load data from cache file
     */
    private boolean loadFromCache() {
        if (!Files.exists(cacheFile)) {
            return false;
        }

        try {
            String jsonData = Files.readString(cacheFile, StandardCharsets.UTF_8);
            JsonObject cacheJson = JsonParser.parseString(jsonData).getAsJsonObject();

            lastUpdateTime = cacheJson.get("lastUpdate").getAsLong();
            JsonObject dataJson = cacheJson.getAsJsonObject("data");

            pokemonDatabase.clear();
            pokemonNames.clear();

            Type pokemonType = new TypeToken<PokemonData>(){}.getType();

            for (Map.Entry<String, JsonElement> entry : dataJson.entrySet()) {
                String pokemonName = entry.getKey();
                PokemonData pokemon = GSON.fromJson(entry.getValue(), pokemonType);

                pokemonDatabase.put(pokemonName.toLowerCase(), pokemon);
                pokemonNames.add(pokemon.getName());
            }

            return !pokemonDatabase.isEmpty();
        } catch (Exception e) {
            SmogonImporter.LOGGER.error("Failed to load cache", e);
            return false;
        }
    }

    /**
     * Save current data to cache
     */
    public void saveCache() {
        if (!SmogonConfig.USE_CACHE.get() || pokemonDatabase.isEmpty()) {
            return;
        }

        try {
            JsonObject cacheJson = new JsonObject();
            cacheJson.addProperty("lastUpdate", lastUpdateTime);
            cacheJson.addProperty("generation", SmogonConfig.GENERATION.get());

            JsonObject dataJson = new JsonObject();
            for (Map.Entry<String, PokemonData> entry : pokemonDatabase.entrySet()) {
                dataJson.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
            }
            cacheJson.add("data", dataJson);

            Files.writeString(cacheFile, GSON.toJson(cacheJson),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            SmogonImporter.LOGGER.info("Saved {} Pokemon to cache", pokemonDatabase.size());
        } catch (Exception e) {
            SmogonImporter.LOGGER.error("Failed to save cache", e);
        }
    }

    /**
     * Schedule periodic data updates
     */
    public void scheduleDataUpdate() {
        if (!SmogonConfig.AUTO_UPDATE.get()) {
            return;
        }

        long interval = SmogonConfig.UPDATE_INTERVAL_HOURS.get();

        scheduler.scheduleAtFixedRate(() -> {
            SmogonImporter.LOGGER.info("Running scheduled data update...");
            fetchDataFromWeb();
        }, interval, interval, TimeUnit.HOURS);
    }

    /**
     * Get a Pokemon by name (with fuzzy matching)
     * Handles different spacing/hyphen variations:
     * - "Tapu Lele", "TapuLele", "tapu-lele" all match
     * For Gen9: Returns first role found (use getAllRolesForPokemon for all roles)
     */
    public Optional<PokemonData> getPokemon(String name) {
        // Try exact match first (fast path)
        String lowerName = name.toLowerCase();
        PokemonData exact = pokemonDatabase.get(lowerName);
        if (exact != null) {
            return Optional.of(exact);
        }

        // Try normalized match (remove spaces and hyphens)
        String normalized = normalizeName(name);
        for (Map.Entry<String, PokemonData> entry : pokemonDatabase.entrySet()) {
            String key = entry.getKey();
            // Strip role from key if present (format: "species|role")
            String speciesKey = key.contains("|") ? key.substring(0, key.indexOf("|")) : key;

            if (normalizeName(speciesKey).equals(normalized)) {
                return Optional.of(entry.getValue());
            }
        }

        return Optional.empty();
    }

    /**
     * Get all role variations for a Gen9 Pokemon
     * Returns list of PokemonData, one per role
     * For Gen8, returns single-element list
     */
    public List<PokemonData> getAllRolesForPokemon(String name) {
        List<PokemonData> roles = new ArrayList<>();
        String normalized = normalizeName(name.toLowerCase());

        // Search for all entries matching this species
        for (Map.Entry<String, PokemonData> entry : pokemonDatabase.entrySet()) {
            String key = entry.getKey();
            // Strip role from key if present (format: "species|role")
            String speciesKey = key.contains("|") ? key.substring(0, key.indexOf("|")) : key;

            if (normalizeName(speciesKey).equals(normalized)) {
                roles.add(entry.getValue());
            }
        }

        return roles;
    }

    /**
     * Normalizes a Pokemon name for fuzzy matching
     * Removes spaces, hyphens, and apostrophes, converts to lowercase
     */
    private String normalizeName(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "");
    }

    /**
     * Get random Pokemon
     */
    public List<PokemonData> getRandomPokemon(int count) {
        if (pokemonNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> shuffled = new ArrayList<>(pokemonNames);
        Collections.shuffle(shuffled);

        return shuffled.stream()
                .limit(count)
                .map(name -> pokemonDatabase.get(name.toLowerCase()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get all Pokemon names
     */
    public List<String> getAllPokemonNames() {
        return new ArrayList<>(pokemonNames);
    }

    /**
     * Get all Pokemon data
     */
    public Collection<PokemonData> getAllPokemon() {
        return new ArrayList<>(pokemonDatabase.values());
    }

    /**
     * Add custom Pokemon data
     */
    public void addCustomPokemon(PokemonData pokemon) {
        if (!SmogonConfig.ALLOW_CUSTOM_SETS.get()) {
            throw new IllegalStateException("Custom sets are disabled in config");
        }

        pokemonDatabase.put(pokemon.getName().toLowerCase(), pokemon);
        if (!pokemonNames.contains(pokemon.getName())) {
            pokemonNames.add(pokemon.getName());
        }
    }

    /**
     * Check if data is loaded
     */
    public boolean isInitialized() {
        return initialized && !pokemonDatabase.isEmpty();
    }

    /**
     * Get data statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPokemon", pokemonDatabase.size());
        stats.put("lastUpdate", new Date(lastUpdateTime));
        stats.put("cacheEnabled", SmogonConfig.USE_CACHE.get());
        stats.put("generation", SmogonConfig.GENERATION.get());

        return stats;
    }

    /**
     * Shutdown the data manager
     */
    public void shutdown() {
        scheduler.shutdown();
        saveCache();
    }
}