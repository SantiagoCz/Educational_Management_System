package controller;

import model.entities.CargoDocente;
import model.entities.Docente;
import model.entities.Instituto;
import model.entities.Status;
import model.persistence.CargoDocentePersistence;

import java.util.List;

public class CargoDocenteController {

    private final CargoDocentePersistence cargoDocentePersistence;

    public CargoDocenteController() {
        this.cargoDocentePersistence = new CargoDocentePersistence();
    }

    public void save(CargoDocente cargoDocente) {
        cargoDocentePersistence.save(cargoDocente);
    }

    public void edit(CargoDocente cargoDocente) {
        cargoDocentePersistence.edit(cargoDocente);
    }

    public void deactivate(CargoDocente cargoDocente) {
        cargoDocente.setStatus(Status.inactivo);
        edit(cargoDocente);
    }

    public CargoDocente findByNumeroCargo(String numeroCargo) {
        return cargoDocentePersistence.findByNumeroCargo(numeroCargo);
    }

    public CargoDocente findActiveCargoByDocenteAndInstituto(Docente docente, Instituto instituto) {
        return cargoDocentePersistence.findActiveCargoByDocenteAndInstituto(docente, instituto);
    }

    public List<CargoDocente> findActiveCargosByInstitutoId(Long institutoId) {
        return cargoDocentePersistence.findActiveCargosByInstitutoId(institutoId);
    }

    public List<CargoDocente> findActiveCargosByDocenteId(Long docenteId) {
        return cargoDocentePersistence.findActiveCargosByDocenteId(docenteId);
    }
}