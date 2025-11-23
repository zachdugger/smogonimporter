package com.pixelmon.smogonimporter.data;

import com.pixelmon.smogonimporter.SmogonImporter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages the pool of Gen9 pre-built competitive Pokemon sets
 *
 * Unlike Gen8 which dynamically generates sets, Gen9 uses a static pool
 * of 1200+ hand-crafted competitive sets from draft-factory-matchups.json
 *
 * Features:
 * - Random set selection
 * - Species-based lookup (with fuzzy matching)
 * - Team generation
 * - Pool statistics
 */
public class Gen9SetSelector {

    private final List<Gen9PokemonSet> allSets;
    private final Map<String, List<Gen9PokemonSet>> setsBySpecies;
    private final Random random;

    public Gen9SetSelector() {
        this.allSets = new ArrayList<>();
        this.setsBySpecies = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    /**
     * Initialize the selector with parsed sets
     */
    public void loadSets(List<Gen9PokemonSet> sets) {
        allSets.clear();
        setsBySpecies.clear();

        if (sets == null || sets.isEmpty()) {
            SmogonImporter.LOGGER.warn("No Gen9 sets provided to selector");
            return;
        }

        allSets.addAll(sets);

        // Index by species for fast lookup
        for (Gen9PokemonSet set : sets) {
            String species = normalizeName(set.getSpecies());
            setsBySpecies.computeIfAbsent(species, k -> new ArrayList<>()).add(set);
        }

        SmogonImporter.LOGGER.info("Gen9 selector loaded {} sets for {} unique species",
                allSets.size(), setsBySpecies.size());
    }

    /**
     * Get a random Pokemon set from the entire pool
     */
    public Optional<Gen9PokemonSet> getRandomSet() {
        if (allSets.isEmpty()) {
            return Optional.empty();
        }

        int index = random.nextInt(allSets.size());
        return Optional.of(allSets.get(index));
    }

    /**
     * Get a random team of N Pokemon
     */
    public List<Gen9PokemonSet> getRandomTeam(int count) {
        if (allSets.isEmpty()) {
            return Collections.emptyList();
        }

        List<Gen9PokemonSet> shuffled = new ArrayList<>(allSets);
        Collections.shuffle(shuffled, random);

        return shuffled.stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Get a random set for a specific Pokemon (with fuzzy name matching)
     */
    public Optional<Gen9PokemonSet> getPokemonByName(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }

        String normalized = normalizeName(name);

        // Try exact match first
        List<Gen9PokemonSet> exactMatch = setsBySpecies.get(normalized);
        if (exactMatch != null && !exactMatch.isEmpty()) {
            int index = random.nextInt(exactMatch.size());
            return Optional.of(exactMatch.get(index));
        }

        // Try fuzzy match (partial match)
        for (Map.Entry<String, List<Gen9PokemonSet>> entry : setsBySpecies.entrySet()) {
            if (entry.getKey().contains(normalized) || normalized.contains(entry.getKey())) {
                List<Gen9PokemonSet> sets = entry.getValue();
                if (!sets.isEmpty()) {
                    int index = random.nextInt(sets.size());
                    return Optional.of(sets.get(index));
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Get all sets for a specific species
     */
    public List<Gen9PokemonSet> getAllSetsForSpecies(String name) {
        String normalized = normalizeName(name);
        return setsBySpecies.getOrDefault(normalized, Collections.emptyList());
    }

    /**
     * Get all available species names
     */
    public Set<String> getAllSpeciesNames() {
        return setsBySpecies.keySet();
    }

    /**
     * Get pool statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSets", allSets.size());
        stats.put("uniqueSpecies", setsBySpecies.size());

        // Count sets per species
        Map<String, Integer> countsPerSpecies = new HashMap<>();
        for (Map.Entry<String, List<Gen9PokemonSet>> entry : setsBySpecies.entrySet()) {
            countsPerSpecies.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("setsPerSpecies", countsPerSpecies);

        return stats;
    }

    /**
     * Check if pool is loaded
     */
    public boolean isLoaded() {
        return !allSets.isEmpty();
    }

    /**
     * Get total number of sets
     */
    public int getSetCount() {
        return allSets.size();
    }

    /**
     * Normalizes a Pokemon name for fuzzy matching
     * Removes spaces, hyphens, apostrophes, converts to lowercase
     */
    private String normalizeName(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "")
                .replace("tera captain", ""); // Remove Tera Captain prefix if still present
    }
}
