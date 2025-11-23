package com.pixelmon.smogonimporter.logic;

import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.data.*;

import java.util.*;

/**
 * Analyzes a Pokemon's moveset to determine strategic properties and synergies.
 * Ported from Pokemon Showdown's queryMoves() function.
 *
 * This class examines moves to identify:
 * - STAB coverage
 * - Physical/Special/Status distribution
 * - Setup type
 * - Ability synergies
 * - Strategic properties (priority, hazards, recovery, etc.)
 */
public class MoveAnalyzer {

    // Hardcoded move data (normally would come from a database or API)
    // For now, we'll use a simple map structure
    private static final Map<String, MoveInfo> MOVE_DATA = initializeMoveData();

    /**
     * Analyzes a set of moves and returns detailed statistics
     *
     * @param moves List of move names
     * @param types List of Pokemon's types
     * @param abilities List of Pokemon's possible abilities
     * @return MoveCounter with all tracked properties
     */
    public static MoveCounter queryMoves(
            Set<String> moves,
            List<String> types,
            List<String> abilities
    ) {
        MoveCounter counter = new MoveCounter();

        Set<String> typesSet = new HashSet<>();
        for (String type : types) {
            typesSet.add(normalize(type));
        }

        Set<String> normalizedAbilities = new HashSet<>();
        for (String ability : abilities) {
            normalizedAbilities.add(normalize(ability));
        }

        // Analyze each move
        for (String move : moves) {
            String normalized = normalize(move);
            MoveInfo info = getMoveInfo(normalized);

            // Count by category
            counter.add(info.category.getDisplayName());

            if (info.category.isDamaging()) {
                counter.addDamagingMove(normalized);

                // Count by type
                String moveType = normalize(info.type);
                counter.add(moveType);

                // Check for STAB
                if (typesSet.contains(moveType) || MoveCategories.contains(MoveCategories.NO_STAB, normalized)) {
                    counter.add("stab");
                }

                // Count damaging moves by category
                if (info.category == MoveCategory.PHYSICAL) {
                    counter.add("physicalpool");
                } else if (info.category == MoveCategory.SPECIAL) {
                    counter.add("specialpool");
                }
            }

            // Setup moves
            SetupType setupType = MoveCategories.getSetupType(normalized);
            if (setupType != SetupType.NONE) {
                counter.add("setup");
                updateSetupType(counter, setupType);

                if (setupType == SetupType.PHYSICAL) counter.add("physicalsetup");
                else if (setupType == SetupType.SPECIAL) counter.add("specialsetup");
                else if (setupType == SetupType.MIXED) counter.add("mixedsetup");
                else if (setupType == SetupType.SPEED) counter.add("speedsetup");
            }

            // Hazards
            if (MoveCategories.contains(MoveCategories.HAZARDS, normalized)) {
                counter.add("hazards");
                if ("stealthrock".equals(normalized)) counter.add("stealthrock");
                if ("spikes".equals(normalized)) counter.add("spikes");
                if ("stickyweb".equals(normalized)) counter.add("stickyweb");
                if ("toxicspikes".equals(normalized)) counter.add("toxicspikes");
            }

            // Recovery
            if (MoveCategories.contains(MoveCategories.RECOVERY_MOVES, normalized)) {
                counter.add("recovery");
            }

            // Priority
            if (MoveCategories.contains(MoveCategories.PRIORITY_MOVES, normalized)) {
                counter.add("priority");
            }

            // Recoil
            if (MoveCategories.contains(MoveCategories.RECOIL_MOVES, normalized)) {
                counter.add("recoil");
            }

            // Drain
            if (MoveCategories.contains(MoveCategories.DRAIN_MOVES, normalized)) {
                counter.add("drain");
            }

            // Pivot
            if (MoveCategories.contains(MoveCategories.PIVOT_MOVES, normalized)) {
                counter.add("pivot");
            }

            // Sound
            if (MoveCategories.contains(MoveCategories.SOUND_MOVES, normalized)) {
                counter.add("sound");
            }

            // Ability synergies
            checkAbilitySynergies(counter, normalized, normalizedAbilities);

            // Specific move tracking
            trackSpecificMoves(counter, normalized);
        }

        return counter;
    }

    /**
     * Updates the setup type based on the new setup move
     */
    private static void updateSetupType(MoveCounter counter, SetupType newType) {
        String current = counter.getSetupType();
        SetupType currentType = SetupType.fromString(current);

        if (currentType == SetupType.NONE) {
            counter.setSetupType(newType.getDisplayName());
        } else if (currentType != newType && newType != SetupType.SPEED) {
            // If we have different offensive setup types, it's mixed
            if (currentType == SetupType.PHYSICAL && newType == SetupType.SPECIAL ||
                currentType == SetupType.SPECIAL && newType == SetupType.PHYSICAL) {
                counter.setSetupType(SetupType.MIXED.getDisplayName());
            }
        }
    }

    /**
     * Checks for ability synergies with moves
     */
    private static void checkAbilitySynergies(MoveCounter counter, String move, Set<String> abilities) {
        // Skill Link - multi-hit moves
        if (MoveCategories.contains(MoveCategories.SKILL_LINK_MOVES, move)) {
            counter.add("skilllink");
        }

        // Iron Fist - punch moves
        if (MoveCategories.contains(MoveCategories.PUNCH_MOVES, move)) {
            counter.add("ironfist");
        }

        // Strong Jaw - bite moves
        if (MoveCategories.contains(MoveCategories.BITE_MOVES, move)) {
            counter.add("strongjaw");
        }

        // Contrary - moves with stat drops
        if (MoveCategories.contains(MoveCategories.CONTRARY_MOVES, move)) {
            counter.add("contrary");
        }

        // Sheer Force - moves with secondary effects
        if (MoveCategories.contains(MoveCategories.SHEER_FORCE_MOVES, move)) {
            counter.add("sheerforce");
        }

        // Tough Claws - contact moves
        if (MoveCategories.contains(MoveCategories.CONTACT_MOVES, move)) {
            counter.add("toughclaws");
        }

        // Specific ability checks
        if (abilities.contains("technician")) {
            MoveInfo info = getMoveInfo(move);
            if (info.basePower > 0 && info.basePower <= 60) {
                counter.add("technician");
            }
        }

        if (abilities.contains("adaptability") && counter.get("stab") > 0) {
            counter.add("adaptability");
        }
    }

    /**
     * Tracks specific important moves
     */
    private static void trackSpecificMoves(MoveCounter counter, String move) {
        switch (move) {
            case "bellydrum":
                counter.add("bellydrum");
                break;
            case "substitute":
                counter.add("substitute");
                break;
            case "rest":
                counter.add("rest");
                break;
            case "sleeptalk":
                counter.add("sleeptalk");
                break;
            case "knockoff":
                counter.add("knockoff");
                break;
            case "rapidspin":
            case "defog":
                counter.add("hazardremoval");
                break;
            case "lightscreen":
            case "reflect":
                counter.add("screens");
                break;
            case "willowisp":
            case "thunderwave":
            case "toxic":
                counter.add("statusmove");
                break;
        }
    }

    /**
     * Gets move info (category, type, power)
     * Now uses SmogonDataRegistry, MoveDatabase, and fallbacks!
     */
    private static MoveInfo getMoveInfo(String move) {
        // Try to get from registry first (comprehensive data)
        SmogonDataRegistry registry = SmogonImporter.getDataManager().getDataRegistry();
        if (registry != null && registry.isInitialized()) {
            MoveDataEntry moveData = registry.getMove(move);
            if (moveData != null) {
                return new MoveInfo(
                    moveData.getCategoryEnum(),
                    moveData.getType(),
                    moveData.getBasePower()
                );
            }
        }

        // Try MoveDatabase (424 competitive moves extracted from Gen9 data)
        // This fixes the Kyogre bug where Water Spout, Origin Pulse, Thunder weren't recognized
        MoveCategory category = MoveDatabase.getCategory(move);
        if (category != null) {
            // We have the category, but not type/power - use smart defaults
            String type = guessTypeFromName(move);
            int basePower = category.isDamaging() ? 80 : 0; // Reasonable default
            return new MoveInfo(category, type, basePower);
        }

        // Fallback to small hardcoded map (legacy support)
        MoveInfo info = MOVE_DATA.get(normalize(move));
        if (info != null) {
            return info;
        }

        // Last resort: guess STATUS by name
        return new MoveInfo(MoveCategory.STATUS, "Normal", 0);
    }

    /**
     * Guess move type from name patterns
     * Used when we have category but not full move data
     */
    private static String guessTypeFromName(String move) {
        String normalized = normalize(move);

        // Water moves
        if (normalized.contains("water") || normalized.contains("aqua") || normalized.contains("hydro")) {
            return "Water";
        }
        // Electric moves
        if (normalized.contains("thunder") || normalized.contains("electric") || normalized.contains("volt") || normalized.contains("shock")) {
            return "Electric";
        }
        // Fire moves
        if (normalized.contains("fire") || normalized.contains("flame") || normalized.contains("blaze") || normalized.contains("heat")) {
            return "Fire";
        }
        // Ice moves
        if (normalized.contains("ice") || normalized.contains("frost") || normalized.contains("freeze") || normalized.contains("blizzard")) {
            return "Ice";
        }
        // Grass moves
        if (normalized.contains("leaf") || normalized.contains("seed") || normalized.contains("vine") || normalized.contains("petal")) {
            return "Grass";
        }
        // Psychic moves
        if (normalized.contains("psychic") || normalized.contains("psych") || normalized.contains("confusion")) {
            return "Psychic";
        }
        // Dark moves
        if (normalized.contains("dark") || normalized.contains("bite") || normalized.contains("crunch")) {
            return "Dark";
        }
        // Dragon moves
        if (normalized.contains("dragon") || normalized.contains("draco")) {
            return "Dragon";
        }
        // Ground moves
        if (normalized.contains("earth") || normalized.contains("dig") || normalized.contains("ground")) {
            return "Ground";
        }
        // Rock moves
        if (normalized.contains("rock") || normalized.contains("stone")) {
            return "Rock";
        }
        // Steel moves
        if (normalized.contains("steel") || normalized.contains("iron") || normalized.contains("metal")) {
            return "Steel";
        }
        // Fighting moves
        if (normalized.contains("punch") || normalized.contains("kick") || normalized.contains("combat") || normalized.contains("chop")) {
            return "Fighting";
        }
        // Poison moves
        if (normalized.contains("poison") || normalized.contains("toxic") || normalized.contains("acid") || normalized.contains("sludge")) {
            return "Poison";
        }
        // Bug moves
        if (normalized.contains("bug") || normalized.contains("sting")) {
            return "Bug";
        }
        // Ghost moves
        if (normalized.contains("shadow") || normalized.contains("phantom") || normalized.contains("hex") || normalized.contains("curse")) {
            return "Ghost";
        }
        // Flying moves
        if (normalized.contains("wing") || normalized.contains("aerial") || normalized.contains("gust") || normalized.contains("peck")) {
            return "Flying";
        }
        // Fairy moves
        if (normalized.contains("fairy") || normalized.contains("charm") || normalized.contains("moon") || normalized.contains("play")) {
            return "Fairy";
        }

        // Default to Normal
        return "Normal";
    }

    /**
     * Normalizes a name (lowercase, no spaces/hyphens)
     */
    private static String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "");
    }


    /**
     * Initialize hardcoded move data
     * NOTE: This should be replaced with Pixelmon API calls in production
     */
    private static Map<String, MoveInfo> initializeMoveData() {
        Map<String, MoveInfo> data = new HashMap<>();

        // Physical moves
        data.put("earthquake", new MoveInfo(MoveCategory.PHYSICAL, "Ground", 100));
        data.put("closecombat", new MoveInfo(MoveCategory.PHYSICAL, "Fighting", 120));
        data.put("uturn", new MoveInfo(MoveCategory.PHYSICAL, "Bug", 70));
        data.put("knockoff", new MoveInfo(MoveCategory.PHYSICAL, "Dark", 65));
        data.put("extremespeed", new MoveInfo(MoveCategory.PHYSICAL, "Normal", 80));
        data.put("waterfall", new MoveInfo(MoveCategory.PHYSICAL, "Water", 80));
        data.put("bravebird", new MoveInfo(MoveCategory.PHYSICAL, "Flying", 120));
        data.put("ironhead", new MoveInfo(MoveCategory.PHYSICAL, "Steel", 80));
        data.put("poisonjab", new MoveInfo(MoveCategory.PHYSICAL, "Poison", 80));

        // Special moves
        data.put("thunderbolt", new MoveInfo(MoveCategory.SPECIAL, "Electric", 90));
        data.put("icebeam", new MoveInfo(MoveCategory.SPECIAL, "Ice", 90));
        data.put("flamethrower", new MoveInfo(MoveCategory.SPECIAL, "Fire", 90));
        data.put("surf", new MoveInfo(MoveCategory.SPECIAL, "Water", 90));
        data.put("psychic", new MoveInfo(MoveCategory.SPECIAL, "Psychic", 90));
        data.put("focusblast", new MoveInfo(MoveCategory.SPECIAL, "Fighting", 120));
        data.put("shadowball", new MoveInfo(MoveCategory.SPECIAL, "Ghost", 80));
        data.put("energyball", new MoveInfo(MoveCategory.SPECIAL, "Grass", 90));
        data.put("dracometeor", new MoveInfo(MoveCategory.SPECIAL, "Dragon", 130));

        // Status moves
        data.put("stealthrock", new MoveInfo(MoveCategory.STATUS, "Rock", 0));
        data.put("spikes", new MoveInfo(MoveCategory.STATUS, "Ground", 0));
        data.put("swordsdance", new MoveInfo(MoveCategory.STATUS, "Normal", 0));
        data.put("nastyplot", new MoveInfo(MoveCategory.STATUS, "Dark", 0));
        data.put("willowisp", new MoveInfo(MoveCategory.STATUS, "Fire", 0));
        data.put("toxic", new MoveInfo(MoveCategory.STATUS, "Poison", 0));
        data.put("recover", new MoveInfo(MoveCategory.STATUS, "Normal", 0));
        data.put("roost", new MoveInfo(MoveCategory.STATUS, "Flying", 0));
        data.put("substitute", new MoveInfo(MoveCategory.STATUS, "Normal", 0));
        data.put("protect", new MoveInfo(MoveCategory.STATUS, "Normal", 0));

        return data;
    }
}
