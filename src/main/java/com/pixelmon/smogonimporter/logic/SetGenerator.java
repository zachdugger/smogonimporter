package com.pixelmon.smogonimporter.logic;

import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.data.*;

import java.util.*;

/**
 * Main Pokemon set generator with full competitive logic.
 * Ported from Pokemon Showdown's randomSet() function.
 *
 * Implements the iterative move selection algorithm:
 * 1. Sample random moves from pool
 * 2. Analyze moveset with MoveAnalyzer
 * 3. Validate each move with MoveValidator
 * 4. Cull invalid moves and retry
 * 5. Select ability, item, nature, EVs, IVs
 *
 * This is the core of the competitive set generation system.
 */
public class SetGenerator {

    private final Random random;

    public SetGenerator() {
        this.random = new Random();
    }

    public SetGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generates a complete competitive Pokemon set
     *
     * @param pokemonData Pokemon data from JSON (with optional role for Gen9)
     * @param species Pokemon species name
     * @param types Pokemon's types (optional - will fetch from Pokedex if null)
     * @return Complete RandomSet ready for use
     */
    public RandomSet generateSet(PokemonData pokemonData, String species, List<String> types) {
        // Auto-fetch types from Pokedex if not provided
        if (types == null || types.isEmpty() || (types.size() == 1 && "Normal".equals(types.get(0)))) {
            SmogonDataRegistry registry = SmogonImporter.getDataManager().getDataRegistry();
            if (registry != null && registry.isInitialized()) {
                List<String> fetchedTypes = registry.getPokemonTypes(species);
                if (fetchedTypes != null && !fetchedTypes.isEmpty()) {
                    types = fetchedTypes;
                }
            }
        }

        // Fallback to Normal if still no types
        if (types == null || types.isEmpty()) {
            types = java.util.Arrays.asList("Normal");
        }

        // Get available options from JSON data
        List<String> availableMoves = pokemonData.getMoves();
        List<String> availableAbilities = pokemonData.getAbilities();
        List<String> availableItems = pokemonData.getItems();
        int level = pokemonData.getLevel();

        // Extract role (Gen9 only, null for Gen8)
        String role = pokemonData.getRole();

        // Filter out null/empty moves
        if (availableMoves != null) {
            availableMoves = availableMoves.stream()
                .filter(move -> move != null && !move.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        } else {
            availableMoves = new ArrayList<>();
        }

        // Validate we have moves to work with
        if (availableMoves.isEmpty()) {
            SmogonImporter.LOGGER.error("No valid moves available for {}", species);
            // Return a minimal set with placeholder data
            return createFallbackSet(species, level, availableAbilities, availableItems);
        }

        // Initialize move pool
        List<String> movePool = new ArrayList<>(availableMoves);
        List<String> rejectedPool = new ArrayList<>();
        Set<String> selectedMoves = new HashSet<>();

        // Iterative move selection with validation
        int maxAttempts = 100;
        int attempts = 0;

        while (selectedMoves.size() < 4 && attempts < maxAttempts) {
            attempts++;

            // Sample moves to fill set
            while (selectedMoves.size() < 4 && !movePool.isEmpty()) {
                String move = sampleNoReplace(movePool);
                if (move != null && !move.isEmpty()) {
                    selectedMoves.add(move);
                }
            }

            // If we don't have 4 moves yet, try rejected pool
            while (selectedMoves.size() < 4 && !rejectedPool.isEmpty()) {
                String move = sampleNoReplace(rejectedPool);
                if (move != null && !move.isEmpty()) {
                    selectedMoves.add(move);
                }
            }

            // Analyze the moveset
            MoveCounter counter = MoveAnalyzer.queryMoves(
                selectedMoves,
                types,
                availableAbilities
            );

            // Validate each move
            boolean needsRestart = false;
            List<String> movesToRemove = new ArrayList<>();

            for (String move : selectedMoves) {
                MoveValidator.CullResult result = MoveValidator.shouldCullMove(
                    move,
                    counter,
                    selectedMoves,
                    types,
                    availableAbilities,
                    species
                );

                if (result.cull) {
                    movesToRemove.add(move);
                    rejectedPool.add(move);
                    needsRestart = true;
                    break;  // Restart validation loop
                }
            }

            // Remove culled moves
            for (String move : movesToRemove) {
                selectedMoves.remove(move);
            }

            // Check if moveset has required coverage
            if (selectedMoves.size() == 4) {
                if (!MoveValidator.hasRequiredCoverage(counter, selectedMoves, types, counter.getSetupType())) {
                    // Remove a non-STAB move and try again
                    for (String move : selectedMoves) {
                        // Simple heuristic: remove non-STAB move
                        selectedMoves.remove(move);
                        rejectedPool.add(move);
                        break;
                    }
                    continue;
                }

                // Valid moveset found!
                break;
            }
        }

        // Ensure we have 4 moves (fill with random if needed)
        int safetyCounter = 0;
        while (selectedMoves.size() < 4 && !availableMoves.isEmpty() && safetyCounter < 100) {
            String move = availableMoves.get(random.nextInt(availableMoves.size()));
            if (move != null && !move.isEmpty()) {
                selectedMoves.add(move);
            }
            safetyCounter++;
        }

        // If we still don't have enough moves, pad with basic moves
        if (selectedMoves.size() < 4) {
            SmogonImporter.LOGGER.warn("Could not generate 4 moves for {}, padding with basics", species);
            List<String> basicMoves = Arrays.asList("Tackle", "Growl", "Quick Attack", "Scratch");
            for (String basicMove : basicMoves) {
                if (selectedMoves.size() >= 4) break;
                selectedMoves.add(basicMove);
            }
        }

        // Final analysis
        MoveCounter finalCounter = MoveAnalyzer.queryMoves(
            selectedMoves,
            types,
            availableAbilities
        );

        // Select ability
        String ability = selectAbility(availableAbilities, finalCounter, selectedMoves, types, species);

        // Select item (with role for Gen9)
        String item = ItemSelector.selectItem(
            finalCounter,
            selectedMoves,
            ability,
            types,
            species,
            availableItems,
            role  // Pass role for Gen9-specific item logic
        );

        // Select nature (with role for Gen9)
        String nature = NatureSelector.selectOptimalNature(finalCounter, species, role);

        // Generate EVs and IVs (with role for Gen9)
        Map<String, Integer> evs = StatOptimizer.generateRandomEVs(finalCounter, random, role);
        Map<String, Integer> ivs = StatOptimizer.generateOptimalIVs(finalCounter, role);

        // Optimize EVs if needed
        // (Would need base stats for HP optimization - skipping for now)

        // Determine gender
        String gender = determineGender(species);

        // Build final set (with role for Gen9)
        return new RandomSet(
            species,
            level,
            ability,
            item,
            new ArrayList<>(selectedMoves),
            evs,
            ivs,
            nature,
            gender,
            role  // Gen9 only, null for Gen8
        );
    }

    /**
     * Selects the best ability from available options
     */
    private String selectAbility(
            List<String> abilities,
            MoveCounter counter,
            Set<String> moves,
            List<String> types,
            String species
    ) {
        if (abilities == null || abilities.isEmpty()) {
            return "Unknown";
        }

        // Filter out bad abilities
        List<String> validAbilities = new ArrayList<>();
        for (String ability : abilities) {
            if (!AbilityValidator.shouldCullAbility(ability, counter, moves, types, species)) {
                validAbilities.add(ability);
            }
        }

        // If all abilities were culled, use original list
        if (validAbilities.isEmpty()) {
            validAbilities = new ArrayList<>(abilities);
        }

        // Sort by rating
        List<String> sorted = AbilityValidator.sortAbilitiesByRating(validAbilities, counter, species);

        // Probabilistic selection (bias toward best)
        // 66% chance for best, 24% for second-best, 10% for third-best
        if (sorted.size() == 1) return sorted.get(0);

        double rand = random.nextDouble();
        if (rand < 0.66) return sorted.get(0);
        if (sorted.size() > 1 && rand < 0.90) return sorted.get(1);
        if (sorted.size() > 2) return sorted.get(2);

        return sorted.get(0);
    }

    /**
     * Determines gender (simplified)
     * In production, would check species gender ratio
     */
    private String determineGender(String species) {
        // Genderless Pokemon
        Set<String> genderless = new HashSet<>(Arrays.asList(
            "magnemite", "magneton", "magnezone", "voltorb", "electrode",
            "staryu", "starmie", "porygon", "porygon2", "porygonz",
            "beldum", "metang", "metagross", "bronzor", "bronzong"
        ));

        String normalized = species.toLowerCase().replace("-", "").replace(" ", "");
        if (genderless.contains(normalized)) {
            return "Genderless";
        }

        // Random M/F for others
        return random.nextBoolean() ? "Male" : "Female";
    }

    /**
     * Samples and removes a random element from a list
     */
    private String sampleNoReplace(List<String> list) {
        if (list.isEmpty()) return null;
        int index = random.nextInt(list.size());
        return list.remove(index);
    }

    /**
     * Creates a fallback set when generation fails
     */
    private RandomSet createFallbackSet(String species, int level, List<String> abilities, List<String> items) {
        SmogonImporter.LOGGER.warn("Creating fallback set for {}", species);

        // Use placeholder data
        List<String> fallbackMoves = Arrays.asList("Tackle", "Growl", "Quick Attack", "Scratch");
        String fallbackAbility = (abilities != null && !abilities.isEmpty()) ? abilities.get(0) : "Unknown";
        String fallbackItem = (items != null && !items.isEmpty()) ? items.get(0) : "Leftovers";

        return new RandomSet(
            species,
            level,
            fallbackAbility,
            fallbackItem,
            fallbackMoves,
            StatOptimizer.createBalancedEVs(),
            StatOptimizer.createPerfectIVs(),
            "Hardy",
            "Genderless"
        );
    }

    /**
     * Complete random set data structure
     */
    public static class RandomSet {
        private final String species;
        private final int level;
        private final String ability;
        private final String item;
        private final List<String> moves;
        private final Map<String, Integer> evs;
        private final Map<String, Integer> ivs;
        private final String nature;
        private final String gender;
        private final String role;  // Gen9 only, null for Gen8

        public RandomSet(String species, int level, String ability, String item,
                        List<String> moves, Map<String, Integer> evs,
                        Map<String, Integer> ivs, String nature,
                        String gender, String role) {
            this.species = species;
            this.level = level;
            this.ability = ability;
            this.item = item;
            this.moves = moves;
            this.evs = evs;
            this.ivs = ivs;
            this.nature = nature;
            this.gender = gender;
            this.role = role;
        }

        // Constructor without role (for backwards compatibility)
        public RandomSet(String species, int level, String ability, String item,
                        List<String> moves, Map<String, Integer> evs,
                        Map<String, Integer> ivs, String nature,
                        String gender) {
            this(species, level, ability, item, moves, evs, ivs, nature, gender, null);
        }

        // Getters
        public String getSpecies() { return species; }
        public int getLevel() { return level; }
        public String getAbility() { return ability; }
        public String getItem() { return item; }
        public List<String> getMoves() { return moves; }
        public Map<String, Integer> getEvs() { return evs; }
        public Map<String, Integer> getIvs() { return ivs; }
        public String getNature() { return nature; }
        public String getGender() { return gender; }
        public String getRole() { return role; }  // Gen9 only
        public boolean hasRole() { return role != null && !role.isEmpty(); }

        @Override
        public String toString() {
            return String.format("%s @ %s\nAbility: %s\nLevel: %d\nNature: %s\nEVs: %s\nIVs: %s\n- %s\n- %s\n- %s\n- %s",
                species, item, ability, level, nature,
                evs, ivs,
                moves.size() > 0 ? moves.get(0) : "",
                moves.size() > 1 ? moves.get(1) : "",
                moves.size() > 2 ? moves.get(2) : "",
                moves.size() > 3 ? moves.get(3) : ""
            );
        }
    }
}
