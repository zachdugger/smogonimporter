package com.pixelmon.smogonimporter.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Represents a Pokemon species entry from Smogon's Pokedex
 * Contains types, base stats, abilities, etc.
 */
public class PokedexEntry {

    @SerializedName("num")
    private int number;

    private String name;

    @SerializedName("types")
    private List<String> types;

    @SerializedName("baseStats")
    private Map<String, Integer> baseStats;

    @SerializedName("abilities")
    private AbilitiesInfo abilities;

    @SerializedName("weightkg")
    private double weight;

    @SerializedName("prevo")
    private String prevo;  // Pre-evolution

    @SerializedName("evo")
    private List<String> evolutions;

    // Getters
    public int getNumber() { return number; }
    public String getName() { return name; }
    public List<String> getTypes() { return types; }
    public Map<String, Integer> getBaseStats() { return baseStats; }
    public AbilitiesInfo getAbilities() { return abilities; }
    public double getWeight() { return weight; }
    public String getPrevo() { return prevo; }
    public List<String> getEvolutions() { return evolutions; }

    // Setters
    public void setName(String name) { this.name = name; }

    /**
     * Check if this Pokemon is Not Fully Evolved
     */
    public boolean isNFE() {
        return evolutions != null && !evolutions.isEmpty();
    }

    /**
     * Get base stat by name
     */
    public int getBaseStat(String stat) {
        return baseStats != null ? baseStats.getOrDefault(stat, 0) : 0;
    }

    public static class AbilitiesInfo {
        @SerializedName("0")
        private String ability0;  // Primary ability

        @SerializedName("1")
        private String ability1;  // Secondary ability

        @SerializedName("H")
        private String hiddenAbility;

        @SerializedName("S")
        private String specialAbility;

        public String getAbility0() { return ability0; }
        public String getAbility1() { return ability1; }
        public String getHiddenAbility() { return hiddenAbility; }
        public String getSpecialAbility() { return specialAbility; }

        public List<String> getAllAbilities() {
            java.util.List<String> abilities = new java.util.ArrayList<>();
            if (ability0 != null) abilities.add(ability0);
            if (ability1 != null) abilities.add(ability1);
            if (hiddenAbility != null) abilities.add(hiddenAbility);
            if (specialAbility != null) abilities.add(specialAbility);
            return abilities;
        }
    }

    @Override
    public String toString() {
        return String.format("PokedexEntry{name='%s', types=%s, baseStats=%s}",
            name, types, baseStats);
    }
}
