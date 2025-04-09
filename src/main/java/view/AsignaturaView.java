package view;

import javafx.collections.transformation.SortedList;
import controller.AsignaturaController;
import controller.DocenteController;
import controller.InstitutoController;
import controller.util.Validation;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
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

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class AsignaturaView extends Application{

    private final AsignaturaController asignaturaController;
    private final Validation validation;

    private static TableView<Asignatura> activeTableView;
    private static Instituto currentInstituto; // Para mantener referencia del instituto actual
    private static Docente currentDocente; // Para mantener referencia del docente actual
    private static Label cantidadAsignaturas;
    private ObservableList<Asignatura> asignaturas;
    private FilteredList<Asignatura> filteredAsignaturas;

    public AsignaturaView() {
        this.asignaturaController = new AsignaturaController();
        this.validation = new Validation();
        this.asignaturas = FXCollections.observableArrayList();
    }

    //Abrir el formulario de registro/edición---------------------------------------------------------------------------
    private void openForm(Asignatura asignatura, Stage primaryStage, String title) {
        Stage formStage = ViewManager.getInstance().createFormStage(primaryStage, title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader(title);
        GridPane form = ViewManager.getInstance().createFormGrid();

        // Labels del formulario
        Label lblCodigo = ViewManager.getInstance().createLabel("Código:");
        Label lblNombre = ViewManager.getInstance().createLabel("Nombre:");
        Label lblDescripcion = ViewManager.getInstance().createLabel("Descripción:");
        Label lblInstituto = ViewManager.getInstance().createLabel("Instituto:");
        Label lblDocente = ViewManager.getInstance().createLabel("Docente:");

        // Campos del formulario
        TextField txtCodigo = ViewManager.getInstance().createTextField(asignatura != null ? asignatura.getCodigo() : "", "Formato: AAA123");
        TextField txtNombre = ViewManager.getInstance().createTextField(asignatura != null ? asignatura.getNombre() : "", "Ingrese el nombre");
        TextArea txtDescripcion = ViewManager.getInstance().createTextArea(asignatura != null ? asignatura.getDescripcion() : "", "Ingrese la descripción");
        ComboBox<Instituto> cmbInstituto = ViewManager.getInstance().createInstitutoComboBox();
        ComboBox<Docente> cmbDocente = ViewManager.getInstance().createDocenteComboBox();

        // Para ComboBox de Institutos
        InstitutoController institutoController = new InstitutoController();
        List<Instituto> institutos = institutoController.findAllActives();
        List<Instituto> institutosActivos = configureInstitutoComboBox(cmbInstituto, institutos);

        // Para ComboBox de Docentes
        configureDocenteComboBox(cmbDocente);
        BiConsumer<Instituto, Docente> cargarDocentes = createDocenteLoader(cmbDocente);
        cmbInstituto.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            cargarDocentes.accept(newValue, null);
        });
        //Si se esta editando
        setupInitialValues(asignatura, cmbInstituto, institutosActivos, cargarDocentes);

        // Validaciones en tiempo real
        validateInRealTime(asignatura, txtCodigo);

        // Agregar campos al formulario
        form.addRow(0, lblCodigo, txtCodigo);
        form.addRow(1, lblNombre, txtNombre);
        form.addRow(2, lblDescripcion, txtDescripcion);
        form.addRow(3, lblInstituto, cmbInstituto);
        form.addRow(4, lblDocente, cmbDocente);

        // Botones
        HBox buttonBox = createButtonBox(formStage, asignatura, txtCodigo, txtNombre, txtDescripcion, cmbInstituto, cmbDocente);

        mainContainer.getChildren().addAll(header, form, buttonBox);

        Scene scene = new Scene(mainContainer);
        formStage.setScene(scene);
        formStage.setMinWidth(800);
        formStage.setMinHeight(700);
        formStage.show();
    }
    //------------------------------------------------------------------------------------------------------------------

    // Abrir el formulario de registro para un docente seleccionado-----------------------------------------------------
    public void openForm(Stage primaryStage, String title, Docente docente) {
        Stage formStage = ViewManager.getInstance().createFormStage(primaryStage, title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader(title);
        GridPane form = ViewManager.getInstance().createFormGrid();

        // Campos del formulario (datos del docente - solo lectura)
        Label lblDocente = ViewManager.getInstance().createLabel("Nombre completo:");
        TextField txtNombreCompleto = new TextField(docente.getApellidos() + ", " + docente.getNombres());
        txtNombreCompleto.setEditable(false);
        txtNombreCompleto.setFocusTraversable(false);
        txtNombreCompleto.setStyle(ViewManager.getInstance().readOnlyStyle);

        // Labels del formulario
        Label lblCodigo = ViewManager.getInstance().createLabel("Código:");
        Label lblNombre = ViewManager.getInstance().createLabel("Nombre:");
        Label lblDescripcion = ViewManager.getInstance().createLabel("Descripción:");
        Label lblInstituto = ViewManager.getInstance().createLabel("Instituto:");

        // Campos del formulario
        TextField txtCodigo = ViewManager.getInstance().createTextField("","Formato: AAA123");
        TextField txtNombre = ViewManager.getInstance().createTextField("", "Ingrese el nombre");
        TextArea txtDescripcion = ViewManager.getInstance().createTextArea("", "Ingrese la descripción");
        ComboBox<Instituto> cmbInstituto = ViewManager.getInstance().createInstitutoComboBox();
        ComboBox<Docente> cmbDocente = ViewManager.getInstance().createDocenteComboBox();

        cmbDocente.setItems(FXCollections.observableArrayList(docente));
        cmbDocente.setValue(docente);

        // Para ComboBox de Institutos
        InstitutoController institutoController = new InstitutoController();
        List<Instituto> institutos = institutoController.findActiveInstitutosByDocenteIdWithCargo(docente.getId());
        configureInstitutoComboBox(cmbInstituto, institutos);

        // Validaciones en tiempo real
        validateInRealTime(null, txtCodigo);

        // Agregar campos al formulario
        form.addRow(0, lblDocente, txtNombreCompleto);
        form.addRow(1, lblCodigo, txtCodigo);
        form.addRow(2, lblNombre, txtNombre);
        form.addRow(3, lblDescripcion, txtDescripcion);
        form.addRow(4, lblInstituto, cmbInstituto);

        // Botones
        HBox buttonBox = createButtonBox(formStage, null, txtCodigo, txtNombre, txtDescripcion, cmbInstituto, cmbDocente);

        mainContainer.getChildren().addAll(header, form, buttonBox);

        Scene scene = new Scene(mainContainer);
        formStage.setScene(scene);
        formStage.setMinWidth(800);
        formStage.setMinHeight(700);
        formStage.show();

    }
    //------------------------------------------------------------------------------------------------------------------

    // Abrir el formulario de registro para un instituto seleccionado---------------------------------------------------
    public void openForm(Stage primaryStage, String title, Instituto instituto) {
        Stage formStage = ViewManager.getInstance().createFormStage(primaryStage, title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader(title);
        GridPane form = ViewManager.getInstance().createFormGrid();

        // Campos del formulario (datos del instituto - solo lectura)
        Label lblInstituto = ViewManager.getInstance().createLabel("Instituto:");
        TextField txtInstituto = new TextField(instituto.getCodigo() + " - " + instituto.getDenominacion());
        txtInstituto.setEditable(false);
        txtInstituto.setFocusTraversable(false);
        txtInstituto.setStyle(ViewManager.getInstance().readOnlyStyle);

        // Labels del formulario
        Label lblCodigo = ViewManager.getInstance().createLabel("Código:");
        Label lblNombre = ViewManager.getInstance().createLabel("Nombre:");
        Label lblDescripcion = ViewManager.getInstance().createLabel("Descripción:");
        Label lblDocente = ViewManager.getInstance().createLabel("Docente:");

        // Campos del formulario
        TextField txtCodigo = ViewManager.getInstance().createTextField("","Formato: AAA123");
        TextField txtNombre = ViewManager.getInstance().createTextField("", "Ingrese el nombre");
        TextArea txtDescripcion = ViewManager.getInstance().createTextArea("", "Ingrese la descripción");
        ComboBox<Instituto> cmbInstituto = ViewManager.getInstance().createInstitutoComboBox();
        ComboBox<Docente> cmbDocente = ViewManager.getInstance().createDocenteComboBox();

        cmbInstituto.setItems(FXCollections.observableArrayList(instituto));
        cmbInstituto.setValue(instituto);

        // Para Docentes
        configureDocenteComboBox(cmbDocente);
        BiConsumer<Instituto, Docente> cargarDocentes = createDocenteLoader(cmbDocente);
        cargarDocentes.accept(instituto, null);

        // Validaciones en tiempo real------------------------------------------------------
        validateInRealTime(null, txtCodigo);

        // Agregar campos al formulario
        form.addRow(0, lblInstituto, txtInstituto);
        form.addRow(1, lblCodigo, txtCodigo);
        form.addRow(2, lblNombre, txtNombre);
        form.addRow(3, lblDescripcion, txtDescripcion);
        form.addRow(4, lblDocente, cmbDocente);

        // Botones
        HBox buttonBox = createButtonBox(formStage, null, txtCodigo, txtNombre, txtDescripcion, cmbInstituto, cmbDocente);

        mainContainer.getChildren().addAll(header, form, buttonBox);

        Scene scene = new Scene(mainContainer);
        formStage.setScene(scene);
        formStage.setMinWidth(800);
        formStage.setMinHeight(700);
        formStage.show();
    }
    //------------------------------------------------------------------------------------------------------------------

    // Configuraciones de los ComboBox----------------------------------------------------------------------------------
    private List<Instituto> configureInstitutoComboBox(ComboBox<Instituto> cmbInstituto, List<Instituto> institutos) {
        if (institutos.isEmpty()) {
            Instituto noInstitutosItem = new Instituto();
            noInstitutosItem.setCodigo("N/A");
            noInstitutosItem.setDenominacion("No hay Institutos activos registrados");
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
        return institutos;
    }

    private void configureDocenteComboBox(ComboBox<Docente> cmbDocente) {
        cmbDocente.setCellFactory(lv -> new ListCell<Docente>() {
            @Override
            protected void updateItem(Docente item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("");

                if (empty || item == null) {
                    setText(null);
                } else {
                    if ("N/A".equals(item.getLegajo())) {
                        setText(item.getApellidos() + " " + item.getNombres());
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    } else {
                        setText(item.getLegajo() + " - " + item.getApellidos() + ", " + item.getNombres());
                    }
                }
            }
        });
        cmbDocente.setButtonCell(cmbDocente.getCellFactory().call(null));
    }

    private BiConsumer<Instituto, Docente> createDocenteLoader(ComboBox<Docente> cmbDocente) {
        return (instituto, docenteSeleccionar) -> {
            if (instituto != null) {
                DocenteController docenteController = new DocenteController();
                List<Docente> docentes = docenteController.findActiveDocentesByInstitutoIdWithCargo(instituto.getId());

                if (docentes.isEmpty()) {
                    Docente noDocentesItem = new Docente();
                    noDocentesItem.setLegajo("N/A");
                    noDocentesItem.setApellidos("No hay");
                    noDocentesItem.setNombres("docentes activos para el Instituto");

                    cmbDocente.setItems(FXCollections.observableArrayList(noDocentesItem));
                    cmbDocente.setValue(noDocentesItem);
                } else {
                    cmbDocente.setItems(FXCollections.observableArrayList(docentes));
                    if (docenteSeleccionar != null) {
                        Docente docenteEncontrado = docentes.stream()
                                .filter(d -> d.getId().equals(docenteSeleccionar.getId()))
                                .findFirst()
                                .orElse(null);
                        cmbDocente.setValue(docenteEncontrado);
                    } else {
                        cmbDocente.setValue(null);
                    }
                }
            } else {
                cmbDocente.setItems(FXCollections.observableArrayList());
                cmbDocente.setValue(null);
            }
        };
    }

    private void setupInitialValues(Asignatura asignatura, ComboBox<Instituto> cmbInstituto,
                                    List<Instituto> institutosActivos, BiConsumer<Instituto, Docente> cargarDocentes) {
        if (asignatura != null) {
            Instituto institutoActual = asignatura.getInstituto();
            Instituto institutoEncontrado = institutosActivos.stream()
                    .filter(i -> i.getId().equals(institutoActual.getId()))
                    .findFirst()
                    .orElse(null);

            cmbInstituto.setValue(institutoEncontrado);

            if (institutoEncontrado != null) {
                cargarDocentes.accept(institutoEncontrado, asignatura.getDocente());
            }
        }
    }

    // Métodos de Validacion--------------------------------------------------------------------------------------------

    // Método para validar codigo
    private boolean isValidCodigo(String codigo, Asignatura asignatura) {
        return !codigo.isEmpty() &&
                validation.isValidCodigoAsignatura(codigo) &&
                !validation.isCodigoAsignaturaDuplicated(codigo, asignatura != null ? asignatura.getId() : null);
    }

    // Método para validar instituto y docente
    private boolean validateInstitutoAndDocente(ComboBox<Instituto> cmbInstituto,
                                                ComboBox<Docente> cmbDocente,
                                                StringBuilder errorMessage) {
        if (cmbInstituto.getValue() == null) {
            errorMessage.append("- Debe seleccionar un instituto\n");
            ViewManager.getInstance().setErrorStyle(cmbInstituto);
            return false;
        }

        if (cmbDocente.getValue() != null && "N/A".equals(cmbDocente.getValue().getLegajo())) {
            errorMessage.append("No es posible registrar asignatura para el instituto: "
                    + cmbInstituto.getValue().getDenominacion()
                    + " porque aún no tiene docentes asignados\n");
            return false;
        } else if (cmbDocente.getValue() == null) {
            errorMessage.append("- Debe seleccionar un docente\n");
            return false;
        }
        return true;
    }

    // Método para validación en tiempo real
    private void validateInRealTime(Asignatura asignatura, TextField txtCodigo) {
        // Validación del código
        txtCodigo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                resetField(txtCodigo);
            } else {
                updateFieldStyle(txtCodigo, isValidCodigo(newValue, asignatura));
            }
        });
    }

    // Método para validación antes de guardar
    private boolean validateFields(Asignatura asignatura, TextField txtCodigo,
                                   TextField txtNombre, TextArea txtDescripcion,
                                   ComboBox<Instituto> cmbInstituto, ComboBox<Docente> cmbDocente) {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Validación del código
        if (txtCodigo.getText().isEmpty() || !validation.isValidCodigoAsignatura(txtCodigo.getText())) {
            isValid = false;
            errorMessage.append("- El código debe tener el formato AAA123\n");
            ViewManager.getInstance().setErrorStyle(txtCodigo);
        } else if (validation.isCodigoAsignaturaDuplicated(txtCodigo.getText(), asignatura != null ? asignatura.getId() : null)) {
            isValid = false;
            errorMessage.append("- Ya existe una asignatura registrada con este código\n");
            ViewManager.getInstance().setErrorStyle(txtCodigo);
        }

        // Validación del nombre
        if (txtNombre.getText().isEmpty() || !validation.isValidTextAndNumbers(txtNombre.getText())) {
            isValid = false;
            errorMessage.append("- El nombre es obligatorio\n");
            ViewManager.getInstance().setErrorStyle(txtNombre);
        }

        // Validación de la descripción
        if (txtDescripcion.getText().isEmpty()) {
            isValid = false;
            errorMessage.append("- La descripción es obligatoria\n");
            ViewManager.getInstance().setErrorStyle(txtDescripcion);
        }

        // Validación de instituto y docente
        if (!validateInstitutoAndDocente(cmbInstituto, cmbDocente, errorMessage)) {
            isValid = false;
        }

        if (!isValid) {
            ViewManager.getInstance().showError("Error de validación",
                    "Por favor corrija los siguientes errores: ", errorMessage.toString());
        }

        return isValid;
    }

    private void updateFieldStyle(TextInputControl field, boolean isValid) {
        if (isValid) {
            ViewManager.getInstance().resetStyle(field);
        } else {
            ViewManager.getInstance().setErrorStyle(field);
        }
        field.setTooltip(null);
    }

    private void resetField(TextInputControl field) {
        ViewManager.getInstance().resetStyle(field);
        field.setTooltip(null);
    }
    //------------------------------------------------------------------------------------------------------------------

    // Método para crear el contenedor de botones-----------------------------------------------------------------------
    private HBox createButtonBox(Stage formStage, Asignatura asignatura, TextField txtCodigo,
                                 TextField txtNombre, TextArea txtDescripcion,
                                 ComboBox<Instituto> cmbInstituto, ComboBox<Docente> cmbDocente) {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20px;");

        Button btnCancelar = createCancelButton(formStage);
        Button btnGuardar = createSaveButton(formStage, asignatura, txtCodigo, txtNombre,
                txtDescripcion, cmbInstituto, cmbDocente);

        buttonBox.getChildren().addAll(btnCancelar, btnGuardar);
        return buttonBox;
    }

    // Método para crear el botón de cancelar
    private Button createCancelButton(Stage formStage) {
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-font-weight: bold;");
        btnCancelar.setOnAction(e -> formStage.close());
        return btnCancelar;
    }

    // Método para crear el botón de guardar
    private Button createSaveButton(Stage formStage, Asignatura asignatura, TextField txtCodigo,
                                    TextField txtNombre, TextArea txtDescripcion,
                                    ComboBox<Instituto> cmbInstituto, ComboBox<Docente> cmbDocente) {
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> handleSaveAction(formStage, asignatura, txtCodigo, txtNombre,
                txtDescripcion, cmbInstituto, cmbDocente));
        return btnGuardar;
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para guardar la asignatura-------------------------------------------------------------------------------

    // Método para manejar la acción de guardar
    private void handleSaveAction(Stage formStage, Asignatura asignatura, TextField txtCodigo,
                                  TextField txtNombre, TextArea txtDescripcion,
                                  ComboBox<Instituto> cmbInstituto, ComboBox<Docente> cmbDocente) {

        if (validateFields( asignatura, txtCodigo, txtNombre, txtDescripcion, cmbInstituto, cmbDocente)) {
            saveAsignatura(formStage, asignatura, txtCodigo, txtNombre, txtDescripcion,
                    cmbInstituto, cmbDocente);
        }
    }

    private void saveAsignatura(Stage formStage, Asignatura asignatura, TextField txtCodigo,
                                TextField txtNombre, TextArea txtDescripcion,
                                ComboBox<Instituto> cmbInstituto, ComboBox<Docente> cmbDocente) {
        try {
            Asignatura asignaturaToSave = asignatura != null ? asignatura : new Asignatura();
            setAsignaturaFields(asignaturaToSave, txtCodigo, txtNombre, txtDescripcion,
                    cmbInstituto, cmbDocente);

            if (asignatura == null) {
                asignaturaToSave.setStatus(Status.activo);
                asignaturaController.save(asignaturaToSave);
            } else {
                asignaturaController.edit(asignaturaToSave);
            }

            handleSuccessfulSave(formStage, asignatura);
        } catch (Exception ex) {
            ViewManager.getInstance().showError("Error al guardar",
                    "Ocurrió un error al intentar guardar la asignatura: ", ex.getMessage());
        }
    }

    // Método para establecer los campos de la asignatura
    private void setAsignaturaFields(Asignatura asignaturaToSave, TextField txtCodigo,
                                     TextField txtNombre, TextArea txtDescripcion,
                                     ComboBox<Instituto> cmbInstituto, ComboBox<Docente> cmbDocente) {
        asignaturaToSave.setCodigo(txtCodigo.getText());
        asignaturaToSave.setNombre(txtNombre.getText());
        asignaturaToSave.setDescripcion(txtDescripcion.getText());
        asignaturaToSave.setInstituto(cmbInstituto.getValue());
        asignaturaToSave.setDocente(cmbDocente.getValue());
    }

    // Método para manejar el guardado exitoso
    private void handleSuccessfulSave(Stage formStage, Asignatura asignatura) {
        ViewManager.getInstance().showSuccess("Éxito",
                asignatura == null ? "Asignatura registrada correctamente" : "Asignatura actualizada correctamente");
        formStage.close();
        refreshTable();
    }
    //------------------------------------------------------------------------------------------------------------------

    //Mostrar Asignaturas del Instituto (sobrecarga de método)----------------------------------------------------------
    public void showAsignaturas(Instituto instituto, Stage owner, String title) {
        Stage asignaturasStage = ViewManager.getInstance().createNewStage(title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Asignaturas - " + instituto.getDenominacion());
        VBox contentContainer = ViewManager.getInstance().createContentContainer();

        // Configurar la tabla y sus componentes
        TableView<Asignatura> tableView = setupTableView(instituto, null, asignaturasStage);

        // Configurar el label de cantidad y obtener los datos
        List<Asignatura> asignaturas = loadAsignaturasData(instituto, null);
        setupQuantityLabel(asignaturas.size());

        // Configurar contenedores
        setupContainers(contentContainer, mainContainer, header, tableView, asignaturasStage);

        // Configurar y mostrar la ventana
        setupStage(asignaturasStage, mainContainer, tableView);
    }
    //------------------------------------------------------------------------------------------------------------------

    //Mostrar Asignaturas del Docente (sobrecarga de método)------------------------------------------------------------
    public void showAsignaturas(Docente docente, Stage owner, String title) {

        Stage asignaturasStage = ViewManager.getInstance().createNewStage(title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Asignaturas de docente - " + docente.getApellidos() + ", " + docente.getNombres());
        VBox contentContainer = ViewManager.getInstance().createContentContainer();

        // Configurar la tabla y sus componentes
        TableView<Asignatura> tableView = setupTableView(null, docente, asignaturasStage);

        // Configurar el label de cantidad y obtener los datos
        List<Asignatura> asignaturas = loadAsignaturasData(null, docente);
        setupQuantityLabel(asignaturas.size());

        // Configurar contenedores
        setupContainers(contentContainer, mainContainer, header, tableView, asignaturasStage);

        // Configurar y mostrar la ventana
        setupStage(asignaturasStage, mainContainer, tableView);
    }
    //------------------------------------------------------------------------------------------------------------------

    //Actualizar Tabla--------------------------------------------------------------------------------------------------
    private void refreshTable() {
        // Primero actualizamos la tabla de institutos si está visible
        if (InstitutoView.activeTableView != null) {
            InstitutoView.refreshTable();
        }

        // Luego actualizamos la tabla de asignaturas según el contexto
        if (activeTableView != null) {
            if (currentInstituto != null) {
                List<Asignatura> asignaturasInstituto = asignaturaController.findActiveAsignaturasByInstitutoId(currentInstituto.getId());
                activeTableView.setItems(FXCollections.observableArrayList(asignaturasInstituto));
                cantidadAsignaturas.setText("Total de Asignaturas: " + asignaturasInstituto.size());
            } else if (currentDocente != null) {
                List<Asignatura> asignaturasDocente = asignaturaController.findActiveAsignaturasByDocenteId(currentDocente.getId());
                activeTableView.setItems(FXCollections.observableArrayList(asignaturasDocente));
                cantidadAsignaturas.setText("Total de Asignaturas: " + asignaturasDocente.size());
            } else {
                if (asignaturas != null) {
                    asignaturas.setAll(asignaturaController.findAllActives());
                } else {
                    activeTableView.setItems(FXCollections.observableArrayList(asignaturaController.findAllActives()));
                }
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Inactivar Asignaturas---------------------------------------------------------------------------------------------
    private void deactivateAsignatura(Asignatura asignatura) {
        Alert confirmacion = ViewManager.getInstance().createAlert("Confirmar Baja", "¿Está seguro de dar de baja a la asignatura?", "Esta acción cambiará el estado de la asignatura a inactiva.");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                asignaturaController.deactivate(asignatura);
                refreshTable();

                ViewManager.getInstance().showSuccess("Éxito", "Asignatura dada de baja correctamente");

            } catch (Exception ex) {
                ViewManager.getInstance().showError("Error", "No se pudo dar de baja a la asignatura: " , ex.getMessage());
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Menú Contextual---------------------------------------------------------------------------------------------------
    public TableRow<Asignatura> createContextMenuRow(TableView<Asignatura> tableView, Stage owner) {
        TableRow<Asignatura> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                Asignatura asignatura = row.getItem();
                row.setContextMenu(createContextMenu(asignatura, owner));
            }
        });
        return row;
    }

    private ContextMenu createContextMenu(Asignatura asignatura, Stage owner) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editarItem = new MenuItem("Editar Asignatura");
        editarItem.setOnAction(e -> openForm(asignatura, owner, "Editar Asignatura"));

        MenuItem eliminarItem = new MenuItem("Dar de Baja Asignatura");
        eliminarItem.setOnAction(e -> deactivateAsignatura(asignatura));

        MenuItem verInstitutoItem = new MenuItem("Ver Detalles del Instituto");
        verInstitutoItem.setOnAction(e -> {
            Instituto instituto = asignatura.getInstituto();
            ViewManager.getInstance().showInformeInstituto(instituto);
        });

        MenuItem verDocenteItem = new MenuItem("Ver Detalles del Docente");
        verDocenteItem.setOnAction(e -> {
            Docente docente = asignatura.getDocente();
            ViewManager.getInstance().showInformeDocente(docente);
        });

        // Separador para organizar el menú
        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        contextMenu.getItems().addAll(
                editarItem,
                eliminarItem,
                separator1,
                verInstitutoItem,
                verDocenteItem
        );

        return contextMenu;
    }
    //------------------------------------------------------------------------------------------------------------------

    // Modifica el método start para evitar crear dos TextField diferentes
    @Override
    public void start(Stage primaryStage) {
        // Configuración inicial
        ViewManager.getInstance().setupInitialStage(primaryStage, "Sistema de Gestión Académica - Asignaturas");

        // Crear contenedores principales
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Gestión de Asignaturas");

        // Crear y configurar la tabla primero
        TableView<Asignatura> tableView = setupTableView(null, null, primaryStage);

        // Crear panel superior con botones
        HBox buttonPanel = createButtonPanel(primaryStage, "Nueva Asignatura", "Registrar Nueva Asignatura");

        // Crear el campo de búsqueda
        VBox searchContainer = ViewManager.getInstance().createSearchContainer();
        Label searchLabel = ViewManager.getInstance().createSearchLabel("Buscar asignatura:");
        TextField searchField = ViewManager.getInstance().createSearchField("Ingrese código, nombre o descripción..."); // Este es el único TextField que usaremos
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

    private List<Asignatura> loadAsignaturasData(Instituto instituto, Docente docente) {
        List<Asignatura> asignaturas;
        if (instituto != null && docente == null) {
            asignaturas = asignaturaController.findActiveAsignaturasByInstitutoId(instituto.getId());
        } else if (docente != null && instituto ==null) {
            asignaturas = asignaturaController.findActiveAsignaturasByDocenteId(docente.getId());
        } else {
            asignaturas = asignaturaController.findAllActives();
        }
        if (activeTableView != null) {
            activeTableView.setItems(FXCollections.observableArrayList(asignaturas));
        }
        return asignaturas;
    }

    private void setupQuantityLabel(int cantidad) {
        cantidadAsignaturas = new Label("Total de Asignaturas: " + cantidad);
        cantidadAsignaturas.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    }

    private void setupContainers(VBox contentContainer, VBox mainContainer, HBox header,
                                 TableView<Asignatura> tableView, Stage asignaturasStage) {
        HBox buttonBox = createCancelButtonBox(asignaturasStage);
        contentContainer.getChildren().addAll(cantidadAsignaturas, tableView);
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

    private TableView<Asignatura> setupTableView(Instituto instituto, Docente docente, Stage asignaturasStage) {
        TableView<Asignatura> tableView = createAsignaturaTable();

        // Guardar referencias
        activeTableView = tableView;
        currentInstituto = instituto;
        currentDocente = docente;

        // Configurar columnas y menú contextual
        configureTableColumns(tableView, instituto, docente);
        tableView.setRowFactory(tv -> createContextMenuRow(tableView, asignaturasStage));

        return tableView;
    }

    // Método para crear la tabla base
    private TableView<Asignatura> createAsignaturaTable() {
        TableView<Asignatura> tableView = new TableView<>();
        tableView.setStyle("-fx-font-size: 14px; -fx-background-color: white; -fx-border-color: #e0e0e0;");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableView;
    }

    // Método para configurar las columnas de la tabla
    private void configureTableColumns(TableView<Asignatura> tableView, Instituto instituto, Docente docente) {
        // Columnas base
        TableColumn<Asignatura, String> codigoCol = createColumn("Código", "codigo", 70);
        TableColumn<Asignatura, String> nombreCol = createColumn("Nombre", "nombre", 200);
        TableColumn<Asignatura, String> descripcionCol = createColumn("Descripción", "descripcion", 250);

        tableView.getColumns().addAll(codigoCol, nombreCol, descripcionCol);

        if (instituto == null) {
            tableView.getColumns().add(createInstitutoColumn(150));
        }

        if (docente == null) {
            tableView.getColumns().add(createDocenteColumn(150));
        }
    }

    // Método para crear una columna genérica
    private TableColumn<Asignatura, String> createColumn(String title, String propertyName, double width) {
        TableColumn<Asignatura, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        column.setPrefWidth(width);
        column.setMinWidth(width);
        return column;
    }

    // Método para crear una columna para docente
    private TableColumn<Asignatura, String> createDocenteColumn(double width) {
        TableColumn<Asignatura, String> docenteCol = new TableColumn<>("Docente Responsable");
        docenteCol.setCellValueFactory(cellData -> {
            Docente docente = cellData.getValue().getDocente();
            if (docente != null) {
                if (docente.getStatus() == Status.activo) {
                    return new SimpleStringProperty(docente.getApellidos() + ", " + docente.getNombres());
                } else {
                    return new SimpleStringProperty("Sin docente activo");
                }
            }
            return new SimpleStringProperty("");
        });
        docenteCol.setStyle("-fx-alignment: CENTER;");
        docenteCol.setPrefWidth(width);
        docenteCol.setMinWidth(width);

        return docenteCol;
    }

    // Método para crear una columna para instituto
    private TableColumn<Asignatura, String> createInstitutoColumn(double width) {
        TableColumn<Asignatura, String> institutoCol = new TableColumn<>("Instituto");
        institutoCol.setCellValueFactory(cellData -> {
            Instituto instituto = cellData.getValue().getInstituto();
            return new SimpleStringProperty(instituto != null ? instituto.getDenominacion() : "");
        });
        institutoCol.setStyle("-fx-alignment: CENTER;");
        institutoCol.setPrefWidth(width);
        institutoCol.setMinWidth(width);
        return institutoCol;
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

    // Método para crear el botón de nueva asignatura
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
    private void setupTableFiltering(TableView<Asignatura> tableView, TextField searchField) {
        // Cargar los datos
        asignaturas = FXCollections.observableArrayList(asignaturaController.findAllActives());
        filteredAsignaturas = new FilteredList<>(asignaturas, p -> true);

        // Configurar el filtro basado en el texto de búsqueda
        setupSearchFilter(searchField);

        // Crear un SortedList basado en la FilteredList (parte clave que faltaba)
        SortedList<Asignatura> sortedData = new SortedList<>(filteredAsignaturas);

        // Vincular el comparador de la lista ordenada con el comparador de la tabla
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        // Usar sortedData como fuente de datos para la tabla (no filteredAsignaturas directamente)
        tableView.setItems(sortedData);
    }

    // Método para configurar el filtro de búsqueda
    private void setupSearchFilter(TextField searchField) {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredAsignaturas.setPredicate(asignatura -> filterAsignatura(asignatura, newValue));
        });
    }

    // Método para filtrar una asignatura según el texto de búsqueda
    private boolean filterAsignatura(Asignatura asignatura, String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        String lowerCaseFilter = filterText.toLowerCase();

        return asignatura.getCodigo().toLowerCase().contains(lowerCaseFilter) ||
                asignatura.getNombre().toLowerCase().contains(lowerCaseFilter) ||
                asignatura.getDescripcion().toLowerCase().contains(lowerCaseFilter) ||
                asignatura.getInstituto().getDenominacion().toLowerCase().contains(lowerCaseFilter) ||
                asignatura.getDocente().getApellidos().toLowerCase().contains(lowerCaseFilter) ||
                asignatura.getDocente().getNombres().toLowerCase().contains(lowerCaseFilter);
    }

    // Método para crear el contenedor de la tabla
    private VBox createTableContainer(TableView<Asignatura> tableView) {
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

        Label totalAsignaturas = createTotalLabel();
        infoPanel.getChildren().add(totalAsignaturas);

        setupTotalLabelListener(totalAsignaturas);

        return infoPanel;
    }

    // Método para crear la etiqueta del total
    private Label createTotalLabel() {
        Label totalAsignaturas = new Label("Total de asignaturas: " + asignaturas.size());
        totalAsignaturas.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return totalAsignaturas;
    }

    // Método para configurar el listener de la etiqueta total
    private void setupTotalLabelListener(Label totalAsignaturas) {
        filteredAsignaturas.addListener((ListChangeListener<Asignatura>) c -> {
            totalAsignaturas.setText("Total de asignaturas mostradas: " + filteredAsignaturas.size());
        });
    }

    private void setupStage(Stage asignaturasStage, VBox mainContainer, TableView<Asignatura> tableView) {
        Scene scene = new Scene(mainContainer);
        asignaturasStage.setScene(scene);
        asignaturasStage.setMinWidth(800);
        asignaturasStage.setMinHeight(500);

        // Configurar el evento de cierre
        asignaturasStage.setOnCloseRequest(event -> cleanupOnClose(tableView));

        asignaturasStage.show();
    }

    private void cleanupOnClose(TableView<Asignatura> tableView) {
        if (activeTableView == tableView) {
            activeTableView = null;
            currentInstituto = null;
            currentDocente = null;
            cantidadAsignaturas = null;
        }
    }

}