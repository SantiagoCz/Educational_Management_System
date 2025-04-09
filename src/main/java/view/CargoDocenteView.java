package view;

import controller.CargoDocenteController;
import controller.DocenteController;
import controller.util.Validation;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class CargoDocenteView extends Stage {

    private CargoDocenteController cargoDocenteController;
    private final Validation validation;

    private TableView<CargoDocente> activeTableView; // Para mantener referencia a la tabla activa
    private static Label cantidadCargoDocentes;
    private Stage activeStage; // Para mantener referencia al Stage de showCargos
    private ObservableList<CargoDocente> cargos;

    public CargoDocenteView() {
        this.cargoDocenteController = new CargoDocenteController();
        this.validation = new Validation();
        this.cargos = FXCollections.observableArrayList();
    }

    //Abrir el formulario de registro/edición---------------------------------------------------------------------------
    public void openForm(Instituto instituto, CargoDocente cargoDocente, Stage owner, String title) {
        Stage formStage = ViewManager.getInstance().createFormStage(activeStage != null ? activeStage : owner, title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader(title);
        GridPane form = ViewManager.getInstance().createFormGrid();

        // Labels del formulario
        Label lblNumeroCargo = ViewManager.getInstance().createLabel("Número de Cargo:");
        Label lblDedicacion = ViewManager.getInstance().createLabel("Dedicación (horas):");
        Label lblDocente = ViewManager.getInstance().createLabel("Docente:");

        // Campos del formulario
        TextField txtNumeroCargo = ViewManager.getInstance().createTextField(cargoDocente != null ? cargoDocente.getNumeroCargo() : "", "Formato: 1234");
        TextField txtDedicacion = ViewManager.getInstance().createTextField(cargoDocente != null ? String.valueOf(cargoDocente.getDedicacionHoras()) : "", "Cantidad de horas semanales");
        ComboBox<Docente> cmbDocente = ViewManager.getInstance().createDocenteComboBox();

        DocenteController docenteController = new DocenteController();
        List<Docente> docentes;
        if (cargoDocente != null) {
            docentes = docenteController.findActiveDocentesByInstitutoId(instituto.getId());
        } else {
            docentes = docenteController.findActiveDocentesByInstitutoIdWithoutCargo(instituto.getId());
        }
        configureDocenteComboBox(cargoDocente, cmbDocente, docentes);

        // Validaciones en tiempo real
        setupRealTimeValidations(cargoDocente, txtNumeroCargo, txtDedicacion);

        // Agregar campos al formulario
        addFieldsToForm(form, lblNumeroCargo, txtNumeroCargo, lblDedicacion, txtDedicacion, lblDocente, cmbDocente);

        // Botones
        HBox buttonBox = createButtonBox(formStage, instituto, cargoDocente, txtNumeroCargo, txtDedicacion, cmbDocente);

        // Configurar y mostrar la ventana
        configureAndShowStage(formStage, mainContainer, header, form, buttonBox);
    }
    //------------------------------------------------------------------------------------------------------------------

    private void configureDocenteComboBox(CargoDocente cargoDocente, ComboBox<Docente> cmbDocente, List<Docente> docentes) {
        if (docentes.isEmpty()) {
            // Si no hay docentes activos, agregar un elemento especial
            Docente noDocentesItem = new Docente();
            noDocentesItem.setLegajo("N/A");
            noDocentesItem.setApellidos("No se");
            noDocentesItem.setNombres("encontraron docentes para el Instituto");

            cmbDocente.setItems(FXCollections.observableArrayList(noDocentesItem));
        } else {
            // Si hay docentes, proceder normalmente
            cmbDocente.setItems(FXCollections.observableArrayList(docentes));
        }

        cmbDocente.setCellFactory(lv -> new ListCell<Docente>() {
            @Override
            protected void updateItem(Docente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Verificar si es el elemento especial de "No hay docentes"
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

        // Establecer el valor inicial si estamos en modo edición
        setInitialDocenteValue(cargoDocente, cmbDocente, docentes);
    }

    // Método para establecer el valor inicial del ComboBox de docente
    private void setInitialDocenteValue(CargoDocente cargoDocente, ComboBox<Docente> cmbDocente, List<Docente> docentes) {
        if (cargoDocente != null) {
            Docente docenteActual = cargoDocente.getDocente();
            Docente docenteEncontrado = docentes.stream()
                    .filter(i -> i.getId().equals(docenteActual.getId()))
                    .findFirst()
                    .orElse(null);

            // Establecer el docente seleccionado en el ComboBox
            cmbDocente.setValue(docenteEncontrado);
        }
    }

    // Validaciones en tiempo real--------------------------------------------------------------------------------------
    private void setupRealTimeValidations(CargoDocente cargoDocente, TextField txtNumeroCargo, TextField txtDedicacion) {
        txtNumeroCargo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!validation.isValidNumeroCargo(newValue)) {
                    ViewManager.getInstance().setErrorStyle(txtNumeroCargo);
                } else if (validation.isNumeroCargoDuplicated(newValue, cargoDocente != null ? cargoDocente.getId() : null)) {
                    ViewManager.getInstance().setErrorStyle(txtNumeroCargo);
                } else {
                    ViewManager.getInstance().resetStyle(txtNumeroCargo);
                }
            } else {
                ViewManager.getInstance().resetStyle(txtNumeroCargo);
                txtNumeroCargo.setTooltip(null);
            }
        });

        txtDedicacion.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !validation.isValidHoras(newValue)) {
                ViewManager.getInstance().setErrorStyle(txtDedicacion);
            } else {
                ViewManager.getInstance().resetStyle(txtDedicacion);
            }
        });
    }
    //------------------------------------------------------------------------------------------------------------------

    // Configuración del formulario-------------------------------------------------------------------------------------
    private void addFieldsToForm(GridPane form, Label lblNumeroCargo, TextField txtNumeroCargo, Label lblDedicacion, TextField txtDedicacion,
                                 Label lblDocente, ComboBox<Docente> cmbDocente) {
        // Agregar campos al formulario
        form.addRow(0, lblNumeroCargo, txtNumeroCargo);
        form.addRow(1, lblDedicacion, txtDedicacion);
        form.addRow(2, lblDocente, cmbDocente);
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para crear botones---------------------------------------------------------------------------------------
    private HBox createButtonBox(Stage formStage, Instituto instituto, CargoDocente cargoDocente,
                                 TextField txtNumeroCargo, TextField txtDedicacion, ComboBox<Docente> cmbDocente) {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20px;");

        Button btnCancelar = createCancelButton(formStage);
        Button btnGuardar = createSaveButton(formStage, instituto, cargoDocente, txtNumeroCargo, txtDedicacion, cmbDocente);

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

    private Button createSaveButton(Stage formStage, Instituto instituto, CargoDocente cargoDocente,
                                    TextField txtNumeroCargo, TextField txtDedicacion, ComboBox<Docente> cmbDocente) {
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> handleSaveAction(formStage, instituto, cargoDocente, txtNumeroCargo, txtDedicacion, cmbDocente));
        return btnGuardar;
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para validación y guardado-------------------------------------------------------------------------------
    private void handleSaveAction(Stage formStage, Instituto instituto, CargoDocente cargoDocente,
                                  TextField txtNumeroCargo, TextField txtDedicacion, ComboBox<Docente> cmbDocente) {
        if (validateFields(instituto, cargoDocente, txtNumeroCargo, txtDedicacion, cmbDocente)) {
            saveCargoDocente(formStage, instituto, cargoDocente, txtNumeroCargo, txtDedicacion, cmbDocente);
        }
    }

    private boolean validateFields(Instituto instituto, CargoDocente cargoDocente,
                                   TextField txtNumeroCargo, TextField txtDedicacion, ComboBox<Docente> cmbDocente) {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        String numeroCargo = txtNumeroCargo.getText();
        if (numeroCargo.isEmpty() || !validation.isValidNumeroCargo(numeroCargo)) {
            isValid = false;
            errorMessage.append("- El número de cargo debe contener 4 dígitos\n");
            ViewManager.getInstance().setErrorStyle(txtNumeroCargo);
        } else if (validation.isNumeroCargoDuplicated(numeroCargo, cargoDocente != null ? cargoDocente.getId() : null)) {
            isValid = false;
            errorMessage.append("- Ya existe un cargo docente registrado con este número\n");
            ViewManager.getInstance().setErrorStyle(txtNumeroCargo);
        }

        if (txtDedicacion.getText().isEmpty() || !validation.isValidHoras(txtDedicacion.getText())) {
            isValid = false;
            errorMessage.append("- La dedicación de horas debe ser un numero entero entre 1 y 40\n");
            ViewManager.getInstance().setErrorStyle(txtDedicacion);
        }

        // Validación específica para cuando no hay docentes
        if (cmbDocente.getValue() != null && "N/A".equals(cmbDocente.getValue().getLegajo())) {
            isValid = false;
            errorMessage.append("No es posible registrar cargos docentes porque el instituto: "
                    + instituto.getDenominacion()
                    + " aún no tiene docentes asignados\n");
        } else if (cmbDocente.getValue() == null) {
            isValid = false;
            errorMessage.append("- Debe seleccionar un docente\n");
        }

        if (!isValid) {
            ViewManager.getInstance().showError("Error de validación", "Por favor corrija los siguientes errores: ", errorMessage.toString());
        }
        return isValid;
    }

    private void saveCargoDocente(Stage formStage, Instituto instituto, CargoDocente cargoDocente,
                               TextField txtNumeroCargo, TextField txtDedicacion, ComboBox<Docente> cmbDocente) {
        try {
            CargoDocente cargoDocenteToSave = cargoDocente != null ? cargoDocente : new CargoDocente();
            cargoDocenteToSave.setNumeroCargo(txtNumeroCargo.getText());
            cargoDocenteToSave.setDedicacionHoras(Integer.parseInt(txtDedicacion.getText()));
            cargoDocenteToSave.setInstituto(instituto);
            cargoDocenteToSave.setDocente(cmbDocente.getValue());

            if (cargoDocente == null) {
                cargoDocenteToSave.setStatus(Status.activo);
                cargoDocenteController.save(cargoDocenteToSave);
            } else {
                cargoDocenteController.edit(cargoDocenteToSave);
            }

            handleSuccessfulSave(formStage, instituto, cargoDocente);
        } catch (Exception ex) {
            ViewManager.getInstance().showError("Error al guardar", "Ocurrió un error al intentar guardar el docente: ", ex.getMessage());
        }
    }

    private void handleSuccessfulSave(Stage formStage, Instituto instituto, CargoDocente cargoDocente) {
        ViewManager.getInstance().showSuccess("Éxito",
                cargoDocente == null ? "Cargo Docente registrado correctamente" : "Cargo Docente actualizado correctamente");
        formStage.close();
        refreshTable(instituto);
    }
    //------------------------------------------------------------------------------------------------------------------

    // Método para configurar y mostrar la ventana ---------------------------------------------------------------------
    private void configureAndShowStage(Stage formStage, VBox mainContainer, HBox header, GridPane form, HBox buttonBox) {
        mainContainer.getChildren().addAll(header, form, buttonBox);

        // Configurar y mostrar la ventana
        setupStage(formStage, mainContainer, 800, 500);
    }
    //------------------------------------------------------------------------------------------------------------------

    //Inactivar cargos docentes-----------------------------------------------------------------------------------------
    private void deactivateCargoDocente(CargoDocente cargoDocente) {
        Alert confirmacion = ViewManager.getInstance().createAlert("Confirmar Baja", "¿Está seguro de dar de baja el cargo docente?", "Esta acción cambiará el estado del cargo docente a inactivo.");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cargoDocenteController.deactivate(cargoDocente);
                refreshTable(cargoDocente.getInstituto());
                ViewManager.getInstance().showSuccess("Éxito", "Cargo Docente dado de baja correctamente");
            } catch (Exception ex) {
                ViewManager.getInstance().showError("Error", "No se pudo dar de baja el cargo docente: ", ex.getMessage());
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Actualizar Tabla--------------------------------------------------------------------------------------------------
    private void refreshTable(Instituto instituto) {
        // Primero actualizamos la tabla de institutos si está visible
        if (InstitutoView.activeTableView != null) {
            InstitutoView.refreshTable();
        }

        // Luego actualizamos la tabla de docentes según el contexto
        if (activeTableView != null) {
            if (instituto != null) {
                List<CargoDocente> cargos = cargoDocenteController.findActiveCargosByInstitutoId(instituto.getId());
                activeTableView.setItems(FXCollections.observableArrayList(cargos));
                cantidadCargoDocentes.setText("Total de cargos docentes: " + cargos.size());
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Menú Contextual---------------------------------------------------------------------------------------------------
    public TableRow<CargoDocente> createContextMenuRow(TableView<CargoDocente> tableView, Instituto instituto, Stage owner) {
        TableRow<CargoDocente> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                CargoDocente cargoDocente = row.getItem();
                row.setContextMenu(createContextMenu(cargoDocente, instituto, owner));
            }
        });
        return row;
    }

    private ContextMenu createContextMenu(CargoDocente cargoDocente, Instituto instituto, Stage owner) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editarItem = new MenuItem("Editar Cargo Docente");
        editarItem.setOnAction(e -> {
            openForm(instituto, cargoDocente, owner, "Editar Cargo Docente");
        });

        MenuItem eliminarItem = new MenuItem("Dar de Baja Cargo Docente");
        eliminarItem.setOnAction(e ->
                deactivateCargoDocente(cargoDocente)
        );

        MenuItem verDocenteItem = new MenuItem("Ver Detalles del Docente");
        verDocenteItem.setOnAction(e -> {
            ViewManager.getInstance().showInformeDocente(cargoDocente.getDocente());
        });

        // Separadores para organizar el menú
        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        contextMenu.getItems().addAll(
                editarItem,
                eliminarItem,
                separator1,
                verDocenteItem
        );

        return contextMenu;
    }
    //------------------------------------------------------------------------------------------------------------------

    // Mostrar Cargos del Instituto-------------------------------------------------------------------------------------
    public void showCargos(Instituto instituto, Stage owner, String title) {
        Stage cargosStage = ViewManager.getInstance().createNewStage(title);
        this.activeStage = cargosStage; // Guardar referencia al Stage actual
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Cargos Docentes - " + instituto.getDenominacion());
        VBox contentContainer = ViewManager.getInstance().createContentContainer();

        // Crear y configurar la tabla
        TableView<CargoDocente> tableView = setupTableView(owner, instituto);

        // Configurar el label de cantidad y obtener los datos
        List<CargoDocente> cargos = loadCargosData(instituto);
        setupQuantityLabel(cargos.size());

        // Configurar contenedores
        setupContainers(contentContainer, mainContainer, header, tableView, cargosStage);

        // Configurar y mostrar la ventana
        setupStage(cargosStage, mainContainer, 900, 500);
    }

    // Método para crear y configurar la tabla
    private TableView<CargoDocente> setupTableView(Stage owner, Instituto instituto) {
        TableView<CargoDocente> tableView = createCargoDocenteTable();

        // Guardar referencias
        activeTableView = tableView;

        // Configurar columnas y menú contextual
        configureTableColumns(tableView);
        tableView.setRowFactory(tv -> createContextMenuRow(tableView, instituto, owner));

        return tableView;
    }

    // Método para crear la tabla base
    private TableView<CargoDocente> createCargoDocenteTable() {
        TableView<CargoDocente> tableView = new TableView<>();
        tableView.setStyle("-fx-font-size: 14px; -fx-background-color: white; -fx-border-color: #e0e0e0;");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableView;
    }

    // Método para configurar las columnas de la tabla
    private void configureTableColumns(TableView<CargoDocente> tableView) {
        // Columnas base
        TableColumn<CargoDocente, String> numeroCol = createColumn("Número de Cargo", "numeroCargo", 150);
        TableColumn<CargoDocente, String> docenteCol = createDocenteColumn(300);
        TableColumn<CargoDocente, String> horasCol = createColumn("Dedicación (hs)", "dedicacionHoras", 150);

        tableView.getColumns().addAll(numeroCol, docenteCol, horasCol);

    }

    // Método para crear una columna genérica
    private <T> TableColumn<CargoDocente, T> createColumn(String title, String propertyName, double width) {
        TableColumn<CargoDocente, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        column.setPrefWidth(width);
        column.setMinWidth(width);
        return column;
    }

    // Método para crear una columna para docente
    private TableColumn<CargoDocente, String> createDocenteColumn(double width) {
        TableColumn<CargoDocente, String> docenteCol = new TableColumn<>("Docente");
        docenteCol.setCellValueFactory(cellData -> {
            Docente docente = cellData.getValue().getDocente();
            return new SimpleStringProperty(docente.getApellidos() + ", " + docente.getNombres());
        });
        docenteCol.setStyle("-fx-alignment: CENTER;");
        docenteCol.setPrefWidth(width);
        docenteCol.setMinWidth(width);

        return docenteCol;
    }

    private List<CargoDocente> loadCargosData(Instituto instituto) {
        List<CargoDocente> cargos = cargoDocenteController.findActiveCargosByInstitutoId(instituto.getId());
        if (activeTableView != null) {
            activeTableView.setItems(FXCollections.observableArrayList(cargos));
        }
        return cargos;
    }

    private void setupQuantityLabel(int cantidad) {
        cantidadCargoDocentes = new Label("Total de cargos docente: " + cantidad);
        cantidadCargoDocentes.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    }

    private void setupContainers(VBox contentContainer, VBox mainContainer, HBox header,
                                 TableView<CargoDocente> tableView, Stage institutosStage) {
        HBox buttonBox = createCancelButtonBox(institutosStage);
        contentContainer.getChildren().addAll(cantidadCargoDocentes, tableView);
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

    private void setupStage(Stage cargosStage, VBox mainContainer, double width, double height) {
        Scene scene = new Scene(mainContainer);
        cargosStage.setScene(scene);
        cargosStage.setMinWidth(width);
        cargosStage.setMinHeight(height);
        cargosStage.show();
        cargosStage.setOnCloseRequest(event -> {
            activeStage = null;
            activeTableView = null;
            cantidadCargoDocentes = null;
        });
    }
    //------------------------------------------------------------------------------------------------------------------
}