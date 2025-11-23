package com.pixelmon.smogonimporter.pixelmon;

import com.pixelmon.smogonimporter.logic.SetGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Builds Pixelmon Pokemon objects from generated competitive sets.
 *
 * This class interfaces with the Pixelmon API to create actual Pokemon.
 * When Pixelmon is not available, it provides detailed logging of what
 * would be created.
 *
 * NOTE: Uncomment Pixelmon API calls once the Pixelmon JAR is in libs/
 */
public class PixelmonBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PixelmonBuilder.class);

    /**
     * Creates a Pixelmon Pokemon from a RandomSet
     *
     * @param set The generated competitive set
     * @return Pixelmon Pokemon object (or null if Pixelmon not available)
     */
    public static Object buildPokemon(SetGenerator.RandomSet set) {
        if (set == null) {
            LOGGER.error("Cannot build Pokemon from null set");
            return null;
        }

        try {
            // TODO: Uncomment when Pixelmon dependency is available
            return buildPixelmonPokemon(set);
        } catch (Exception e) {
            LOGGER.error("Failed to build Pixelmon Pokemon: {}", e.getMessage());
            logSetDetails(set);
            return null;
        }
    }

    /**
     * Builds actual Pixelmon Pokemon using Pixelmon API
     * UNCOMMENT WHEN PIXELMON JAR IS AVAILABLE
     */
    private static Object buildPixelmonPokemon(SetGenerator.RandomSet set) {
        LOGGER.info("Building Pokemon: {}", set.getSpecies());

        // Translate names from Smogon to Pixelmon format
        String pixelmonSpecies = NameTranslationService.smogonToPixelmon(set.getSpecies());
        String pixelmonAbility = NameTranslationService.smogonAbilityToPixelmon(set.getAbility());
        String pixelmonItem = NameTranslationService.smogonItemToPixelmon(set.getItem());

        /* UNCOMMENT WHEN PIXELMON IS AVAILABLE:

        // Create Pokemon
        Pokemon pokemon = PokemonFactory.create(pixelmonSpecies);

        if (pokemon == null) {
            LOGGER.error("Failed to create Pokemon: {}", pixelmonSpecies);
            return null;
        }

        // Set level
        pokemon.setLevel(set.getLevel());

        // Set ability
        Ability ability = AbilityRegistry.getAbility(pixelmonAbility);
        if (ability != null) {
            pokemon.setAbility(ability);
        } else {
            LOGGER.warn("Unknown ability: {}", pixelmonAbility);
        }

        // Set nature
        Nature nature = Nature.valueOf(set.getNature().toUpperCase());
        pokemon.setNature(nature);

        // Set gender
        if ("Male".equals(set.getGender())) {
            pokemon.setGender(Gender.MALE);
        } else if ("Female".equals(set.getGender())) {
            pokemon.setGender(Gender.FEMALE);
        } else {
            pokemon.setGender(Gender.NONE);
        }

        // Set EVs
        Map<String, Integer> evs = set.getEvs();
        pokemon.getEVs().setStat(StatsType.HP, evs.getOrDefault("hp", 0));
        pokemon.getEVs().setStat(StatsType.ATTACK, evs.getOrDefault("atk", 0));
        pokemon.getEVs().setStat(StatsType.DEFENCE, evs.getOrDefault("def", 0));
        pokemon.getEVs().setStat(StatsType.SPECIAL_ATTACK, evs.getOrDefault("spa", 0));
        pokemon.getEVs().setStat(StatsType.SPECIAL_DEFENCE, evs.getOrDefault("spd", 0));
        pokemon.getEVs().setStat(StatsType.SPEED, evs.getOrDefault("spe", 0));

        // Set IVs
        Map<String, Integer> ivs = set.getIvs();
        pokemon.getIVs().setStat(StatsType.HP, ivs.getOrDefault("hp", 31));
        pokemon.getIVs().setStat(StatsType.ATTACK, ivs.getOrDefault("atk", 31));
        pokemon.getIVs().setStat(StatsType.DEFENCE, ivs.getOrDefault("def", 31));
        pokemon.getIVs().setStat(StatsType.SPECIAL_ATTACK, ivs.getOrDefault("spa", 31));
        pokemon.getIVs().setStat(StatsType.SPECIAL_DEFENCE, ivs.getOrDefault("spd", 31));
        pokemon.getIVs().setStat(StatsType.SPEED, ivs.getOrDefault("spe", 31));

        // Set moves
        Moveset moveset = pokemon.getMoveset();
        for (int i = 0; i < set.getMoves().size() && i < 4; i++) {
            String moveName = NameTranslationService.smogonMoveToPixelmon(set.getMoves().get(i));
            Attack move = AttackRegistry.getAttack(moveName);
            if (move != null) {
                moveset.set(i, move);
            } else {
                LOGGER.warn("Unknown move: {}", moveName);
            }
        }

        // Set held item
        if (pixelmonItem != null && !pixelmonItem.isEmpty()) {
            ItemStack itemStack = ItemRegistry.getItem(pixelmonItem);
            if (itemStack != null) {
                pokemon.setHeldItem(itemStack);
            } else {
                LOGGER.warn("Unknown item: {}", pixelmonItem);
            }
        }

        // Recalculate stats
        pokemon.updateStats();

        LOGGER.info("Successfully created Pokemon: {} @ {}", pixelmonSpecies, pixelmonItem);
        return pokemon;

        */

        // TEMPORARY: Log what would be created
        logSetDetails(set);
        LOGGER.warn("Pixelmon API not available - returning null");
        LOGGER.warn("To enable Pixelmon integration:");
        LOGGER.warn("1. Add Pixelmon JAR to libs/Pixelmon-1.21-9.3.9.jar");
        LOGGER.warn("2. Uncomment Pixelmon API calls in PixelmonBuilder.java");
        LOGGER.warn("3. Rebuild the mod");

        return null;
    }

    /**
     * Logs detailed information about the generated set
     */
    private static void logSetDetails(SetGenerator.RandomSet set) {
        LOGGER.info("=== Generated Pokemon Set ===");
        LOGGER.info("Species: {}", set.getSpecies());
        if (set.hasRole()) {
            LOGGER.info("Role: {}", set.getRole());
        }
        LOGGER.info("Level: {}", set.getLevel());
        LOGGER.info("Ability: {}", set.getAbility());
        LOGGER.info("Nature: {}", set.getNature());
        LOGGER.info("Item: {}", set.getItem());
        LOGGER.info("Gender: {}", set.getGender());
        LOGGER.info("Moves: {}", set.getMoves());
        LOGGER.info("EVs: {}", set.getEvs());
        LOGGER.info("IVs: {}", set.getIvs());
        LOGGER.info("=============================");
    }

    /**
     * Validates that a Pokemon can learn a move
     * This would query Pixelmon's learnset data
     */
    public static boolean canLearnMove(String species, String move) {
        /* UNCOMMENT WHEN PIXELMON IS AVAILABLE:

        String pixelmonSpecies = NameTranslationService.smogonToPixelmon(species);
        String pixelmonMove = NameTranslationService.smogonMoveToPixelmon(move);

        Pokemon pokemon = PokemonFactory.create(pixelmonSpecies);
        if (pokemon == null) return false;

        Attack attack = AttackRegistry.getAttack(pixelmonMove);
        if (attack == null) return false;

        return pokemon.getBaseStats().getEggMoves().contains(pixelmonMove) ||
               pokemon.getBaseStats().getLevelUpMoves().containsValue(pixelmonMove) ||
               pokemon.getBaseStats().getTutorMoves().contains(pixelmonMove) ||
               pokemon.getBaseStats().getTMmoves().contains(pixelmonMove);

        */

        // TEMPORARY: Assume all moves are learnable
        LOGGER.debug("Move validation not available without Pixelmon - assuming {} can learn {}", species, move);
        return true;
    }

    /**
     * Gets base stats for a Pokemon
     * This would query Pixelmon's stats data
     */
    public static Map<String, Integer> getBaseStats(String species) {
        /* UNCOMMENT WHEN PIXELMON IS AVAILABLE:

        String pixelmonSpecies = NameTranslationService.smogonToPixelmon(species);
        Pokemon pokemon = PokemonFactory.create(pixelmonSpecies);

        if (pokemon == null) return null;

        BaseStats baseStats = pokemon.getBaseStats();
        Map<String, Integer> stats = new HashMap<>();
        stats.put("hp", baseStats.getHP());
        stats.put("atk", baseStats.getAttack());
        stats.put("def", baseStats.getDefence());
        stats.put("spa", baseStats.getSpAtt());
        stats.put("spd", baseStats.getSpDef());
        stats.put("spe", baseStats.getSpeed());

        return stats;

        */

        // TEMPORARY: Return default stats
        LOGGER.debug("Base stats not available without Pixelmon for {}", species);
        return null;
    }

    /**
     * Checks if Pixelmon API is available
     */
    public static boolean isPixelmonAvailable() {
        try {
            // Try to load a Pixelmon class
            Class.forName("com.pixelmonmod.pixelmon.api.pokemon.Pokemon");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
