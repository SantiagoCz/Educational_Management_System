package controller;

import controller.util.FormatUtils;
import model.entities.CargoDocente;
import model.entities.Docente;
import model.entities.Instituto;
import model.entities.Status;
import model.persistence.DocentePersistence;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DocenteController {

    private final DocentePersistence docentePersistence;

    public DocenteController() {
        this.docentePersistence = new DocentePersistence();
    }

    public void save(Docente docente) {
        docente.setNombres(FormatUtils.formatWords(docente.getNombres()));
        docente.setApellidos(FormatUtils.formatWords(docente.getApellidos()));
        docente.setDireccion(FormatUtils.formatWords(docente.getDireccion()));
        docentePersistence.save(docente);
    }

    public void edit(Docente docente) {
        docente.setNombres(FormatUtils.formatWords(docente.getNombres()));
        docente.setApellidos(FormatUtils.formatWords(docente.getApellidos()));
        docente.setDireccion(FormatUtils.formatWords(docente.getDireccion()));
        docentePersistence.edit(docente);
    }

    public void activate(Docente docente) {
        docente.setStatus(Status.activo);
        docentePersistence.edit(docente);
    }

    public void deactivate(Docente docente) {
        //Cambiar de estado los cargos docentes asociados
        CargoDocenteController cargoDocenteController = new CargoDocenteController();
        List<CargoDocente> cargos = cargoDocenteController.findActiveCargosByDocenteId(docente.getId());
        for (CargoDocente cargoDocente : cargos) {
            cargoDocenteController.deactivate(cargoDocente);
        }
        //Desvincular institutos asociados
        InstitutoController institutoController = new InstitutoController();
        List<Instituto> institutos = institutoController.findActiveInstitutosByDocenteId(docente.getId());
        for (Instituto instituto : institutos) {
            institutoController.unlinkInstitutoFromDocente(instituto, docente);
        }
        docente.setStatus(Status.inactivo);
        docentePersistence.edit(docente);
    }

    public Docente findByDni(String dni) {
        return docentePersistence.findByDni(dni);
    }

    public Docente findByLegajo(String legajo) {
        return docentePersistence.findByLegajo(legajo);
    }

    public List<Docente> findAll() {
        return docentePersistence.findAll();
    }

    public List<Docente> findAllActives() {
        return docentePersistence.findAllActives();
    }

    public List<Docente> findActiveDocentesByInstitutoId(Long institutoId) { return docentePersistence.findActiveDocentesByInstitutoId(institutoId); }

    public List<Docente> findActiveDocentesByInstitutoIdWithCargo(Long institutoId) { return docentePersistence.findActiveDocentesByInstitutoIdWithCargo(institutoId); }

    public List<Docente> findActiveDocentesByInstitutoIdWithoutCargo(Long institutoId) {
        return docentePersistence.findActiveDocentesByInstitutoIdWithoutCargo(institutoId);
    }

    public void asignInstituto(Long docenteId, Long institutoId) {
        docentePersistence.asignInstituto(docenteId, institutoId);
    }

    public int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        Calendar current = Calendar.getInstance();

        int age = current.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (current.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

}

