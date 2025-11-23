package com.pixelmon.smogonimporter.api;

import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.config.SmogonConfig;
import com.pixelmon.smogonimporter.data.DataManager;
import com.pixelmon.smogonimporter.data.Gen9PokemonSet;
import com.pixelmon.smogonimporter.data.PokemonData;
import com.pixelmon.smogonimporter.logic.SetGenerator;
import com.pixelmon.smogonimporter.pixelmon.PixelmonBuilder;

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

    // ==================== NEW COMPETITIVE GENERATION METHODS ====================

    /**
     * Generate a COMPETITIVE Pokemon set using full Smogon logic
     * This uses move validation, ability synergy, nature selection, etc.
     *
     * Gen8: Uses dynamic generation with full competitive logic
     * Gen9: Uses pre-built sets from static pool (draft-factory-matchups)
     *
     * @param pokemonName The Pokemon's name
     * @param types The Pokemon's types (e.g., Arrays.asList("Fire", "Flying"))
     * @return Optional containing a competitive SetGenerator.RandomSet
     */
    public Optional<SetGenerator.RandomSet> generateCompetitiveSet(String pokemonName, List<String> types) {
        Optional<PokemonData> pokemonOpt = getPokemon(pokemonName);
        if (pokemonOpt.isEmpty()) {
            SmogonImporter.LOGGER.warn("Pokemon not found in database: {}", pokemonName);
            return Optional.empty();
        }

        try {
            // GENERATION ROUTING: Gen8 vs Gen9
            if (SmogonConfig.isGen9()) {
                SmogonImporter.LOGGER.info("Using Gen9 mode for {}", pokemonName);
                // Gen9: Get pre-built set from static pool
                return generateGen9Set(pokemonName);
            } else {
                SmogonImporter.LOGGER.info("Using Gen8 mode for {}", pokemonName);
                // Gen8: Dynamic generation with full competitive logic
                return generateGen8Set(pokemonOpt.get(), pokemonName, types);
            }
        } catch (Exception e) {
            SmogonImporter.LOGGER.error("Failed to generate competitive set for {}", pokemonName, e);
            return Optional.empty();
        }
    }

    /**
     * Generate a Gen8 competitive set using dynamic generation
     */
    private Optional<SetGenerator.RandomSet> generateGen8Set(PokemonData pokemonData, String pokemonName, List<String> types) {
        SetGenerator generator = new SetGenerator();
        SetGenerator.RandomSet set = generator.generateSet(pokemonData, pokemonName, types);
        return Optional.of(set);
    }

    /**
     * Generate a Gen9 competitive set using dynamic generation with role selection
     * Randomly selects from available roles for the Pokemon
     */
    private Optional<SetGenerator.RandomSet> generateGen9Set(String pokemonName) {
        SmogonImporter.LOGGER.info("Generating Gen9 competitive set for: {}", pokemonName);

        // Get ALL roles for this Pokemon
        List<PokemonData> allRoles = dataManager.getAllRolesForPokemon(pokemonName);

        if (allRoles.isEmpty()) {
            SmogonImporter.LOGGER.warn("No Gen9 data found for: {}", pokemonName);
            return Optional.empty();
        }

        // Randomly select a role
        PokemonData selectedRole = allRoles.get(new java.util.Random().nextInt(allRoles.size()));

        SmogonImporter.LOGGER.info("Selected role '{}' for {} ({} total roles available)",
            selectedRole.getRole(), pokemonName, allRoles.size());

        // Get types from pokedex
        List<String> types = dataManager.getDataRegistry().getPokemonTypes(pokemonName);

        // Use SetGenerator to apply competitive logic (with role)
        SetGenerator generator = new SetGenerator();
        SetGenerator.RandomSet set = generator.generateSet(selectedRole, pokemonName, types);

        SmogonImporter.LOGGER.debug("Generated Gen9 competitive set for {} ({}): {}",
            pokemonName, selectedRole.getRole(), set);
        return Optional.of(set);
    }

    /**
     * Generate a COMPETITIVE team using full Smogon logic
     *
     * @param count Number of Pokemon in the team
     * @return List of competitive sets
     */
    public List<SetGenerator.RandomSet> generateCompetitiveTeam(int count) {
        List<PokemonData> randomPokemon = getRandomPokemon(count);
        List<SetGenerator.RandomSet> team = new ArrayList<>();

        for (PokemonData pokemon : randomPokemon) {
            // TODO: Get actual types from Pixelmon or store in JSON
            // For now, use placeholder types
            List<String> types = Arrays.asList("Normal");

            Optional<SetGenerator.RandomSet> setOpt = generateCompetitiveSet(pokemon.getName(), types);
            setOpt.ifPresent(team::add);
        }

        return team;
    }

    /**
     * Create an actual Pixelmon Pokemon object from a competitive set
     *
     * @param set The generated competitive set
     * @return Pixelmon Pokemon object (or null if Pixelmon not available)
     */
    public Object createPixelmonPokemon(SetGenerator.RandomSet set) {
        return PixelmonBuilder.buildPokemon(set);
    }

    /**
     * Generate and spawn a competitive Pokemon directly
     *
     * @param pokemonName The Pokemon's name
     * @param types The Pokemon's types
     * @return Pixelmon Pokemon object (or null if generation/Pixelmon failed)
     */
    public Object generateAndSpawnCompetitivePokemon(String pokemonName, List<String> types) {
        Optional<SetGenerator.RandomSet> setOpt = generateCompetitiveSet(pokemonName, types);

        if (setOpt.isEmpty()) {
            return null;
        }

        return createPixelmonPokemon(setOpt.get());
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