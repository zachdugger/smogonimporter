package com.pixelmon.smogonimporter.data;

import com.google.gson.*;
import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.config.SmogonConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all Smogon competitive data
 * Loads and caches: Pokedex, Moves, Items, Abilities, Types, Natures
 *
 * Data is now loaded from GitHub Pokemon Showdown TypeScript sources
 * instead of the old play.pokemonshowdown.com JSON API
 */
public class SmogonDataRegistry {

    private static final Gson GSON = new GsonBuilder().create();

    // Data caches
    private final Map<String, PokedexEntry> pokedex = new ConcurrentHashMap<>();
    private final Map<String, MoveDataEntry> moves = new ConcurrentHashMap<>();
    private final Map<String, ItemEntry> items = new ConcurrentHashMap<>();
    private final Map<String, AbilityEntry> abilities = new ConcurrentHashMap<>();
    private final List<String> types = new ArrayList<>();
    private final Map<String, NatureEntry> natures = new ConcurrentHashMap<>();

    private boolean initialized = false;

    /**
     * Initialize all data sources from GitHub Pokemon Showdown TypeScript files
     */
    public void initialize() {
        if (initialized) {
            SmogonImporter.LOGGER.info("SmogonDataRegistry already initialized");
            return;
        }

        SmogonImporter.LOGGER.info("Initializing Smogon Data Registry from GitHub sources...");

        try {
            loadPokedex();
            loadMoves();
            loadItems();
            loadAbilities();
            loadTypes();
            loadNatures();

            initialized = true;
            SmogonImporter.LOGGER.info("✓ Loaded {} Pokemon species", pokedex.size());
            SmogonImporter.LOGGER.info("✓ Loaded {} moves", moves.size());
            SmogonImporter.LOGGER.info("✓ Loaded {} items", items.size());
            SmogonImporter.LOGGER.info("✓ Loaded {} abilities", abilities.size());
            SmogonImporter.LOGGER.info("✓ Loaded {} types", types.size());
            SmogonImporter.LOGGER.info("✓ Loaded {} natures", natures.size());
        } catch (Exception e) {
            SmogonImporter.LOGGER.error("Failed to initialize Smogon Data Registry", e);
        }
    }

    /**
     * Load Pokedex data (types, stats, abilities) from pokedex.ts
     */
    private void loadPokedex() throws Exception {
        SmogonImporter.LOGGER.info("Loading Pokedex data from GitHub...");
        String tsContent = fetchFromUrl(SmogonConfig.getPokedexDataURL());
        JsonObject root = TypeScriptParser.parseTypeScript(tsContent, "Pokedex");

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String id = entry.getKey();
            try {
                PokedexEntry pokemon = GSON.fromJson(entry.getValue(), PokedexEntry.class);
                pokemon.setName(id);  // Ensure name is set
                pokedex.put(normalize(id), pokemon);
            } catch (Exception e) {
                SmogonImporter.LOGGER.warn("Failed to parse Pokemon '{}': {}", id, e.getMessage());
            }
        }
    }

    /**
     * Load Move data (category, type, power) from moves.ts
     */
    private void loadMoves() throws Exception {
        SmogonImporter.LOGGER.info("Loading move data from GitHub...");
        String tsContent = fetchFromUrl(SmogonConfig.getMovesDataURL());
        JsonObject root = TypeScriptParser.parseTypeScript(tsContent, "Moves");

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String id = entry.getKey();
            try {
                MoveDataEntry move = GSON.fromJson(entry.getValue(), MoveDataEntry.class);
                move.setName(id);  // Ensure name is set
                moves.put(normalize(id), move);
            } catch (Exception e) {
                SmogonImporter.LOGGER.warn("Failed to parse move '{}': {}", id, e.getMessage());
            }
        }
    }

    /**
     * Load Item data from items.ts
     */
    private void loadItems() throws Exception {
        SmogonImporter.LOGGER.info("Loading item data from GitHub...");
        String tsContent = fetchFromUrl(SmogonConfig.getItemsDataURL());
        JsonObject root = TypeScriptParser.parseTypeScript(tsContent, "Items");

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String id = entry.getKey();
            try {
                JsonObject itemObj = entry.getValue().getAsJsonObject();
                ItemEntry item = GSON.fromJson(itemObj, ItemEntry.class);
                item.setId(id);
                items.put(normalize(id), item);
            } catch (Exception e) {
                SmogonImporter.LOGGER.warn("Failed to parse item '{}': {}", id, e.getMessage());
            }
        }
    }

    /**
     * Load Ability data from abilities.ts
     */
    private void loadAbilities() throws Exception {
        SmogonImporter.LOGGER.info("Loading ability data from GitHub...");
        String tsContent = fetchFromUrl(SmogonConfig.getAbilitiesDataURL());
        JsonObject root = TypeScriptParser.parseTypeScript(tsContent, "Abilities");

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String id = entry.getKey();
            try {
                JsonObject abilityObj = entry.getValue().getAsJsonObject();
                AbilityEntry ability = GSON.fromJson(abilityObj, AbilityEntry.class);
                ability.setId(id);
                abilities.put(normalize(id), ability);
            } catch (Exception e) {
                SmogonImporter.LOGGER.warn("Failed to parse ability '{}': {}", id, e.getMessage());
            }
        }
    }

    /**
     * Load Pokemon types from typechart.ts
     */
    private void loadTypes() throws Exception {
        SmogonImporter.LOGGER.info("Loading type data from GitHub...");
        String tsContent = fetchFromUrl(SmogonConfig.getTypeChartDataURL());
        JsonObject root = TypeScriptParser.parseTypeScript(tsContent, "TypeChart");

        types.clear();
        for (String typeName : root.keySet()) {
            types.add(typeName);
        }

        // Sort types alphabetically for consistency
        Collections.sort(types);
    }

    /**
     * Load Natures from natures.ts
     */
    private void loadNatures() throws Exception {
        SmogonImporter.LOGGER.info("Loading nature data from GitHub...");
        String tsContent = fetchFromUrl(SmogonConfig.getNaturesDataURL());
        JsonObject root = TypeScriptParser.parseTypeScript(tsContent, "Natures");

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String id = entry.getKey();
            try {
                JsonObject natureObj = entry.getValue().getAsJsonObject();
                NatureEntry nature = GSON.fromJson(natureObj, NatureEntry.class);
                nature.setId(id);
                natures.put(normalize(id), nature);
            } catch (Exception e) {
                SmogonImporter.LOGGER.warn("Failed to parse nature '{}': {}", id, e.getMessage());
            }
        }
    }

    /**
     * Fetch content from URL (TypeScript or JSON)
     */
    private String fetchFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "SmogonImporter/1.0");
            connection.setConnectTimeout(SmogonConfig.CONNECTION_TIMEOUT_SECONDS.get() * 1000);
            connection.setReadTimeout(SmogonConfig.READ_TIMEOUT_SECONDS.get() * 1000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP " + responseCode + " for URL: " + urlString);
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
                return response.toString();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // === Query Methods ===

    /**
     * Get full Pokedex entry for a Pokemon
     */
    public PokedexEntry getPokedexEntry(String pokemonName) {
        return pokedex.get(normalize(pokemonName));
    }

    /**
     * Get Pokemon types
     */
    public List<String> getPokemonTypes(String pokemonName) {
        PokedexEntry entry = pokedex.get(normalize(pokemonName));
        return entry != null ? entry.getTypes() : Collections.singletonList("Normal");
    }

    /**
     * Get Pokemon base stats
     */
    public Map<String, Integer> getBaseStats(String pokemonName) {
        PokedexEntry entry = pokedex.get(normalize(pokemonName));
        return entry != null ? entry.getBaseStats() : Collections.emptyMap();
    }

    /**
     * Get move data
     */
    public MoveDataEntry getMove(String moveName) {
        return moves.get(normalize(moveName));
    }

    /**
     * Get move category
     */
    public MoveCategory getMoveCategory(String moveName) {
        MoveDataEntry move = getMove(moveName);
        if (move != null) {
            return move.getCategoryEnum();
        }
        // Fallback: guess by name
        return guessCategoryByName(moveName);
    }

    /**
     * Get item by name
     */
    public ItemEntry getItem(String itemName) {
        return items.get(normalize(itemName));
    }

    /**
     * Check if item exists
     */
    public boolean isValidItem(String itemName) {
        return items.containsKey(normalize(itemName));
    }

    /**
     * Get ability by name
     */
    public AbilityEntry getAbility(String abilityName) {
        return abilities.get(normalize(abilityName));
    }

    /**
     * Check if ability exists
     */
    public boolean isValidAbility(String abilityName) {
        return abilities.containsKey(normalize(abilityName));
    }

    /**
     * Get all Pokemon types
     */
    public List<String> getAllTypes() {
        return new ArrayList<>(types);
    }

    /**
     * Get nature by name
     */
    public NatureEntry getNature(String natureName) {
        return natures.get(normalize(natureName));
    }

    /**
     * Get all natures
     */
    public Collection<NatureEntry> getAllNatures() {
        return new ArrayList<>(natures.values());
    }

    /**
     * Check if Pokemon is NFE (Not Fully Evolved)
     */
    public boolean isNFE(String pokemonName) {
        PokedexEntry entry = pokedex.get(normalize(pokemonName));
        return entry != null && entry.isNFE();
    }

    /**
     * Guess move category by name patterns (fallback)
     */
    private MoveCategory guessCategoryByName(String moveName) {
        String normalized = normalize(moveName);

        // Special attack indicators
        if (normalized.contains("beam") || normalized.contains("bolt") ||
            normalized.contains("pulse") || normalized.contains("blast") ||
            normalized.contains("ball") || normalized.contains("wave") ||
            normalized.contains("burst") || normalized.contains("breath") ||
            normalized.contains("spout") || normalized.contains("storm")) {
            return MoveCategory.SPECIAL;
        }

        // Physical attack indicators
        if (normalized.contains("punch") || normalized.contains("kick") ||
            normalized.contains("claw") || normalized.contains("fang") ||
            normalized.contains("strike") || normalized.contains("slash")) {
            return MoveCategory.PHYSICAL;
        }

        return MoveCategory.STATUS;
    }

    /**
     * Normalize name for lookups
     */
    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "")
                .replace(".", "")
                .replace(":", "");
    }

    public boolean isInitialized() {
        return initialized;
    }
}
