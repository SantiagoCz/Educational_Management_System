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
@Table(name = "cargo_docente")
public class CargoDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cargo", unique = true, nullable = false)
    private String numeroCargo;

    @Column(name = "dedicacion_horas", nullable = false)
    private int dedicacionHoras;

    @Column(name = "status")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "instituto_id", nullable = false)
    private Instituto instituto;

    @ManyToOne
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

}