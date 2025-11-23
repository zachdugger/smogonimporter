package com.pixelmon.smogonimporter.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a Pokemon with all its competitive data from Smogon
 */
public class PokemonData {
    private String name;
    private int level;
    private List<String> abilities;
    private List<String> items;
    private List<String> moves;

    @SerializedName("evs")
    private Map<String, Integer> evs;

    @SerializedName("ivs")
    private Map<String, Integer> ivs;

    // Additional metadata
    private transient String generation;
    private transient long lastUpdated;

    // Gen9: Role information (e.g., "Bulky Support", "Fast Attacker")
    private transient String role;

    public PokemonData() {
        this.lastUpdated = System.currentTimeMillis();
    }

    public PokemonData(String name, int level, List<String> abilities, List<String> items, List<String> moves) {
        this.name = name;
        this.level = level;
        this.abilities = abilities;
        this.items = items;
        this.moves = moves;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<String> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<String> abilities) {
        this.abilities = abilities;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public Map<String, Integer> getEvs() {
        return evs;
    }

    public void setEvs(Map<String, Integer> evs) {
        this.evs = evs;
    }

    public Map<String, Integer> getIvs() {
        return ivs;
    }

    public void setIvs(Map<String, Integer> ivs) {
        this.ivs = ivs;
    }

    public String getGeneration() {
        return generation;
    }

    public void setGeneration(String generation) {
        this.generation = generation;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Utility methods
    public String getRandomAbility() {
        if (abilities == null || abilities.isEmpty()) {
            return null;
        }
        return abilities.get((int) (Math.random() * abilities.size()));
    }

    public String getRandomItem() {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return items.get((int) (Math.random() * items.size()));
    }

    public List<String> getRandomMoves(int count) {
        if (moves == null || moves.isEmpty()) {
            return null;
        }

        // Shuffle and return up to 'count' moves
        List<String> shuffled = new java.util.ArrayList<>(moves);
        java.util.Collections.shuffle(shuffled);

        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PokemonData that = (PokemonData) o;
        return Objects.equals(name, that.name) && Objects.equals(generation, that.generation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, generation);
    }

    @Override
    public String toString() {
        return "PokemonData{" +
                "name='" + name + '\'' +
                ", level=" + level +
                ", abilities=" + abilities +
                ", items=" + items +
                ", moves=" + moves +
                ", evs=" + evs +
                ", ivs=" + ivs +
                '}';
    }
}