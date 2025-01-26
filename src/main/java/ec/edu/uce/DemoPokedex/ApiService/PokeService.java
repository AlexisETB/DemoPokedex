package ec.edu.uce.DemoPokedex.ApiService;

import ec.edu.uce.DemoPokedex.Model.*;
import ec.edu.uce.DemoPokedex.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class PokeService {
    @Autowired
    private PokeApiClient pokeApiClient;

    @Autowired
    private PokemonRepository pokemonRepository;

    @Autowired
    private TypeRepository typeRepository;

//    @Autowired
//    private AbilityRepository abilityRepository;

    // Metodo para guardar

    public void saveAllData() {
        long startTime = System.currentTimeMillis();

        System.out.println("Iniciando sincronización completa de datos...");
        CompletableFuture<Void> saveTypesFuture = saveAllTypes();
        saveTypesFuture.join();


        CompletableFuture<Void> savePokemonsFuture = saveAllPokemon();
        CompletableFuture<Void> saveEvolutionsFuture = saveAllEvolutions();

        // Esperar a que ambas tareas asíncronas finalicen
        CompletableFuture.allOf(savePokemonsFuture, saveEvolutionsFuture).join();
        long endTime = System.currentTimeMillis(); // Hora de fin
        System.out.println("Sincronización completa finalizada.");
        System.out.println("Tiempo total: " + (endTime - startTime)/1000 + " s");
    }

    @Transactional
    public CompletableFuture<Void> saveAllTypes() {
        System.out.println("Iniciando sincronización de tipos...");
        List<Type> types = pokeApiClient.getAllTypes();

        types.forEach(type -> {
            typeRepository.findByName(type.getName()).orElseGet(() -> typeRepository.save(type));
        });

        System.out.println("Sincronización de tipos completada. Total sincronizados: " + types.size());
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public CompletableFuture<Void> saveAllEvolutions() {
        return CompletableFuture.runAsync(() -> {
            List<Pokemon> pokemons = pokemonRepository.findAll();
            pokemons.parallelStream().forEach(pokemon -> {
                try {
                    // Obtener la URL de la cadena de evolución
                    String evolutionChainUrl = pokeApiClient.getEvolutionChainUrl(pokemon.getId());
                    // Obtener los IDs de los Pokémon en la cadena de evolución
                    List<Long> evolutionIds = pokeApiClient.getEvolutionIdsByUrl(evolutionChainUrl);

                    // Iterar sobre los IDs de evolución y agregarlos al Pokémon
                    for (Long evolutionId : evolutionIds) {
                        Pokemon evolution = pokemonRepository.findByIdWithEvolutions(evolutionId)
                                .orElse(null);
                        if (evolution != null) {
                            pokemon.addEvolution(evolution);
                        }
                    }

                    // Guardar el Pokémon con sus evoluciones
                    pokemonRepository.save(pokemon);
                } catch (Exception e) {
                    System.err.println("Error al procesar evoluciones para Pokémon: " + pokemon.getName());
                    e.printStackTrace();
                }
            });

            System.out.println("Sincronización de evoluciones completada.");
        });
    }

    // Metodo para guardar todos los Pokémon desde la API
    @Transactional
    public CompletableFuture<Void> saveAllPokemon() {
        System.out.println("Iniciando sincronización de Pokémon...");
        List<Pokemon> pokemons = pokeApiClient.getAllPokemon(); // Obtiene todos los Pokémon desde la API

        pokemons.parallelStream().forEach(pokemon -> {
            try {
                processAndSavePokemon(pokemon);
            } catch (Exception e) {
                System.err.println("Error al procesar el Pokémon: " + pokemon.getName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        });

        System.out.println("Sincronización de Pokémon completada. Total sincronizados: " + pokemons.size());
        return CompletableFuture.completedFuture(null);
//        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
//
//            List<CompletableFuture<Void>> pokemonFutures = pokemons.stream()
//                    .map(pokemon -> CompletableFuture.runAsync(() -> processAndSavePokemon(pokemon), executorService))
//                    .collect(Collectors.toList());
//
//            return CompletableFuture.allOf(pokemonFutures.toArray(new CompletableFuture[0]))
//                    .thenRunAsync(() -> {
//                        executorService.shutdown();
//                        System.out.println("Sincronización de Pokémon completada. Total sincronizados: " + pokemons.size());
//                    });

    }

    @Async
    public void processAndSavePokemon(Pokemon pokemon) {
        try {
            // Procesar y guardar los tipos
            List<Type> types = pokemon.getTypes().stream()
                    .map(type -> typeRepository.findByName(type.getName())
                            .orElseThrow(() -> new RuntimeException("Tipo no encontrado: " + type.getName())))
                    .collect(Collectors.toList());
            pokemon.setTypes(types);

            // Procesar y guardar las habilidades
//            List<Ability> abilities = pokemon.getAbilities().stream()
//                    .map(ability ->
//                         abilityRepository.findByName(ability.getName())
//                                .orElseGet(() -> {
//                                    Ability newAbility = new Ability();
//                                    newAbility.setName(ability.getName());
//                                    return abilityRepository.save(newAbility);
//                                }))
//                    .collect(Collectors.toList());
//            pokemon.setAbilities(abilities);

            // Guardar el Pokémon en la base de datos
            pokemonRepository.save(pokemon);

        } catch (Exception e) {
            System.err.println("Error al procesar el Pokémon: " + pokemon.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
//
//    // Metodo para guardar todas las evoluciones desde la API
//    public CompletableFuture<Void> saveAllEvolutions() {
//        System.out.println("Iniciando sincronización de evoluciones...");
//        return CompletableFuture.runAsync(() -> {
//            List<Evolution> evolutions = pokeApiClient.getAllEvolutions(); // Obtiene todas las cadenas de evolución desde la API
//            try {
//                // Guardar las evoluciones en la base de datos
//                evolutionRepository.saveAll(evolutions);
//                System.out.println("Sincronización de evoluciones completada. Total sincronizadas: " + evolutions.size());
//            } catch (Exception e) {
//                System.err.println("Error durante la sincronización de evoluciones: " + e.getMessage());
//                e.printStackTrace();
//            }
//
//        });
//    }

}