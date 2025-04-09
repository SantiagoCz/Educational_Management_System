package controller;

import controller.util.FormatUtils;
import jakarta.transaction.Transactional;
import model.entities.*;
import model.persistence.InstitutoPersistence;

import java.util.List;

public class InstitutoController {

    private final InstitutoPersistence institutoPersistence;

    public InstitutoController() {
        this.institutoPersistence = new InstitutoPersistence();
    }

    public void save(Instituto instituto) {
        instituto.setDenominacion(FormatUtils.formatWords(instituto.getDenominacion()));
        institutoPersistence.save(instituto);
    }

    public void edit(Instituto instituto) {
        instituto.setDenominacion(FormatUtils.formatWords(instituto.getDenominacion()));
        institutoPersistence.edit(instituto);
    }

    public void activate(Instituto instituto) {
        instituto.setStatus(Status.activo);
        edit(instituto);
    }

    @Transactional
    public void deactivate(Instituto instituto) {
        //Cambiar de estado las asignaturas asociadas
        AsignaturaController asignaturaController = new AsignaturaController();
        List<Asignatura> asignaturas = asignaturaController.findActiveAsignaturasByInstitutoId(instituto.getId());
        for (Asignatura asignatura : asignaturas ) {
            asignaturaController.deactivate(asignatura);
        }
        //Cambiar de estado los cargos asociados
        CargoDocenteController cargoDocenteController = new CargoDocenteController();
        List<CargoDocente> cargos = cargoDocenteController.findActiveCargosByInstitutoId(instituto.getId());
        for (CargoDocente cargo : cargos ) {
            cargoDocenteController.deactivate(cargo);
        }
        //Desvincular los docentes asociados
        DocenteController docenteController = new DocenteController();
        List<Docente> docentes = docenteController.findActiveDocentesByInstitutoId(instituto.getId());
        for (Docente docente : docentes) {
            unlinkInstitutoFromDocente(instituto, docente);
        }
        instituto.setStatus(Status.inactivo);
        edit(instituto);
    }

    public Instituto findByCodigo(String codigo) {
        return institutoPersistence.findByCodigo(codigo);
    }

    public List<Instituto> findAll() {
        return institutoPersistence.findAll();
    }

    public List<Instituto> findAllActives() {
        return institutoPersistence.findAllActives();
    }

    public List<Instituto> findActiveInstitutosByDocenteId(Long docenteId) {
        return institutoPersistence.findActiveInstitutosByDocenteId(docenteId);
    }

    public List<Instituto> findActiveInstitutosByDocenteIdWithCargo(Long docenteId) {
        return institutoPersistence.findActiveInstitutosByDocenteIdWithCargo(docenteId);
    }

    public List<Instituto> findActiveUnassignedInstitutos(Long docenteId) {
        return institutoPersistence.findActiveUnassignedInstitutos(docenteId);
    }

    public boolean hasAssociatedData(Instituto instituto) {
        if (instituto == null || instituto.getId() == null) {
            return false;
        }
        AsignaturaController asignaturaController = new AsignaturaController();
        DocenteController docenteController = new DocenteController();
        CargoDocenteController cargoDocenteController = new CargoDocenteController();

        return asignaturaController.findActiveAsignaturasByInstitutoId(instituto.getId()).size() > 0 ||
                docenteController.findActiveDocentesByInstitutoId(instituto.getId()).size() > 0 ||
                cargoDocenteController.findActiveCargosByInstitutoId(instituto.getId()).size() > 0;
    }

    public int countAsignaturas(Instituto instituto) {
        if (instituto == null || instituto.getId() == null) {
            return 0;
        }
        AsignaturaController asignaturaController = new AsignaturaController();
        return asignaturaController.findActiveAsignaturasByInstitutoId(instituto.getId()).size();
    }

    public int countCargosDocentes(Instituto instituto) {
        if (instituto == null || instituto.getId() == null) {
            return 0;
        }
        CargoDocenteController cargoDocenteController = new CargoDocenteController();
        return cargoDocenteController.findActiveCargosByInstitutoId(instituto.getId()).size();
    }

    public int countDocentes(Instituto instituto) {
        if (instituto == null || instituto.getId() == null) {
            return 0;
        }
        DocenteController docenteController = new DocenteController();
        return docenteController.findActiveDocentesByInstitutoId(instituto.getId()).size();
    }

    public void unlinkInstitutoFromDocente(Instituto instituto, Docente docente) {
        institutoPersistence.unlinkInstitutoFromDocente(instituto, docente);
    }

}