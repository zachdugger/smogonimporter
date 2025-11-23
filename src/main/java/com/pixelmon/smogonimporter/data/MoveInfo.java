package com.pixelmon.smogonimporter.data;

/**
 * Simple move information class.
 * In production, this would be replaced with queries to Pixelmon's move data API.
 */
public class MoveInfo {
    public final MoveCategory category;
    public final String type;
    public final int basePower;

    public MoveInfo(MoveCategory category, String type, int basePower) {
        this.category = category;
        this.type = type;
        this.basePower = basePower;
    }

    @Override
    public String toString() {
        return String.format("MoveInfo{category=%s, type=%s, power=%d}",
                category, type, basePower);
    }
}
