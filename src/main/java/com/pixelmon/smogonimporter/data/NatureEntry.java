package com.pixelmon.smogonimporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a nature entry from Smogon's nature database
 * Contains nature name, stat modifications (plus/minus)
 */
public class NatureEntry {

    private String id;  // Normalized ID (e.g., "adamant")

    private String name;  // Display name (e.g., "Adamant")

    @SerializedName("plus")
    private String plus;  // Stat increased (atk, def, spa, spd, spe)

    @SerializedName("minus")
    private String minus;  // Stat decreased (atk, def, spa, spd, spe)

    @SerializedName("num")
    private int number;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPlus() { return plus; }
    public String getMinus() { return minus; }
    public int getNumber() { return number; }

    // Setters
    public void setId(String id) {
        this.id = id;
        // If name is not set, derive it from ID
        if (this.name == null || this.name.isEmpty()) {
            this.name = capitalize(id);
        }
    }

    /**
     * Capitalize first letter
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Check if this is a neutral nature (no stat changes)
     */
    public boolean isNeutral() {
        return plus == null || minus == null || plus.equals(minus);
    }

    /**
     * Get the stat multiplier for a given stat
     * @param stat The stat name (atk, def, spa, spd, spe)
     * @return 1.1 if boosted, 0.9 if hindered, 1.0 if neutral
     */
    public double getMultiplier(String stat) {
        if (stat == null) return 1.0;

        String normalizedStat = stat.toLowerCase();

        if (plus != null && plus.equalsIgnoreCase(normalizedStat)) {
            return 1.1;
        }

        if (minus != null && minus.equalsIgnoreCase(normalizedStat)) {
            return 0.9;
        }

        return 1.0;
    }

    @Override
    public String toString() {
        if (isNeutral()) {
            return String.format("Nature{name='%s', neutral}", name);
        }
        return String.format("Nature{name='%s', +%s, -%s}", name, plus, minus);
    }
}
