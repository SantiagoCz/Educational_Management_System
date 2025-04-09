package controller.util;

import controller.AsignaturaController;
import controller.CargoDocenteController;
import controller.DocenteController;
import controller.InstitutoController;
import model.entities.Asignatura;
import model.entities.CargoDocente;
import model.entities.Docente;
import model.entities.Instituto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Validation {

    private final AsignaturaController asignaturaController;
    private final CargoDocenteController cargoDocenteController;
    private final DocenteController docenteController;
    private final InstitutoController institutoController;

    public Validation() {
        this.asignaturaController = new AsignaturaController();
        this.cargoDocenteController = new CargoDocenteController();
        this.docenteController = new DocenteController();
        this.institutoController = new InstitutoController();
    }

    //TEXTO------------------------------------------------------------------------------------
    // Validación para campos de solo texto
    public boolean isValidTextOnly(String text) {
        return text != null && text.trim().length() >= 2 && text.matches("[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+");
    }

    // Validación para campos de texto y numeros
    public boolean isValidTextAndNumbers(String text) {
        return text != null && text.trim().length() >= 2 && text.matches("[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s0-9]+");
    }

    //ASIGNATURA--------------------------------------------------------------------------------
    // Validación para código de asignatura
    public boolean isValidCodigoAsignatura(String codigo) {
        return codigo != null && codigo.matches("[A-Z]{3}\\d{3}");
    }

    // Verificar código duplicado
    public boolean isCodigoAsignaturaDuplicated(String codigo, Long currentId) {
        Asignatura existingAsignatura = asignaturaController.findByCodigo(codigo);
        return existingAsignatura != null && !existingAsignatura.getId().equals(currentId);
    }

    //CARGO DOCENTE-----------------------------------------------------------------------------
    //Validación de numero de cargo
    public boolean isValidNumeroCargo(String numero) {
        return numero != null && numero.matches("\\d{4}");
    }

    // Verificar numero de cargo duplicado
    public boolean isNumeroCargoDuplicated(String numeroCargo, Long currentId) {
        CargoDocente existingCargoDocente = cargoDocenteController.findByNumeroCargo(numeroCargo);
        return existingCargoDocente != null && !existingCargoDocente.getId().equals(currentId);
    }

        //Validación de horas semanales
    public boolean isValidHoras(String horas) {
        try {
            int horasInt = Integer.parseInt(horas);
            return horasInt > 0 && horasInt <= 40;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //DOCENTE-----------------------------------------------------------------------------------
    // Validación para DNI
    public boolean isValidDNI(String dni) {
        return dni.matches("\\d{8}");
    }

    // Verificar DNI duplicado
    public boolean isDNIDuplicated(String dni, Long currentId) {
        Docente existingDocente = docenteController.findByDni(dni);
        return existingDocente != null && !existingDocente.getId().equals(currentId);
    }

    // Validación para edad del docente (mayor de 18)
    public boolean isValidAge(LocalDate fechaNacimiento) {
        // Convertir LocalDate a Date
        Date fechaNacimientoDate = Date.from(fechaNacimiento.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int edad = docenteController.calculateAge(fechaNacimientoDate);
        return edad >= 18;
    }

    // Validación para legajo
    public boolean isValidLegajo(String legajo) {
        return legajo.matches("[A-Z]\\d{4}");
    }

    // Verificar legajo duplicado
    public boolean isLegajoDuplicated(String legajo, Long currentId) {
        Docente existingDocente = docenteController.findByLegajo(legajo);
        return existingDocente != null && !existingDocente.getId().equals(currentId);
    }

    //INSTITUTO--------------------------------------------------------------------------------
    public boolean isValidCodigoInstituto(String codigo) {
        return codigo != null && codigo.matches("[A-Z]{2}\\d{3}");
    }

    // Verificar código duplicado
    public boolean isCodigoInstitutoDuplicated(String codigo, Long currentId) {
        Instituto existingInstituto = institutoController.findByCodigo(codigo);
        return existingInstituto != null && !existingInstituto.getId().equals(currentId);
    }
}
