package model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asignatura")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "status")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "instituto_id", nullable = false)
    private Instituto instituto;

    @ManyToOne
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

}