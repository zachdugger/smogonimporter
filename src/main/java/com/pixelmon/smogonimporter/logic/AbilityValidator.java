package com.pixelmon.smogonimporter.logic;

import com.pixelmon.smogonimporter.data.*;

import java.util.*;

/**
 * Validates ability selections against movesets for competitive viability.
 * Ported from Pokemon Showdown's shouldCullAbility() function.
 *
 * Ensures abilities:
 * - Have synergy with selected moves
 * - Match the Pokemon's setup type
 * - Aren't universally bad abilities
 */
public class AbilityValidator {

    // Universally bad abilities that should be avoided
    private static final Set<String> BAD_ABILITIES = new HashSet<>(Arrays.asList(
        "defeatist", "emergencyexit", "klutz", "minus", "plus", "slow start",
        "truant", "wimp out"
    ));

    /**
     * Determines if an ability should be culled
     *
     * @param ability The ability to validate
     * @param counter Move counter with current moveset analysis
     * @param moves Current set of moves
     * @param types Pokemon's types
     * @param species Pokemon species name
     * @return true if the ability should be removed
     */
    public static boolean shouldCullAbility(
            String ability,
            MoveCounter counter,
            Set<String> moves,
            List<String> types,
            String species
    ) {
        String normalized = normalize(ability);

        // Remove universally bad abilities
        if (BAD_ABILITIES.contains(normalized)) {
            return true;
        }

        String setupType = counter.getSetupType();

        // Ability-specific validation
        switch (normalized) {
            // Requires specific moves
            case "contrary":
                // Needs Contrary-boosting moves
                return counter.get("contrary") == 0;

            case "skilllink":
                // Needs multi-hit moves
                return counter.get("skilllink") == 0;

            case "ironfist":
                // Needs punch moves
                return counter.get("ironfist") == 0;

            case "strongjaw":
                // Needs bite moves
                return counter.get("strongjaw") == 0;

            case "sheerforce":
                // Needs moves with secondary effects
                return counter.get("sheerforce") == 0;

            case "technician":
                // Needs low base power moves
                return counter.get("technician") == 0;

            case "toughclaws":
                // Needs contact moves
                return counter.get("toughclaws") == 0;

            case "adaptability":
                // Needs STAB moves
                return counter.get("stab") == 0;

            // Setup compatibility
            case "hugepower":
            case "purepower":
            case "gorillatactics":
                // Physical abilities - need physical moves
                return counter.get("physicalpool") < 2;

            case "soulheart":
            case "beastboost":
                // Generally want offensive sets
                if ("".equals(setupType) && counter.getDamagingMoveCount() < 2) {
                    return true;
                }
                break;

            // Defensive abilities on offensive sets
            case "regenerator":
            case "naturalcure":
                // Don't use on pure setup sweepers
                if ("Physical".equals(setupType) || "Special".equals(setupType)) {
                    return true;
                }
                break;

            // Status immunity abilities
            case "immunity":
            case "waterveil":
            case "magmaarmor":
                // Generally outclassed by other abilities
                return true;

            // Weather abilities
            case "drizzle":
            case "drought":
            case "sandstream":
            case "snowwarning":
                // These are generally good, but check if we have weather-boosted moves
                return !hasWeatherSynergy(normalized, moves, types);

            // Speed abilities
            case "quickfeet":
                // Needs status moves to activate
                if (counter.get("statusmove") == 0) {
                    return true;
                }
                break;

            case "unburden":
                // Needs consumable item + speed
                // Generally just keep it
                break;

            case "slowstart":
                // Always bad
                return true;

            // Ability that needs specific move
            case "liquidvoice":
                // Needs sound moves
                return counter.get("sound") == 0;

            case "megalauncher":
                // Needs aura/pulse moves
                // For now, generally keep
                break;

            case "normalize":
                // Generally bad competitive ability
                return true;

            // Doubles-specific abilities (remove in singles)
            case "friendguard":
            case "healer":
            case "telepathy":
                return true;  // Bad in singles

            // Abilities that need setup moves
            case "moxie":
                // Work well with offensive sets
                if (counter.getDamagingMoveCount() < 2) {
                    return true;
                }
                break;

            // Abilities requiring no setup
            case "choiceband":
            case "choicespecs":
            case "choicescarf":
                // These are items, not abilities, but check anyway
                if (!"".equals(setupType)) {
                    return true;
                }
                break;

            default:
                // Unknown ability - keep it
                return false;
        }

        return false;
    }

    /**
     * Sorts abilities by competitive viability
     * Returns a sorted list with best abilities first
     */
    public static List<String> sortAbilitiesByRating(
            List<String> abilities,
            MoveCounter counter,
            String species
    ) {
        List<String> sorted = new ArrayList<>(abilities);

        sorted.sort((a, b) -> {
            int ratingA = getAbilityRating(a, counter);
            int ratingB = getAbilityRating(b, counter);
            return Integer.compare(ratingB, ratingA);  // Descending order
        });

        return sorted;
    }

    /**
     * Rates an ability based on competitive viability and moveset synergy
     * Higher is better
     */
    private static int getAbilityRating(String ability, MoveCounter counter) {
        String normalized = normalize(ability);

        // Bad abilities get negative rating
        if (BAD_ABILITIES.contains(normalized)) {
            return -100;
        }

        String setupType = counter.getSetupType();
        int rating = 0;

        // Base ratings for top-tier abilities
        switch (normalized) {
            case "hugepower":
            case "purepower":
                return 100 + (counter.get("physicalpool") * 10);

            case "magicguard":
            case "regenerator":
                return 90;

            case "speedboost":
                return 85;

            case "prankster":
                return 80 + (counter.get("Status") * 5);

            case "adaptability":
                return 70 + (counter.get("stab") * 10);

            case "sheerforce":
                return 65 + (counter.get("sheerforce") * 10);

            case "technician":
                return 60 + (counter.get("technician") * 10);

            case "skilllink":
                return 55 + (counter.get("skilllink") * 15);

            case "ironfist":
                return 50 + (counter.get("ironfist") * 10);

            case "strongjaw":
                return 50 + (counter.get("strongjaw") * 10);

            case "contrary":
                return 45 + (counter.get("contrary") * 20);

            case "toughclaws":
                return 40 + (counter.get("toughclaws") * 5);

            // Weather setters
            case "drizzle":
            case "drought":
            case "sandstream":
            case "snowwarning":
                return 75;

            // Good defensive abilities
            case "waterabsorb":
            case "voltabsorb":
            case "flashfire":
            case "sapsipper":
            case "stormdrain":
            case "lightningrod":
                return 50;

            // Setup abilities
            case "moxie":
            case "beastboost":
                if (!"".equals(setupType)) return 60;
                return 40;

            default:
                return 30;  // Default rating for unknown abilities
        }
    }

    /**
     * Checks if a weather ability has synergy with moves/types
     */
    private static boolean hasWeatherSynergy(String ability, Set<String> moves, List<String> types) {
        switch (normalize(ability)) {
            case "drizzle":
                // Synergy with Water moves
                return types.contains("Water") || hasWaterMoves(moves);

            case "drought":
                // Synergy with Fire moves
                return types.contains("Fire") || hasFireMoves(moves);

            case "sandstream":
                // Synergy with Rock/Ground/Steel types
                return types.contains("Rock") || types.contains("Ground") || types.contains("Steel");

            case "snowwarning":
                // Synergy with Ice types
                return types.contains("Ice");

            default:
                return false;
        }
    }

    private static boolean hasWaterMoves(Set<String> moves) {
        return moves.stream().anyMatch(m ->
                m.toLowerCase().contains("water") ||
                m.toLowerCase().contains("surf") ||
                m.toLowerCase().contains("hydro")
        );
    }

    private static boolean hasFireMoves(Set<String> moves) {
        return moves.stream().anyMatch(m ->
                m.toLowerCase().contains("fire") ||
                m.toLowerCase().contains("flame") ||
                m.toLowerCase().contains("blaze")
        );
    }

    /**
     * Normalizes an ability name
     */
    private static String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "");
    }
}
