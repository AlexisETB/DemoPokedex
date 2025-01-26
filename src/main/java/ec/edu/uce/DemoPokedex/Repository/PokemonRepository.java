package ec.edu.uce.DemoPokedex.Repository;

import ec.edu.uce.DemoPokedex.Model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PokemonRepository extends JpaRepository<Pokemon, Long> {

    // Método para buscar Pokémon por nombre (case insensitive)
    List<Pokemon> findByNameIgnoreCase(String name);

    // Método para buscar Pokémon por tipo
    @Query("SELECT p FROM Pokemon p JOIN p.types t WHERE t.name = :typeName")
    List<Pokemon> findByType(@Param("typeName") String typeName);

    // Método para buscar Pokémon por habilidad
//    @Query("SELECT p FROM Pokemon p JOIN p.abilities a WHERE a.name = :abilityName")
//    List<Pokemon> findByAbility(@Param("abilityName") String abilityName);

    @Query("SELECT p FROM Pokemon p LEFT JOIN FETCH p.evolutions WHERE p.id = :id")
    Optional<Pokemon> findByIdWithEvolutions(@Param("id") Long id);


}
