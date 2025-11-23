package com.pixelmon.smogonimporter.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pixelmon.smogonimporter.SmogonImporter;

import java.util.*;

/**
 * Parses Gen9 sets.json into Gen9PokemonSet objects
 *
 * New Format (from GitHub):
 * {
 *   "venusaur": {
 *     "level": 84,
 *     "sets": [
 *       {
 *         "role": "Bulky Support",
 *         "movepool": ["Giga Drain", "Leech Seed", ...],
 *         "abilities": ["Chlorophyll", "Overgrow"],
 *         "teraTypes": ["Steel", "Water"]
 *       }
 *     ]
 *   }
 * }
 *
 * Handles:
 * - Pokemon species with multiple sets
 * - Movepool selection (4 moves random from pool)
 * - Ability selection (random from list)
 * - Tera types (for future Pixelmon support)
 */
public class Gen9DataParser {

    /**
     * Parse the entire sets.json file and extract all Pokemon sets
     */
    public static List<Gen9PokemonSet> parseMatchupsFile(String jsonData) {
        List<Gen9PokemonSet> allSets = new ArrayList<>();

        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();

            SmogonImporter.LOGGER.info("Gen9 sets.json root keys: {}", root.keySet().size());

            // Iterate through each Pokemon species
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String species = entry.getKey();
                JsonObject pokemonData = entry.getValue().getAsJsonObject();

                try {
                    List<Gen9PokemonSet> speciesSets = parseSpeciesData(species, pokemonData);
                    allSets.addAll(speciesSets);
                } catch (Exception e) {
                    SmogonImporter.LOGGER.warn("Failed to parse Gen9 sets for '{}': {}", species, e.getMessage());
                }
            }

            SmogonImporter.LOGGER.info("Parsed {} unique Gen9 competitive sets from {} species",
                allSets.size(), root.keySet().size());

        } catch (Exception e) {
            SmogonImporter.LOGGER.error("Failed to parse Gen9 sets.json file", e);
        }

        return allSets;
    }

    /**
     * Parse all sets for a single Pokemon species
     */
    private static List<Gen9PokemonSet> parseSpeciesData(String species, JsonObject pokemonData) {
        List<Gen9PokemonSet> sets = new ArrayList<>();

        int level = pokemonData.has("level") ? pokemonData.get("level").getAsInt() : 100;

        if (!pokemonData.has("sets")) {
            SmogonImporter.LOGGER.warn("Pokemon '{}' has no sets array", species);
            return sets;
        }

        JsonArray setsArray = pokemonData.getAsJsonArray("sets");

        for (JsonElement setElement : setsArray) {
            if (!setElement.isJsonObject()) continue;

            JsonObject setData = setElement.getAsJsonObject();

            try {
                Gen9PokemonSet pokemonSet = parseSetData(species, level, setData);
                if (pokemonSet != null) {
                    sets.add(pokemonSet);
                }
            } catch (Exception e) {
                SmogonImporter.LOGGER.warn("Failed to parse set for '{}': {}", species, e.getMessage());
            }
        }

        return sets;
    }

    /**
     * Parse a single set configuration
     */
    private static Gen9PokemonSet parseSetData(String species, int level, JsonObject setData) {
        Gen9PokemonSet set = new Gen9PokemonSet();

        set.setSpecies(species);
        set.setLevel(level);

        // Role (optional, for reference)
        if (setData.has("role")) {
            String role = setData.get("role").getAsString();
            // Store role as metadata (we can add this field if needed)
            // For now, just log it in debug mode
            if (SmogonImporter.LOGGER.isDebugEnabled()) {
                SmogonImporter.LOGGER.debug("Set role for {}: {}", species, role);
            }
        }

        // Movepool - store all moves, random selection happens at generation time
        if (setData.has("movepool")) {
            JsonArray movepoolArray = setData.getAsJsonArray("movepool");
            List<String> movepool = new ArrayList<>();

            for (JsonElement moveElement : movepoolArray) {
                String move = moveElement.getAsString();
                movepool.add(move);
            }

            set.setMoves(movepool);

            if (movepool.size() < 4) {
                SmogonImporter.LOGGER.warn("Species '{}' has only {} moves in movepool (need at least 4)",
                    species, movepool.size());
            }
        } else {
            SmogonImporter.LOGGER.warn("Species '{}' has no movepool", species);
            return null;
        }

        // Abilities - store all, random selection happens at generation time
        if (setData.has("abilities")) {
            JsonArray abilitiesArray = setData.getAsJsonArray("abilities");
            List<String> abilities = new ArrayList<>();

            for (JsonElement abilityElement : abilitiesArray) {
                abilities.add(abilityElement.getAsString());
            }

            // Store first ability as default (will be randomly selected later)
            if (!abilities.isEmpty()) {
                set.setAbility(abilities.get(0));
            }
        }

        // Tera Types - for future Pixelmon support
        if (setData.has("teraTypes")) {
            JsonArray teraTypesArray = setData.getAsJsonArray("teraTypes");
            List<String> teraTypes = new ArrayList<>();

            for (JsonElement teraElement : teraTypesArray) {
                teraTypes.add(teraElement.getAsString());
            }

            // Note: Pixelmon doesn't support Tera types yet, but we store them for future use
            // set.setTeraTypes(teraTypes);
        }

        // Default values for fields not in sets.json
        set.setItem("None");  // Will be determined by SetGenerator
        set.setNature("Hardy");  // Will be determined by NatureSelector
        set.setEvs(new HashMap<>());  // Will be determined by StatOptimizer
        set.setIvs(getDefaultIVs());  // Perfect IVs
        set.setGender("");  // Random
        set.setShiny(false);  // Not shiny by default
        set.setHappiness(255);  // Max happiness

        return set;
    }


    /**
     * Get default perfect IVs (all 31)
     */
    private static Map<String, Integer> getDefaultIVs() {
        Map<String, Integer> ivs = new HashMap<>();
        ivs.put("hp", 31);
        ivs.put("atk", 31);
        ivs.put("def", 31);
        ivs.put("spa", 31);
        ivs.put("spd", 31);
        ivs.put("spe", 31);
        return ivs;
    }
}
