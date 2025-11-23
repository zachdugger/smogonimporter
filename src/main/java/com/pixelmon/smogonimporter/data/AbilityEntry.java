package com.pixelmon.smogonimporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an ability entry from Smogon's ability database
 * Contains ability name, number, rating, and description
 */
public class AbilityEntry {

    private String id;  // Normalized ID (e.g., "adaptability")

    private String name;  // Display name (e.g., "Adaptability")

    @SerializedName("num")
    private int number;

    private int rating;  // Competitive viability rating (-1 to 5)

    private String desc;  // Description

    @SerializedName("shortDesc")
    private String shortDesc;  // Short description

    @SerializedName("isNonstandard")
    private String isNonstandard;  // "Past", "Future", "CAP", etc.

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getNumber() { return number; }
    public int getRating() { return rating; }
    public String getDesc() { return desc; }
    public String getShortDesc() { return shortDesc; }
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
     * Convert "adaptability" to "Adaptability"
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
        return String.format("Ability{id='%s', name='%s', rating=%d}", id, name, rating);
    }
}
