package com.pixelmon.smogonimporter.data;

/**
 * Represents the setup type of a Pokemon's moveset.
 * Used to determine stat-boosting strategy and validate move compatibility.
 */
public enum SetupType {
    /**
     * Physical setup (Swords Dance, Dragon Dance, etc.)
     * Boosts Attack and requires physical moves
     */
    PHYSICAL("Physical"),

    /**
     * Special setup (Nasty Plot, Calm Mind, etc.)
     * Boosts Special Attack and requires special moves
     */
    SPECIAL("Special"),

    /**
     * Mixed setup (Shell Smash, Work Up, etc.)
     * Boosts both offensive stats
     */
    MIXED("Mixed"),

    /**
     * Speed setup only (Agility, Rock Polish, etc.)
     * Boosts Speed without offensive stat boosts
     */
    SPEED("Speed"),

    /**
     * No setup moves
     */
    NONE("");

    private final String displayName;

    SetupType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if this setup type allows physical moves
     */
    public boolean allowsPhysical() {
        return this == PHYSICAL || this == MIXED;
    }

    /**
     * Checks if this setup type allows special moves
     */
    public boolean allowsSpecial() {
        return this == SPECIAL || this == MIXED;
    }

    /**
     * Checks if this is an offensive setup
     */
    public boolean isOffensive() {
        return this != NONE && this != SPEED;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Converts from string (for compatibility with old code)
     */
    public static SetupType fromString(String str) {
        if (str == null || str.isEmpty()) return NONE;
        for (SetupType type : values()) {
            if (type.displayName.equalsIgnoreCase(str)) {
                return type;
            }
        }
        return NONE;
    }
}
