package com.pixelmon.smogonimporter.data;

import com.google.gson.*;
import com.pixelmon.smogonimporter.SmogonImporter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing TypeScript data files from Pokemon Showdown
 *
 * Uses targeted field extraction instead of trying to convert entire TypeScript to JSON.
 * Extracts only the fields we need for competitive Pokemon generation.
 */
public class TypeScriptParser {

    /**
     * Parse a TypeScript file and extract the data object as JSON
     * Uses targeted extraction based on the export type
     *
     * @param tsContent The raw TypeScript file content
     * @param exportName The name of the export to extract (e.g., "Moves", "Pokedex", "Abilities")
     * @return JsonObject containing the parsed data
     */
    public static JsonObject parseTypeScript(String tsContent, String exportName) {
        try {
            SmogonImporter.LOGGER.info("Parsing TypeScript file for export '{}'", exportName);

            // Route to specialized parser based on export type
            switch (exportName.toLowerCase()) {
                case "moves":
                    return parseMovesData(tsContent);
                case "pokedex":
                    return parsePokedexData(tsContent);
                case "items":
                    return parseItemsData(tsContent);
                case "abilities":
                    return parseAbilitiesData(tsContent);
                case "natures":
                    return parseNaturesData(tsContent);
                case "typechart":
                    return parseTypeChartData(tsContent);
                default:
                    SmogonImporter.LOGGER.warn("Unknown export type '{}', using generic parser", exportName);
                    return parseGeneric(tsContent, exportName);
            }

        } catch (Exception e) {
            SmogonImporter.LOGGER.error("Failed to parse TypeScript file for export '{}'", exportName, e);
            return new JsonObject();
        }
    }

    /**
     * Parse Moves data - extract only essential fields
     * Fields: name, type, category, basePower, accuracy, flags, target
     */
    private static JsonObject parseMovesData(String tsContent) {
        JsonObject result = new JsonObject();

        // Remove comments first
        String cleaned = removeComments(tsContent);

        // Pattern to match move entries: movename: { ... }
        Pattern entryPattern = Pattern.compile("(\\w+):\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}", Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(cleaned);

        int count = 0;
        while (matcher.find()) {
            String moveName = matcher.group(1);
            String moveBody = matcher.group(2);

            JsonObject moveData = new JsonObject();

            // Extract simple string fields
            extractStringField(moveBody, "type", moveData);
            extractStringField(moveBody, "category", moveData);
            extractStringField(moveBody, "target", moveData);

            // Extract numeric fields
            extractNumericField(moveBody, "basePower", moveData);
            extractNumericField(moveBody, "accuracy", moveData);
            extractNumericField(moveBody, "priority", moveData);

            // Extract boolean fields
            extractBooleanField(moveBody, "hasSheerForce", moveData);

            result.add(moveName, moveData);
            count++;
        }

        SmogonImporter.LOGGER.info("Parsed {} moves from TypeScript", count);
        return result;
    }

    /**
     * Parse Pokedex data - extract only essential fields
     * Fields: num, types, baseStats, abilities
     */
    private static JsonObject parsePokedexData(String tsContent) {
        JsonObject result = new JsonObject();

        String cleaned = removeComments(tsContent);

        // Pattern to match Pokemon entries
        Pattern entryPattern = Pattern.compile("(\\w+):\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}", Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(cleaned);

        int count = 0;
        while (matcher.find()) {
            String pokemonName = matcher.group(1);
            String pokemonBody = matcher.group(2);

            JsonObject pokemonData = new JsonObject();

            // Extract num
            extractNumericField(pokemonBody, "num", pokemonData);

            // Extract types array
            extractArrayField(pokemonBody, "types", pokemonData);

            // Extract baseStats object
            Pattern statsPattern = Pattern.compile("baseStats:\\s*\\{([^}]+)\\}");
            Matcher statsMatcher = statsPattern.matcher(pokemonBody);
            if (statsMatcher.find()) {
                String statsBody = statsMatcher.group(1);
                JsonObject stats = new JsonObject();

                extractNumericField(statsBody, "hp", stats);
                extractNumericField(statsBody, "atk", stats);
                extractNumericField(statsBody, "def", stats);
                extractNumericField(statsBody, "spa", stats);
                extractNumericField(statsBody, "spd", stats);
                extractNumericField(statsBody, "spe", stats);

                pokemonData.add("baseStats", stats);
            }

            // Extract abilities object
            Pattern abilitiesPattern = Pattern.compile("abilities:\\s*\\{([^}]+)\\}");
            Matcher abilitiesMatcher = abilitiesPattern.matcher(pokemonBody);
            if (abilitiesMatcher.find()) {
                String abilitiesBody = abilitiesMatcher.group(1);
                JsonObject abilities = new JsonObject();

                extractStringField(abilitiesBody, "0", abilities);
                extractStringField(abilitiesBody, "1", abilities);
                extractStringField(abilitiesBody, "H", abilities);
                extractStringField(abilitiesBody, "S", abilities);

                pokemonData.add("abilities", abilities);
            }

            result.add(pokemonName, pokemonData);
            count++;
        }

        SmogonImporter.LOGGER.info("Parsed {} Pokemon from Pokedex", count);
        return result;
    }

    /**
     * Parse Items data - extract only essential fields
     * Fields: name, num, isChoice, isBerry
     */
    private static JsonObject parseItemsData(String tsContent) {
        JsonObject result = new JsonObject();

        String cleaned = removeComments(tsContent);

        Pattern entryPattern = Pattern.compile("(\\w+):\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}", Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(cleaned);

        int count = 0;
        while (matcher.find()) {
            String itemName = matcher.group(1);
            String itemBody = matcher.group(2);

            JsonObject itemData = new JsonObject();

            extractStringField(itemBody, "name", itemData);
            extractNumericField(itemBody, "num", itemData);
            extractBooleanField(itemBody, "isChoice", itemData);
            extractBooleanField(itemBody, "isBerry", itemData);

            result.add(itemName, itemData);
            count++;
        }

        SmogonImporter.LOGGER.info("Parsed {} items from TypeScript", count);
        return result;
    }

    /**
     * Parse Abilities data - extract only essential fields
     * Fields: name, num
     */
    private static JsonObject parseAbilitiesData(String tsContent) {
        JsonObject result = new JsonObject();

        String cleaned = removeComments(tsContent);

        Pattern entryPattern = Pattern.compile("(\\w+):\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}", Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(cleaned);

        int count = 0;
        while (matcher.find()) {
            String abilityName = matcher.group(1);
            String abilityBody = matcher.group(2);

            JsonObject abilityData = new JsonObject();

            extractStringField(abilityBody, "name", abilityData);
            extractNumericField(abilityBody, "num", abilityData);

            result.add(abilityName, abilityData);
            count++;
        }

        SmogonImporter.LOGGER.info("Parsed {} abilities from TypeScript", count);
        return result;
    }

    /**
     * Parse Natures data - extract only essential fields
     * Fields: name, plus, minus
     */
    private static JsonObject parseNaturesData(String tsContent) {
        JsonObject result = new JsonObject();

        String cleaned = removeComments(tsContent);

        Pattern entryPattern = Pattern.compile("(\\w+):\\s*\\{([^}]*)\\}", Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(cleaned);

        int count = 0;
        while (matcher.find()) {
            String natureName = matcher.group(1);
            String natureBody = matcher.group(2);

            JsonObject natureData = new JsonObject();

            extractStringField(natureBody, "name", natureData);
            extractStringField(natureBody, "plus", natureData);
            extractStringField(natureBody, "minus", natureData);

            result.add(natureName, natureData);
            count++;
        }

        SmogonImporter.LOGGER.info("Parsed {} natures from TypeScript", count);
        return result;
    }

    /**
     * Parse TypeChart data - extract type effectiveness
     */
    private static JsonObject parseTypeChartData(String tsContent) {
        JsonObject result = new JsonObject();

        String cleaned = removeComments(tsContent);

        Pattern entryPattern = Pattern.compile("(\\w+):\\s*\\{([^}]*(?:\\{[^}]*\\}[^}]*)*)\\}", Pattern.DOTALL);
        Matcher matcher = entryPattern.matcher(cleaned);

        int count = 0;
        while (matcher.find()) {
            String typeName = matcher.group(1);
            String typeBody = matcher.group(2);

            JsonObject typeData = new JsonObject();

            // Extract damageTaken object
            Pattern damageTakenPattern = Pattern.compile("damageTaken:\\s*\\{([^}]+)\\}");
            Matcher damageMatcher = damageTakenPattern.matcher(typeBody);
            if (damageMatcher.find()) {
                String damageBody = damageMatcher.group(1);
                JsonObject damageTaken = new JsonObject();

                // Extract all type effectiveness values
                Pattern effectivenessPattern = Pattern.compile("(\\w+):\\s*(\\d+)");
                Matcher effMatcher = effectivenessPattern.matcher(damageBody);
                while (effMatcher.find()) {
                    String attackType = effMatcher.group(1);
                    int effectiveness = Integer.parseInt(effMatcher.group(2));
                    damageTaken.addProperty(attackType, effectiveness);
                }

                typeData.add("damageTaken", damageTaken);
            }

            result.add(typeName, typeData);
            count++;
        }

        SmogonImporter.LOGGER.info("Parsed {} types from TypeChart", count);
        return result;
    }

    /**
     * Generic fallback parser (uses old logic)
     */
    private static JsonObject parseGeneric(String tsContent, String exportName) {
        try {
            String cleaned = removeComments(tsContent);
            String objectContent = extractExportedObject(cleaned, exportName);

            if (objectContent == null) {
                return new JsonObject();
            }

            String withoutFunctions = stripFunctions(objectContent);
            String withoutTypes = removeTypeAnnotations(withoutFunctions);
            String validJson = fixJsonSyntax(withoutTypes);

            return JsonParser.parseString(validJson).getAsJsonObject();
        } catch (Exception e) {
            SmogonImporter.LOGGER.error("Generic parser failed", e);
            return new JsonObject();
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Extract a string field from TypeScript object body
     */
    private static void extractStringField(String body, String fieldName, JsonObject target) {
        Pattern pattern = Pattern.compile(fieldName + ":\\s*[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            target.addProperty(fieldName, matcher.group(1));
        }
    }

    /**
     * Extract a numeric field from TypeScript object body
     */
    private static void extractNumericField(String body, String fieldName, JsonObject target) {
        Pattern pattern = Pattern.compile(fieldName + ":\\s*(-?\\d+\\.?\\d*)");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            String value = matcher.group(1);
            if (value.contains(".")) {
                target.addProperty(fieldName, Double.parseDouble(value));
            } else {
                target.addProperty(fieldName, Integer.parseInt(value));
            }
        }
    }

    /**
     * Extract a boolean field from TypeScript object body
     */
    private static void extractBooleanField(String body, String fieldName, JsonObject target) {
        Pattern pattern = Pattern.compile(fieldName + ":\\s*(true|false)");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            target.addProperty(fieldName, Boolean.parseBoolean(matcher.group(1)));
        }
    }

    /**
     * Extract an array field from TypeScript object body
     */
    private static void extractArrayField(String body, String fieldName, JsonObject target) {
        Pattern pattern = Pattern.compile(fieldName + ":\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            JsonArray array = new JsonArray();

            // Extract quoted strings from array
            Pattern stringPattern = Pattern.compile("[\"']([^\"']+)[\"']");
            Matcher stringMatcher = stringPattern.matcher(arrayContent);
            while (stringMatcher.find()) {
                array.add(stringMatcher.group(1));
            }

            target.add(fieldName, array);
        }
    }

    /**
     * Remove single-line and multi-line comments
     */
    private static String removeComments(String content) {
        // Remove multi-line comments /* ... */
        content = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        // Remove single-line comments // ...
        content = content.replaceAll("//.*?(?=\n|$)", "");

        return content;
    }

    /**
     * Extract the exported object from TypeScript
     * Handles: export const Name: Type = { ... }
     */
    private static String extractExportedObject(String content, String exportName) {
        // Pattern to match: export const ExportName ... = { ... }
        // We need to find the opening brace and match all content until the closing brace
        Pattern exportPattern = Pattern.compile(
            "export\\s+const\\s+" + exportName + "\\s*:?[^=]*=\\s*\\{",
            Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = exportPattern.matcher(content);

        if (!matcher.find()) {
            return null;
        }

        int startIndex = matcher.end() - 1; // Position of opening brace

        // Find matching closing brace
        int braceCount = 1;
        int currentIndex = startIndex + 1;

        while (currentIndex < content.length() && braceCount > 0) {
            char c = content.charAt(currentIndex);

            // Skip string literals to avoid counting braces inside strings
            if (c == '"' || c == '\'' || c == '`') {
                currentIndex = skipString(content, currentIndex, c);
            } else if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
            }

            currentIndex++;
        }

        if (braceCount != 0) {
            SmogonImporter.LOGGER.warn("Mismatched braces in TypeScript export '{}'", exportName);
            return null;
        }

        // Extract content between braces
        return content.substring(startIndex, currentIndex);
    }

    /**
     * Skip over a string literal to avoid processing its contents
     */
    private static int skipString(String content, int start, char quote) {
        int i = start + 1;

        while (i < content.length()) {
            char c = content.charAt(i);

            if (c == '\\') {
                // Skip escaped character
                i += 2;
            } else if (c == quote) {
                // Found closing quote
                return i;
            } else {
                i++;
            }
        }

        return i;
    }

    /**
     * Strip function definitions and callbacks from object properties
     * More aggressive approach - remove entire properties that contain functions
     */
    private static String stripFunctions(String content) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < content.length()) {
            char c = content.charAt(i);

            // Skip string literals
            if (c == '"' || c == '\'' || c == '`') {
                int stringEnd = skipString(content, i, c);
                result.append(content, i, stringEnd + 1);
                i = stringEnd + 1;
                continue;
            }

            // Check if this is a property with a function
            if (isFunctionProperty(content, i)) {
                // Skip entire property line until comma or closing brace
                int propertyEnd = skipToEndOfProperty(content, i);

                // Skip the comma too if present
                if (propertyEnd < content.length() && content.charAt(propertyEnd) == ',') {
                    propertyEnd++;
                }

                // Skip trailing whitespace (including newlines) after comma
                while (propertyEnd < content.length() && Character.isWhitespace(content.charAt(propertyEnd))) {
                    propertyEnd++;
                }

                // Move index forward - we've skipped the entire property
                i = propertyEnd;
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    /**
     * Check if current position starts a property that contains a function
     */
    private static boolean isFunctionProperty(String content, int pos) {
        // Skip whitespace to find the actual start
        while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) {
            pos++;
        }

        if (pos >= content.length()) return false;

        // Find next significant character (colon, paren, or brace)
        int searchEnd = Math.min(pos + 300, content.length());
        String segment = content.substring(pos, searchEnd);

        // Extract property name (word characters before : or ()
        int nameEnd = 0;
        while (nameEnd < segment.length() &&
               (Character.isLetterOrDigit(segment.charAt(nameEnd)) || segment.charAt(nameEnd) == '_')) {
            nameEnd++;
        }

        if (nameEnd == 0) return false; // No property name found

        String propertyName = segment.substring(0, nameEnd);
        String afterName = segment.substring(nameEnd).trim();

        // Pattern 1: propertyName(args) { ... }  (method shorthand)
        if (afterName.startsWith("(")) {
            return true;
        }

        // Pattern 2: propertyName: function
        if (afterName.startsWith(":")) {
            String afterColon = afterName.substring(1).trim();

            // function keyword
            if (afterColon.startsWith("function")) {
                return true;
            }

            // Arrow function: (args) =>
            if (afterColon.matches("^\\(.*?\\)\\s*=>.*")) {
                return true;
            }

            // Callback properties (common patterns in Pokemon Showdown)
            if (propertyName.startsWith("on") || propertyName.endsWith("Callback") ||
                propertyName.equals("condition") || propertyName.equals("effect")) {
                return true;
            }

            // Nested object that likely contains functions
            if (afterColon.startsWith("{")) {
                // Sample the content - if it has function-like patterns, skip it
                String sample = afterColon.substring(0, Math.min(100, afterColon.length()));
                if (sample.contains("function") || sample.contains("=>") ||
                    sample.matches(".*\\w+\\s*\\(.*")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Skip to the end of a property (until comma or closing brace at same level)
     */
    private static int skipToEndOfProperty(String content, int start) {
        int i = start;
        int braceDepth = 0;
        int parenDepth = 0;
        boolean inString = false;
        char stringChar = 0;

        while (i < content.length()) {
            char c = content.charAt(i);

            // Handle strings
            if ((c == '"' || c == '\'' || c == '`') && !inString) {
                inString = true;
                stringChar = c;
                i++;
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    i += 2; // Skip escaped character
                    continue;
                }
                if (c == stringChar) {
                    inString = false;
                }
                i++;
                continue;
            }

            // Track nesting
            if (c == '{') braceDepth++;
            if (c == '}') braceDepth--;
            if (c == '(') parenDepth++;
            if (c == ')') parenDepth--;

            // At top level, comma or closing brace ends the property
            if (braceDepth == 0 && parenDepth == 0) {
                if (c == ',' || c == '}') {
                    return i;
                }
            }

            i++;
        }

        return i;
    }

    /**
     * Remove TypeScript type annotations
     *
     * Removes patterns like:
     * - propertyName: Type = value
     * - as Type
     * - <Type>
     */
    private static String removeTypeAnnotations(String content) {
        // Remove "as Type" casts
        content = content.replaceAll("\\s+as\\s+[\\w<>\\[\\]|&]+", "");

        // Remove generic type parameters <Type>
        content = content.replaceAll("<[\\w\\s,<>\\[\\]|&]+>", "");

        return content;
    }

    /**
     * Fix JSON syntax issues
     *
     * - Remove trailing commas before closing braces/brackets
     * - Remove double commas (from stripped properties)
     * - Remove orphaned property names
     * - Ensure proper quote usage
     */
    private static String fixJsonSyntax(String content) {
        // Remove trailing commas before closing braces
        content = content.replaceAll(",\\s*}", "}");
        content = content.replaceAll(",\\s*]", "]");

        // Remove double commas (from stripped adjacent properties)
        content = content.replaceAll(",\\s*,", ",");

        // Remove orphaned property names followed by comma (leftover from function stripping)
        // Pattern: word characters followed by comma with no colon
        content = content.replaceAll("\\w+\\s*,(?!\\s*[\"'\\w])", "");

        // Remove property names at start of object with no value
        // Pattern: { word, or { word }
        content = content.replaceAll("\\{\\s*(\\w+)\\s*,", "{");
        content = content.replaceAll("\\{\\s*(\\w+)\\s*\\}", "{}");

        // Clean up whitespace around commas
        content = content.replaceAll("\\s*,\\s*", ",");

        // Re-add spacing after commas for readability
        content = content.replaceAll(",", ", ");

        // Final cleanup of any remaining issues
        content = content.replaceAll(",\\s*}", "}");
        content = content.replaceAll(",\\s*]", "]");

        return content;
    }

    /**
     * Extract a simple string field from a TypeScript object
     * Useful for extracting single values like names
     */
    public static String extractStringField(JsonObject obj, String fieldName) {
        if (obj.has(fieldName) && obj.get(fieldName).isJsonPrimitive()) {
            return obj.get(fieldName).getAsString();
        }
        return null;
    }

    /**
     * Extract an integer field from a TypeScript object
     */
    public static int extractIntField(JsonObject obj, String fieldName, int defaultValue) {
        if (obj.has(fieldName) && obj.get(fieldName).isJsonPrimitive()) {
            try {
                return obj.get(fieldName).getAsInt();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Extract a boolean field from a TypeScript object
     */
    public static boolean extractBooleanField(JsonObject obj, String fieldName, boolean defaultValue) {
        if (obj.has(fieldName) && obj.get(fieldName).isJsonPrimitive()) {
            try {
                return obj.get(fieldName).getAsBoolean();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
