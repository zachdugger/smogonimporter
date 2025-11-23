package com.pixelmon.smogonimporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an item entry from Smogon's item database
 * Contains item name, number, generation, and flags
 */
public class ItemEntry {

    private String id;  // Normalized ID (e.g., "choiceband")

    private String name;  // Display name (e.g., "Choice Band")

    @SerializedName("num")
    private int number;

    @SerializedName("gen")
    private int generation;

    @SerializedName("spritenum")
    private int spriteNumber;

    private String desc;  // Description

    @SerializedName("isChoice")
    private boolean isChoice;

    @SerializedName("isBerry")
    private boolean isBerry;

    @SerializedName("isGem")
    private boolean isGem;

    @SerializedName("isPokeball")
    private boolean isPokeball;

    @SerializedName("megaStone")
    private String megaStone;

    @SerializedName("megaEvolves")
    private String megaEvolves;

    @SerializedName("isNonstandard")
    private String isNonstandard;  // "Past", "Future", "Unobtainable", etc.

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getNumber() { return number; }
    public int getGeneration() { return generation; }
    public int getSpriteNumber() { return spriteNumber; }
    public String getDesc() { return desc; }
    public boolean isChoice() { return isChoice; }
    public boolean isBerry() { return isBerry; }
    public boolean isGem() { return isGem; }
    public boolean isPokeball() { return isPokeball; }
    public String getMegaStone() { return megaStone; }
    public String getMegaEvolves() { return megaEvolves; }
    public String getIsNonstandard() { return isNonstandard; }

    // Setters
    public void setId(String id) {
        this.id = id;
        // If name is not set, derive it from ID
        if (this.name == null || this.name.isEmpty()) {
            this.name = capitalizeWords(id);
        }
    }

    /**
     * Convert "choiceband" to "Choice Band"
     */
    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;

        // Insert spaces before capital letters
        String withSpaces = str.replaceAll("([a-z])([A-Z])", "$1 $2");

        // Capitalize first letter
        return Character.toUpperCase(withSpaces.charAt(0)) + withSpaces.substring(1);
    }

    @Override
    public String toString() {
        return String.format("Item{id='%s', name='%s', gen=%d}", id, name, generation);
    }
}
