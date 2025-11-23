package com.pixelmon.smogonimporter.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks move counts and properties for competitive set analysis.
 * Ported from Pokemon Showdown's MoveCounter class.
 *
 * This class extends HashMap to count occurrences of different move properties:
 * - Move types (Fire, Water, etc.)
 * - Move categories (Physical, Special, Status)
 * - Strategic properties (setup, hazards, recovery, STAB, etc.)
 * - Ability synergies (skilllink, ironfist, etc.)
 */
public class MoveCounter extends HashMap<String, Integer> {

    /**
     * Set of all damaging moves in the current moveset
     */
    private final Set<String> damagingMoves;

    /**
     * The setup type of the moveset:
     * - "Physical" - Physical setup moves present
     * - "Special" - Special setup moves present
     * - "Mixed" - Both physical and special setup
     * - "" (empty) - No setup moves
     */
    private String setupType;

    public MoveCounter() {
        super();
        this.damagingMoves = new HashSet<>();
        this.setupType = "";
    }

    /**
     * Gets the count for a property, returning 0 if not present
     */
    @Override
    public Integer get(Object key) {
        return super.getOrDefault(key, 0);
    }

    /**
     * Adds a value to the existing count for a property
     */
    public void add(String key, int value) {
        put(key, get(key) + value);
    }

    /**
     * Adds a value to the existing count (convenience method)
     */
    public void add(String key) {
        add(key, 1);
    }

    /**
     * Adds a damaging move to the set
     */
    public void addDamagingMove(String move) {
        damagingMoves.add(move);
    }

    /**
     * Gets all damaging moves
     */
    public Set<String> getDamagingMoves() {
        return new HashSet<>(damagingMoves);
    }

    /**
     * Checks if a specific damaging move is present
     */
    public boolean hasDamagingMove(String move) {
        return damagingMoves.contains(move);
    }

    /**
     * Gets the number of damaging moves
     */
    public int getDamagingMoveCount() {
        return damagingMoves.size();
    }

    /**
     * Gets the setup type
     */
    public String getSetupType() {
        return setupType;
    }

    /**
     * Sets the setup type
     */
    public void setSetupType(String setupType) {
        this.setupType = setupType != null ? setupType : "";
    }

    /**
     * Checks if this is a physical setup
     */
    public boolean isPhysicalSetup() {
        return "Physical".equals(setupType);
    }

    /**
     * Checks if this is a special setup
     */
    public boolean isSpecialSetup() {
        return "Special".equals(setupType);
    }

    /**
     * Checks if this is a mixed setup
     */
    public boolean isMixedSetup() {
        return "Mixed".equals(setupType);
    }

    /**
     * Checks if there's any setup
     */
    public boolean hasSetup() {
        return !setupType.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MoveCounter{");
        sb.append("setupType='").append(setupType).append('\'');
        sb.append(", damagingMoves=").append(damagingMoves.size());
        sb.append(", properties=").append(super.toString());
        sb.append('}');
        return sb.toString();
    }
}
