package model.persistence;

import jakarta.persistence.TypedQuery;
import model.entities.Docente;
import model.entities.Instituto;
import model.entities.Status;

import java.io.Serializable;
import java.util.List;

public class InstitutoPersistence implements Serializable {

    // Crear un nuevo instituto
    public void save(Instituto instituto) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            entityManager.persist(instituto);
            return null;
        });
    }

    // Editar un instituto existente
    public void edit(Instituto instituto) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            // Verificar si existe antes de mergear
            if (instituto.getId() == null || findById(instituto.getId()) == null) {
                throw new IllegalArgumentException("El instituto con id " +
                        (instituto.getId() != null ? instituto.getId() : "nulo") +
                        " no existe.");
            }
            entityManager.merge(instituto);
            return null;
        });
    }

    // Encontrar un instituto por ID
    public Instituto findById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return EntityManagerUtil.findOrThrow(Instituto.class, id);
        } catch (EntityManagerUtil.EntityNotFoundException e) {
            return null;
        }
    }

    // Encontrar un instituto por código
    public Instituto findByCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }

        return EntityManagerUtil.executeInTransaction(em -> {
            List<Instituto> institutos = em.createQuery(
                            "SELECT i FROM Instituto i WHERE i.codigo = :codigo", Instituto.class)
                    .setParameter("codigo", codigo)
                    .getResultList();

            return institutos.isEmpty() ? null : institutos.get(0);
        });
    }

    // Listar todos los institutos ordenados por denominación
    public List<Instituto> findAll() {
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Instituto> query = entityManager.createQuery(
                    "SELECT i FROM Instituto i ORDER BY i.denominacion",
                    Instituto.class
            );
            return query.getResultList();
        });
    }

    // Listar todos los institutos activos ordenados por denominación
    public List<Instituto> findAllActives() {
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Instituto> query = entityManager.createQuery(
                    "SELECT i FROM Instituto i WHERE i.status = :status ORDER BY i.denominacion",
                    Instituto.class
            );
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }

    // Listar Institutos activos asignados a un docente y ordenanos por denominación
    public List<Instituto> findActiveInstitutosByDocenteId(Long docenteId) {
        if (docenteId == null) {
            return List.of();
        }
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Instituto> query = entityManager.createQuery(
                    "SELECT DISTINCT i FROM Instituto i " +
                            "JOIN i.docentes d " +
                            "WHERE d.id = :docenteId " +
                            "AND i.status = :status " +
                            "ORDER BY i.denominacion", Instituto.class);
            query.setParameter("docenteId", docenteId);
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }

    // Listar institutos activos asociados a un docente, ordenados por denominacion y que tengan un cargo docente activo asociado al docente
    public List<Instituto> findActiveInstitutosByDocenteIdWithCargo(Long docenteId) {
        if (docenteId == null) {
            return List.of();
        }

        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Instituto> query = entityManager.createQuery(
                    "SELECT DISTINCT i FROM Instituto i " +
                            "JOIN i.docentes d " +
                            "JOIN CargoDocente cd ON cd.instituto = i AND cd.docente = d " +
                            "WHERE i.status = :status " +
                            "AND d.id = :docenteId " +
                            "AND cd.status = :status " +
                            "ORDER BY i.denominacion",
                    Instituto.class);

            query.setParameter("status", Status.activo);
            query.setParameter("docenteId", docenteId);

            return query.getResultList();
        });
    }

    // Listar Institutos activos no asignados a un docente
    public List<Instituto> findActiveUnassignedInstitutos(Long docenteId) {
        if (docenteId == null) {
            return List.of();
        }
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Instituto> query = entityManager.createQuery(
                    "SELECT i FROM Instituto i WHERE i.status = :status " +
                            "AND i NOT IN (SELECT inst FROM Docente d JOIN d.institutos inst WHERE d.id = :docenteId)",
                    Instituto.class);
            query.setParameter("status", Status.activo);
            query.setParameter("docenteId", docenteId);
            return query.getResultList();
        });
    }

    //Desvincular objetos asociados: Instituto-Docente
    public void unlinkInstitutoFromDocente(Instituto instituto, Docente docente) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            // Obtenemos las entidades administradas por el EntityManager
            Instituto managedInstituto = entityManager.merge(instituto);
            Docente managedDocente = entityManager.merge(docente);

            // Removemos la relación bidireccional
            managedInstituto.getDocentes().remove(managedDocente);
            managedDocente.getInstitutos().remove(managedInstituto);

            // Guardamos los cambios
            entityManager.merge(managedInstituto);
            entityManager.merge(managedDocente);

            return null;
        });
    }

}