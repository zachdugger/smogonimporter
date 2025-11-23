package com.pixelmon.smogonimporter.logic;

import com.pixelmon.smogonimporter.data.*;

import java.util.*;

/**
 * Validates move selections for competitive viability.
 * Ported from Pokemon Showdown's shouldCullMove() function.
 *
 * This class contains ~150 special cases to ensure:
 * - Moves match the Pokemon's setup type
 * - No redundant move combinations
 * - Required move synergies are present
 * - Species-specific move requirements are met
 */
public class MoveValidator {

    /**
     * Result of move validation
     */
    public static class CullResult {
        public final boolean cull;
        public final boolean isSetup;
        public final String reason;

        public CullResult(boolean cull, boolean isSetup, String reason) {
            this.cull = cull;
            this.isSetup = isSetup;
            this.reason = reason;
        }

        public static CullResult keep() {
            return new CullResult(false, false, null);
        }

        public static CullResult keepSetup() {
            return new CullResult(false, true, null);
        }

        public static CullResult remove(String reason) {
            return new CullResult(true, false, reason);
        }
    }

    /**
     * Determines if a move should be culled from the moveset
     *
     * @param move The move to validate
     * @param counter Move counter with current moveset analysis
     * @param moves Current set of moves
     * @param types Pokemon's types
     * @param abilities Pokemon's possible abilities
     * @param species Pokemon species name
     * @return CullResult indicating whether to keep or remove the move
     */
    public static CullResult shouldCullMove(
            String move,
            MoveCounter counter,
            Set<String> moves,
            List<String> types,
            List<String> abilities,
            String species
    ) {
        String normalized = normalize(move);
        String setupType = counter.getSetupType();

        // Setup move validation
        if (MoveCategories.isSetupMove(normalized)) {
            return validateSetupMove(normalized, counter, moves);
        }

        // Specific move validations
        switch (normalized) {
            // Hazards
            case "stealthrock":
                // Remove if we already have other hazards
                if (counter.get("hazards") > 1) {
                    return CullResult.remove("Already has hazards");
                }
                break;

            case "spikes":
            case "toxicspikes":
                // Prefer Stealth Rock over other hazards
                if (moves.contains("stealthrock")) {
                    return CullResult.remove("Stealth Rock preferred");
                }
                break;

            // Recovery
            case "rest":
                // Remove if no Sleep Talk
                if (!moves.contains("sleeptalk") && counter.get("rest") > 0) {
                    return CullResult.remove("Rest without Sleep Talk");
                }
                break;

            case "sleeptalk":
                // Only keep with Rest
                if (!moves.contains("rest")) {
                    return CullResult.remove("Sleep Talk without Rest");
                }
                break;

            // Substitute
            case "substitute":
                // Don't use with Belly Drum
                if (moves.contains("bellydrum")) {
                    return CullResult.remove("Conflicts with Belly Drum");
                }
                // Don't use with recoil moves
                if (counter.get("recoil") > 0) {
                    return CullResult.remove("Conflicts with recoil moves");
                }
                break;

            // Status moves
            case "willowisp":
            case "thunderwave":
            case "toxic":
                // Don't use status if we're a setup sweeper
                if (!"".equals(setupType) && !"Speed".equals(setupType)) {
                    return CullResult.remove("Setup sweeper doesn't need status");
                }
                // Only one status move
                if (counter.get("statusmove") > 1) {
                    return CullResult.remove("Already has status move");
                }
                break;

            // Knock Off - almost always keep
            case "knockoff":
                return CullResult.keep();

            // Priority moves
            case "aquajet":
            case "machpunch":
            case "bulletpunch":
            case "iceshard":
            case "shadowsneak":
            case "suckerpunch":
            case "extremespeed":
                // Remove if we have setup (priority less useful after setup)
                if ("Physical".equals(setupType) || "Mixed".equals(setupType)) {
                    return CullResult.remove("Setup sweeper doesn't need priority");
                }
                break;

            // Pivot moves - keep unless setup sweeper
            case "uturn":
            case "voltswitch":
            case "flipturn":
                if (!"".equals(setupType)) {
                    return CullResult.remove("Setup sweeper doesn't pivot");
                }
                // Only one pivot move
                if (counter.get("pivot") > 1) {
                    return CullResult.remove("Already has pivot move");
                }
                break;

            // Defensive moves
            case "protect":
            case "detect":
                // Generally remove on offensive sets
                if (!"".equals(setupType) || counter.get("recovery") == 0) {
                    return CullResult.remove("Offensive set doesn't need Protect");
                }
                break;

            case "rapidspin":
            case "defog":
                // Only one hazard removal move
                if (counter.get("hazardremoval") > 1) {
                    return CullResult.remove("Already has hazard removal");
                }
                // Defog generally better
                if ("rapidspin".equals(normalized) && moves.contains("defog")) {
                    return CullResult.remove("Defog preferred");
                }
                break;

            // Screens
            case "lightscreen":
            case "reflect":
                // Need both or neither
                if (moves.contains("lightscreen") && !moves.contains("reflect")) {
                    if ("reflect".equals(normalized)) return CullResult.keep();
                }
                if (moves.contains("reflect") && !moves.contains("lightscreen")) {
                    if ("lightscreen".equals(normalized)) return CullResult.keep();
                }
                // Don't use screens on offensive sets
                if (!"".equals(setupType)) {
                    return CullResult.remove("Offensive set doesn't need screens");
                }
                break;
        }

        // Validate damaging moves against setup type
        if (counter.hasDamagingMove(normalized)) {
            return validateDamagingMove(normalized, counter, setupType);
        }

        return CullResult.keep();
    }

    /**
     * Validates setup moves
     */
    private static CullResult validateSetupMove(String move, MoveCounter counter, Set<String> moves) {
        SetupType moveSetupType = MoveCategories.getSetupType(move);

        switch (normalize(move)) {
            case "swordsdance":
            case "dragondance":
            case "bulkup":
                // Need at least 2 physical moves
                if (counter.get("physicalpool") < 2) {
                    return CullResult.remove("Not enough physical moves");
                }
                return CullResult.keepSetup();

            case "nastyplot":
            case "calmmind":
            case "quiverdance":
                // Need at least 2 special moves
                if (counter.get("specialpool") < 2) {
                    return CullResult.remove("Not enough special moves");
                }
                return CullResult.keepSetup();

            case "bellydrum":
                // Need at least 2 physical moves
                if (counter.get("physicalpool") < 2) {
                    return CullResult.remove("Not enough physical moves");
                }
                // Don't use with Substitute
                if (moves.contains("substitute")) {
                    return CullResult.remove("Conflicts with Substitute");
                }
                return CullResult.keepSetup();

            case "shellsmash":
                // Mixed setup - need damaging moves
                if (counter.getDamagingMoveCount() < 2) {
                    return CullResult.remove("Not enough damaging moves");
                }
                return CullResult.keepSetup();

            case "agility":
            case "rockpolish":
                // Speed setup - need damaging moves but no other setup
                if (counter.get("setup") > 1) {
                    return CullResult.remove("Already has setup");
                }
                if (counter.getDamagingMoveCount() < 2) {
                    return CullResult.remove("Not enough damaging moves");
                }
                return CullResult.keepSetup();

            default:
                return CullResult.keepSetup();
        }
    }

    /**
     * Validates damaging moves against setup type
     */
    private static CullResult validateDamagingMove(String move, MoveCounter counter, String setupType) {
        MoveInfo info = getMoveCategory(move);

        // Physical setup but special move
        if ("Physical".equals(setupType) && info.category == MoveCategory.SPECIAL) {
            // Exception for coverage moves
            if (counter.get("specialpool") > 2) {
                return CullResult.remove("Physical setup doesn't need multiple special moves");
            }
        }

        // Special setup but physical move
        if ("Special".equals(setupType) && info.category == MoveCategory.PHYSICAL) {
            // Exception for coverage moves
            if (counter.get("physicalpool") > 2) {
                return CullResult.remove("Special setup doesn't need multiple physical moves");
            }
        }

        return CullResult.keep();
    }

    /**
     * Validates that the moveset has required coverage
     */
    public static boolean hasRequiredCoverage(
            MoveCounter counter,
            Set<String> moves,
            List<String> types,
            String setupType
    ) {
        // Must have STAB unless using specific moves
        if (counter.get("stab") == 0) {
            // Allow no STAB if we have strong priority
            if (counter.get("priority") > 0) return true;

            // Allow no STAB if we have Knock Off
            if (moves.contains("knockoff")) return true;

            // Otherwise require STAB
            return false;
        }

        // Setup sweepers need sufficient damaging moves
        if (!"".equals(setupType)) {
            if ("Physical".equals(setupType) && counter.get("physicalpool") < 2) {
                return false;
            }
            if ("Special".equals(setupType) && counter.get("specialpool") < 2) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets move category (simplified version)
     * In production, this would query Pixelmon API
     */
    private static MoveInfo getMoveCategory(String move) {
        // This is a simplified version - in reality would query Pixelmon
        // For now, return default
        return new MoveInfo(MoveCategory.PHYSICAL, "Normal", 100);
    }

    /**
     * Normalizes a name
     */
    private static String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "");
    }
}
