package com.pixelmon.smogonimporter.logic;

import com.pixelmon.smogonimporter.data.MoveCounter;

import java.util.*;

/**
 * Optimizes EV and IV spreads for competitive Pokemon sets.
 * Ported from Pokemon Showdown's EV/IV optimization logic.
 *
 * Handles:
 * - Gen8 EV distribution (0-255 per stat, no 510 total limit)
 * - IV minimization for specific strategies (0 Atk for special attackers, etc.)
 * - HP EV optimization for Stealth Rock, Substitute, Belly Drum
 */
public class StatOptimizer {

    /**
     * Generates EVs with role-based spreads for Gen9, random for Gen8
     *
     * @param counter Move counter for determining stat priorities
     * @param random Random number generator
     * @param role Pokemon's role (Gen9 only, null for Gen8)
     * @return Map of stat -> EV value
     */
    public static Map<String, Integer> generateRandomEVs(MoveCounter counter, Random random, String role) {
        // Gen9: Use role-based spreads
        if (role != null) {
            return generateRoleBasedEVs(counter, role);
        }

        // Gen8: Use existing random logic
        return generateRandomEVsGen8(counter, random);
    }

    /**
     * Gen8 random EV generation (original logic)
     */
    private static Map<String, Integer> generateRandomEVsGen8(MoveCounter counter, Random random) {
        Map<String, Integer> evs = new HashMap<>();

        // Initialize all stats
        evs.put("hp", 0);
        evs.put("atk", 0);
        evs.put("def", 0);
        evs.put("spa", 0);
        evs.put("spd", 0);
        evs.put("spe", 0);

        String setupType = counter.getSetupType();
        int physicalMoves = counter.get("physicalpool");
        int specialMoves = counter.get("specialpool");

        // Determine if offensive or defensive
        boolean isOffensive = counter.getDamagingMoveCount() >= 3;

        // For Gen8 random battles, each stat gets random EVs 0-255
        for (String stat : evs.keySet()) {
            // Bias certain stats based on role
            int value;

            switch (stat) {
                case "hp":
                    // Always useful
                    value = 85;
                    break;

                case "atk":
                    // High if physical attacker, 0 if special only
                    if (physicalMoves > 0) {
                        value = 85;
                    } else {
                        value = 0;  // Minimize for confusion damage
                    }
                    break;

                case "def":
                    // Moderate on most sets
                    value = 85;
                    break;

                case "spa":
                    // High if special attacker, lower otherwise
                    if (specialMoves > 0) {
                        value = 85;
                    } else {
                        value = 85;
                    }
                    break;

                case "spd":
                    // Moderate on most sets
                    value = 85;
                    break;

                case "spe":
                    // High on offensive sets, moderate on defensive
                    if (isOffensive || !"".equals(setupType)) {
                        value = 85;
                    } else {
                        value = 85;
                    }
                    break;

                default:
                    value = 85;
            }

            evs.put(stat, value);
        }

        return evs;
    }

    /**
     * Optimizes HP EVs for specific strategies
     * Based on Pokemon Showdown's HP optimization logic
     *
     * @param evs Current EV spread
     * @param counter Move counter
     * @param baseHP Base HP stat of the Pokemon
     * @param level Pokemon level
     * @return Optimized HP EV value
     */
    public static int optimizeHPEVs(Map<String, Integer> evs, MoveCounter counter, int baseHP, int level) {
        int hpEV = evs.getOrDefault("hp", 85);

        // Check for moves that care about HP values
        boolean hasStealthRock = counter.get("stealthrock") > 0;  // Team has SR
        boolean hasSubstitute = counter.get("substitute") > 0;
        boolean hasBellyDrum = counter.get("bellydrum") > 0;

        // Optimize for Stealth Rock (minimize damage on switch-in)
        // Ideal: HP not divisible by 2 (takes 25% instead of 25%+1)
        if (hasStealthRock) {
            // Try to get HP that's odd
            for (int testEV = 85; testEV > 0; testEV -= 4) {
                int hp = calculateHP(baseHP, testEV, 31, level);
                if (hp % 2 == 1) {
                    return testEV;
                }
            }
        }

        // Optimize for Substitute (HP divisible by 4)
        if (hasSubstitute) {
            // Want HP divisible by 4 for 4 Substitutes
            for (int testEV = 85; testEV > 0; testEV -= 4) {
                int hp = calculateHP(baseHP, testEV, 31, level);
                if (hp % 4 == 0) {
                    return testEV;
                }
            }
        }

        // Belly Drum wants HP divisible by 2 but not 4
        if (hasBellyDrum) {
            for (int testEV = 85; testEV > 0; testEV -= 4) {
                int hp = calculateHP(baseHP, testEV, 31, level);
                if (hp % 2 == 0 && hp % 4 != 0) {
                    return testEV;
                }
            }
        }

        return hpEV;
    }

    /**
     * Generate role-based EV spreads for Gen9
     */
    private static Map<String, Integer> generateRoleBasedEVs(MoveCounter counter, String role) {
        Map<String, Integer> evs = new HashMap<>();

        int physicalMoves = counter.get("physicalpool");
        int specialMoves = counter.get("specialpool");
        String setupType = counter.getSetupType();

        // Bulky roles: HP/Def/SpD focus
        if (role.contains("Bulky") || role.contains("Wall") || role.contains("Tank")) {
            evs.put("hp", 252);
            evs.put("def", 252);
            evs.put("spd", 4);
            evs.put("atk", 0);
            evs.put("spa", 0);
            evs.put("spe", 0);
            return evs;
        }

        // Fast Attacker: Speed + offensive stat
        if (role.contains("Fast Attacker") || role.contains("Choice Scarf")) {
            evs.put("spe", 252);
            if (physicalMoves > specialMoves) {
                evs.put("atk", 252);
                evs.put("hp", 4);
                evs.put("spa", 0);
            } else {
                evs.put("spa", 252);
                evs.put("hp", 4);
                evs.put("atk", 0);
            }
            evs.put("def", 0);
            evs.put("spd", 0);
            return evs;
        }

        // Wallbreaker: Max offensive stat + Speed
        if (role.contains("Wallbreaker")) {
            evs.put("spe", 252);
            if (physicalMoves > specialMoves) {
                evs.put("atk", 252);
                evs.put("spa", 0);
            } else {
                evs.put("spa", 252);
                evs.put("atk", 0);
            }
            evs.put("hp", 4);
            evs.put("def", 0);
            evs.put("spd", 0);
            return evs;
        }

        // Setup Sweeper: Speed + offensive stat
        if (role.contains("Setup") || role.contains("Sweeper")) {
            evs.put("spe", 252);
            if (physicalMoves > specialMoves || !"".equals(setupType)) {
                evs.put("atk", 252);
                evs.put("spa", 0);
            } else {
                evs.put("spa", 252);
                evs.put("atk", 0);
            }
            evs.put("hp", 4);
            evs.put("def", 0);
            evs.put("spd", 0);
            return evs;
        }

        // Support: Mixed bulk
        if (role.contains("Support") || role.contains("Pivot")) {
            evs.put("hp", 252);
            evs.put("def", 128);
            evs.put("spd", 128);
            evs.put("atk", 0);
            evs.put("spa", 0);
            evs.put("spe", 0);
            return evs;
        }

        // Default: Balanced offensive spread
        evs.put("hp", 4);
        evs.put("spe", 252);
        if (physicalMoves > specialMoves) {
            evs.put("atk", 252);
            evs.put("spa", 0);
        } else {
            evs.put("spa", 252);
            evs.put("atk", 0);
        }
        evs.put("def", 0);
        evs.put("spd", 0);
        return evs;
    }

    /**
     * Generates optimal IVs based on moveset and role
     *
     * @param counter Move counter
     * @param role Pokemon's role (Gen9 only, null for Gen8)
     * @return Map of stat -> IV value (usually 31, but 0 for specific cases)
     */
    public static Map<String, Integer> generateOptimalIVs(MoveCounter counter, String role) {
        Map<String, Integer> ivs = new HashMap<>();

        // Default: all perfect IVs
        ivs.put("hp", 31);
        ivs.put("atk", 31);
        ivs.put("def", 31);
        ivs.put("spa", 31);
        ivs.put("spd", 31);
        ivs.put("spe", 31);

        int physicalMoves = counter.get("physicalpool");
        int specialMoves = counter.get("specialpool");

        // Minimize Attack IV if no physical moves (reduces confusion damage)
        if (physicalMoves == 0 && specialMoves > 0) {
            ivs.put("atk", 0);
        }

        // Minimize Speed IV for Gyro Ball or Trick Room
        // TODO: Check for these specific moves
        boolean hasGyroball = false;
        boolean hasTrickRoom = false;

        if (hasGyroball || hasTrickRoom) {
            ivs.put("spe", 0);
        }

        return ivs;
    }

    /**
     * Calculates actual HP stat
     * Formula: floor((2 * Base + IV + floor(EV/4)) * Level / 100) + Level + 10
     */
    private static int calculateHP(int base, int ev, int iv, int level) {
        return (int) Math.floor((2.0 * base + iv + Math.floor(ev / 4.0)) * level / 100.0) + level + 10;
    }

    /**
     * Calculates actual stat (non-HP)
     * Formula: (floor((2 * Base + IV + floor(EV/4)) * Level / 100) + 5) * Nature
     */
    private static int calculateStat(int base, int ev, int iv, int level, double natureMod) {
        return (int) Math.floor(
            (Math.floor((2.0 * base + iv + Math.floor(ev / 4.0)) * level / 100.0) + 5) * natureMod
        );
    }

    /**
     * Validates that an EV spread is legal
     */
    public static boolean isValidEVSpread(Map<String, Integer> evs, int generation) {
        int total = 0;

        for (Map.Entry<String, Integer> entry : evs.entrySet()) {
            int value = entry.getValue();

            // Each EV must be 0-255
            if (value < 0 || value > 255) {
                return false;
            }

            total += value;
        }

        // Gen 3-7: Total must not exceed 510
        // Gen 8+: No total limit (each stat independent)
        if (generation < 8 && total > 510) {
            return false;
        }

        return true;
    }

    /**
     * Validates that an IV spread is legal
     */
    public static boolean isValidIVSpread(Map<String, Integer> ivs) {
        for (Integer value : ivs.values()) {
            if (value < 0 || value > 31) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a balanced EV spread (85 in each stat for Gen8)
     */
    public static Map<String, Integer> createBalancedEVs() {
        Map<String, Integer> evs = new HashMap<>();
        evs.put("hp", 85);
        evs.put("atk", 85);
        evs.put("def", 85);
        evs.put("spa", 85);
        evs.put("spd", 85);
        evs.put("spe", 85);
        return evs;
    }

    /**
     * Creates perfect IVs (31 in all stats)
     */
    public static Map<String, Integer> createPerfectIVs() {
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
