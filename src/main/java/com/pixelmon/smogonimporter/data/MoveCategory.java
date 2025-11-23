package com.pixelmon.smogonimporter.data;

/**
 * Represents the damage category of a Pokemon move.
 * Based on Gen 4+ Physical/Special split.
 */
public enum MoveCategory {
    /**
     * Physical moves - use Attack stat
     * Examples: Earthquake, Close Combat, Waterfall
     */
    PHYSICAL("Physical"),

    /**
     * Special moves - use Special Attack stat
     * Examples: Flamethrower, Thunderbolt, Psychic
     */
    SPECIAL("Special"),

    /**
     * Status moves - no direct damage
     * Examples: Stealth Rock, Will-O-Wisp, Swords Dance
     */
    STATUS("Status");

    private final String displayName;

    MoveCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this is a damaging category
     */
    public boolean isDamaging() {
        return this == PHYSICAL || this == SPECIAL;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Converts from string
     */
    public static MoveCategory fromString(String str) {
        if (str == null) return STATUS;
        for (MoveCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(str)) {
                return category;
            }
        }
        return STATUS;
    }
}
