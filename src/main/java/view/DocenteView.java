package view;

import javafx.collections.transformation.SortedList;
import controller.*;
import controller.util.Validation;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.entities.*;

import java.time.LocalDate;
import java.util.*;

public class DocenteView extends Application {

    private final DocenteController docenteController;
    private final Validation validation;

    private static TableView<Docente> activeTableView;
    private static Instituto currentInstituto; // Para mantener referencia del instituto actual
    private static Label cantidadDocentes;
    private ObservableList<Docente> docentes;
    private FilteredList<Docente> filteredDocentes;

    public DocenteView() {
        this.docenteController = new DocenteController();
        this.validation = new Validation();
        this.docentes = FXCollections.observableArrayList();
    }

    //Abrir el formulario de registro/edición---------------------------------------------------------------------------
    private void openForm(Docente docente, Stage owner, String title) {
        Stage formStage = ViewManager.getInstance().createFormStage(owner, title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader(title);
        GridPane form = ViewManager.getInstance().createFormGrid();

        // Labels del formulario
        Label lblNombre = ViewManager.getInstance().createLabel("Nombre:");
        Label lblApellido = ViewManager.getInstance().createLabel("Apellido:");
        Label lblDni = ViewManager.getInstance().createLabel("DNI:");
        Label lblFechaNac = ViewManager.getInstance().createLabel("Fecha de Nacimiento:");
        Label lblLegajo = ViewManager.getInstance().createLabel("Legajo:");
        Label lblDireccion = ViewManager.getInstance().createLabel("Dirección:");

        // Campos del formulario
        TextField txtNombre = ViewManager.getInstance().createTextField(docente != null ? docente.getNombres() : "", "Ingrese el nombre");
        TextField txtApellido = ViewManager.getInstance().createTextField(docente != null ? docente.getApellidos() : "", "Ingrese el nombre");
        TextField txtDni = ViewManager.getInstance().createTextField(docente != null ? docente.getDni() : "", "Ingrese el DNI (8 dígitos)");
        DatePicker datePicker = ViewManager.getInstance().createDatePicker(docente, "Seleccione la fecha");
        TextField txtLegajo = ViewManager.getInstance().createTextField(docente != null ? docente.getLegajo() : "", "Formato: A1234");
        TextField txtDireccion = ViewManager.getInstance().createTextField(docente != null ? docente.getDireccion() : "", "Ingrese la dirección");

        // Para ComboBox de Institutos (solo para nuevos docentes)
        Label lblInstituto = null;
        ComboBox<Instituto> cmbInstituto = null;
        if (docente == null) {
            lblInstituto = ViewManager.getInstance().createLabel("Instituto:");
            cmbInstituto = ViewManager.getInstance().createInstitutoComboBox();
            InstitutoController institutoController = new InstitutoController();
            List<Instituto> institutos = institutoController.findAllActives();
            configureInstitutoComboBox(cmbInstituto, institutos);
        }

        // Validaciones en tiempo real
        setupRealTimeValidations(docente, txtNombre, txtApellido, txtDni, txtLegajo);

        // Agregar campos al formulario
        addFieldsToForm(form, lblNombre, txtNombre, lblApellido, txtApellido, lblDni, txtDni,
                lblFechaNac, datePicker, lblLegajo, txtLegajo, lblDireccion, txtDireccion,
                lblInstituto, cmbInstituto, docente);

        // Botones
        HBox buttonBox = createButtonBox(formStage, docente, txtNombre, txtApellido, txtDni,
                datePicker, txtLegajo, txtDireccion, cmbInstituto);

        // Configurar y mostrar la ventana
        configureAndShowStage(formStage, mainContainer, header, form, buttonBox);
    }
    //------------------------------------------------------------------------------------------------------------------

    //Asignar instituto a un docente------------------------------------------------------------------------------------
    private void openAsignarInstitutoForm(Docente docente, Stage owner, String title) {
        Stage formStage = ViewManager.getInstance().createFormStage(owner, title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader(title);
        GridPane form = ViewManager.getInstance().createFormGrid();

        // Labels del formulario
        Label lblNombreCompleto = ViewManager.getInstance().createLabel("Nombre completo:");
        Label lblDni = ViewManager.getInstance().createLabel("DNI:");
        Label lblLegajo = ViewManager.getInstance().createLabel("Legajo:");
        Label lblInstituto = ViewManager.getInstance().createLabel("Instituto:");


        // Campos del formulario (datos del docente - solo lectura)
        TextField txtNombreCompleto = ViewManager.getInstance().createTextField(docente.getApellidos() + ", " + docente.getNombres(),"");
        txtNombreCompleto.setEditable(false);
        txtNombreCompleto.setStyle(ViewManager.getInstance().readOnlyStyle);
        TextField txtDni = ViewManager.getInstance().createTextField(docente.getDni(), "");
        txtDni.setEditable(false);
        txtDni.setStyle(ViewManager.getInstance().readOnlyStyle);
        TextField txtLegajo = ViewManager.getInstance().createTextField(docente.getLegajo(), "");
        txtLegajo.setEditable(false);
        txtLegajo.setStyle(ViewManager.getInstance().readOnlyStyle);

        // ComboBox para seleccionar instituto
        ComboBox<Instituto> cmbInstituto = ViewManager.getInstance().createInstitutoComboBox();

        // Obtener institutos activos que NO estén asignados al docente
        InstitutoController institutoController = new InstitutoController();
        List<Instituto> institutosDisponibles = institutoController.findActiveUnassignedInstitutos(docente.getId());
        configureInstitutoComboBox(cmbInstituto, institutosDisponibles);

        // Agregar campos al formulario
        form.addRow(0, lblLegajo, txtLegajo);
        form.addRow(1, lblNombreCompleto, txtNombreCompleto);
        form.addRow(2, lblDni, txtDni);
        form.addRow(3, lblInstituto, cmbInstituto);

        // Crear y agregar botones
        HBox buttonBox = createButtonBox(formStage, docente, cmbInstituto);

        // Configurar y mostrar la ventana
        configureAndShowStage(formStage, mainContainer, header, form, buttonBox);
    }
    //------------------------------------------------------------------------------------------------------------------

    private void configureInstitutoComboBox(ComboBox<Instituto> cmbInstituto, List<Instituto> institutos) {
        if (institutos.isEmpty()) {
            Instituto noInstitutosItem = new Instituto();
            noInstitutosItem.setCodigo("N/A");
            noInstitutosItem.setDenominacion("No se encontraron Institutos");
            cmbInstituto.setItems(FXCollections.observableArrayList(noInstitutosItem));
        } else {
            cmbInstituto.setItems(FXCollections.observableArrayList(institutos));
        }

        cmbInstituto.setCellFactory(lv -> new ListCell<Instituto>() {
            @Override
            protected void updateItem(Instituto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if ("N/A".equals(item.getCodigo())) {
                        setText(item.getDenominacion());
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    } else {
                        setText(item.getCodigo() + " - " + item.getDenominacion());
                    }
                }
            }
        });
        cmbInstituto.setButtonCell(cmbInstituto.getCellFactory().call(null));
    }
    //------------------------------------------------------------------------------------------------------------------

    // Validaciones en tiempo real--------------------------------------------------------------------------------------
    private void setupRealTimeValidations(Docente docente, TextField txtNombre, TextField txtApellido,
                                          TextField txtDni, TextField txtLegajo) {
        // Validación del nombre
        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !validation.isValidTextOnly(newValue)) {
                ViewManager.getInstance().setErrorStyle(txtNombre);
            } else {
                ViewManager.getInstance().resetStyle(txtNombre);
            }
        });

        // Validación del apellido
        txtApellido.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !validation.isValidTextOnly(newValue)) {
                ViewManager.getInstance().setErrorStyle(txtApellido);
            } else {
                ViewManager.getInstance().resetStyle(txtApellido);
            }
        });

        // Validación del DNI
        txtDni.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!validation.isValidDNI(newValue)) {
                    ViewManager.getInstance().setErrorStyle(txtDni);
                } else if (validation.isDNIDuplicated(newValue, docente != null ? docente.getId() : null)) {
                    ViewManager.getInstance().setErrorStyle(txtDni);
                } else {
                    ViewManager.getInstance().resetStyle(txtDni);
                }
            } else {
                ViewManager.getInstance().resetStyle(txtDni);
                txtDni.setTooltip(null);
            }
        });

        // Validación del legajo
        txtLegajo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!validation.isValidLegajo(newValue)) {
                    ViewManager.getInstance().setErrorStyle(txtLegajo);
                } else if (validation.isLegajoDuplicated(newValue, docente != null ? docente.getId() : null)) {
                    ViewManager.getInstance().setErrorStyle(txtLegajo);
                } else {
                    ViewManager.getInstance().resetStyle(txtLegajo);
                }
            } else {
                ViewManager.getInstance().resetStyle(txtLegajo);
                txtLegajo.setTooltip(null);
            }
        });
    }
    //------------------------------------------------------------------------------------------------------------------

    // Configuración del formulario-------------------------------------------------------------------------------------
    private void addFieldsToForm(GridPane form, Label lblNombre, TextField txtNombre, Label lblApellido, TextField txtApellido, Label lblDni, TextField txtDni,
                                 Label lblFechaNac, DatePicker datePicker, Label lblLegajo, TextField txtLegajo, Label lblDireccion, TextField txtDireccion,
                                 Label lblInstituto, ComboBox<Instituto> cmbInstituto, Docente docente) {
        form.addRow(0, lblNombre, txtNombre);
        form.addRow(1, lblApellido, txtApellido);
        form.addRow(2, lblDni, txtDni);
        form.addRow(3, lblFechaNac, datePicker);
        form.addRow(4, lblLegajo, txtLegajo);
        form.addRow(5, lblDireccion, txtDireccion);
        if (docente == null && lblInstituto != null && cmbInstituto != null) {
            form.addRow(6, lblInstituto, cmbInstituto);
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para crear botones---------------------------------------------------------------------------------------
    private HBox createButtonBox(Stage formStage, Docente docente, TextField txtNombre,
                                 TextField txtApellido, TextField txtDni, DatePicker datePicker,
                                 TextField txtLegajo, TextField txtDireccion,
                                 ComboBox<Instituto> cmbInstituto) {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20px;");

        Button btnCancelar = createCancelButton(formStage);
        Button btnGuardar = createSaveButton(formStage, docente, txtNombre, txtApellido, txtDni,
                datePicker, txtLegajo, txtDireccion, cmbInstituto);

        buttonBox.getChildren().addAll(btnCancelar, btnGuardar);
        return buttonBox;
    }

    private Button createCancelButton(Stage formStage) {
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-font-weight: bold;");
        btnCancelar.setOnAction(e -> formStage.close());
        return btnCancelar;
    }

    private Button createSaveButton(Stage formStage, Docente docente, TextField txtNombre,
                                    TextField txtApellido, TextField txtDni, DatePicker datePicker,
                                    TextField txtLegajo, TextField txtDireccion,
                                    ComboBox<Instituto> cmbInstituto) {
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> handleSaveAction(formStage, docente, txtNombre, txtApellido, txtDni,
                datePicker, txtLegajo, txtDireccion, cmbInstituto));
        return btnGuardar;
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para validación y guardado-------------------------------------------------------------------------------
    private void handleSaveAction(Stage formStage, Docente docente, TextField txtNombre,
                                  TextField txtApellido, TextField txtDni, DatePicker datePicker,
                                  TextField txtLegajo, TextField txtDireccion,
                                  ComboBox<Instituto> cmbInstituto) {
        if (validateFields(docente, txtNombre, txtApellido, txtDni, datePicker,
                txtLegajo, txtDireccion, cmbInstituto)) {
            saveDocente(formStage, docente, txtNombre, txtApellido, txtDni, datePicker,
                    txtLegajo, txtDireccion, cmbInstituto);
        }
    }

    private boolean validateFields(Docente docente, TextField txtNombre, TextField txtApellido,
                                   TextField txtDni, DatePicker datePicker, TextField txtLegajo,
                                   TextField txtDireccion, ComboBox<Instituto> cmbInstituto) {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Validación del nombre
        if (txtNombre.getText().isEmpty() || !validation.isValidTextOnly(txtNombre.getText())) {
            isValid = false;
            errorMessage.append("- El nombre es obligatorio y solo debe contener letras\n");
            ViewManager.getInstance().setErrorStyle(txtNombre);
        }

        // Validación del apellido
        if (txtApellido.getText().isEmpty() || !validation.isValidTextOnly(txtApellido.getText())) {
            isValid = false;
            errorMessage.append("- El apellido es obligatorio y solo debe contener letras\n");
            ViewManager.getInstance().setErrorStyle(txtApellido);
        }

        // Validación del DNI
        String dni = txtDni.getText();
        if (dni.isEmpty() || !validation.isValidDNI(dni)) {
            isValid = false;
            errorMessage.append("- El DNI debe contener 8 dígitos\n");
            ViewManager.getInstance().setErrorStyle(txtDni);
        } else if (validation.isDNIDuplicated(dni, docente != null ? docente.getId() : null)) {
            isValid = false;
            errorMessage.append("- Ya existe un docente registrado con este DNI\n");
            ViewManager.getInstance().setErrorStyle(txtDni);
        }

        // Validación de la fecha
        if (datePicker.getValue() == null) {
            isValid = false;
            errorMessage.append("- La fecha de nacimiento es obligatoria\n");
        } else {
            LocalDate fechaNacimiento = datePicker.getValue();
            if (!validation.isValidAge(fechaNacimiento)) {
                isValid = false;
                errorMessage.append("- El docente debe ser mayor de 18 años\n");
            }
        }

        // Validación del legajo
        String legajo = txtLegajo.getText();
        if (legajo.isEmpty() || !validation.isValidLegajo(legajo)) {
            isValid = false;
            errorMessage.append("- El legajo debe tener el formato A1234\n");
            ViewManager.getInstance().setErrorStyle(txtLegajo);
        } else if (validation.isLegajoDuplicated(legajo, docente != null ? docente.getId() : null)) {
            isValid = false;
            errorMessage.append("- Ya existe un docente registrado con este legajo\n");
            ViewManager.getInstance().setErrorStyle(txtLegajo);
        }

        // Validación de la dirección
        if (txtDireccion.getText().isEmpty()) {
            isValid = false;
            errorMessage.append("- La dirección es obligatoria\n");
            ViewManager.getInstance().setErrorStyle(txtDireccion);
        }

        // Validación del instituto (solo para nuevos docentes)
        if (docente == null && cmbInstituto != null && cmbInstituto.getValue() == null) {
            isValid = false;
            errorMessage.append("- Debe seleccionar un instituto\n");
        }

        if (!isValid) {
            ViewManager.getInstance().showError("Error de validación",
                    "Por favor corrija los siguientes errores: ",
                    errorMessage.toString());
        }

        return isValid;
    }

    private void saveDocente(Stage formStage, Docente docente, TextField txtNombre,
                             TextField txtApellido, TextField txtDni, DatePicker datePicker,
                             TextField txtLegajo, TextField txtDireccion,
                             ComboBox<Instituto> cmbInstituto) {
        try {
            Docente docenteToSave = docente != null ? docente : new Docente();
            setDocenteFields(docenteToSave, txtNombre, txtApellido, txtDni, datePicker,
                    txtLegajo, txtDireccion, cmbInstituto);

            if (docente == null) {
                docenteToSave.setStatus(Status.activo);
                docenteController.save(docenteToSave);
            } else {
                docenteController.edit(docenteToSave);
            }

            handleSuccessfulSave(formStage, docente);
        } catch (Exception ex) {
            ViewManager.getInstance().showError("Error al guardar",
                    "Ocurrió un error al intentar guardar el docente: ",
                    ex.getMessage());
        }
    }

    private void setDocenteFields(Docente docenteToSave, TextField txtNombre, TextField txtApellido,
                                  TextField txtDni, DatePicker datePicker, TextField txtLegajo,
                                  TextField txtDireccion, ComboBox<Instituto> cmbInstituto) {
        docenteToSave.setNombres(txtNombre.getText());
        docenteToSave.setApellidos(txtApellido.getText());
        docenteToSave.setDni(txtDni.getText());
        docenteToSave.setFechaNacimiento(java.sql.Date.valueOf(datePicker.getValue()));
        docenteToSave.setLegajo(txtLegajo.getText());
        docenteToSave.setDireccion(txtDireccion.getText());

        if (cmbInstituto != null && cmbInstituto.getValue() != null) {
            if (docenteToSave.getInstitutos() == null) {
                docenteToSave.setInstitutos(new ArrayList<>());
            }
            docenteToSave.getInstitutos().add(cmbInstituto.getValue());
        }
    }

    private void handleSuccessfulSave(Stage formStage, Docente docente) {
        ViewManager.getInstance().showSuccess("Éxito",
                docente == null ? "Docente registrado correctamente"
                        : "Docente actualizado correctamente");
        formStage.close();
        refreshTable();
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para crear botones --------------------------------------------------------------------------------------
    private HBox createButtonBox(Stage formStage, Docente docente, ComboBox<Instituto> cmbInstituto) {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20px;");

        Button btnCancelar = createCancelButton(formStage);
        Button btnAsignar = createAssignButton(formStage, docente, cmbInstituto);

        buttonBox.getChildren().addAll(btnCancelar, btnAsignar);
        return buttonBox;
    }

    private Button createAssignButton(Stage formStage, Docente docente, ComboBox<Instituto> cmbInstituto) {
        Button btnAsignar = new Button("Asignar");
        btnAsignar.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-font-weight: bold;");
        btnAsignar.setOnAction(e -> handleAssignAction(formStage, docente, cmbInstituto));
        return btnAsignar;
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para manejar la asignación ------------------------------------------------------------------------------
    private void handleAssignAction(Stage formStage, Docente docente, ComboBox<Instituto> cmbInstituto) {
        if (!validateInstitutoSelection(cmbInstituto)) {
            return;
        }

        try {
            assignInstitutoToDocente(docente, cmbInstituto.getValue());
            handleSuccessfulAssignment(formStage);
        } catch (Exception ex) {
            handleAssignmentError(ex);
        }
    }

    private boolean validateInstitutoSelection(ComboBox<Instituto> cmbInstituto) {
        if (cmbInstituto.getValue() == null) {
            ViewManager.getInstance().showError("Error", "Error de validación: ",
                    "Debe seleccionar un instituto");
            return false;
        }
        return true;
    }

    private void assignInstitutoToDocente(Docente docente, Instituto instituto) {
        docenteController.asignInstituto(docente.getId(), instituto.getId());
    }

    private void handleSuccessfulAssignment(Stage formStage) {
        ViewManager.getInstance().showSuccess("Éxito", "Instituto asignado correctamente al docente");
        formStage.close();
        refreshTable(); // Actualizar la tabla de docentes
    }

    private void handleAssignmentError(Exception ex) {
        ViewManager.getInstance().showError("Error al asignar",
                "Ocurrió un error al intentar asignar el instituto: ",
                ex.getMessage());
    }
    //------------------------------------------------------------------------------------------------------------------

    // Método para configurar y mostrar la ventana ---------------------------------------------------------------------
    private void configureAndShowStage(Stage formStage, VBox mainContainer,
                                       HBox header, GridPane form, HBox buttonBox) {
        mainContainer.getChildren().addAll(header, form, buttonBox);

        Scene scene = new Scene(mainContainer);
        formStage.setScene(scene);
        formStage.setMinWidth(800);
        formStage.setMinHeight(700);
        formStage.show();
    }
    //------------------------------------------------------------------------------------------------------------------

    //Mostrar Docentes del Instituto------------------------------------------------------------------------------------
    public void showDocentes(Instituto instituto, Stage owner, String title) {
        Stage docentesStage = ViewManager.getInstance().createNewStage(title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Docentes - " + instituto.getDenominacion());
        VBox contentContainer = ViewManager.getInstance().createContentContainer();

        // Crear y configurar la tabla
        TableView<Docente> tableView = setupTableView(owner, instituto);

        // Configurar el label de cantidad y obtener los datos
        List<Docente> docentes = loadDocentesData(instituto);
        setupQuantityLabel(docentes.size());

        // Configurar contenedores
        setupContainers(contentContainer, mainContainer, header, tableView, docentesStage);

        // Configurar y mostrar la ventana
        setupStage(docentesStage, mainContainer, tableView);
    }
    //------------------------------------------------------------------------------------------------------------------

    //Actualizar Tabla--------------------------------------------------------------------------------------------------
    public void refreshTable() {
        // Primero actualizamos la tabla de institutos si está visible
        if (InstitutoView.activeTableView != null) {
            InstitutoView.refreshTable();
        }

        // Luego actualizamos la tabla de docentes según el contexto
        if (activeTableView != null) {
            if (currentInstituto != null) {
                // Si hay un instituto seleccionado, actualizamos solo los docentes de ese instituto
                List<Docente> docentesInstituto = docenteController.findActiveDocentesByInstitutoId(currentInstituto.getId());
                activeTableView.setItems(FXCollections.observableArrayList(docentesInstituto));
                cantidadDocentes.setText("Total de Docentes: " + docentesInstituto.size());
            } else {
                // Si no hay instituto seleccionado, actualizamos todos los docentes
                if (docentes != null) {
                    docentes.setAll(docenteController.findAll());
                } else {
                    activeTableView.setItems(FXCollections.observableArrayList(docenteController.findAll()));
                }
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Informe docente---------------------------------------------------------------------------------------------------
    public void showInformeDocente(Docente docente) {
        Stage informeStage = ViewManager.getInstance().createInformeStage("Informe del Docente - " + docente.getApellidos() + ", " + docente.getNombres());

        // Contenedor principal con scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox mainContainer = ViewManager.getInstance().createInformeMainContainer();

        // Título principal
        Label titleLabel = new Label("INFORMACIÓN DEL DOCENTE");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        mainContainer.getChildren().add(titleLabel);

        // Datos personales
        VBox datosPersonales = new VBox(10);
        datosPersonales.setStyle("-fx-padding: 10px; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        DocenteController docenteController = new DocenteController();
        ViewManager.getInstance().addInfoRow(datosPersonales, "Apellido y Nombre:", docente.getApellidos() +", "+ docente.getNombres());
        ViewManager.getInstance().addInfoRow(datosPersonales, "DNI:", docente.getDni());
        ViewManager.getInstance().addInfoRow(datosPersonales, "Legajo:", docente.getLegajo());
        ViewManager.getInstance().addInfoRow(datosPersonales, "Edad:", docenteController.calculateAge(docente.getFechaNacimiento()) + " años");
        ViewManager.getInstance().addInfoRow(datosPersonales, "Dirección:", docente.getDireccion());
        ViewManager.getInstance().addInfoRow(datosPersonales, "Estado:", docente.getStatus().toString());

        mainContainer.getChildren().add(datosPersonales);

        // Sección de Institutos
        Label institutosTitle = new Label("INSTITUTOS");
        institutosTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #283593;");
        mainContainer.getChildren().add(institutosTitle);

        InstitutoController institutoController = new InstitutoController();
        List<Instituto> institutos = institutoController.findActiveInstitutosByDocenteId(docente.getId());

        // Por cada instituto
        for (Instituto instituto : institutos) {
            VBox institutoBox = ViewManager.getInstance().createInstitutoBoxForDocente(docente, instituto);
            mainContainer.getChildren().add(institutoBox);
        }

        // Botón de cerrar
        Button btnCerrar = ViewManager.getInstance().createCloseButton(informeStage);

        HBox buttonBox = new HBox(btnCerrar);
        buttonBox.setAlignment(Pos.CENTER);
        mainContainer.getChildren().add(buttonBox);

        scrollPane.setContent(mainContainer);
        ViewManager.getInstance().setupAndShowScene(informeStage, scrollPane);
    }
    //------------------------------------------------------------------------------------------------------------------

    //Activar e Inactivar Docentes--------------------------------------------------------------------------------------
    private void activateDocente(Docente docente) {
        Alert confirmacion = ViewManager.getInstance().createAlert("Confirmar Alta", "¿Está seguro de dar de alta al docente nuevamente?", "Esta acción cambiará el estado del docente a activo.");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                docenteController.activate(docente);
                refreshTable();
                ViewManager.getInstance().showSuccess("Éxito", "Docente dado de alta correctamente");
            } catch (Exception ex) {
                ViewManager.getInstance().showError("Error", "No se pudo dar de alta al docente: ", ex.getMessage());
            }
        }
    }

    private void deactivateDocente(Docente docente) {
        Alert confirmacion = ViewManager.getInstance().createAlert("Confirmar Baja", "¿Está seguro de dar de baja al docente?", "Esta acción cambiará el estado del docente a inactivo.");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                docenteController.deactivate(docente);
                refreshTable();
                ViewManager.getInstance().showSuccess("Éxito", "Docente dado de baja correctamente");
            } catch (Exception ex) {
                ViewManager.getInstance().showError("Error", "No se pudo dar de baja al docente: ", ex.getMessage());
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Menú Contextual---------------------------------------------------------------------------------------------------
    private TableRow<Docente> createContextMenuRow(TableView<Docente> tableView, Stage primaryStage) {
        TableRow<Docente> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                Docente docente = row.getItem();
                row.setContextMenu(createContextMenu(docente, primaryStage));
            }
        });
        return row;
    }

    private ContextMenu createContextMenu(Docente docente, Stage owner) {
        ContextMenu contextMenu = new ContextMenu();

        if (docente.getStatus() == Status.activo) {
            MenuItem informeItem = new MenuItem("Ver Detalles del Docente");
            informeItem.setOnAction(e ->
                    showInformeDocente(docente)
            );

            MenuItem editarItem = new MenuItem("Editar Docente");
            editarItem.setOnAction(e ->
                    openForm(docente, owner, "Editar Docente")
            );

            MenuItem eliminarItem = new MenuItem("Dar de Baja Docente");
            eliminarItem.setOnAction(e ->
                    deactivateDocente(docente)
            );

            MenuItem verInstitutosItem = new MenuItem("Ver Institutos");
            verInstitutosItem.setOnAction(e ->
                    ViewManager.getInstance().showInstitutos(docente, owner, "Institutos del Docente")
            );

            MenuItem verAsignaturasItem = new MenuItem("Ver Asignaturas");
            verAsignaturasItem.setOnAction(e ->
                    ViewManager.getInstance().showAsignaturas(docente, owner, "Asignaturas del Docente")
            );

            MenuItem agregarInstitutoItem = new MenuItem("Agregar Instituto");
            agregarInstitutoItem.setOnAction(e ->
                    openAsignarInstitutoForm(docente, owner, "Asignar Instituto a Docente")
            );

            MenuItem registrarAsignaturaItem = new MenuItem("Registrar Asignatura");
            registrarAsignaturaItem.setOnAction(e ->
                    ViewManager.getInstance().openAsignaturaForm(owner, "Registrar Nueva Asignatura" ,docente)
            );

            // Separadores para organizar el menú
            SeparatorMenuItem separator1 = new SeparatorMenuItem();
            SeparatorMenuItem separator2 = new SeparatorMenuItem();

            contextMenu.getItems().addAll(
                    informeItem,
                    editarItem,
                    eliminarItem,
                    separator1,
                    verInstitutosItem,
                    verAsignaturasItem,
                    separator2,
                    agregarInstitutoItem,
                    registrarAsignaturaItem
            );
        } else {
            MenuItem altaItem = new MenuItem("Dar de Alta Docente");
            altaItem.setOnAction(e -> activateDocente(docente));
            contextMenu.getItems().add(altaItem);
        }

        return contextMenu;
    }
    //------------------------------------------------------------------------------------------------------------------

    //Menú Contextual---------------------------------------------------------------------------------------------------
    private TableRow<Docente> createContextMenuRow(TableView<Docente> tableView, Instituto instituto, Stage primaryStage) {
        TableRow<Docente> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                Docente docente = row.getItem();
                row.setContextMenu(createContextMenu(instituto, docente, primaryStage));
            }
        });
        return row;
    }

    public ContextMenu createContextMenu(Instituto instituto, Docente docente, Stage owner) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem desvincularItem = new MenuItem("Desvincular Instituto-Docente");
        desvincularItem.setOnAction(e -> {
            InstitutoView institutoView = new InstitutoView();
            institutoView.unlinkInstitutoFromDocente(instituto, docente);
        });

        MenuItem verDocenteItem = new MenuItem("Ver Detalles del Docente");
        verDocenteItem.setOnAction(e -> {
            showInformeDocente(docente);
        });

        // Separadores para organizar el menú
        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        // Agregar todos los items al menú contextual
        contextMenu.getItems().addAll(
                desvincularItem,
                separator1,
                verDocenteItem
        );
        return contextMenu;
    }
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void start(Stage primaryStage) {
        // Configuración inicial
        ViewManager.getInstance().setupInitialStage(primaryStage, "Sistema de Gestión Académica - Docentes");

        // Crear contenedores principales
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Gestión de Docentes");

        // Crear y configurar la tabla
        TableView<Docente> tableView = setupTableView(primaryStage, null);

        // Crear panel superior con botones
        HBox buttonPanel = createButtonPanel(primaryStage, "Nuevo Docente", "Registrar Nuevo Docente");

        // Crear el campo de búsqueda
        VBox searchContainer = ViewManager.getInstance().createSearchContainer();
        Label searchLabel = ViewManager.getInstance().createSearchLabel("Buscar docente:");
        TextField searchField = ViewManager.getInstance().createSearchField("Ingrese nombre, apellido, DNI, legajo o dirección...");
        searchContainer.getChildren().addAll(searchLabel, searchField);

        // Crear el topPanel y añadirle los componentes
        VBox topPanel = ViewManager.getInstance().createTopPanel();
        topPanel.getChildren().addAll(buttonPanel, searchContainer);

        // Configurar el filtrado
        setupTableFiltering(tableView, getSearchFieldFromContainer(searchContainer));

        // Crear panel de información
        VBox tableContainer = createTableContainer(tableView);

        // Ensamblar la interfaz
        mainContainer.getChildren().addAll(header, topPanel, tableContainer);

        // Configurar y mostrar la ventana
        setupStage(primaryStage, mainContainer, tableView);
    }

    private List<Docente> loadDocentesData(Instituto instituto) {
        List<Docente> docentes;
        if (instituto != null) {
            docentes = docenteController.findActiveDocentesByInstitutoId(instituto.getId());
        } else {
            docentes = docenteController.findAll();
        }
        if (activeTableView != null) {
            activeTableView.setItems(FXCollections.observableArrayList(docentes));
        }
        return docentes;
    }

    private void setupQuantityLabel(int cantidad) {
        cantidadDocentes = new Label("Total de docentes: " + cantidad);
        cantidadDocentes.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    }

    private void setupContainers(VBox contentContainer, VBox mainContainer, HBox header,
                                 TableView<Docente> tableView, Stage docentesStage) {
        HBox buttonBox = createCancelButtonBox(docentesStage);
        contentContainer.getChildren().addAll(cantidadDocentes, tableView);
        mainContainer.getChildren().addAll(header, contentContainer, buttonBox);
    }

    private HBox createCancelButtonBox(Stage stage) {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20px;");

        Button btnCancelar = createCancelButton(stage);
        buttonBox.getChildren().addAll(btnCancelar);
        return buttonBox;
    }

    // Método para crear y configurar la tabla
    private TableView<Docente> setupTableView(Stage primaryStage, Instituto instituto) {
        TableView<Docente> tableView = createDocenteTable();

        // Guardar referencias
        activeTableView = tableView;
        currentInstituto = instituto;

        // Configurar columnas y menú contextual
        configureTableColumns(tableView);
        if(currentInstituto != null) {
            tableView.setRowFactory(tv -> createContextMenuRow(tableView, instituto, primaryStage));
        } else {
            tableView.setRowFactory(tv -> createContextMenuRow(tableView, primaryStage));
        }

        return tableView;
    }

    // Método para crear la tabla base
    private TableView<Docente> createDocenteTable() {
        TableView<Docente> tableView = new TableView<>();
        tableView.setStyle("-fx-font-size: 14px; -fx-background-color: white; -fx-border-color: #e0e0e0;");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableView;
    }

    // Método para configurar las columnas de la tabla
    private void configureTableColumns(TableView<Docente> tableView) {
        TableColumn<Docente, String> legajoCol = createColumn("Legajo", "legajo", 80);
        TableColumn<Docente, String> dniCol = createColumn("DNI", "dni", 100);
        TableColumn<Docente, String> nombresCol = createColumn("Nombres", "nombres", 150);
        TableColumn<Docente, String> apellidosCol = createColumn("Apellidos", "apellidos", 150);
        TableColumn<Docente, Integer> edadCol = createEdadColumn();
        TableColumn<Docente, String> direccionCol = createColumn("Dirección", "direccion", 200);
        TableColumn<Docente, Status> statusCol = createColumn("Estado", "status", 80);

        tableView.getColumns().addAll(legajoCol, dniCol, nombresCol, apellidosCol, edadCol, direccionCol, statusCol);
    }

    // Método para crear una columna genérica
    private <T> TableColumn<Docente, T> createColumn(String title, String propertyName, double width) {
        TableColumn<Docente, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        column.setPrefWidth(width);
        column.setMinWidth(width);
        return column;
    }

    // Método para crear la columna de edad
    private TableColumn<Docente, Integer> createEdadColumn() {
        TableColumn<Docente, Integer> edadCol = new TableColumn<>("Edad");
        edadCol.setCellValueFactory(cellData -> {
            int edad = docenteController.calculateAge(cellData.getValue().getFechaNacimiento());
            return new SimpleIntegerProperty(edad).asObject();
        });
        edadCol.setStyle("-fx-alignment: CENTER;");
        edadCol.setPrefWidth(60);
        edadCol.setMinWidth(60);
        return edadCol;
    }

    // Método para crear el panel de botones
    private HBox createButtonPanel(Stage primaryStage, String text, String title) {
        HBox buttonPanel = new HBox();
        HBox.setHgrow(buttonPanel, Priority.ALWAYS);

        HBox leftContainer = createLeftButtonContainer(primaryStage, text, title);
        HBox rightContainer = ViewManager.getInstance().createRightButtonContainer(primaryStage);

        buttonPanel.getChildren().addAll(leftContainer, rightContainer);
        return buttonPanel;
    }

    // Método para crear el contenedor izquierdo de botones
    private HBox createLeftButtonContainer(Stage primaryStage, String text, String title) {
        HBox leftContainer = new HBox();
        Button btnNuevo = createNewButton(text, title, primaryStage);
        leftContainer.getChildren().add(btnNuevo);
        return leftContainer;
    }

    // Método para crear el botón de nuevo docente
    private Button createNewButton(String text, String title, Stage primaryStage) {
        Button btnNuevo = new Button(text);
        btnNuevo.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnNuevo.setOnAction(e -> openForm(null, primaryStage, title));
        return btnNuevo;
    }

    // Método auxiliar para obtener el campo de búsqueda del contenedor
    private TextField getSearchFieldFromContainer(VBox searchContainer) {
        return (TextField) searchContainer.getChildren().get(1);
    }

    // Método para configurar el filtrado de la tabla
    private void setupTableFiltering(TableView<Docente> tableView, TextField searchField) {
        // Cargar los datos
        docentes = FXCollections.observableArrayList(docenteController.findAll());
        filteredDocentes = new FilteredList<>(docentes, p -> true);

        // Configurar el filtro basado en el texto de búsqueda
        setupSearchFilter(searchField);

        // Crear un SortedList basado en la FilteredList
        SortedList<Docente> sortedData = new SortedList<>(filteredDocentes);

        // Vincular el comparador de la lista ordenada con el comparador de la tabla
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        // Usar sortedData como fuente de datos para la tabla
        tableView.setItems(sortedData);
    }

    // Método para configurar el filtro de búsqueda
    private void setupSearchFilter(TextField searchField) {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredDocentes.setPredicate(docente -> filterDocente(docente, newValue));
        });
    }

    // Método para filtrar un docente según el texto de búsqueda
    private boolean filterDocente(Docente docente, String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        String lowerCaseFilter = filterText.toLowerCase();

        return docente.getLegajo().toLowerCase().contains(lowerCaseFilter) ||
                docente.getDni().toLowerCase().contains(lowerCaseFilter) ||
                docente.getNombres().toLowerCase().contains(lowerCaseFilter) ||
                docente.getApellidos().toLowerCase().contains(lowerCaseFilter) ||
                (docente.getDireccion() != null &&
                        docente.getDireccion().toLowerCase().contains(lowerCaseFilter));
    }

    // Método para crear el contenedor de la tabla
    private VBox createTableContainer(TableView<Docente> tableView) {
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-padding: 20px;");

        HBox infoPanel = createInfoPanel();

        tableContainer.getChildren().addAll(tableView, infoPanel);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        return tableContainer;
    }

    // Método para crear el panel de información
    private HBox createInfoPanel() {
        HBox infoPanel = new HBox(10);
        infoPanel.setStyle("-fx-padding: 10px; -fx-background-color: white;");

        Label totalDocentes = createTotalLabel();
        infoPanel.getChildren().add(totalDocentes);

        setupTotalLabelListener(totalDocentes);

        return infoPanel;
    }

    // Método para crear la etiqueta del total
    private Label createTotalLabel() {
        Label totalDocentes = new Label("Total de docentes: " + docentes.size());
        totalDocentes.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return totalDocentes;
    }

    // Método para configurar el listener de la etiqueta total
    private void setupTotalLabelListener(Label totalDocentes) {
        filteredDocentes.addListener((ListChangeListener<Docente>) c -> {
            totalDocentes.setText("Total de docentes mostrados: " + filteredDocentes.size());
        });
    }

    private void setupStage(Stage asignaturasStage, VBox mainContainer, TableView<Docente> tableView) {
        Scene scene = new Scene(mainContainer);
        asignaturasStage.setScene(scene);
        asignaturasStage.setMinWidth(800);
        asignaturasStage.setMinHeight(500);

        // Configurar el evento de cierre
        asignaturasStage.setOnCloseRequest(event -> cleanupOnClose(tableView));

        asignaturasStage.show();
    }

    private void cleanupOnClose(TableView<Docente> tableView) {
        if (activeTableView == tableView) {
            activeTableView = null;
            currentInstituto = null;
        }
    }

}