package softuni.exam.models.entity;

import javax.persistence.*;
import java.util.Set;


@Entity
@Table(name = "towns")
public class Town extends BaseEntity {

    private String name;
    private Integer population;
    private String guide;
    private Set<Passenger> passengers;

    @OneToMany(mappedBy = "town", fetch = FetchType.EAGER)
    public Set<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(Set<Passenger> passengers) {
        this.passengers = passengers;
    }

    public Town() {
    }

    @Column(unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column
    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    @Column
    public String getGuide() {
        return guide;
    }

    public void setGuide(String guide) {
        this.guide = guide;
    }
}
