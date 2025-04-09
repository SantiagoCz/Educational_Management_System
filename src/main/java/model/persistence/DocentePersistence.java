package model.persistence;

import jakarta.persistence.TypedQuery;
import model.entities.Asignatura;
import model.entities.Docente;
import model.entities.Instituto;
import model.entities.Status;

import java.io.Serializable;
import java.util.List;

public class DocentePersistence implements Serializable {

    // Crear un nuevo docente y asignarle un instituto
    public void save(Docente docente) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            // Primero, persistir el docente
            entityManager.persist(docente);

            // Luego, manejar la relación con institutos
            if (docente.getInstitutos() != null && !docente.getInstitutos().isEmpty()) {
                for (Instituto instituto : docente.getInstitutos()) {
                    // Encontrar el instituto managed
                    Instituto managedInstituto = entityManager.find(Instituto.class, instituto.getId());
                    // Verificar si ya existe la relación
                    if (!managedInstituto.getDocentes().contains(docente)) {
                        managedInstituto.getDocentes().add(docente);
                        entityManager.merge(managedInstituto);
                    }
                }
            }
            return null;
        });
    }

    // Editar un docente existente
    public void edit(Docente docente) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            // Verificar si existe antes de mergear
            if (docente.getId() == null || findById(docente.getId()) == null) {
                throw new IllegalArgumentException("Docente con id " +
                        (docente.getId() != null ? docente.getId() : "nulo") +
                        " no existe.");
            }
            entityManager.merge(docente);
            return null;
        });
    }

    // Encontrar un docente por ID
    public Docente findById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return EntityManagerUtil.findOrThrow(Docente.class, id);
        } catch (EntityManagerUtil.EntityNotFoundException e) {
            return null;
        }
    }

    // Encontrar un docente por DNI
    public Docente findByDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return null;
        }

        return EntityManagerUtil.executeInTransaction(em -> {
            List<Docente> docentes = em.createQuery(
                            "SELECT d FROM Docente d WHERE d.dni = :dni", Docente.class)
                    .setParameter("dni", dni)
                    .getResultList();

            return docentes.isEmpty() ? null : docentes.get(0);
        });
    }

    // Encontrar un docente por legajo
    public Docente findByLegajo(String legajo) {
        if (legajo == null || legajo.trim().isEmpty()) {
            return null;
        }

        return EntityManagerUtil.executeInTransaction(em -> {
            List<Docente> docentes = em.createQuery(
                            "SELECT d FROM Docente d WHERE d.legajo = :legajo", Docente.class)
                    .setParameter("legajo", legajo)
                    .getResultList();

            return docentes.isEmpty() ? null : docentes.get(0);
        });
    }

    // Listar todos los docentes ordenados por apellido
    public List<Docente> findAll() {
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Docente> query = entityManager.createQuery(
                    "SELECT d FROM Docente d ORDER BY d.apellidos, d.nombres",
                    Docente.class
            );
            return query.getResultList();
        });
    }

    // Listar todos los docentes activos ordenados por apellido
    public List<Docente> findAllActives() {
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Docente> query = entityManager.createQuery(
                    "SELECT d FROM Docente d WHERE d.status = :status ORDER BY d.apellidos, d.nombres",
                    Docente.class
            );
            query.setParameter("status", Status.activo);
            return query.getResultList();
        });
    }

    // Listar docentes activos asociados a un instituto ordenados por apellido
    public List<Docente> findActiveDocentesByInstitutoId(Long institutoId) {
        if (institutoId == null) {
            return List.of();
        }
        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Docente> query = entityManager.createQuery(
                    "SELECT d FROM Docente d JOIN d.institutos i WHERE d.status = :status AND i.id = :institutoId ORDER BY d.apellidos, d.nombres",
                    Docente.class);
            query.setParameter("status", Status.activo);
            query.setParameter("institutoId", institutoId);
            return query.getResultList();
        });
    }

    // Listar docentes activos asociados a un instituto, ordenados por apellido y que tengan un cargo docente activo en dicho instituto
    public List<Docente> findActiveDocentesByInstitutoIdWithCargo(Long institutoId) {
        if (institutoId == null) {
            return List.of();
        }

        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Docente> query = entityManager.createQuery(
                    "SELECT DISTINCT d FROM Docente d " +
                            "JOIN d.institutos i " +
                            "JOIN CargoDocente cd ON cd.docente = d AND cd.instituto = i " +
                            "WHERE d.status = :status " +
                            "AND i.id = :institutoId " +
                            "AND cd.status = :status " +
                            "ORDER BY d.apellidos, d.nombres",
                    Docente.class);

            query.setParameter("status", Status.activo);
            query.setParameter("institutoId", institutoId);

            return query.getResultList();
        });
    }

    // Listar docentes activos asociados a un instituto, ordenados por apellido y que tengan NO un cargo docente activo en dicho instituto
    public List<Docente> findActiveDocentesByInstitutoIdWithoutCargo(Long institutoId) {
        if (institutoId == null) {
            return List.of();
        }

        return EntityManagerUtil.executeInTransaction(entityManager -> {
            TypedQuery<Docente> query = entityManager.createQuery(
                    "SELECT DISTINCT d FROM Docente d " +
                            "JOIN d.institutos i " +
                            "WHERE d.status = :status " +
                            "AND i.id = :institutoId " +
                            "AND NOT EXISTS (SELECT 1 FROM CargoDocente cd " +
                            "WHERE cd.docente = d " +
                            "AND cd.instituto = i " +
                            "AND cd.status = :status) " +
                            "ORDER BY d.apellidos, d.nombres",
                    Docente.class);

            query.setParameter("status", Status.activo);
            query.setParameter("institutoId", institutoId);

            return query.getResultList();
        });
    }

    // Asignar un instituto a un docente
    public void asignInstituto(Long docenteId, Long institutoId) {
        EntityManagerUtil.executeInTransaction(entityManager -> {
            // Obtener el docente con sus institutos inicializados
            Docente docente = entityManager.createQuery(
                            "SELECT d FROM Docente d LEFT JOIN FETCH d.institutos WHERE d.id = :id",
                            Docente.class)
                    .setParameter("id", docenteId)
                    .getSingleResult();

            // Obtener el instituto con sus docentes inicializados
            Instituto instituto = entityManager.createQuery(
                            "SELECT i FROM Instituto i LEFT JOIN FETCH i.docentes WHERE i.id = :id",
                            Instituto.class)
                    .setParameter("id", institutoId)
                    .getSingleResult();

            // Verificar que ambas entidades existen
            if (docente == null || instituto == null) {
                throw new RuntimeException("Docente o Instituto no encontrado");
            }

            // Actualizar la relación bidireccional
            if (!docente.getInstitutos().contains(instituto)) {
                docente.getInstitutos().add(instituto);
                instituto.getDocentes().add(docente);
            }

            entityManager.merge(docente);
            entityManager.merge(instituto);

            return null;
        });
    }
}