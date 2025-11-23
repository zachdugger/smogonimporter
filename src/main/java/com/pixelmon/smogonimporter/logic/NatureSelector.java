package com.pixelmon.smogonimporter.logic;

import com.pixelmon.smogonimporter.data.MoveCounter;

import java.util.*;

/**
 * Selects optimal Pokemon natures based on moveset analysis.
 * Calculates which stats to boost/drop based on:
 * - Physical vs Special attackers
 * - Setup type
 * - Speed requirements
 */
public class NatureSelector {

    /**
     * Pokemon nature with stat modifications
     */
    public static class Nature {
        public final String name;
        public final String boosted;  // Stat that gets +10%
        public final String reduced;  // Stat that gets -10%

        public Nature(String name, String boosted, String reduced) {
            this.name = name;
            this.boosted = boosted;
            this.reduced = reduced;
        }

        public boolean isNeutral() {
            return boosted.equals(reduced);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // All 25 natures
    private static final List<Nature> ALL_NATURES = Arrays.asList(
        // Neutral natures
        new Nature("Hardy", "Attack", "Attack"),
        new Nature("Docile", "Defense", "Defense"),
        new Nature("Serious", "SpAtk", "SpAtk"),
        new Nature("Bashful", "SpDef", "SpDef"),
        new Nature("Quirky", "Speed", "Speed"),

        // Attack boosting
        new Nature("Lonely", "Attack", "Defense"),
        new Nature("Brave", "Attack", "Speed"),
        new Nature("Adamant", "Attack", "SpAtk"),
        new Nature("Naughty", "Attack", "SpDef"),

        // Defense boosting
        new Nature("Bold", "Defense", "Attack"),
        new Nature("Relaxed", "Defense", "Speed"),
        new Nature("Impish", "Defense", "SpAtk"),
        new Nature("Lax", "Defense", "SpDef"),

        // SpAtk boosting
        new Nature("Modest", "SpAtk", "Attack"),
        new Nature("Mild", "SpAtk", "Defense"),
        new Nature("Quiet", "SpAtk", "Speed"),
        new Nature("Rash", "SpAtk", "SpDef"),

        // SpDef boosting
        new Nature("Calm", "SpDef", "Attack"),
        new Nature("Gentle", "SpDef", "Defense"),
        new Nature("Sassy", "SpDef", "Speed"),
        new Nature("Careful", "SpDef", "SpAtk"),

        // Speed boosting
        new Nature("Timid", "Speed", "Attack"),
        new Nature("Hasty", "Speed", "Defense"),
        new Nature("Jolly", "Speed", "SpAtk"),
        new Nature("Naive", "Speed", "SpDef")
    );

    /**
     * Selects the optimal nature for a Pokemon based on its moveset and role
     *
     * @param counter Move counter analysis
     * @param species Pokemon species (for special cases)
     * @param role Pokemon's role (Gen9 only, null for Gen8)
     * @return Optimal nature name
     */
    public static String selectOptimalNature(MoveCounter counter, String species, String role) {
        String setupType = counter.getSetupType();
        int physicalMoves = counter.get("physicalpool");
        int specialMoves = counter.get("specialpool");

        // Determine if this is a physical, special, or mixed attacker
        boolean isPhysical = physicalMoves > specialMoves;
        boolean isSpecial = specialMoves > physicalMoves;
        boolean isMixed = physicalMoves > 0 && specialMoves > 0 &&
                         Math.abs(physicalMoves - specialMoves) <= 1;

        // Role-based nature selection (Gen9)
        if (role != null) {
            // Bulky roles: boost defenses
            if (role.contains("Bulky") || role.contains("Wall") || role.contains("Tank")) {
                if (isPhysical) {
                    return "Impish";  // +Def, -SpA
                } else if (isSpecial) {
                    return "Calm";  // +SpD, -Atk
                } else {
                    return "Bold";  // +Def, -Atk (default defensive)
                }
            }

            // Support roles: boost Speed or defenses
            if (role.contains("Support")) {
                return isPhysical ? "Jolly" : "Timid";
            }

            // Fast Attacker: always boost Speed
            if (role.contains("Fast Attacker")) {
                return isPhysical ? "Jolly" : "Timid";
            }

            // Wallbreaker: boost power
            if (role.contains("Wallbreaker")) {
                return isPhysical ? "Adamant" : "Modest";
            }

            // Setup Sweeper: usually speed, sometimes power
            if (role.contains("Setup") || role.contains("Sweeper")) {
                return isPhysical ? "Jolly" : "Timid";
            }
        }

        // Check for specific move-based decisions
        boolean hasTrickRoom = false;  // TODO: check for Trick Room
        boolean hasGyroball = false;   // TODO: check for Gyro Ball

        // Trick Room sets want -Speed nature
        if (hasTrickRoom) {
            if (isPhysical) {
                return "Brave";  // +Atk, -Spe
            } else if (isSpecial) {
                return "Quiet";  // +SpA, -Spe
            } else {
                return "Relaxed";  // +Def, -Spe (defensive)
            }
        }

        // Gyro Ball wants -Speed nature
        if (hasGyroball) {
            return "Brave";  // +Atk, -Spe
        }

        // Physical setup sweeper
        if ("Physical".equals(setupType) || isPhysical) {
            // Boost Attack or Speed, drop Special Attack
            List<Nature> candidates = Arrays.asList(
                getNature("Jolly"),   // +Spe, -SpA (preferred for speed)
                getNature("Adamant")  // +Atk, -SpA (preferred for power)
            );

            // Choose based on number of priority moves
            if (counter.get("priority") > 0) {
                return "Adamant";  // Don't need speed with priority
            }
            return "Jolly";  // Default to speed
        }

        // Special setup sweeper
        if ("Special".equals(setupType) || isSpecial) {
            // Boost Special Attack or Speed, drop Attack
            List<Nature> candidates = Arrays.asList(
                getNature("Timid"),   // +Spe, -Atk (preferred for speed)
                getNature("Modest")   // +SpA, -Atk (preferred for power)
            );

            // Choose based on move analysis
            if (counter.get("priority") > 0) {
                return "Modest";  // Don't need speed with priority
            }
            return "Timid";  // Default to speed
        }

        // Mixed attacker
        if (isMixed || "Mixed".equals(setupType)) {
            // Boost Speed, drop a defensive stat
            List<Nature> candidates = Arrays.asList(
                getNature("Hasty"),  // +Spe, -Def
                getNature("Naive")   // +Spe, -SpD
            );

            // Choose based on Pokemon's natural bulk
            // For now, default to Hasty
            return "Hasty";
        }

        // Defensive/Support Pokemon (no clear offensive role)
        if (counter.getDamagingMoveCount() <= 1) {
            // Boost defenses, drop offenses
            List<Nature> candidates = Arrays.asList(
                getNature("Bold"),    // +Def, -Atk
                getNature("Calm"),    // +SpD, -Atk
                getNature("Impish"),  // +Def, -SpA
                getNature("Careful")  // +SpD, -SpA
            );

            // Default to Bold (physical defense)
            return "Bold";
        }

        // Default: boost speed, drop unused offensive stat
        if (isPhysical || physicalMoves > 0) {
            return "Jolly";  // +Spe, -SpA
        } else {
            return "Timid";  // +Spe, -Atk
        }
    }

    /**
     * Gets a nature by name
     */
    private static Nature getNature(String name) {
        return ALL_NATURES.stream()
                .filter(n -> n.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(ALL_NATURES.get(0));  // Hardy as fallback
    }

    /**
     * Gets all natures that boost a specific stat
     */
    public static List<Nature> getNaturesBoostingStat(String stat) {
        List<Nature> result = new ArrayList<>();
        for (Nature nature : ALL_NATURES) {
            if (nature.boosted.equalsIgnoreCase(stat) && !nature.isNeutral()) {
                result.add(nature);
            }
        }
        return result;
    }

    /**
     * Gets all natures that reduce a specific stat
     */
    public static List<Nature> getNaturesReducingStat(String stat) {
        List<Nature> result = new ArrayList<>();
        for (Nature nature : ALL_NATURES) {
            if (nature.reduced.equalsIgnoreCase(stat) && !nature.isNeutral()) {
                result.add(nature);
            }
        }
        return result;
    }

    /**
     * Gets all neutral natures
     */
    public static List<Nature> getNeutralNatures() {
        List<Nature> result = new ArrayList<>();
        for (Nature nature : ALL_NATURES) {
            if (nature.isNeutral()) {
                result.add(nature);
            }
        }
        return result;
    }

    /**
     * Validates that a nature makes sense for the moveset
     * Returns true if the nature is reasonable
     */
    public static boolean isNatureValid(String natureName, MoveCounter counter) {
        Nature nature = getNature(natureName);

        // Neutral natures are always valid
        if (nature.isNeutral()) {
            return true;
        }

        int physicalMoves = counter.get("physicalpool");
        int specialMoves = counter.get("specialpool");

        // Don't boost Attack if no physical moves
        if ("Attack".equals(nature.boosted) && physicalMoves == 0) {
            return false;
        }

        // Don't boost SpAtk if no special moves
        if ("SpAtk".equals(nature.boosted) && specialMoves == 0) {
            return false;
        }

        // Don't reduce Attack if it's our main offense
        if ("Attack".equals(nature.reduced) && physicalMoves > specialMoves) {
            return false;
        }

        // Don't reduce SpAtk if it's our main offense
        if ("SpAtk".equals(nature.reduced) && specialMoves > physicalMoves) {
            return false;
        }

        return true;
    }
}
