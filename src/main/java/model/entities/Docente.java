package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "docente")
@PrimaryKeyJoinColumn(name = "id")
public class Docente extends Persona {

    @Column(name = "status")
    private Status status;

    @OneToMany(mappedBy = "docente")
    private List<Asignatura> asignaturas;

    @OneToMany(mappedBy = "docente")
    private List<CargoDocente> cargosDocentes;

    @ManyToMany(mappedBy = "docentes")
    private List<Instituto> institutos;

}
