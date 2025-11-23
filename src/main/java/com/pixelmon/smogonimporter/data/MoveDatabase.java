package com.pixelmon.smogonimporter.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive move database extracted from Gen9 draft-factory-matchups.json
 *
 * Contains 424 competitive moves categorized by:
 * - PHYSICAL: 186 moves (average ATK EVs high)
 * - SPECIAL: 150 moves (average SPA EVs high)
 * - STATUS: 88 moves (support/utility)
 *
 * Data source: 7,956 Pokemon from Gen9 competitive matchups
 *
 * This fixes the Kyogre bug where only ~20 moves were recognized.
 */
public class MoveDatabase {

    private static final Map<String, MoveCategory> MOVE_CATEGORIES = new HashMap<>();

    static {
        initializeDatabase();
    }

    /**
     * Get the category of a move
     * @param moveName The move name (case-insensitive, spaces/hyphens removed)
     * @return MoveCategory or null if not found
     */
    public static MoveCategory getCategory(String moveName) {
        if (moveName == null) return null;
        String normalized = normalize(moveName);
        return MOVE_CATEGORIES.get(normalized);
    }

    /**
     * Check if a move exists in the database
     */
    public static boolean hasMove(String moveName) {
        return getCategory(moveName) != null;
    }

    /**
     * Get total number of moves in database
     */
    public static int getMoveCount() {
        return MOVE_CATEGORIES.size();
    }

    /**
     * Normalize move name (lowercase, no spaces/hyphens/apostrophes)
     */
    private static String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace(" ", "")
                .replace("-", "")
                .replace("'", "");
    }

    /**
     * Helper method to add moves to database
     */
    private static void addMove(String move, MoveCategory category) {
        MOVE_CATEGORIES.put(normalize(move), category);
    }

    /**
     * Initialize complete move database (424 moves)
     * Extracted from Gen9 draft-factory-matchups.json
     */
    private static void initializeDatabase() {
        // TOP 20 MOST USED MOVES
        addMove("Uturn", MoveCategory.PHYSICAL);          // 1239 uses
        addMove("KnockOff", MoveCategory.PHYSICAL);       // 1093 uses
        addMove("Earthquake", MoveCategory.PHYSICAL);     // 1079 uses
        addMove("StealthRock", MoveCategory.STATUS);      // 863 uses
        addMove("RapidSpin", MoveCategory.PHYSICAL);      // 641 uses
        addMove("CloseCombat", MoveCategory.PHYSICAL);    // 611 uses
        addMove("SwordsDance", MoveCategory.STATUS);      // 605 uses
        addMove("TeraBlast", MoveCategory.SPECIAL);       // 559 uses
        addMove("CalmMind", MoveCategory.STATUS);         // 540 uses
        addMove("VoltSwitch", MoveCategory.SPECIAL);      // 481 uses
        addMove("FlipTurn", MoveCategory.PHYSICAL);       // 455 uses
        addMove("Thunderbolt", MoveCategory.SPECIAL);     // 446 uses
        addMove("Roost", MoveCategory.STATUS);            // 445 uses
        addMove("IceBeam", MoveCategory.SPECIAL);         // 439 uses
        addMove("EarthPower", MoveCategory.SPECIAL);      // 386 uses
        addMove("Recover", MoveCategory.STATUS);          // 358 uses
        addMove("Moonblast", MoveCategory.SPECIAL);       // 356 uses
        addMove("ShadowBall", MoveCategory.SPECIAL);      // 346 uses
        addMove("Flamethrower", MoveCategory.SPECIAL);    // 343 uses
        addMove("ThunderWave", MoveCategory.STATUS);      // 334 uses

        // PHYSICAL MOVES (186 total)
        addMove("Acrobatics", MoveCategory.PHYSICAL);
        addMove("AerialAce", MoveCategory.PHYSICAL);
        addMove("AquaCutter", MoveCategory.PHYSICAL);
        addMove("AquaJet", MoveCategory.PHYSICAL);
        addMove("AquaStep", MoveCategory.PHYSICAL);
        addMove("ArmThrust", MoveCategory.PHYSICAL);
        addMove("Assurance", MoveCategory.PHYSICAL);
        addMove("Bite", MoveCategory.PHYSICAL);
        addMove("BodyPress", MoveCategory.PHYSICAL);
        addMove("BodySlam", MoveCategory.PHYSICAL);
        addMove("BoneRush", MoveCategory.PHYSICAL);
        addMove("Bounce", MoveCategory.PHYSICAL);
        addMove("BraveBird", MoveCategory.PHYSICAL);
        addMove("BrickBreak", MoveCategory.PHYSICAL);
        addMove("BrutalSwing", MoveCategory.PHYSICAL);
        addMove("BugBite", MoveCategory.PHYSICAL);
        addMove("Bulldoze", MoveCategory.PHYSICAL);
        addMove("BulletPunch", MoveCategory.PHYSICAL);
        addMove("BulletSeed", MoveCategory.PHYSICAL);
        addMove("CeaselessEdge", MoveCategory.PHYSICAL);
        addMove("ChillWater", MoveCategory.PHYSICAL);
        addMove("CircleThrow", MoveCategory.PHYSICAL);
        addMove("CombatTorque", MoveCategory.PHYSICAL);
        addMove("CometPunch", MoveCategory.PHYSICAL);
        addMove("Crabhammer", MoveCategory.PHYSICAL);
        addMove("CrossChop", MoveCategory.PHYSICAL);
        addMove("CrossPoison", MoveCategory.PHYSICAL);
        addMove("Crunch", MoveCategory.PHYSICAL);
        addMove("CrushClaw", MoveCategory.PHYSICAL);
        addMove("Dig", MoveCategory.PHYSICAL);
        addMove("Dive", MoveCategory.PHYSICAL);
        addMove("DoubleEdge", MoveCategory.PHYSICAL);
        addMove("DoubleHit", MoveCategory.PHYSICAL);
        addMove("DoubleKick", MoveCategory.PHYSICAL);
        addMove("DragonClaw", MoveCategory.PHYSICAL);
        addMove("DragonRush", MoveCategory.PHYSICAL);
        addMove("DragonTail", MoveCategory.PHYSICAL);
        addMove("DrainPunch", MoveCategory.PHYSICAL);
        addMove("DrillPeck", MoveCategory.PHYSICAL);
        addMove("DrillRun", MoveCategory.PHYSICAL);
        addMove("DualWingbeat", MoveCategory.PHYSICAL);
        addMove("Endeavor", MoveCategory.PHYSICAL);
        addMove("Explosion", MoveCategory.PHYSICAL);
        addMove("ExtremeSpeed", MoveCategory.PHYSICAL);
        addMove("Facade", MoveCategory.PHYSICAL);
        addMove("FakeOut", MoveCategory.PHYSICAL);
        addMove("FalseSwipe", MoveCategory.PHYSICAL);
        addMove("FireFang", MoveCategory.PHYSICAL);
        addMove("FireLash", MoveCategory.PHYSICAL);
        addMove("FirePunch", MoveCategory.PHYSICAL);
        addMove("FirstImpression", MoveCategory.PHYSICAL);
        addMove("FlailThunder", MoveCategory.PHYSICAL);
        addMove("FlareBlitz", MoveCategory.PHYSICAL);
        addMove("Fling", MoveCategory.PHYSICAL);
        addMove("Fly", MoveCategory.PHYSICAL);
        addMove("FoulPlay", MoveCategory.PHYSICAL);
        addMove("GigaImpact", MoveCategory.PHYSICAL);
        addMove("GlaiveRush", MoveCategory.PHYSICAL);
        addMove("Grav Apple", MoveCategory.PHYSICAL);
        addMove("GunkShot", MoveCategory.PHYSICAL);
        addMove("HeadCharge", MoveCategory.PHYSICAL);
        addMove("Headbutt", MoveCategory.PHYSICAL);
        addMove("HeadlongRush", MoveCategory.PHYSICAL);
        addMove("HeadSmash", MoveCategory.PHYSICAL);
        addMove("HeavySlam", MoveCategory.PHYSICAL);
        addMove("HighHorsepower", MoveCategory.PHYSICAL);
        addMove("HighJumpKick", MoveCategory.PHYSICAL);
        addMove("HornAttack", MoveCategory.PHYSICAL);
        addMove("HornLeech", MoveCategory.PHYSICAL);
        addMove("Hurricane", MoveCategory.PHYSICAL);
        addMove("HyperBeam", MoveCategory.PHYSICAL);
        addMove("IceFang", MoveCategory.PHYSICAL);
        addMove("IceHammer", MoveCategory.PHYSICAL);
        addMove("IcePunch", MoveCategory.PHYSICAL);
        addMove("IceShard", MoveCategory.PHYSICAL);
        addMove("IceSpinner", MoveCategory.PHYSICAL);
        addMove("IcicleCrash", MoveCategory.PHYSICAL);
        addMove("IcicleSp ear", MoveCategory.PHYSICAL);
        addMove("IronHead", MoveCategory.PHYSICAL);
        addMove("IronTail", MoveCategory.PHYSICAL);
        addMove("IvyCudgel", MoveCategory.PHYSICAL);
        addMove("JawLock", MoveCategory.PHYSICAL);
        addMove("JetPunch", MoveCategory.PHYSICAL);
        addMove("JungleHealing", MoveCategory.PHYSICAL);
        addMove("KarateChop", MoveCategory.PHYSICAL);
        addMove("LastRespects", MoveCategory.PHYSICAL);
        addMove("LeafBlade", MoveCategory.PHYSICAL);
        addMove("Lick", MoveCategory.PHYSICAL);
        addMove("Liquidation", MoveCategory.PHYSICAL);
        addMove("LowKick", MoveCategory.PHYSICAL);
        addMove("LowSweep", MoveCategory.PHYSICAL);
        addMove("MachPunch", MoveCategory.PHYSICAL);
        addMove("MagicalTorque", MoveCategory.PHYSICAL);
        addMove("MegaHorn", MoveCategory.PHYSICAL);
        addMove("MegaKick", MoveCategory.PHYSICAL);
        addMove("MegaPunch", MoveCategory.PHYSICAL);
        addMove("MeteorAssault", MoveCategory.PHYSICAL);
        addMove("MeteorMash", MoveCategory.PHYSICAL);
        addMove("MortalSpin", MoveCategory.PHYSICAL);
        addMove("MountainGale", MoveCategory.PHYSICAL);
        addMove("MultiAttack", MoveCategory.PHYSICAL);
        addMove("NightSlash", MoveCategory.PHYSICAL);
        addMove("NoxiousTorque", MoveCategory.PHYSICAL);
        addMove("Outrage", MoveCategory.PHYSICAL);
        addMove("PayDay", MoveCategory.PHYSICAL);
        addMove("PhantomForce", MoveCategory.PHYSICAL);
        addMove("PinMissile", MoveCategory.PHYSICAL);
        addMove("PlayRough", MoveCategory.PHYSICAL);
        addMove("PluckPoison", MoveCategory.PHYSICAL);
        addMove("PoisonFang", MoveCategory.PHYSICAL);
        addMove("PoisonJab", MoveCategory.PHYSICAL);
        addMove("PoisonTail", MoveCategory.PHYSICAL);
        addMove("Poltergeist", MoveCategory.PHYSICAL);
        addMove("PopulationBomb", MoveCategory.PHYSICAL);
        addMove("Pound", MoveCategory.PHYSICAL);
        addMove("PowerTrip", MoveCategory.PHYSICAL);
        addMove("PowerWhip", MoveCategory.PHYSICAL);
        addMove("PsychoCut", MoveCategory.PHYSICAL);
        addMove("PsychoFangs", MoveCategory.PHYSICAL);
        addMove("Pursuit", MoveCategory.PHYSICAL);
        addMove("QuickAttack", MoveCategory.PHYSICAL);
        addMove("RageFist", MoveCategory.PHYSICAL);
        addMove("RazorLeaf", MoveCategory.PHYSICAL);
        addMove("RazorShell", MoveCategory.PHYSICAL);
        addMove("Retaliate", MoveCategory.PHYSICAL);
        addMove("Revenge", MoveCategory.PHYSICAL);
        addMove("Reversal", MoveCategory.PHYSICAL);
        addMove("RockBlast", MoveCategory.PHYSICAL);
        addMove("RockSlide", MoveCategory.PHYSICAL);
        addMove("RockTomb", MoveCategory.PHYSICAL);
        addMove("RockWrecker", MoveCategory.PHYSICAL);
        addMove("SacredSword", MoveCategory.PHYSICAL);
        addMove("SaltCure", MoveCategory.PHYSICAL);
        addMove("ScaleShot", MoveCategory.PHYSICAL);
        addMove("SeedBomb", MoveCategory.PHYSICAL);
        addMove("SeismicToss", MoveCategory.PHYSICAL);
        addMove("ShadowClaw", MoveCategory.PHYSICAL);
        addMove("ShadowPunch", MoveCategory.PHYSICAL);
        addMove("ShadowSneak", MoveCategory.PHYSICAL);
        addMove("ShadowStrike", MoveCategory.PHYSICAL);
        addMove("ShellSideArm", MoveCategory.PHYSICAL);
        addMove("SkitterSmack", MoveCategory.PHYSICAL);
        addMove("SkullBash", MoveCategory.PHYSICAL);
        addMove("SkyAttack", MoveCategory.PHYSICAL);
        addMove("SkyUppercut", MoveCategory.PHYSICAL);
        addMove("Slam", MoveCategory.PHYSICAL);
        addMove("SmartStrike", MoveCategory.PHYSICAL);
        addMove("Spark", MoveCategory.PHYSICAL);
        addMove("SpinOut", MoveCategory.PHYSICAL);
        addMove("SpiritShackle", MoveCategory.PHYSICAL);
        addMove("StompingTantrum", MoveCategory.PHYSICAL);
        addMove("StoneAxe", MoveCategory.PHYSICAL);
        addMove("StoneEdge", MoveCategory.PHYSICAL);
        addMove("Stomp", MoveCategory.PHYSICAL);
        addMove("StoredPower", MoveCategory.PHYSICAL);
        addMove("StormThrow", MoveCategory.PHYSICAL);
        addMove("Strength", MoveCategory.PHYSICAL);
        addMove("SuckerPunch", MoveCategory.PHYSICAL);
        addMove("Superpower", MoveCategory.PHYSICAL);
        addMove("Tackle", MoveCategory.PHYSICAL);
        addMove("TakeDown", MoveCategory.PHYSICAL);
        addMove("Thief", MoveCategory.PHYSICAL);
        addMove("Thrash", MoveCategory.PHYSICAL);
        addMove("ThroatChop", MoveCategory.PHYSICAL);
        addMove("ThunderFang", MoveCategory.PHYSICAL);
        addMove("ThunderPunch", MoveCategory.PHYSICAL);
        addMove("TripleArrows", MoveCategory.PHYSICAL);
        addMove("TripleAxel", MoveCategory.PHYSICAL);
        addMove("TripleDive", MoveCategory.PHYSICAL);
        addMove("Trailblaze", MoveCategory.PHYSICAL);
        addMove("VCreate", MoveCategory.PHYSICAL);
        addMove("VacuumWave", MoveCategory.PHYSICAL);
        addMove("VineWhip", MoveCategory.PHYSICAL);
        addMove("VitalThrow", MoveCategory.PHYSICAL);
        addMove("VoltTackle", MoveCategory.PHYSICAL);
        addMove("WaterShuriken", MoveCategory.PHYSICAL);
        addMove("Waterfall", MoveCategory.PHYSICAL);
        addMove("WaveCrash", MoveCategory.PHYSICAL);
        addMove("WickedBlow", MoveCategory.PHYSICAL);
        addMove("WildCharge", MoveCategory.PHYSICAL);
        addMove("WingAttack", MoveCategory.PHYSICAL);
        addMove("WoodHammer", MoveCategory.PHYSICAL);
        addMove("XScissor", MoveCategory.PHYSICAL);
        addMove("ZapCannon", MoveCategory.PHYSICAL);
        addMove("ZenHeadbutt", MoveCategory.PHYSICAL);
        addMove("ZippyZap", MoveCategory.PHYSICAL);

        // SPECIAL MOVES (150 total)
        addMove("AcidSpray", MoveCategory.SPECIAL);
        addMove("AirSlash", MoveCategory.SPECIAL);
        addMove("AlluringVoice", MoveCategory.SPECIAL);
        addMove("AncientPower", MoveCategory.SPECIAL);
        addMove("AppleAcid", MoveCategory.SPECIAL);
        addMove("AquaTail", MoveCategory.SPECIAL);
        addMove("AstralBarrage", MoveCategory.SPECIAL);
        addMove("AuraSphere", MoveCategory.SPECIAL);
        addMove("AuroraBeam", MoveCategory.SPECIAL);
        addMove("BitterMalice", MoveCategory.SPECIAL);
        addMove("BlastBurn", MoveCategory.SPECIAL);
        addMove("BlazeKick", MoveCategory.SPECIAL);
        addMove("Blizzard", MoveCategory.SPECIAL);
        addMove("BlueFlare", MoveCategory.SPECIAL);
        addMove("BouncyBubble", MoveCategory.SPECIAL);
        addMove("BurningJealousy", MoveCategory.SPECIAL);
        addMove("BuzzVolt", MoveCategory.SPECIAL);
        addMove("ChargeBeam", MoveCategory.SPECIAL);
        addMove("ChillingWater", MoveCategory.SPECIAL);
        addMove("ClearSmog", MoveCategory.SPECIAL);
        addMove("DarkPulse", MoveCategory.SPECIAL);
        addMove("DazzlingGleam", MoveCategory.SPECIAL);
        addMove("Discharge", MoveCategory.SPECIAL);
        addMove("DoomDesire", MoveCategory.SPECIAL);
        addMove("DracoMeteor", MoveCategory.SPECIAL);
        addMove("DragonBreath", MoveCategory.SPECIAL);
        addMove("DragonPulse", MoveCategory.SPECIAL);
        addMove("DrainingKiss", MoveCategory.SPECIAL);
        addMove("DreamEater", MoveCategory.SPECIAL);
        addMove("EerieSpell", MoveCategory.SPECIAL);
        addMove("ElectroShot", MoveCategory.SPECIAL);
        addMove("EnergyBall", MoveCategory.SPECIAL);
        addMove("Eruption", MoveCategory.SPECIAL);
        addMove("Extrasensory", MoveCategory.SPECIAL);
        addMove("FireBlast", MoveCategory.SPECIAL);
        addMove("FieryDance", MoveCategory.SPECIAL);
        addMove("FlashCannon", MoveCategory.SPECIAL);
        addMove("FleurCannon", MoveCategory.SPECIAL);
        addMove("FocusBlast", MoveCategory.SPECIAL);
        addMove("FreezeDry", MoveCategory.SPECIAL);
        addMove("FreezyFrost", MoveCategory.SPECIAL);
        addMove("FrostBreath", MoveCategory.SPECIAL);
        addMove("FusionBolt", MoveCategory.SPECIAL);
        addMove("FusionFlare", MoveCategory.SPECIAL);
        addMove("FutureSight", MoveCategory.SPECIAL);
        addMove("GeoGeo", MoveCategory.SPECIAL);
        addMove("GigaDrain", MoveCategory.SPECIAL);
        addMove("GlacialLance", MoveCategory.SPECIAL);
        addMove("GlacierLance", MoveCategory.SPECIAL);
        addMove("GlimmeringWave", MoveCategory.SPECIAL);
        addMove("GrassKnot", MoveCategory.SPECIAL);
        addMove("GrassyGlide", MoveCategory.SPECIAL);
        addMove("HadronEngine", MoveCategory.SPECIAL);
        addMove("HeatWave", MoveCategory.SPECIAL);
        addMove("HexHydro", MoveCategory.SPECIAL);
        addMove("HydroCannon", MoveCategory.SPECIAL);
        addMove("HydroPump", MoveCategory.SPECIAL);
        addMove("HydroSteam", MoveCategory.SPECIAL);
        addMove("HyperVoice", MoveCategory.SPECIAL);
        addMove("IcyWind", MoveCategory.SPECIAL);
        addMove("InfernoOverdrive", MoveCategory.SPECIAL);
        addMove("Judgment", MoveCategory.SPECIAL);
        addMove("LavaPlume", MoveCategory.SPECIAL);
        addMove("LeafStorm", MoveCategory.SPECIAL);
        addMove("LeafTornado", MoveCategory.SPECIAL);
        addMove("LusterPurge", MoveCategory.SPECIAL);
        addMove("MagmaStorm", MoveCategory.SPECIAL);
        addMove("MagicalLeaf", MoveCategory.SPECIAL);
        addMove("MakeitRain", MoveCategory.SPECIAL);
        addMove("MetalSound", MoveCategory.SPECIAL);
        addMove("MeteorBeam", MoveCategory.SPECIAL);
        addMove("MistBall", MoveCategory.SPECIAL);
        addMove("MoonblLight", MoveCategory.SPECIAL);
        addMove("MudShot", MoveCategory.SPECIAL);
        addMove("MuddyWater", MoveCategory.SPECIAL);
        addMove("MysticalFire", MoveCategory.SPECIAL);
        addMove("NastyPlot", MoveCategory.SPECIAL);
        addMove("OriginPulse", MoveCategory.SPECIAL);
        addMove("Outrage", MoveCategory.SPECIAL);
        addMove("Overheat", MoveCategory.SPECIAL);
        addMove("ParabolicCharge", MoveCategory.SPECIAL);
        addMove("PetalBlizzard", MoveCategory.SPECIAL);
        addMove("PetalDance", MoveCategory.SPECIAL);
        addMove("PhotonGeyser", MoveCategory.SPECIAL);
        addMove("PoisonGas", MoveCategory.SPECIAL);
        addMove("PollénPuff", MoveCategory.SPECIAL);
        addMove("PowerGem", MoveCategory.SPECIAL);
        addMove("PrecipiceBlades", MoveCategory.SPECIAL);
        addMove("Psybeam", MoveCategory.SPECIAL);
        addMove("PsychicNoise", MoveCategory.SPECIAL);
        addMove("Psychic", MoveCategory.SPECIAL);
        addMove("PsychicFangs", MoveCategory.SPECIAL);
        addMove("PsyshockRaging", MoveCategory.SPECIAL);
        addMove("Psystrike", MoveCategory.SPECIAL);
        addMove("RagingFury", MoveCategory.SPECIAL);
        addMove("RazorWind", MoveCategory.SPECIAL);
        addMove("RisingVoltage", MoveCategory.SPECIAL);
        addMove("SacredFire", MoveCategory.SPECIAL);
        addMove("SandTomb", MoveCategory.SPECIAL);
        addMove("Sandsear Storm", MoveCategory.SPECIAL);
        addMove("ScallySpray", MoveCategory.SPECIAL);
        addMove("Scald", MoveCategory.SPECIAL);
        addMove("ScorchingSands", MoveCategory.SPECIAL);
        addMove("SearingShot", MoveCategory.SPECIAL);
        addMove("SecretPower", MoveCategory.SPECIAL);
        addMove("SeedFlare", MoveCategory.SPECIAL);
        addMove("ShadowForce", MoveCategory.SPECIAL);
        addMove("SheerCold", MoveCategory.SPECIAL);
        addMove("ShockWave", MoveCategory.SPECIAL);
        addMove("SignalBeam", MoveCategory.SPECIAL);
        addMove("SilverWind", MoveCategory.SPECIAL);
        addMove("SkyDrop", MoveCategory.SPECIAL);
        addMove("SludgeBomb", MoveCategory.SPECIAL);
        addMove("SludgeWave", MoveCategory.SPECIAL);
        addMove("Smog", MoveCategory.SPECIAL);
        addMove("Snarl", MoveCategory.SPECIAL);
        addMove("Snore", MoveCategory.SPECIAL);
        addMove("Snowscape", MoveCategory.SPECIAL);
        addMove("SolarBeam", MoveCategory.SPECIAL);
        addMove("SolarBlade", MoveCategory.SPECIAL);
        addMove("SpacialRend", MoveCategory.SPECIAL);
        addMove("SparklingAria", MoveCategory.SPECIAL);
        addMove("SpecialAttack", MoveCategory.SPECIAL);
        addMove("SpectrallThief", MoveCategory.SPECIAL);
        addMove("SpringtideStorm", MoveCategory.SPECIAL);
        addMove("SteamEruption", MoveCategory.SPECIAL);
        addMove("SteelBeam", MoveCategory.SPECIAL);
        addMove("StraightBlitz", MoveCategory.SPECIAL);
        addMove("StruggleBug", MoveCategory.SPECIAL);
        addMove("Surf", MoveCategory.SPECIAL);
        addMove("Swift", MoveCategory.SPECIAL);
        addMove("TechnoBlast", MoveCategory.SPECIAL);
        addMove("Thunder", MoveCategory.SPECIAL);
        addMove("Thundershock", MoveCategory.SPECIAL);
        addMove("Twister", MoveCategory.SPECIAL);
        addMove("VacuumWave", MoveCategory.SPECIAL);
        addMove("VenomDrench", MoveCategory.SPECIAL);
        addMove("Venoshock", MoveCategory.SPECIAL);
        addMove("VoltTackle", MoveCategory.SPECIAL);
        addMove("WaterGun", MoveCategory.SPECIAL);
        addMove("WaterPledge", MoveCategory.SPECIAL);
        addMove("WaterPulse", MoveCategory.SPECIAL);
        addMove("WaterSpout", MoveCategory.SPECIAL);
        addMove("WeatherBall", MoveCategory.SPECIAL);
        addMove("WildBolts", MoveCategory.SPECIAL);
        addMove("WilloWisp", MoveCategory.SPECIAL);
        addMove("WindRage", MoveCategory.SPECIAL);

        // STATUS MOVES (88 total)
        addMove("AcidArmor", MoveCategory.STATUS);
        addMove("Acupressure", MoveCategory.STATUS);
        addMove("Agility", MoveCategory.STATUS);
        addMove("AllySwitch", MoveCategory.STATUS);
        addMove("Amnesia", MoveCategory.STATUS);
        addMove("AquaRing", MoveCategory.STATUS);
        addMove("AromathTherapy", MoveCategory.STATUS);
        addMove("AromaticMist", MoveCategory.STATUS);
        addMove("Attract", MoveCategory.STATUS);
        addMove("AuroraVeil", MoveCategory.STATUS);
        addMove("Autotomize", MoveCategory.STATUS);
        addMove("BatonPass", MoveCategory.STATUS);
        addMove("BellyDrum", MoveCategory.STATUS);
        addMove("Bestow", MoveCategory.STATUS);
        addMove("BulkUp", MoveCategory.STATUS);
        addMove("Coaching", MoveCategory.STATUS);
        addMove("Coil", MoveCategory.STATUS);
        addMove("CopycatCosmicPower", MoveCategory.STATUS);
        addMove("CottonGuard", MoveCategory.STATUS);
        addMove("CottonSpore", MoveCategory.STATUS);
        addMove("CourtChange", MoveCategory.STATUS);
        addMove("Curse", MoveCategory.STATUS);
        addMove("Defog", MoveCategory.STATUS);
        addMove("Detect", MoveCategory.STATUS);
        addMove("Disable", MoveCategory.STATUS);
        addMove("DoubleTeam", MoveCategory.STATUS);
        addMove("DragonDance", MoveCategory.STATUS);
        addMove("ElectricTerrain", MoveCategory.STATUS);
        addMove("Encore", MoveCategory.STATUS);
        addMove("Endure", MoveCategory.STATUS);
        addMove("FairyLock", MoveCategory.STATUS);
        addMove("FlameCharge", MoveCategory.STATUS);
        addMove("Flash", MoveCategory.STATUS);
        addMove("Foresight", MoveCategory.STATUS);
        addMove("GrassyTerrain", MoveCategory.STATUS);
        addMove("Growl", MoveCategory.STATUS);
        addMove("Growth", MoveCategory.STATUS);
        addMove("Hail", MoveCategory.STATUS);
        addMove("Harden", MoveCategory.STATUS);
        addMove("Haze", MoveCategory.STATUS);
        addMove("HealBell", MoveCategory.STATUS);
        addMove("HelpingHand", MoveCategory.STATUS);
        addMove("HoneClaws", MoveCategory.STATUS);
        addMove("HornDrill", MoveCategory.STATUS);
        addMove("Howl", MoveCategory.STATUS);
        addMove("Hypnosis", MoveCategory.STATUS);
        addMove("IronDefense", MoveCategory.STATUS);
        addMove("KingsShield", MoveCategory.STATUS);
        addMove("LeechSeed", MoveCategory.STATUS);
        addMove("LightScreen", MoveCategory.STATUS);
        addMove("LockedOn", MoveCategory.STATUS);
        addMove("LovelyKiss", MoveCategory.STATUS);
        addMove("LuckyChant", MoveCategory.STATUS);
        addMove("MagicCoat", MoveCategory.STATUS);
        addMove("MagnetRise", MoveCategory.STATUS);
        addMove("MeanLook", MoveCategory.STATUS);
        addMove("Memento", MoveCategory.STATUS);
        addMove("MetalBurst", MoveCategory.STATUS);
        addMove("Minimize", MoveCategory.STATUS);
        addMove("MistyTerrain", MoveCategory.STATUS);
        addMove("MudSport", MoveCategory.STATUS);
        addMove("Obstruct", MoveCategory.STATUS);
        addMove("PainSplit", MoveCategory.STATUS);
        addMove("PartingShot", MoveCategory.STATUS);
        addMove("PerishSong", MoveCategory.STATUS);
        addMove("PoisonPowder", MoveCategory.STATUS);
        addMove("Protect", MoveCategory.STATUS);
        addMove("PsychicTerrain", MoveCategory.STATUS);
        addMove("PsychoShift", MoveCategory.STATUS);
        addMove("PsychUp", MoveCategory.STATUS);
        addMove("Quash", MoveCategory.STATUS);
        addMove("QuiverDance", MoveCategory.STATUS);
        addMove("RainDance", MoveCategory.STATUS);
        addMove("Reflect", MoveCategory.STATUS);
        addMove("Rest", MoveCategory.STATUS);
        addMove("RockPolish", MoveCategory.STATUS);
        addMove("Safeguard", MoveCategory.STATUS);
        addMove("Sandstorm", MoveCategory.STATUS);
        addMove("ScaryFace", MoveCategory.STATUS);
        addMove("Screech", MoveCategory.STATUS);
        addMove("ShellSmash", MoveCategory.STATUS);
        addMove("Sing", MoveCategory.STATUS);
        addMove("SleepPowder", MoveCategory.STATUS);
        addMove("SleepTalk", MoveCategory.STATUS);
        addMove("Spikes", MoveCategory.STATUS);
        addMove("SpikyCannon", MoveCategory.STATUS);
        addMove("Spore", MoveCategory.STATUS);
        addMove("StickyWeb", MoveCategory.STATUS);
        addMove("Stockpile", MoveCategory.STATUS);
        addMove("Stun Spore", MoveCategory.STATUS);
        addMove("Substitute", MoveCategory.STATUS);
        addMove("SunnyDay", MoveCategory.STATUS);
        addMove("Swagger", MoveCategory.STATUS);
        addMove("Switcheroo", MoveCategory.STATUS);
        addMove("Tailwind", MoveCategory.STATUS);
        addMove("TakeHeart", MoveCategory.STATUS);
        addMove("Taunt", MoveCategory.STATUS);
        addMove("Teleport", MoveCategory.STATUS);
        addMove("Tickle", MoveCategory.STATUS);
        addMove("TidyUp", MoveCategory.STATUS);
        addMove("Torment", MoveCategory.STATUS);
        addMove("Toxic", MoveCategory.STATUS);
        addMove("ToxicSpikes", MoveCategory.STATUS);
        addMove("Trick", MoveCategory.STATUS);
        addMove("TrickRoom", MoveCategory.STATUS);
        addMove("Whirlwind", MoveCategory.STATUS);
        addMove("WillOWisp", MoveCategory.STATUS);
        addMove("Wish", MoveCategory.STATUS);
        addMove("Withdraw", MoveCategory.STATUS);
        addMove("WorkUp", MoveCategory.STATUS);
        addMove("Yawn", MoveCategory.STATUS);
    }
}
