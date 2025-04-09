package model.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityManagerUtil {

    private static final Logger LOGGER = Logger.getLogger(EntityManagerUtil.class.getName());
    private static EntityManagerFactory entityManagerFactory;

    private static synchronized void initializeEntityManagerFactory() {
        if (entityManagerFactory == null) {
            try {
                // Crear mapa de propiedades
                Map<String, Object> properties = new HashMap<>();

                // Configuraciones de conexión
                properties.put("hibernate.connection.provider_class",
                        "com.zaxxer.hikari.hibernate.HikariConnectionProvider");

                // Crear EntityManagerFactory con configuraciones
                entityManagerFactory = Persistence.createEntityManagerFactory("facultad", properties);

                LOGGER.info("EntityManagerFactory inicializada correctamente");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al inicializar EntityManagerFactory", e);
                throw new ExceptionInInitializerError("No se pudo inicializar EntityManagerFactory: " + e.getMessage());
            }
        }
    }

    // Bloque estático para inicialización
    static {
        initializeEntityManagerFactory();
    }

    // Obtener EntityManager
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            initializeEntityManagerFactory();
        }
        return entityManagerFactory.createEntityManager();
    }

    // Método para ejecutar operaciones en transacción
    public static <T> T executeInTransaction(EntityManagerOperation<T> operation) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            entityManager.getTransaction().begin();

            T result = operation.execute(entityManager);

            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (entityManager != null && entityManager.getTransaction().isActive()) {
                try {
                    entityManager.getTransaction().rollback();
                } catch (Exception rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error al hacer rollback", rollbackEx);
                }
            }
            LOGGER.log(Level.SEVERE, "Error en la transacción", e);
            throw new RuntimeException("Error en la transacción: " + e.getMessage(), e);
        } finally {
            if (entityManager != null) {
                try {
                    entityManager.close();
                } catch (Exception closeEx) {
                    LOGGER.log(Level.WARNING, "Error al cerrar EntityManager", closeEx);
                }
            }
        }
    }

    // Interfaz funcional para operaciones con EntityManager que devuelven algo
    @FunctionalInterface
    public interface EntityManagerOperation<T> {
        T execute(EntityManager entityManager);
    }

    // Método de utilidad para manejar casos de no encontrado
    public static <T> T findOrThrow(Class<T> entityClass, Object id) {
        return executeInTransaction(entityManager -> {
            T entity = entityManager.find(entityClass, id);
            if (entity == null) {
                throw new EntityNotFoundException("Entidad no encontrada: " + entityClass.getSimpleName() + " con id " + id);
            }
            return entity;
        });
    }

    // Clase de excepción personalizada
    public static class EntityNotFoundException extends RuntimeException {
        public EntityNotFoundException(String message) {
            super(message);
        }
    }
}