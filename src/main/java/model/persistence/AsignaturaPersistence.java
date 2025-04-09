package model.persistence;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.TypedQuery;
import model.entities.Asignatura;
import model.entities.Docente;
import model.entities.Instituto;
import model.entities.Status;

public class AsignaturaPersistence implements Serializable {

    // Método para guardar una asignatura
    public void save(Asignatura asignatura) {
            EntityManagerUtil.executeInTransaction(entityManager -> {
                entityManager.persist(asignatura);
                return null;
            });
    }

    // Editar asignatura
    public void edit(Asignatura asignatura) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            // Verificar si existe antes de mergear
            if (asignatura.getId() == null || findById(asignatura.getId()) == null) {
                throw new IllegalArgumentException("La asignatura con id " +
                        (asignatura.getId() != null ? asignatura.getId() : "nulo") +
                        " no existe.");
            }
            entityManager.merge(asignatura);
            return null;
        });
    }

    // Encontrar asignatura por ID
    public Asignatura findById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return EntityManagerUtil.findOrThrow(Asignatura.class, id);
        } catch (EntityManagerUtil.EntityNotFoundException e) {
            return null;
        }
    }

    // Encontrar asignatura por código
    public Asignatura findByCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }

        return EntityManagerUtil.executeInTransaction(entityManager -> {
            List<Asignatura> asignaturas = entityManager.createQuery(
                            "SELECT a FROM Asignatura a WHERE a.codigo = :codigo", Asignatura.class)
                    .setParameter("codigo", codigo)
                    .getResultList();

            return asignaturas.isEmpty() ? null : asignaturas.get(0);
        });
    }

    // Listar asignaturas activas ordenadas por nombre
    public List<Asignatura> findAllActives() {
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Asignatura> query = entityManager.createQuery(
                    "SELECT a FROM Asignatura a WHERE a.status = :status ORDER BY a.nombre",
                    Asignatura.class
            );
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }

    // Obtener asignaturas por docente e instituto ordenadas por nombre
    public List<Asignatura> findActiveAsignaturasByDocenteAndInstituto(Docente docente, Instituto instituto) {
        if (docente == null || instituto == null) {
            return List.of();
        }

        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Asignatura> query = entityManager.createQuery(
                    "SELECT a FROM Asignatura a " +
                            "WHERE a.docente = :docente " +
                            "AND a.instituto = :instituto " +
                            "AND a.status = :status " +
                            "ORDER BY a.nombre ASC",
                    Asignatura.class);

            query.setParameter("docente", docente);
            query.setParameter("instituto", instituto);
            query.setParameter("status", Status.activo);

            return query.getResultList();
        });
    }

    // Obtener asignaturas por docente ordenadas por nombre
    public List<Asignatura> findActiveAsignaturasByDocenteId(Long docenteId) {
        if (docenteId == null) {
            return List.of();
        }
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Asignatura> query = entityManager.createQuery(
                    "SELECT a FROM Asignatura a WHERE a.docente.id = :docenteId AND a.status = :status ORDER BY a.nombre",
                    Asignatura.class);
            query.setParameter("docenteId", docenteId);
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }

    // Obtener asignaturas por instituto ordenadas por nombre
    public List<Asignatura> findActiveAsignaturasByInstitutoId(Long institutoId) {
        if (institutoId == null) {
            return List.of();
        }
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Asignatura> query = entityManager.createQuery(
                    "SELECT a FROM Asignatura a WHERE a.instituto.id = :institutoId AND a.status = :status ORDER BY a.nombre",
                    Asignatura.class);
            query.setParameter("institutoId", institutoId);
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }

}