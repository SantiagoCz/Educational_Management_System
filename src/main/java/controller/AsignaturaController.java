package controller;

import controller.util.FormatUtils;
import model.entities.Asignatura;
import model.entities.Docente;
import model.entities.Instituto;
import model.entities.Status;
import model.persistence.AsignaturaPersistence;

import java.util.List;

public class AsignaturaController {

    private final AsignaturaPersistence asignaturaPersistence;

    public AsignaturaController() {
        this.asignaturaPersistence = new AsignaturaPersistence();
    }

    public void save(Asignatura asignatura) {
        asignatura.setNombre(FormatUtils.formatWords(asignatura.getNombre()));
        asignaturaPersistence.save(asignatura);
    }

    public void edit(Asignatura asignatura) {
        asignatura.setNombre(FormatUtils.formatWords(asignatura.getNombre()));
        asignaturaPersistence.edit(asignatura);
    }

    public void deactivate(Asignatura asignatura) {
        asignatura.setStatus(Status.inactivo);
        asignaturaPersistence.edit(asignatura);
    }

    public Asignatura findByCodigo(String codigo) {
        return asignaturaPersistence.findByCodigo(codigo);
    }

    public List<Asignatura> findAllActives() {
        return  asignaturaPersistence.findAllActives();
    }

    public List<Asignatura> findActiveAsignaturasByDocenteAndInstituto(Docente docente, Instituto instituto) {
        return asignaturaPersistence.findActiveAsignaturasByDocenteAndInstituto(docente, instituto);
    }

    public List<Asignatura> findActiveAsignaturasByDocenteId(Long docenteId) {
        return asignaturaPersistence.findActiveAsignaturasByDocenteId(docenteId);
    }

    public List<Asignatura> findActiveAsignaturasByInstitutoId(Long institutoId) {
        return asignaturaPersistence.findActiveAsignaturasByInstitutoId(institutoId);
    }

}