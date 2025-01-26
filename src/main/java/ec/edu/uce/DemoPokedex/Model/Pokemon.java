package ec.edu.uce.DemoPokedex.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Pokemon {

    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    private int base_experience;
    private double height;
    private double weight;

//    @ManyToMany
//    @JoinTable(
//            name = "pokemon_ability",
//            joinColumns = @JoinColumn(name = "pokemon_id"),
//            inverseJoinColumns = @JoinColumn(name = "ability_id")
//    )
//    private List<Ability> abilities;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stat> stats;

    @ManyToMany
    @JoinTable(
            name = "pokemon_type",
            joinColumns = @JoinColumn(name = "pokemon_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private List<Type> types;

    @Embedded
    private Sprites sprites;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "pokemon_evolution",
            joinColumns = @JoinColumn(name = "pokemon_id"),
            inverseJoinColumns = @JoinColumn(name = "evolution_id")
    )
    private List<Pokemon> evolutions;

    public void addEvolution(Pokemon evolution) {
        if (!this.evolutions.contains(evolution)) {
            this.evolutions.add(evolution);
        }
    }



    @Override
    public String toString() {
        return "Pokemon{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", baseExperience=" + base_experience +
                ", height=" + height +
                ", weight=" + weight +
//                ", abilitites=" + abilities +
                ", stats=" + stats +
                ", types=" + types +
                ", sprites=" + sprites +
                ", evolutions=" + evolutions +
                '}';
    }
}
