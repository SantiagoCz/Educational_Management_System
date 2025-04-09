package model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "instituto")
public class Instituto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String denominacion;

    @Column(name = "status")
    private Status status;

    @OneToMany(mappedBy = "instituto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Asignatura> asignaturas;

    @OneToMany(mappedBy = "instituto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CargoDocente> cargosDocentes;

    @ManyToMany
    @JoinTable(
            name = "instituto_docente",
            joinColumns = @JoinColumn(name = "instituto_id"),
            inverseJoinColumns = @JoinColumn(name = "docente_id")
    )
    private List<Docente> docentes;

}