package com.pixelmon.smogonimporter.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks team composition data for balancing and variety.
 * Ported from Pokemon Showdown's TeamData interface.
 *
 * Used to ensure teams have:
 * - Type diversity
 * - Coverage for common weaknesses
 * - Balanced roles (offensive/defensive)
 * - No duplicate base formes
 */
public class TeamData {

    /**
     * Count of each Pokemon type on the team
     * Key: Type name (e.g., "Fire", "Water")
     * Value: Number of Pokemon with that type
     */
    private final Map<String, Integer> typeCount;

    /**
     * Count of each type combination on the team
     * Key: Type combo (e.g., "Fire/Flying", "Water")
     * Value: Number of Pokemon with that exact typing
     */
    private final Map<String, Integer> typeComboCount;

    /**
     * Count of base formes used on the team
     * Key: Base species name (e.g., "Charizard")
     * Value: Number of instances (should stay <= 1)
     */
    private final Map<String, Integer> baseFormes;

    /**
     * Additional team features and counts
     * Tracks properties like:
     * - "stealthrock": Has Stealth Rock setter
     * - "spikes": Has Spikes setter
     * - "defog": Has hazard removal
     * - "screens": Has Light Screen/Reflect
     * - etc.
     */
    private final Map<String, Integer> has;

    /**
     * Team's collective weaknesses
     * Key: Type name
     * Value: Number of team members weak to it
     */
    private final Map<String, Integer> weaknesses;

    /**
     * Team's collective resistances
     * Key: Type name
     * Value: Number of team members that resist it
     */
    private final Map<String, Integer> resistances;

    /**
     * Active weather setter (if any)
     * Values: "Sun", "Rain", "Sand", "Hail", "Snow", null
     */
    private String weather;

    /**
     * Whether team has a Gigantamax Pokemon
     */
    private boolean gigantamax;

    /**
     * Force a specific result (used in special generation modes)
     */
    private boolean forceResult;

    public TeamData() {
        this.typeCount = new HashMap<>();
        this.typeComboCount = new HashMap<>();
        this.baseFormes = new HashMap<>();
        this.has = new HashMap<>();
        this.weaknesses = new HashMap<>();
        this.resistances = new HashMap<>();
        this.weather = null;
        this.gigantamax = false;
        this.forceResult = false;
    }

    // Type counting methods
    public void addType(String type) {
        typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
    }

    public int getTypeCount(String type) {
        return typeCount.getOrDefault(type, 0);
    }

    public void addTypeCombo(String combo) {
        typeComboCount.put(combo, typeComboCount.getOrDefault(combo, 0) + 1);
    }

    public int getTypeComboCount(String combo) {
        return typeComboCount.getOrDefault(combo, 0);
    }

    // Base forme tracking
    public void addBaseForme(String species) {
        baseFormes.put(species, baseFormes.getOrDefault(species, 0) + 1);
    }

    public boolean hasBaseForme(String species) {
        return baseFormes.getOrDefault(species, 0) > 0;
    }

    public int getBaseFormeCount(String species) {
        return baseFormes.getOrDefault(species, 0);
    }

    // Feature tracking
    public void addFeature(String feature) {
        has.put(feature, has.getOrDefault(feature, 0) + 1);
    }

    public boolean hasFeature(String feature) {
        return has.getOrDefault(feature, 0) > 0;
    }

    public int getFeatureCount(String feature) {
        return has.getOrDefault(feature, 0);
    }

    // Weakness/Resistance tracking
    public void addWeakness(String type) {
        weaknesses.put(type, weaknesses.getOrDefault(type, 0) + 1);
    }

    public int getWeaknessCount(String type) {
        return weaknesses.getOrDefault(type, 0);
    }

    public void addResistance(String type) {
        resistances.put(type, resistances.getOrDefault(type, 0) + 1);
    }

    public int getResistanceCount(String type) {
        return resistances.getOrDefault(type, 0);
    }

    // Getters and setters
    public Map<String, Integer> getTypeCount() {
        return typeCount;
    }

    public Map<String, Integer> getTypeComboCount() {
        return typeComboCount;
    }

    public Map<String, Integer> getBaseFormes() {
        return baseFormes;
    }

    public Map<String, Integer> getHas() {
        return has;
    }

    public Map<String, Integer> getWeaknesses() {
        return weaknesses;
    }

    public Map<String, Integer> getResistances() {
        return resistances;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public boolean isGigantamax() {
        return gigantamax;
    }

    public void setGigantamax(boolean gigantamax) {
        this.gigantamax = gigantamax;
    }

    public boolean isForceResult() {
        return forceResult;
    }

    public void setForceResult(boolean forceResult) {
        this.forceResult = forceResult;
    }

    @Override
    public String toString() {
        return "TeamData{" +
                "typeCount=" + typeCount +
                ", weather='" + weather + '\'' +
                ", gigantamax=" + gigantamax +
                ", features=" + has.keySet() +
                '}';
    }
}
