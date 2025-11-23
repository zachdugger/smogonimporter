package com.pixelmon.smogonimporter.pixelmon;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates between Smogon naming conventions and Pixelmon naming conventions.
 *
 * Handles:
 * - Regional variants (Raichu-Alola → Alolan Raichu)
 * - Galarian/Hisuian forms
 * - Mega evolutions
 * - Gigantamax forms
 * - Special formes (Rotom, Wormadam, etc.)
 */
public class NameTranslationService {

    /**
     * Translates a Smogon Pokemon name to Pixelmon format
     *
     * @param smogonName Pokemon name from Smogon data (e.g., "Raichu-Alola")
     * @return Pixelmon-compatible name
     */
    public static String smogonToPixelmon(String smogonName) {
        if (smogonName == null) return null;

        String normalized = smogonName.trim();

        // Handle regional variants
        if (normalized.contains("-Alola")) {
            String base = normalized.replace("-Alola", "");
            return "Alolan " + base;
        }

        if (normalized.contains("-Galar")) {
            String base = normalized.replace("-Galar", "");
            return "Galarian " + base;
        }

        if (normalized.contains("-Hisui")) {
            String base = normalized.replace("-Hisui", "");
            return "Hisuian " + base;
        }

        // Handle Mega evolutions
        if (normalized.contains("-Mega")) {
            if (normalized.contains("-Mega-X")) {
                String base = normalized.replace("-Mega-X", "");
                return base + " Mega X";
            }
            if (normalized.contains("-Mega-Y")) {
                String base = normalized.replace("-Mega-Y", "");
                return base + " Mega Y";
            }
            String base = normalized.replace("-Mega", "");
            return "Mega " + base;
        }

        // Handle Gigantamax (Pixelmon uses "G-Max" prefix)
        if (normalized.contains("-Gmax")) {
            String base = normalized.replace("-Gmax", "");
            return base;  // Pixelmon handles Gigantamax differently
        }

        // Handle special formes with specific mappings
        Map<String, String> specialFormes = getSpecialFormeMap();
        if (specialFormes.containsKey(normalized)) {
            return specialFormes.get(normalized);
        }

        // Default: return as-is
        return normalized;
    }

    /**
     * Translates a Smogon move name to Pixelmon format
     *
     * Most move names are identical, but some need translation
     */
    public static String smogonMoveToPixelmon(String smogonMove) {
        if (smogonMove == null) return null;

        String normalized = smogonMove.trim();

        // Most moves are identical, but handle special cases
        Map<String, String> moveMap = getMoveTranslationMap();

        return moveMap.getOrDefault(normalized, normalized);
    }

    /**
     * Translates a Smogon ability name to Pixelmon format
     *
     * Most ability names are identical
     */
    public static String smogonAbilityToPixelmon(String smogonAbility) {
        if (smogonAbility == null) return null;

        String normalized = smogonAbility.trim();

        // Handle special cases
        Map<String, String> abilityMap = getAbilityTranslationMap();

        return abilityMap.getOrDefault(normalized, normalized);
    }

    /**
     * Translates a Smogon item name to Pixelmon format
     */
    public static String smogonItemToPixelmon(String smogonItem) {
        if (smogonItem == null) return null;

        String normalized = smogonItem.trim();

        // Handle special cases
        Map<String, String> itemMap = getItemTranslationMap();

        return itemMap.getOrDefault(normalized, normalized);
    }

    /**
     * Special forme mappings
     */
    private static Map<String, String> getSpecialFormeMap() {
        Map<String, String> map = new HashMap<>();

        // Rotom formes
        map.put("Rotom-Heat", "Rotom Heat");
        map.put("Rotom-Wash", "Rotom Wash");
        map.put("Rotom-Frost", "Rotom Frost");
        map.put("Rotom-Fan", "Rotom Fan");
        map.put("Rotom-Mow", "Rotom Mow");

        // Wormadam formes
        map.put("Wormadam-Sandy", "Wormadam Sandy");
        map.put("Wormadam-Trash", "Wormadam Trash");

        // Shaymin
        map.put("Shaymin-Sky", "Shaymin Sky");

        // Giratina
        map.put("Giratina-Origin", "Giratina Origin");

        // Tornadus/Thundurus/Landorus
        map.put("Tornadus-Therian", "Tornadus Therian");
        map.put("Thundurus-Therian", "Thundurus Therian");
        map.put("Landorus-Therian", "Landorus Therian");

        // Kyurem
        map.put("Kyurem-Black", "Kyurem Black");
        map.put("Kyurem-White", "Kyurem White");

        // Keldeo
        map.put("Keldeo-Resolute", "Keldeo Resolute");

        // Meloetta
        map.put("Meloetta-Pirouette", "Meloetta Pirouette");

        // Hoopa
        map.put("Hoopa-Unbound", "Hoopa Unbound");

        // Oricorio
        map.put("Oricorio-Pom-Pom", "Oricorio Pom-Pom");
        map.put("Oricorio-Pa'u", "Oricorio Pa'u");
        map.put("Oricorio-Sensu", "Oricorio Sensu");

        // Lycanroc
        map.put("Lycanroc-Midnight", "Lycanroc Midnight");
        map.put("Lycanroc-Dusk", "Lycanroc Dusk");

        // Wishiwashi
        map.put("Wishiwashi-School", "Wishiwashi School");

        // Minior
        map.put("Minior-Meteor", "Minior Meteor");

        // Necrozma
        map.put("Necrozma-Dusk-Mane", "Necrozma Dusk Mane");
        map.put("Necrozma-Dawn-Wings", "Necrozma Dawn Wings");

        // Toxtricity
        map.put("Toxtricity-Low-Key", "Toxtricity Low Key");

        // Urshifu
        map.put("Urshifu-Rapid-Strike", "Urshifu Rapid Strike");

        // Basculegion
        map.put("Basculegion-F", "Basculegion Female");

        // Oinkologne
        map.put("Oinkologne-F", "Oinkologne Female");

        return map;
    }

    /**
     * Move name translation map
     */
    private static Map<String, String> getMoveTranslationMap() {
        Map<String, String> map = new HashMap<>();

        // Most moves are identical, add special cases if needed
        // Pixelmon usually uses title case
        map.put("thunderbolt", "Thunderbolt");
        map.put("earthquake", "Earthquake");
        map.put("icebeam", "Ice Beam");

        return map;
    }

    /**
     * Ability name translation map
     */
    private static Map<String, String> getAbilityTranslationMap() {
        Map<String, String> map = new HashMap<>();

        // Most abilities are identical, add special cases if needed
        // Pixelmon usually uses title case with spaces
        map.put("hugepower", "Huge Power");
        map.put("purepower", "Pure Power");
        map.put("magicguard", "Magic Guard");

        return map;
    }

    /**
     * Item name translation map
     */
    private static Map<String, String> getItemTranslationMap() {
        Map<String, String> map = new HashMap<>();

        // Most items are identical, add special cases if needed
        map.put("leftovers", "Leftovers");
        map.put("choiceband", "Choice Band");
        map.put("choicescarf", "Choice Scarf");
        map.put("choicespecs", "Choice Specs");
        map.put("lifeorb", "Life Orb");
        map.put("focussash", "Focus Sash");
        map.put("assaultvest", "Assault Vest");

        return map;
    }

    /**
     * Checks if a Pokemon name represents a regional variant
     */
    public static boolean isRegionalVariant(String smogonName) {
        return smogonName != null && (
            smogonName.contains("-Alola") ||
            smogonName.contains("-Galar") ||
            smogonName.contains("-Hisui")
        );
    }

    /**
     * Checks if a Pokemon name represents a Mega evolution
     */
    public static boolean isMegaEvolution(String smogonName) {
        return smogonName != null && smogonName.contains("-Mega");
    }

    /**
     * Checks if a Pokemon name represents a Gigantamax form
     */
    public static boolean isGigantamax(String smogonName) {
        return smogonName != null && smogonName.contains("-Gmax");
    }
}
