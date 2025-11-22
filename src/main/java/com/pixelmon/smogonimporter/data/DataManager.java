package com.pixelmon.smogonimporter.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.config.SmogonConfig;
import com.pixelmon.smogonimporter.data.models.PokemonData;
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

        // Try to load from cache first
        if (SmogonConfig.USE_CACHE.get() && loadFromCache()) {
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
            fetchDataFromWeb();
        }

        initialized = true;
    }

    /**
     * Fetch Pokemon data from the web
     */
    public CompletableFuture<Boolean> fetchDataFromWeb() {
        return CompletableFuture.supplyAsync(() -> {
            SmogonImporter.LOGGER.info("Fetching Pokemon data from web...");

            String dataUrl = SmogonConfig.PRIMARY_DATA_URL.get();
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
                    List<String> backupUrls = (List<String>) SmogonConfig.BACKUP_DATA_URLS.get();
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
     */
    private void parseAndStoreData(String jsonData) {
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

            pokemonDatabase.put(pokemonName.toLowerCase(), pokemon);
            pokemonNames.add(pokemonName);
        }

        lastUpdateTime = System.currentTimeMillis();

        if (SmogonConfig.DEBUG_MODE.get()) {
            SmogonImporter.LOGGER.debug("Parsed {} Pokemon entries", pokemonDatabase.size());
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
     * Get a Pokemon by name
     */
    public Optional<PokemonData> getPokemon(String name) {
        return Optional.ofNullable(pokemonDatabase.get(name.toLowerCase()));
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