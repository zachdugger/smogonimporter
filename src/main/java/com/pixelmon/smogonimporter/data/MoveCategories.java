package com.pixelmon.smogonimporter.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains categorized move lists for competitive set generation.
 * Ported from Pokemon Showdown's move categorization constants.
 */
public class MoveCategories {

    // Setup Moves
    public static final Set<String> PHYSICAL_SETUP = new HashSet<>(Arrays.asList(
        "bellydrum", "bulkup", "coil", "curse", "dragondance", "honeclaws",
        "howl", "meditate", "poweruppunch", "swordsdance", "shiftgear"
    ));

    public static final Set<String> SPECIAL_SETUP = new HashSet<>(Arrays.asList(
        "calmmind", "chargebeam", "geomancy", "nastyplot", "quiverdance", "tailglow"
    ));

    public static final Set<String> MIXED_SETUP = new HashSet<>(Arrays.asList(
        "growth", "happyhour", "holdhands", "celebrate", "shellsmash", "workup"
    ));

    public static final Set<String> SPEED_SETUP = new HashSet<>(Arrays.asList(
        "agility", "autotomize", "flamecharge", "rockpolish"
    ));

    // Hazard Moves
    public static final Set<String> HAZARDS = new HashSet<>(Arrays.asList(
        "spikes", "stealthrock", "stickyweb", "toxicspikes"
    ));

    // Recovery Moves
    public static final Set<String> RECOVERY_MOVES = new HashSet<>(Arrays.asList(
        "healorder", "milkdrink", "moonlight", "morningsun", "recover", "roost",
        "shoreup", "slackoff", "softboiled", "synthesis", "wish"
    ));

    // Contrary Moves (boost becomes drop with Contrary ability)
    public static final Set<String> CONTRARY_MOVES = new HashSet<>(Arrays.asList(
        "closecombat", "dracometeor", "hammerarm", "hyperspacefury", "leafstorm",
        "overheat", "psychoboost", "superpower", "vcreate"
    ));

    // Moves that don't need STAB
    public static final Set<String> NO_STAB = new HashSet<>(Arrays.asList(
        "accelerock", "aquajet", "bulletpunch", "explosion", "extremespeed",
        "fakeout", "feint", "firstimpression", "flamecharge", "iceshard",
        "machpunch", "pursuit", "quickattack", "selfdestruct", "shadowsneak",
        "suckerpunch", "vacuumwave", "watershuriken"
    ));

    // Priority Moves
    public static final Set<String> PRIORITY_MOVES = new HashSet<>(Arrays.asList(
        "accelerock", "aquajet", "bulletpunch", "extremespeed", "fakeout",
        "feint", "firstimpression", "iceshard", "machpunch", "quickattack",
        "shadowsneak", "suckerpunch", "vacuumwave", "watershuriken"
    ));

    // Recoil Moves
    public static final Set<String> RECOIL_MOVES = new HashSet<>(Arrays.asList(
        "bravebird", "doubleedge", "flareblitz", "headcharge", "headsmash",
        "submission", "takedown", "voltackle", "wildcharge", "woodhammer"
    ));

    // Pivot Moves (switch out after use)
    public static final Set<String> PIVOT_MOVES = new HashSet<>(Arrays.asList(
        "batonpass", "flipturn", "partingshot", "teleport", "uturn", "voltswitch"
    ));

    // Screen Moves
    public static final Set<String> SCREEN_MOVES = new HashSet<>(Arrays.asList(
        "auroraveil", "lightscreen", "reflect"
    ));

    // Sound Moves
    public static final Set<String> SOUND_MOVES = new HashSet<>(Arrays.asList(
        "boomburst", "bugbuzz", "chatterm", "clangoroussoul", "disarmingvoice",
        "echoedvoice", "grasswhistle", "growl", "hypervoice", "metalsound",
        "nobleroar", "overdrive", "perishsong", "relicsong", "round", "screech",
        "sing", "snore", "sparklingaria", "supersonic", "uproar"
    ));

    // Punch Moves (for Iron Fist)
    public static final Set<String> PUNCH_MOVES = new HashSet<>(Arrays.asList(
        "bulletpunch", "closecombat", "cometpunch", "dizzypunch", "drainpunch",
        "dynamicpunch", "firepunch", "focuspunch", "hammerarm", "icepunch",
        "machpunch", "megapunch", "meteormash", "plasmafinsts", "poweruppunch",
        "shadowpunch", "skyuppercut", "thunderpunch"
    ));

    // Bite Moves (for Strong Jaw)
    public static final Set<String> BITE_MOVES = new HashSet<>(Arrays.asList(
        "bite", "crunch", "firefang", "fishiousrend", "hyperfang", "icefang",
        "jawlock", "poisonfang", "psychicfangs", "thunderfang"
    ));

    // Multi-Hit Moves (for Skill Link)
    public static final Set<String> SKILL_LINK_MOVES = new HashSet<>(Arrays.asList(
        "armthrust", "barrage", "bonemerang", "bulletseed", "cometpunch",
        "doublehit", "doublekick", "doubleslap", "dualchop", "furyattack",
        "furyswipes", "geargrind", "iciclespear", "pinmissile", "rockblast",
        "scaleshot", "spikecannon", "tailslap", "watershuriken"
    ));

    // Powder Moves
    public static final Set<String> POWDER_MOVES = new HashSet<>(Arrays.asList(
        "cottonspore", "magicpowder", "poisonpowder", "powder", "ragepowder",
        "sleeppowder", "spore", "stunspore"
    ));

    // Moves affected by Sheer Force
    public static final Set<String> SHEER_FORCE_MOVES = new HashSet<>(Arrays.asList(
        "ancientpower", "airslash", "astonish", "aurasphere", "bite", "blizzard",
        "bodyslam", "bounce", "brine", "bugbuzz", "bulldoze", "chargebeam",
        "crunch", "crushclaw", "darkpulse", "discharge", "dragonclaw", "earthpower",
        "energyball", "extrasensory", "fakeout", "fireblast", "firepunch", "flamethrower",
        "flashcannon", "focusblast", "gigadrain", "gunkshot", "headbutt", "headsmash",
        "heartsstamp", "hurricane", "hydropump", "hypervoice", "icefang", "icepunch",
        "icebeam", "ironhead", "ironhead", "lavaplume", "lick", "mudshot", "muddywater",
        "needlearm", "ominouswind", "poisonjab", "powergem", "psychic", "razorshell",
        "rockslide", "scald", "seedflare", "shadowball", "signalbeam", "silverwind",
        "sludgebomb", "sludgewave", "smog", "snore", "stomp", "stoneedge", "thunderbolt",
        "thunder", "thunderfang", "thunderpunch", "tri-attack", "twister", "waterfall",
        "waterpulse", "zenheadbutt"
    ));

    // Moves boosted by Tough Claws
    public static final Set<String> CONTACT_MOVES = new HashSet<>(Arrays.asList(
        "aerialace", "aquajet", "aquatail", "armthrust", "assurance", "beatup",
        "bide", "bind", "bodyslam", "bounce", "bravebird", "brickbreak", "bugbite",
        "bulldoze", "bulletseed", "chargebeam", "circlethrow", "closecombat",
        "comet", "cometpunch", "counter", "crosschop", "crosspoison", "crunch",
        "crushclaw", "cut", "dig", "dive", "dizzypunch", "doubleedge", "doublehit",
        "doublekick", "doubleslap", "dragonrush", "dragontail", "drainpunch",
        "drillpeck", "drillrun", "dualchop", "dynamicpunch", "earthquake",
        "endeavor", "facade", "falseswipe", "feint", "firefang", "firelash",
        "firepunch", "firstimpression", "flail", "flamecharge", "flamewheel",
        "flareblitz", "fly", "focuspunch", "forcepalm", "furyattack", "furycutter",
        "furyswipes", "gigaimpact", "grassknot", "grassyglide", "gunkshot",
        "gyroball", "hammerarm", "headbutt", "headcharge", "headsmash", "heatcrash",
        "heavyslam", "highhorsepower", "hijumpkick", "hornattack", "hornleech",
        "hyperdriveattack", "hyperfang", "hyperspacefury", "hyperspacehole", "iciclecrash",
        "icefang", "icehammer", "icepunch", "iceshard", "ironhead", "irontail",
        "jawlock", "jumpkick", "karatechop", "lashout", "lastresort", "leafblade",
        "lick", "lowkick", "lowsweep", "machpunch", "megahorn", "megakick",
        "megapunch", "meteorassault", "meteormash", "outrage", "payback", "payday",
        "peck", "petaldance", "phantomforce", "pinmissile", "pluck", "poisonfang",
        "poisonjab", "poisontail", "pound", "powertrip", "poweruppunch", "powerwhip",
        "precipiceblades", "pursuit", "quickattack", "rage", "rapidwpin", "razorshell",
        "revenge", "reversal", "rockclimb", "rocksmash", "rocktomb", "rollout",
        "scratch", "seedbomb", "seismictoss", "shadowclaw", "shadowpunch",
        "shadowsneak", "skullbash", "skyattack", "skydrop", "skyuppercut", "slam",
        "slash", "smartstrike", "spark", "stealthrock", "steelwing", "stomp",
        "stormthrow", "strength", "strugglebug", "submission", "suckerpunch",
        "superpower", "superfang", "surf", "tackle", "takedown", "thief",
        "thrash", "throatchop", "thunderfang", "thunderpunch", "tripleaxel",
        "triplekick", "uturn", "vcreate", "venoshock", "vinewhip", "vitalthrow",
        "volttackle", "waterfall", "watershuriken", "waterwheel", "wildcharge",
        "wingattack", "woodhammer", "wrap", "xscissor", "zenheadbutt", "zipzap",
        "zippyzap"
    ));

    // Drain Moves
    public static final Set<String> DRAIN_MOVES = new HashSet<>(Arrays.asList(
        "absorb", "drainpunch", "drainingkiss", "dreameat", "gigadrain", "hornleech",
        "leechlife", "megadrain", "oblivionwing", "paraboliccharge"
    ));

    // Moves blocked by Bulletproof
    public static final Set<String> BULLET_MOVES = new HashSet<>(Arrays.asList(
        "acidspray", "aurasphere", "barrage", "bulletseed", "eggbomb", "electroball",
        "energyball", "focusblast", "gyroball", "iceball", "magnetbomb", "mistball",
        "mudbomb", "octazooka", "overheat", "poisongas", "pyroball", "rockblast",
        "rockwrecker", "searingshot", "seedbomb", "shadowball", "sludgebomb",
        "weatherball", "zapballistics"
    ));

    /**
     * Checks if a move is a setup move of any type
     */
    public static boolean isSetupMove(String move) {
        String normalized = normalize(move);
        return PHYSICAL_SETUP.contains(normalized) ||
               SPECIAL_SETUP.contains(normalized) ||
               MIXED_SETUP.contains(normalized) ||
               SPEED_SETUP.contains(normalized);
    }

    /**
     * Gets the setup type for a move
     */
    public static SetupType getSetupType(String move) {
        String normalized = normalize(move);
        if (PHYSICAL_SETUP.contains(normalized)) return SetupType.PHYSICAL;
        if (SPECIAL_SETUP.contains(normalized)) return SetupType.SPECIAL;
        if (MIXED_SETUP.contains(normalized)) return SetupType.MIXED;
        if (SPEED_SETUP.contains(normalized)) return SetupType.SPEED;
        return SetupType.NONE;
    }

    /**
     * Normalizes move name (lowercase, no spaces/hyphens)
     */
    public static String normalize(String moveName) {
        if (moveName == null) return "";
        return moveName.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "");
    }

    /**
     * Checks if a move is in a set
     */
    public static boolean contains(Set<String> set, String move) {
        return set.contains(normalize(move));
    }
}
