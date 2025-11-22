package com.pixelmon.smogonimporter.api;

import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.data.DataManager;
import com.pixelmon.smogonimporter.data.models.PokemonData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Main API class for accessing Smogon Pokemon data.
 * This is the primary interface that other mods should use.
 */
public class SmogonAPI {
    private final DataManager dataManager;
    private final Random random = new Random();

    public SmogonAPI(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Get a specific Pokemon by name
     * @param name The Pokemon's name (case-insensitive)
     * @return Optional containing the Pokemon data if found
     */
    public Optional<PokemonData> getPokemon(String name) {
        if (!dataManager.isInitialized()) {
            SmogonImporter.LOGGER.warn("API called before data initialization");
            return Optional.empty();
        }
        return dataManager.getPokemon(name);
    }

    /**
     * Get multiple random Pokemon
     * @param count Number of Pokemon to retrieve
     * @return List of random Pokemon (may be less than count if not enough available)
     */
    public List<PokemonData> getRandomPokemon(int count) {
        if (!dataManager.isInitialized()) {
            SmogonImporter.LOGGER.warn("API called before data initialization");
            return Collections.emptyList();
        }
        return dataManager.getRandomPokemon(count);
    }

    /**
     * Get a single random Pokemon
     * @return Optional containing a random Pokemon
     */
    public Optional<PokemonData> getRandomPokemon() {
        List<PokemonData> random = getRandomPokemon(1);
        return random.isEmpty() ? Optional.empty() : Optional.of(random.get(0));
    }

    /**
     * Get random Pokemon with a specific filter
     * @param filter Predicate to filter Pokemon
     * @param count Number of Pokemon to retrieve
     * @return List of filtered random Pokemon
     */
    public List<PokemonData> getRandomPokemonFiltered(Predicate<PokemonData> filter, int count) {
        if (!dataManager.isInitialized()) {
            return Collections.emptyList();
        }

        List<PokemonData> filtered = dataManager.getAllPokemon().stream()
                .filter(filter)
                .collect(Collectors.toList());

        Collections.shuffle(filtered);
        return filtered.stream().limit(count).collect(Collectors.toList());
    }

    /**
     * Get Pokemon by level range
     * @param minLevel Minimum level (inclusive)
     * @param maxLevel Maximum level (inclusive)
     * @return List of Pokemon within the level range
     */
    public List<PokemonData> getPokemonByLevelRange(int minLevel, int maxLevel) {
        return dataManager.getAllPokemon().stream()
                .filter(p -> p.getLevel() >= minLevel && p.getLevel() <= maxLevel)
                .collect(Collectors.toList());
    }

    /**
     * Get Pokemon that can have a specific ability
     * @param ability The ability name
     * @return List of Pokemon that can have this ability
     */
    public List<PokemonData> getPokemonWithAbility(String ability) {
        String lowerAbility = ability.toLowerCase();
        return dataManager.getAllPokemon().stream()
                .filter(p -> p.getAbilities() != null &&
                        p.getAbilities().stream().anyMatch(a -> a.toLowerCase().contains(lowerAbility)))
                .collect(Collectors.toList());
    }

    /**
     * Get Pokemon that can learn a specific move
     * @param move The move name
     * @return List of Pokemon that can learn this move
     */
    public List<PokemonData> getPokemonWithMove(String move) {
        String lowerMove = move.toLowerCase();
        return dataManager.getAllPokemon().stream()
                .filter(p -> p.getMoves() != null &&
                        p.getMoves().stream().anyMatch(m -> m.toLowerCase().contains(lowerMove)))
                .collect(Collectors.toList());
    }

    /**
     * Get Pokemon that can hold a specific item
     * @param item The item name
     * @return List of Pokemon that can hold this item
     */
    public List<PokemonData> getPokemonWithItem(String item) {
        String lowerItem = item.toLowerCase();
        return dataManager.getAllPokemon().stream()
                .filter(p -> p.getItems() != null &&
                        p.getItems().stream().anyMatch(i -> i.toLowerCase().contains(lowerItem)))
                .collect(Collectors.toList());
    }

    /**
     * Generate a random competitive set for a specific Pokemon
     * @param pokemonName The Pokemon's name
     * @return Optional containing a RandomSet with the generated configuration
     */
    public Optional<RandomSet> generateRandomSet(String pokemonName) {
        Optional<PokemonData> pokemonOpt = getPokemon(pokemonName);
        if (pokemonOpt.isEmpty()) {
            return Optional.empty();
        }

        PokemonData pokemon = pokemonOpt.get();
        RandomSet set = new RandomSet();
        set.pokemonName = pokemon.getName();
        set.level = pokemon.getLevel();
        set.ability = pokemon.getRandomAbility();
        set.item = pokemon.getRandomItem();
        set.moves = pokemon.getRandomMoves(4);
        set.evs = pokemon.getEvs() != null ? new HashMap<>(pokemon.getEvs()) : getDefaultEVs();
        set.ivs = pokemon.getIvs() != null ? new HashMap<>(pokemon.getIvs()) : getDefaultIVs();

        return Optional.of(set);
    }

    /**
     * Generate multiple random sets
     * @param count Number of sets to generate
     * @return List of random sets (unique Pokemon)
     */
    public List<RandomSet> generateRandomTeam(int count) {
        List<PokemonData> randomPokemon = getRandomPokemon(count);
        return randomPokemon.stream()
                .map(p -> generateRandomSet(p.getName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get all available Pokemon names
     * @return List of all Pokemon names in the database
     */
    public List<String> getAllPokemonNames() {
        return dataManager.getAllPokemonNames();
    }

    /**
     * Get the total count of available Pokemon
     * @return Number of Pokemon in the database
     */
    public int getPokemonCount() {
        return dataManager.getAllPokemon().size();
    }

    /**
     * Check if the API is ready to use
     * @return true if data is loaded and ready
     */
    public boolean isReady() {
        return dataManager.isInitialized();
    }

    /**
     * Force a data refresh from the web
     * @return CompletableFuture that completes when refresh is done
     */
    public CompletableFuture<Boolean> refreshData() {
        return dataManager.fetchDataFromWeb();
    }

    /**
     * Get statistics about the loaded data
     * @return Map containing various statistics
     */
    public Map<String, Object> getStatistics() {
        return dataManager.getStatistics();
    }

    /**
     * Add a custom Pokemon set (if enabled in config)
     * @param pokemon The custom Pokemon data
     * @throws IllegalStateException if custom sets are disabled
     */
    public void addCustomPokemon(PokemonData pokemon) {
        dataManager.addCustomPokemon(pokemon);
    }

    /**
     * Builder for creating custom Pokemon sets
     */
    public static class PokemonBuilder {
        private final PokemonData pokemon = new PokemonData();

        public PokemonBuilder withName(String name) {
            pokemon.setName(name);
            return this;
        }

        public PokemonBuilder withLevel(int level) {
            pokemon.setLevel(level);
            return this;
        }

        public PokemonBuilder withAbilities(String... abilities) {
            pokemon.setAbilities(Arrays.asList(abilities));
            return this;
        }

        public PokemonBuilder withItems(String... items) {
            pokemon.setItems(Arrays.asList(items));
            return this;
        }

        public PokemonBuilder withMoves(String... moves) {
            pokemon.setMoves(Arrays.asList(moves));
            return this;
        }

        public PokemonBuilder withEVs(Map<String, Integer> evs) {
            pokemon.setEvs(evs);
            return this;
        }

        public PokemonBuilder withIVs(Map<String, Integer> ivs) {
            pokemon.setIvs(ivs);
            return this;
        }

        public PokemonData build() {
            return pokemon;
        }
    }

    /**
     * Represents a generated random set for a Pokemon
     */
    public static class RandomSet {
        public String pokemonName;
        public int level;
        public String ability;
        public String item;
        public List<String> moves;
        public Map<String, Integer> evs;
        public Map<String, Integer> ivs;

        @Override
        public String toString() {
            return String.format("%s @ %s\nAbility: %s\nLevel: %d\nMoves: %s",
                    pokemonName, item, ability, level, String.join(" / ", moves));
        }
    }

    // Helper methods
    private Map<String, Integer> getDefaultEVs() {
        Map<String, Integer> evs = new HashMap<>();
        evs.put("hp", 85);
        evs.put("atk", 85);
        evs.put("def", 85);
        evs.put("spa", 85);
        evs.put("spd", 85);
        evs.put("spe", 85);
        return evs;
    }

    private Map<String, Integer> getDefaultIVs() {
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