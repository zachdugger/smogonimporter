package com.pixelmon.smogonimporter.logic;

import com.pixelmon.smogonimporter.data.MoveCounter;

import java.util.*;

/**
 * Selects optimal held items using a 3-tier priority system.
 * Ported from Pokemon Showdown's item selection logic.
 *
 * Priority tiers:
 * 1. High Priority - Required items, forme-specific, move synergies
 * 2. Medium Priority - Choice items, Life Orb, Assault Vest
 * 3. Low Priority - Focus Sash, Heavy-Duty Boots, utility items
 * 4. Default - Leftovers/Sitrus Berry
 */
public class ItemSelector {

    /**
     * Selects the best item for a Pokemon based on its moveset and role
     *
     * @param counter Move counter analysis
     * @param moves Set of moves
     * @param ability Pokemon's ability
     * @param types Pokemon's types
     * @param species Pokemon species name
     * @param availableItems Items available from JSON data (unused for now)
     * @param role Pokemon's role (Gen9 only, null for Gen8)
     * @return Selected item name
     */
    public static String selectItem(
            MoveCounter counter,
            Set<String> moves,
            String ability,
            List<String> types,
            String species,
            List<String> availableItems,
            String role
    ) {
        // Try each priority tier in order
        String item;

        item = getHighPriorityItem(counter, moves, ability, types, species, role);
        if (item != null) return item;

        item = getMediumPriorityItem(counter, moves, ability, types, species, role);
        if (item != null) return item;

        item = getLowPriorityItem(counter, moves, ability, types, species, role);
        if (item != null) return item;

        // Default item (role-based)
        return getDefaultItem(counter, role);
    }

    /**
     * HIGH PRIORITY ITEMS
     * Required items, forme-specific items, strong move synergies, role-specific
     */
    private static String getHighPriorityItem(
            MoveCounter counter,
            Set<String> moves,
            String ability,
            List<String> types,
            String species,
            String role
    ) {
        // Role-based priority items (Gen9)
        if (role != null) {
            if (role.equals("AV Pivot")) {
                return "Assault Vest";
            }
            if (role.contains("Booster Energy") || role.equals("Fast Bulky Setup")) {
                return "Booster Energy";
            }
            if (role.equals("Tera Blast user") && moves.contains("terablast")) {
                return "Expert Belt";
            }
        }
        String normalized = normalize(species);

        // ARCEUS: Select plate based on type
        if (normalized.startsWith("arceus")) {
            return getArceusPlate(types, normalized);
        }

        // SILVALLY: Select memory based on type
        if (normalized.startsWith("silvally")) {
            return getSilvallyMemory(types, normalized);
        }

        // GENESECT: Select drive based on forme
        if (normalized.startsWith("genesect")) {
            return getGenesectDrive(normalized);
        }

        // Species-specific required items
        switch (normalized) {
            case "pikachu":
                return "Light Ball";

            case "marowak":
            case "marowakalola":
            case "marowakalolatotem":
                return "Thick Club";

            case "cubone":
                return "Thick Club";

            case "farfetchd":
            case "farfetchdgalar":
            case "sirfetchd":
                return "Leek";

            case "clamperl":
                return "DeepSeaTooth";  // or DeepSeaScale

            case "ditto":
                return "Choice Scarf";

            case "dialga":
            case "dialgaorigin":
                return "Adamant Orb";

            case "palkia":
            case "palkiaorigin":
                return "Lustrous Orb";

            case "giratina":
            case "giratinaorigin":
                return "Griseous Orb";

            case "groudon":
            case "groudonprimal":
                return "Red Orb";

            case "kyogre":
            case "kyogreprimal":
                return "Blue Orb";

            case "latios":
            case "latiosmega":
                return "Soul Dew";

            case "latias":
            case "latiasmega":
                return "Soul Dew";

            case "rayquaza":
                // Rayquaza can't hold items when mega
                return null;
        }

        // Eviolite for NFE (Not Fully Evolved) Pokemon
        // This would require checking if the Pokemon can evolve
        // For now, hardcode some common ones
        if (isNFE(normalized)) {
            return "Eviolite";
        }

        // Mega Stones (if applicable)
        // Would need to check if species has mega evolution

        // Move-synergy items
        if (moves.contains("geomancy")) {
            return "Power Herb";
        }

        if (moves.contains("acrobatics")) {
            return null;  // No item for Acrobatics
        }

        if (moves.contains("fling")) {
            return "Iron Ball";  // Or other Fling item
        }

        // Ability-synergy items
        if (normalize(ability).equals("unburden")) {
            return "Sitrus Berry";  // Consumable for Unburden
        }

        return null;  // No high-priority item
    }

    /**
     * MEDIUM PRIORITY ITEMS
     * Choice items, Life Orb, Assault Vest, role-influenced
     */
    private static String getMediumPriorityItem(
            MoveCounter counter,
            Set<String> moves,
            String ability,
            List<String> types,
            String species,
            String role
    ) {
        String setupType = counter.getSetupType();
        int physicalMoves = counter.get("physicalpool");
        int specialMoves = counter.get("specialpool");
        int moveCount = moves.size();

        // Role-based item preferences (Gen9)
        if (role != null) {
            // Wallbreaker roles prefer Life Orb or Choice items
            if (role.contains("Wallbreaker") && counter.getDamagingMoveCount() >= 3) {
                return allDamagingMovesCheck(physicalMoves, specialMoves, moveCount) ?
                    (Math.random() < 0.5 ? "Life Orb" : getChoiceItem(physicalMoves, specialMoves, counter)) :
                    "Life Orb";
            }

            // Fast Attacker roles prefer Choice Scarf
            if (role.contains("Fast Attacker") && allDamagingMovesCheck(physicalMoves, specialMoves, moveCount)) {
                return "Choice Scarf";
            }

            // Bulky roles generally avoid Choice items
            if (isBulkyRole(role)) {
                // Skip to low priority items
                return null;
            }
        }

        // No Choice items with setup moves
        if (!"".equals(setupType)) {
            // Life Orb for setup sweepers
            if (counter.getDamagingMoveCount() >= 3) {
                return "Life Orb";
            }
            return null;
        }

        // FIXED: Choice items ONLY if ALL moves are damaging
        // This prevents Choice Band + Aurora Veil situations
        boolean allPhysical = physicalMoves >= moveCount;
        boolean allSpecial = specialMoves >= moveCount;
        boolean allDamaging = allPhysical || allSpecial ||
                             (physicalMoves + specialMoves >= moveCount);

        // Problematic moves that should never get Choice items
        Set<String> choiceIncompatible = new HashSet<>(Arrays.asList(
            "dragontail", "fakeout", "firstimpression", "flamecharge",
            "rapidspin", "partingshot", "healingwish", "switcheroo", "trick"
        ));

        boolean hasIncompatibleMove = moves.stream()
            .anyMatch(move -> choiceIncompatible.contains(normalize(move)));

        // Choice items only if ALL moves are damaging and no incompatible moves
        if (allDamaging && !hasIncompatibleMove) {
            // Choice Scarf for fast attackers
            if (counter.get("priority") == 0) {
                // 30% chance for Choice Scarf
                if (Math.random() < 0.3) {
                    return "Choice Scarf";
                }
            }

            // Choice Band for physical attackers
            if (physicalMoves > specialMoves) {
                return "Choice Band";
            }

            // Choice Specs for special attackers
            if (specialMoves > physicalMoves) {
                return "Choice Specs";
            }
        }

        // Assault Vest for bulky attackers (no status moves)
        if (counter.get("Status") == 0 && counter.getDamagingMoveCount() >= 3) {
            return "Assault Vest";
        }

        // Life Orb for mixed attackers
        if (physicalMoves > 0 && specialMoves > 0) {
            return "Life Orb";
        }

        return null;
    }

    /**
     * LOW PRIORITY ITEMS
     * Focus Sash, Heavy-Duty Boots, utility items, role-influenced
     */
    private static String getLowPriorityItem(
            MoveCounter counter,
            Set<String> moves,
            String ability,
            List<String> types,
            String species,
            String role
    ) {
        // Bulky roles prefer Heavy-Duty Boots or defensive items
        if (role != null && isBulkyRole(role)) {
            if (isStealthRockWeak(types)) {
                return "Heavy-Duty Boots";
            }
            // Skip to default (Leftovers) for bulky roles
            return null;
        }
        // Heavy-Duty Boots for Stealth Rock-weak Pokemon
        if (isStealthRockWeak(types)) {
            return "Heavy-Duty Boots";
        }

        // Focus Sash for frail leads
        if (counter.get("hazards") > 0) {
            return "Focus Sash";
        }

        // Expert Belt for good coverage
        if (counter.get("stab") > 0 && counter.getDamagingMoveCount() >= 3) {
            return "Expert Belt";
        }

        // Lum Berry for status-vulnerable Pokemon
        // (would need to check if Pokemon is weak to status)

        // Weakness Policy for setup sweepers
        if (!"".equals(counter.getSetupType())) {
            if (Math.random() < 0.2) {  // 20% chance
                return "Weakness Policy";
            }
        }

        return null;
    }

    /**
     * DEFAULT ITEMS
     * Fallback items when no priority item applies, role-influenced
     */
    private static String getDefaultItem(MoveCounter counter, String role) {
        // Role-based defaults (Gen9)
        if (role != null) {
            if (isBulkyRole(role) || role.contains("Support")) {
                return "Leftovers";
            }
            if (role.contains("Setup") || role.contains("Sweeper")) {
                return "Life Orb";
            }
            if (role.contains("Attacker") || role.contains("Wallbreaker")) {
                return "Life Orb";
            }
        }

        // Leftovers for defensive/balanced sets
        if (counter.get("recovery") > 0 || counter.getDamagingMoveCount() <= 2) {
            return "Leftovers";
        }

        // Life Orb for offensive sets
        if (counter.getDamagingMoveCount() >= 3) {
            return "Life Orb";
        }

        // Default to Leftovers
        return "Leftovers";
    }

    /**
     * Checks if a Pokemon is Not Fully Evolved
     */
    private static boolean isNFE(String species) {
        // Hardcoded list of common NFE Pokemon
        Set<String> nfePokemon = new HashSet<>(Arrays.asList(
            "bulbasaur", "ivysaur", "charmander", "charmeleon", "squirtle", "wartortle",
            "pichu", "cleffa", "igglybuff", "togepi", "togetic", "azurill", "wynaut",
            "budew", "chingling", "bonsly", "munchlax", "riolu", "mantyke",
            "porygon2", "chansey", "scyther", "onix", "rhydon", "tangela",
            "electabuzz", "magmar", "dusclops", "roselia", "murkrow", "misdreavus",
            "gligar", "sneasel", "piloswine"
        ));

        return nfePokemon.contains(species);
    }

    /**
     * Checks if a Pokemon is weak to Stealth Rock
     */
    private static boolean isStealthRockWeak(List<String> types) {
        // Pokemon weak to Rock-type moves take extra SR damage
        // 4x weak: Flying + Bug, Flying + Fire, Flying + Ice
        // 2x weak: Flying, Fire, Ice, Bug

        Set<String> weakTypes = new HashSet<>(Arrays.asList("Flying", "Fire", "Ice", "Bug"));

        int weakCount = 0;
        for (String type : types) {
            if (weakTypes.contains(type)) {
                weakCount++;
            }
        }

        // Return true if at least one weak type
        return weakCount > 0;
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

    /**
     * Randomly selects from available items if preferred item not available
     */
    public static String selectFromAvailable(String preferredItem, List<String> availableItems, Random random) {
        if (preferredItem != null && availableItems.contains(preferredItem)) {
            return preferredItem;
        }

        // Fall back to random item from available
        if (!availableItems.isEmpty()) {
            return availableItems.get(random.nextInt(availableItems.size()));
        }

        return "Leftovers";  // Ultimate fallback
    }

    /**
     * Check if role is a bulky/defensive role
     */
    private static boolean isBulkyRole(String role) {
        if (role == null) return false;
        return role.contains("Bulky") || role.contains("Defensive") ||
               role.contains("Wall") || role.contains("Tank");
    }

    /**
     * Check if ALL moves are damaging (no status moves)
     */
    private static boolean allDamagingMovesCheck(int physicalMoves, int specialMoves, int moveCount) {
        return (physicalMoves >= moveCount) || (specialMoves >= moveCount) ||
               (physicalMoves + specialMoves >= moveCount);
    }

    /**
     * Get the appropriate Choice item based on move distribution
     */
    private static String getChoiceItem(int physicalMoves, int specialMoves, MoveCounter counter) {
        // Prefer Choice Scarf if no priority moves
        if (counter.get("priority") == 0 && Math.random() < 0.3) {
            return "Choice Scarf";
        }

        // Choice Band for physical
        if (physicalMoves > specialMoves) {
            return "Choice Band";
        }

        // Choice Specs for special
        if (specialMoves > physicalMoves) {
            return "Choice Specs";
        }

        // Mixed or equal - default to Choice Scarf
        return "Choice Scarf";
    }

    /**
     * Get appropriate plate for Arceus based on type
     */
    private static String getArceusPlate(List<String> types, String forme) {
        // Arceus forme name contains the type
        if (forme.contains("fighting")) return "Fist Plate";
        if (forme.contains("flying")) return "Sky Plate";
        if (forme.contains("poison")) return "Toxic Plate";
        if (forme.contains("ground")) return "Earth Plate";
        if (forme.contains("rock")) return "Stone Plate";
        if (forme.contains("bug")) return "Insect Plate";
        if (forme.contains("ghost")) return "Spooky Plate";
        if (forme.contains("steel")) return "Iron Plate";
        if (forme.contains("fire")) return "Flame Plate";
        if (forme.contains("water")) return "Splash Plate";
        if (forme.contains("grass")) return "Meadow Plate";
        if (forme.contains("electric")) return "Zap Plate";
        if (forme.contains("psychic")) return "Mind Plate";
        if (forme.contains("ice")) return "Icicle Plate";
        if (forme.contains("dragon")) return "Draco Plate";
        if (forme.contains("dark")) return "Dread Plate";
        if (forme.contains("fairy")) return "Pixie Plate";

        // Default to first type if no forme specified
        if (types != null && !types.isEmpty()) {
            String type = types.get(0).toLowerCase();
            switch (type) {
                case "fighting": return "Fist Plate";
                case "flying": return "Sky Plate";
                case "poison": return "Toxic Plate";
                case "ground": return "Earth Plate";
                case "rock": return "Stone Plate";
                case "bug": return "Insect Plate";
                case "ghost": return "Spooky Plate";
                case "steel": return "Iron Plate";
                case "fire": return "Flame Plate";
                case "water": return "Splash Plate";
                case "grass": return "Meadow Plate";
                case "electric": return "Zap Plate";
                case "psychic": return "Mind Plate";
                case "ice": return "Icicle Plate";
                case "dragon": return "Draco Plate";
                case "dark": return "Dread Plate";
                case "fairy": return "Pixie Plate";
            }
        }

        // Default Normal type
        return null;  // Arceus without plate is Normal
    }

    /**
     * Get appropriate memory for Silvally based on type
     */
    private static String getSilvallyMemory(List<String> types, String forme) {
        // Silvally forme name contains the type
        if (forme.contains("fighting")) return "Fighting Memory";
        if (forme.contains("flying")) return "Flying Memory";
        if (forme.contains("poison")) return "Poison Memory";
        if (forme.contains("ground")) return "Ground Memory";
        if (forme.contains("rock")) return "Rock Memory";
        if (forme.contains("bug")) return "Bug Memory";
        if (forme.contains("ghost")) return "Ghost Memory";
        if (forme.contains("steel")) return "Steel Memory";
        if (forme.contains("fire")) return "Fire Memory";
        if (forme.contains("water")) return "Water Memory";
        if (forme.contains("grass")) return "Grass Memory";
        if (forme.contains("electric")) return "Electric Memory";
        if (forme.contains("psychic")) return "Psychic Memory";
        if (forme.contains("ice")) return "Ice Memory";
        if (forme.contains("dragon")) return "Dragon Memory";
        if (forme.contains("dark")) return "Dark Memory";
        if (forme.contains("fairy")) return "Fairy Memory";

        // Default to first type if no forme specified
        if (types != null && !types.isEmpty()) {
            String type = types.get(0).toLowerCase();
            switch (type) {
                case "fighting": return "Fighting Memory";
                case "flying": return "Flying Memory";
                case "poison": return "Poison Memory";
                case "ground": return "Ground Memory";
                case "rock": return "Rock Memory";
                case "bug": return "Bug Memory";
                case "ghost": return "Ghost Memory";
                case "steel": return "Steel Memory";
                case "fire": return "Fire Memory";
                case "water": return "Water Memory";
                case "grass": return "Grass Memory";
                case "electric": return "Electric Memory";
                case "psychic": return "Psychic Memory";
                case "ice": return "Ice Memory";
                case "dragon": return "Dragon Memory";
                case "dark": return "Dark Memory";
                case "fairy": return "Fairy Memory";
            }
        }

        // Default Normal type
        return null;  // Silvally without memory is Normal
    }

    /**
     * Get appropriate drive for Genesect based on forme
     */
    private static String getGenesectDrive(String forme) {
        if (forme.contains("burn") || forme.contains("fire")) return "Burn Drive";
        if (forme.contains("chill") || forme.contains("ice")) return "Chill Drive";
        if (forme.contains("douse") || forme.contains("water")) return "Douse Drive";
        if (forme.contains("shock") || forme.contains("electric")) return "Shock Drive";

        // Default Genesect has no drive (Bug type Techno Blast)
        return null;
    }
}
