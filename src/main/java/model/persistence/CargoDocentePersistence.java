package model.persistence;

import jakarta.persistence.TypedQuery;
import model.entities.CargoDocente;
import model.entities.Docente;
import model.entities.Instituto;
import model.entities.Status;

import java.io.Serializable;
import java.util.List;

public class CargoDocentePersistence implements Serializable {

    // Crear un nuevo cargo docente
    public void save(CargoDocente cargoDocente) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            entityManager.persist(cargoDocente);
            return null;
        });
    }

    // Editar un cargo docente existente
    public void edit(CargoDocente cargoDocente) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            // Verificar si existe antes de mergear
            if (cargoDocente.getId() == null || findById(cargoDocente.getId()) == null) {
                throw new IllegalArgumentException("El cargo docente con id " +
                        (cargoDocente.getId() != null ? cargoDocente.getId() : "nulo") +
                        " no existe.");
            }
            entityManager.merge(cargoDocente);
            return null;
        });
    }

    // Encontrar un cargo docente por ID
    public CargoDocente findById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return EntityManagerUtil.findOrThrow(CargoDocente.class, id);
        } catch (EntityManagerUtil.EntityNotFoundException e) {
            return null;
        }
    }

    // Encontrar un cargo docente por número de cargo
    public CargoDocente findByNumeroCargo(String numeroCargo) {
        if (numeroCargo == null || numeroCargo.trim().isEmpty()) {
            return null;
        }

        return EntityManagerUtil.executeInTransaction(entityManager -> {
            List<CargoDocente> cargosDocentes = entityManager.createQuery(
                            "SELECT c FROM CargoDocente c WHERE c.numeroCargo = :numeroCargo", CargoDocente.class)
                    .setParameter("numeroCargo", numeroCargo)
                    .getResultList();

            return cargosDocentes.isEmpty() ? null : cargosDocentes.get(0);
        });
    }

    // Método para obtener los cargos docentes activos asociados a un docente e instituto específicos
    public CargoDocente findActiveCargoByDocenteAndInstituto(Docente docente, Instituto instituto) {
        if (docente == null || instituto == null) {
            return null;
        }

        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<CargoDocente> query = entityManager.createQuery(
                    "SELECT cd FROM CargoDocente cd " +
                            "WHERE cd.docente = :docente " +
                            "AND cd.instituto = :instituto " +
                            "AND cd.status = :status",
                    CargoDocente.class);

            query.setParameter("docente", docente);
            query.setParameter("instituto", instituto);
            query.setParameter("status", Status.activo);

            List<CargoDocente> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        });
    }

    // Método para obtener los cargos docentes activos asociados a un instituto específico
    public List<CargoDocente> findActiveCargosByInstitutoId(Long institutoId) {
        if (institutoId == null) {
            return List.of();
        }
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<CargoDocente> query = entityManager.createQuery(
                    "SELECT c FROM CargoDocente c WHERE c.instituto.id = :institutoId AND c.status = :status",
                    CargoDocente.class);
            query.setParameter("institutoId", institutoId);
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }

    // Método para obtener los cargos docentes activos asociados a un docente específico
    public List<CargoDocente> findActiveCargosByDocenteId(Long docenteId) {
        if (docenteId == null) {
            return List.of();
        }
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<CargoDocente> query = entityManager.createQuery(
                    "SELECT c FROM CargoDocente c WHERE c.docente.id = :docenteId AND c.status = :status",
                    CargoDocente.class);
            query.setParameter("docenteId", docenteId);
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }
}
