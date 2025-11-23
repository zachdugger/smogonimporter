package com.pixelmon.smogonimporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a move entry from Smogon's move database
 * Contains category, type, power, accuracy, etc.
 */
public class MoveDataEntry {

    private String name;

    @SerializedName("num")
    private int number;

    @SerializedName("basePower")
    private int basePower;

    private String type;

    private String category;  // "Physical", "Special", or "Status"

    private int accuracy;

    private int pp;

    private String target;

    private int priority;

    @SerializedName("flags")
    private MoveFlags flags;

    private String desc;  // Description

    // Getters
    public String getName() { return name; }
    public int getNumber() { return number; }
    public int getBasePower() { return basePower; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public int getAccuracy() { return accuracy; }
    public int getPp() { return pp; }
    public String getTarget() { return target; }
    public int getPriority() { return priority; }
    public MoveFlags getFlags() { return flags; }
    public String getDesc() { return desc; }

    // Setters
    public void setName(String name) { this.name = name; }

    /**
     * Get MoveCategory enum
     */
    public MoveCategory getCategoryEnum() {
        if (category == null) return MoveCategory.STATUS;
        return MoveCategory.fromString(category);
    }

    /**
     * Check if this is a damaging move
     */
    public boolean isDamaging() {
        return getCategoryEnum().isDamaging();
    }

    public static class MoveFlags {
        private boolean contact;
        private boolean protect;
        private boolean mirror;
        private boolean sound;
        private boolean punch;
        private boolean bite;

        public boolean isContact() { return contact; }
        public boolean isProtect() { return protect; }
        public boolean isMirror() { return mirror; }
        public boolean isSound() { return sound; }
        public boolean isPunch() { return punch; }
        public boolean isBite() { return bite; }
    }

    @Override
    public String toString() {
        return String.format("Move{name='%s', category=%s, type=%s, power=%d}",
            name, category, type, basePower);
    }
}
