# Smogon Importer - Centralized Pokemon Data Controller

A NeoForge mod for Minecraft 1.21 that serves as a centralized database and API for Pokemon data from Smogon's Pokemon Showdown repository. This mod dynamically imports and manages competitive Pokemon sets, making it easy for other mods to access this data without reimplementing the import logic.

## Features

- 🔄 **Dynamic Data Import**: Automatically fetches Pokemon data from Smogon's repository
- 💾 **Smart Caching**: Local caching with configurable refresh intervals
- 🎲 **Random Generation**: Built-in methods for getting random Pokemon with various filters
- 🛠️ **Simple API**: Easy-to-use methods for other mods to integrate
- ⚙️ **Highly Configurable**: Extensive configuration options
- 📊 **Admin Commands**: Optional commands for server operators

## Installation

1. Ensure you have NeoForge 21.1.77+ installed for Minecraft 1.21
2. Place the mod JAR in your `mods` folder
3. Configure the mod in `config/smogonimporter-common.toml` (generated on first run)

## For Developers - Using the API

### Adding as Dependency

Add to your `build.gradle`:

```gradle
dependencies {
    implementation fg.deobf("com.pixelmon:smogonimporter:1.0.0")
}
```

### Basic Usage Examples

#### Getting the API Instance

```java
import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.api.SmogonAPI;

// Get the API instance
SmogonAPI api = SmogonImporter.getAPI();

// Check if data is ready
if (api.isReady()) {
    // Use the API
}
```

#### Example 1: Battle Tournament Mod

```java
public class BattleTournamentMod {
    
    public void givePokemonToPlayer(Player player) {
        SmogonAPI api = SmogonImporter.getAPI();
        
        // Get 3 random Pokemon for the player
        List<PokemonData> starterPokemon = api.getRandomPokemon(3);
        
        for (PokemonData pokemon : starterPokemon) {
            // Your code to give Pokemon to player
            System.out.println("Giving " + pokemon.getName() + " to player");
            System.out.println("Level: " + pokemon.getLevel());
            System.out.println("Ability: " + pokemon.getRandomAbility());
            System.out.println("Item: " + pokemon.getRandomItem());
            System.out.println("Moves: " + pokemon.getRandomMoves(4));
        }
    }
    
    public void generateNPCTeam() {
        SmogonAPI api = SmogonImporter.getAPI();
        
        // Generate a full team with random sets
        List<SmogonAPI.RandomSet> npcTeam = api.generateRandomTeam(6);
        
        for (SmogonAPI.RandomSet set : npcTeam) {
            // Your code to create NPC Pokemon
            System.out.println("NPC Pokemon: " + set.toString());
        }
    }
}
```

#### Example 2: Hardcore Pixelmon Mod

```java
public class HardcorePixelmonMod {
    private final Set<String> caughtSpecies = new HashSet<>();
    
    public boolean canCatchPokemon(String pokemonName) {
        SmogonAPI api = SmogonImporter.getAPI();
        
        // Check if this species exists in the database
        Optional<PokemonData> pokemonData = api.getPokemon(pokemonName);
        
        if (pokemonData.isPresent()) {
            // Check if player already caught this species
            if (caughtSpecies.contains(pokemonName.toLowerCase())) {
                return false; // Already caught this species
            }
            
            // Generate randomized stats for this catch
            SmogonAPI.RandomSet randomSet = api.generateRandomSet(pokemonName).orElse(null);
            if (randomSet != null) {
                // Apply the random set to the caught Pokemon
                applyRandomStats(randomSet);
            }
            
            caughtSpecies.add(pokemonName.toLowerCase());
            return true;
        }
        
        return false;
    }
    
    private void applyRandomStats(SmogonAPI.RandomSet set) {
        // Your code to apply stats
        System.out.println("Applying stats for " + set.pokemonName);
        System.out.println("EVs: " + set.evs);
        System.out.println("IVs: " + set.ivs);
    }
}
```

#### Example 3: Random Encounter Mod

```java
public class RandomEncounterMod {
    
    public void spawnRandomEncounter(Level world, BlockPos pos) {
        SmogonAPI api = SmogonImporter.getAPI();
        
        // Get Pokemon within a specific level range
        List<PokemonData> levelAppropriate = api.getPokemonByLevelRange(70, 85);
        
        if (!levelAppropriate.isEmpty()) {
            // Pick a random one
            PokemonData chosen = levelAppropriate.get(
                new Random().nextInt(levelAppropriate.size())
            );
            
            // Spawn the Pokemon
            spawnPokemon(world, pos, chosen);
        }
    }
    
    public void spawnThemedEncounter(String theme) {
        SmogonAPI api = SmogonImporter.getAPI();
        
        List<PokemonData> themed;
        
        switch (theme) {
            case "fire":
                // Get Pokemon with fire moves
                themed = api.getPokemonWithMove("Flamethrower");
                break;
            case "speedy":
                // Get Pokemon with speed-related abilities
                themed = api.getPokemonWithAbility("Speed Boost");
                break;
            case "tank":
                // Get Pokemon with defensive items
                themed = api.getPokemonWithItem("Leftovers");
                break;
            default:
                themed = api.getRandomPokemon(1);
        }
        
        // Spawn themed Pokemon
        themed.forEach(this::spawnPokemon);
    }
    
    private void spawnPokemon(PokemonData data) {
        // Your spawning logic here
    }
}
```

#### Example 4: Custom Filtering

```java
public class CustomFilterExample {
    
    public void getSpecificPokemon() {
        SmogonAPI api = SmogonImporter.getAPI();
        
        // Get high-level Pokemon with specific criteria
        List<PokemonData> filtered = api.getRandomPokemonFiltered(
            pokemon -> pokemon.getLevel() >= 80 && 
                       pokemon.getAbilities().contains("Intimidate") &&
                       pokemon.getMoves().size() >= 5,
            3
        );
        
        filtered.forEach(pokemon -> {
            System.out.println("Found: " + pokemon.getName());
        });
    }
}
```

## API Methods Reference

### Core Methods

- `getPokemon(String name)` - Get specific Pokemon by name
- `getRandomPokemon(int count)` - Get multiple random Pokemon
- `getRandomPokemon()` - Get single random Pokemon
- `generateRandomSet(String pokemonName)` - Generate competitive set
- `generateRandomTeam(int count)` - Generate multiple sets

### Query Methods

- `getPokemonByLevelRange(int min, int max)` - Filter by level
- `getPokemonWithAbility(String ability)` - Filter by ability
- `getPokemonWithMove(String move)` - Filter by move
- `getPokemonWithItem(String item)` - Filter by item
- `getRandomPokemonFiltered(Predicate<PokemonData> filter, int count)` - Custom filter

### Utility Methods

- `getAllPokemonNames()` - Get all available Pokemon names
- `getPokemonCount()` - Get total count
- `isReady()` - Check if API is initialized
- `refreshData()` - Force data refresh
- `getStatistics()` - Get data statistics

## Configuration

The mod creates a configuration file at `config/smogonimporter-common.toml` with these options:

```toml
[data_sources]
    # Primary data source URL
    primary_url = "https://data.pkmn.cc/randbats/gen8randombattle.json"
    # Backup URLs if primary fails
    backup_urls = ["..."]
    # Generation to use (gen1-gen9)
    generation = "gen8"

[cache]
    # Enable caching
    use_cache = true
    # Cache duration in hours
    cache_duration = 24
    # Auto-update data
    auto_update = true
    # Update check interval
    update_interval = 6

[performance]
    # Timeouts and retries
    connection_timeout = 10
    read_timeout = 30
    max_retries = 3

[features]
    # Enable admin commands
    enable_commands = true
    # Debug mode
    debug_mode = false
    # Allow custom Pokemon sets
    allow_custom_sets = true
```

## Admin Commands

When enabled, operators can use these commands:

- `/smogon reload` - Reload data from web
- `/smogon get <pokemon>` - Get Pokemon info
- `/smogon random [count]` - Get random Pokemon
- `/smogon generate <pokemon>` - Generate random set
- `/smogon stats` - View statistics
- `/smogon list` - List available Pokemon

## Building from Source

1. Clone the repository
2. Run `./gradlew build`
3. Find the JAR in `build/libs/`

## License

This mod is licensed under the MIT License.

## Credits

- Pokemon data sourced from [Smogon's Pokemon Showdown](https://github.com/smogon/pokemon-showdown)
- Data API provided by [pkmn.cc](https://data.pkmn.cc)

## Support

For issues or feature requests, please use the GitHub issue tracker.