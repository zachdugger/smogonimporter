package com.pixelmon.smogonimporter.data;

import java.util.*;

/**
 * Represents a pre-built competitive Pokemon set from Gen9 draft-factory-matchups.json
 *
 * Unlike Gen8 which uses dynamic generation, Gen9 uses a static pool of 1200+
 * hand-crafted competitive sets from Pokemon Showdown's Draft Factory format.
 *
 * Pipe format: Species|Item|Ability|Moves|Nature|EVs|IVs|Gender|Shiny|Level|Happiness|TeraType
 * Note: TeraType is stripped as Pixelmon doesn't support Tera yet
 */
public class Gen9PokemonSet {
    private String species;
    private String item;
    private String ability;
    private List<String> moves;
    private String nature;
    private Map<String, Integer> evs;
    private Map<String, Integer> ivs;
    private String gender;
    private boolean shiny;
    private int level;
    private int happiness;

    public Gen9PokemonSet() {
        this.moves = new ArrayList<>();
        this.evs = new HashMap<>();
        this.ivs = new HashMap<>();
        this.level = 100;
        this.happiness = 255;
        this.shiny = false;
        this.gender = "";
    }

    // Getters and Setters
    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isShiny() {
        return shiny;
    }

    public void setShiny(boolean shiny) {
        this.shiny = shiny;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHappiness() {
        return happiness;
    }

    public void setHappiness(int happiness) {
        this.happiness = happiness;
    }

    @Override
    public String toString() {
        return "Gen9PokemonSet{" +
                "species='" + species + '\'' +
                ", item='" + item + '\'' +
                ", ability='" + ability + '\'' +
                ", moves=" + moves +
                ", nature='" + nature + '\'' +
                ", level=" + level +
                '}';
    }
}
