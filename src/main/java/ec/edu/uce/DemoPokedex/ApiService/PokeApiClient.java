package ec.edu.uce.DemoPokedex.ApiService;

import com.fasterxml.jackson.databind.JsonNode;
import ec.edu.uce.DemoPokedex.Model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;


@Service
public class PokeApiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";

    public PokeApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Pokemon> getAllPokemon() {
        try {
            String url = BASE_URL + "pokemon?limit=1024&offset=0";
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            List<Pokemon> pokemons = new ArrayList<>();

            for (JsonNode result : root.get("results")) {
                String pokemonUrl = result.get("url").asText();
                Pokemon pokemon = getPokemonByUrl(pokemonUrl);
                pokemons.add(pokemon);
            }

            return pokemons;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all Pokémon: " + e.getMessage(), e);
        }
    }

//     Metodo para obtener un Pokémon por url
    public Pokemon getPokemonByUrl(String url) {
        try {
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);

            // Mapeo de los campos necesarios
            Pokemon pokemon = new Pokemon();
            pokemon.setId(root.get("id").asLong());
            pokemon.setName(root.get("name").asText());
            pokemon.setWeight(root.get("weight").asDouble());
            pokemon.setHeight(root.get("height").asDouble());
            pokemon.setBase_experience(root.get("base_experience").asInt());


            // Habilidades
//            List<Ability> abilities = new ArrayList<>();
//            for (JsonNode abilityNode : root.get("abilities")) {
//                Ability ability = new Ability();
//                ability.setName(abilityNode.get("ability").get("name").asText());
//                abilities.add(ability);
//            }
//            pokemon.setAbilities(abilities);

            // Tipos
            List<Type> types = new ArrayList<>();
            for (JsonNode typeNode : root.get("types")) {
                Type type = new Type();
                type.setName(typeNode.get("type").get("name").asText());
                types.add(type);
            }
            pokemon.setTypes(types);

            // Estadísticas
            List<Stat> stats = new ArrayList<>();
            for (JsonNode statNode : root.get("stats")) {
                Stat stat = new Stat();
                stat.setName(statNode.get("stat").get("name").asText());
                stat.setBaseStat(statNode.get("base_stat").asInt());
                stat.setEffort(statNode.get("effort").asInt());
                stats.add(stat);
            }
            pokemon.setStats(stats);

            // Sprites
            Sprites sprites = new Sprites();
            sprites.setFrontDefault(getSpriteValue(root, "front_default"));
            sprites.setFrontShiny(getSpriteValue(root, "front_shiny"));
            sprites.setBackDefault(getSpriteValue(root, "back_default"));
            sprites.setBackShiny(getSpriteValue(root, "back_shiny"));
            pokemon.setSprites(sprites);

            // Retorno del objeto mapeado
            return pokemon;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching Pokémon data: " + e.getMessage(), e);
        }
    }

    public List<Type> getAllTypes() {
        try {
            String url = BASE_URL + "type/";
            String response = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(response);
            List<Type> types = new ArrayList<>();

            for (JsonNode result : root.get("results")) {
                Type type = new Type();
                type.setName(result.get("name").asText());
                types.add(type);
            }

            return types;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching all types: " + e.getMessage(), e);
        }
    }

//    public List<Evolution> getAllEvolutions() {
//        try {
//            String url = BASE_URL + "evolution-chain?limit=500&offset=0";
//            String response = restTemplate.getForObject(url, String.class);
//
//            JsonNode root = objectMapper.readTree(response);
//            List<Evolution> evolutions = new ArrayList<>();
//
//            for (JsonNode result : root.get("results")) {
//                String evolutionUrl = result.get("url").asText();
//                List<Evolution> chainEvolutions = getEvolutionChainByUrl(evolutionUrl);
//                evolutions.addAll(chainEvolutions);
//            }
//
//            return evolutions;
//        } catch (Exception e) {
//            throw new RuntimeException("Error fetching all evolutions: " + e.getMessage(), e);
//        }
//    }
//
//    // Metodo para obtener la cadena de evolución por url
//    public List<Evolution> getEvolutionChainByUrl(String url) {
//        try {
//            String response = restTemplate.getForObject(url, String.class);
//
//            // Parseo de la respuesta JSON
//            JsonNode root = objectMapper.readTree(response);
//            JsonNode chain = root.get("chain");
//
//            // Procesar la cadena de evolución recursivamente
//            List<Evolution> evolutions = new ArrayList<>();
//            processEvolutionChain(chain, evolutions);
//
//            return evolutions;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error fetching Evolution Chain data: " + e.getMessage(), e);
//        }
//    }
//
//    private void processEvolutionChain(JsonNode chainNode, List<Evolution> evolutions) {
//        if (chainNode == null) return;
//
//        Evolution evolution = new Evolution();
//        evolution.setSpeciesName(chainNode.get("species").get("name").asText());
//
//        if (chainNode.has("evolution_details") && chainNode.get("evolution_details").size() > 0) {
//            JsonNode details = chainNode.get("evolution_details").get(0);
//        }
//
//        evolutions.add(evolution);
//
//        // Procesar las evoluciones subsecuentes
//        for (JsonNode next : chainNode.get("evolves_to")) {
//            processEvolutionChain(next, evolutions);
//        }
//    }
public String getEvolutionChainUrl(Long pokemonId) {
    try {
        String speciesUrl = BASE_URL + "pokemon-species/" + pokemonId + "/";
        String response = restTemplate.getForObject(speciesUrl, String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.get("evolution_chain").get("url").asText();
    } catch (Exception e) {
        throw new RuntimeException("Error fetching evolution chain URL for Pokémon ID: " + pokemonId, e);
    }
}

    public List<Long> getEvolutionIdsByUrl(String url) {
    try {
        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode chain = root.get("chain");

        List<Long> evolutionIds = new ArrayList<>();
        extractEvolutionIds(chain, evolutionIds);

        return evolutionIds;
    } catch (Exception e) {
        throw new RuntimeException("Error fetching evolution IDs: " + e.getMessage(), e);
    }
}

    private void extractEvolutionIds(JsonNode chainNode, List<Long> evolutionIds) {
        if (chainNode == null) return;

        String speciesUrl = chainNode.get("species").get("url").asText();
        Long speciesId = extractIdFromUrl(speciesUrl);
        if (speciesId != null) {
            evolutionIds.add(speciesId);
        }

        for (JsonNode next : chainNode.get("evolves_to")) {
            extractEvolutionIds(next, evolutionIds);
        }
    }

    private Long extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        try {
            return Long.parseLong(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getSpriteValue(JsonNode root, String spriteKey) {
        return root.get("sprites").has(spriteKey) && !root.get("sprites").get(spriteKey).isNull()
                ? root.get("sprites").get(spriteKey).asText()
                : null;
    }


}
