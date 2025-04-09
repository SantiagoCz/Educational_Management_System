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

import java.util.List;
import java.util.Optional;

public class InstitutoView extends Application {

    private final InstitutoController institutoController;
    private final Validation validation;

    static TableView<Instituto> activeTableView;
    private static Docente currentDocente; // Para mantener referencia del docente actual
    private static Label cantidadInstitutos;
    private static ObservableList<Instituto> institutos;
    private static FilteredList<Instituto> filteredInstitutos;

    public InstitutoView() {
        this.institutoController = new InstitutoController();
        this.validation = new Validation();
        this.institutos = FXCollections.observableArrayList();
    }

    //Abrir el formulario de registro/edición---------------------------------------------------------------------------
    private void openForm(Instituto instituto, Stage owner, String title) {
        Stage formStage = ViewManager.getInstance().createFormStage(owner, title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader(title);
        GridPane form = ViewManager.getInstance().createFormGrid();

        // Labels del formulario
        Label lblCodigo = ViewManager.getInstance().createLabel("Código:");
        Label lblDenominacion = ViewManager.getInstance().createLabel("Denominación:");

        // Campos del formulario
        TextField txtCodigo = ViewManager.getInstance().createTextField(instituto != null ? instituto.getCodigo() : "", "Formato: AA123");
        TextField txtDenominacion = ViewManager.getInstance().createTextField(instituto != null ? instituto.getDenominacion() : "", "Ingrese la denominación");

        // Validaciones en tiempo real
        setupRealTimeValidations(instituto, txtCodigo, txtDenominacion);

        // Agregar campos al formulario
        addFieldsToForm(form, lblCodigo, txtCodigo, lblDenominacion, txtDenominacion);

        // Botones
        HBox buttonBox = createButtonBox(formStage, instituto, txtCodigo, txtDenominacion);

        // Configurar y mostrar la ventana
        configureAndShowStage(formStage, mainContainer, header, form, buttonBox);
    }
    //------------------------------------------------------------------------------------------------------------------

    // Validaciones en tiempo real--------------------------------------------------------------------------------------
    private void setupRealTimeValidations(Instituto instituto, TextField txtCodigo, TextField txtDenominacion) {
        // Validación del código
        txtCodigo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!validation.isValidCodigoInstituto(newValue)) {
                    ViewManager.getInstance().setErrorStyle(txtCodigo);
                } else if (validation.isCodigoInstitutoDuplicated(newValue, instituto != null ? instituto.getId() : null)) {
                    ViewManager.getInstance().setErrorStyle(txtCodigo);
                } else {
                    ViewManager.getInstance().resetStyle(txtCodigo);
                }
            } else {
                ViewManager.getInstance().resetStyle(txtCodigo);
                txtCodigo.setTooltip(null);
            }
        });

        // Validación de la denominación
        txtDenominacion.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !validation.isValidTextAndNumbers(newValue)) {
                ViewManager.getInstance().setErrorStyle(txtDenominacion);
            } else {
                ViewManager.getInstance().resetStyle(txtDenominacion);
            }
        });
    }
    //------------------------------------------------------------------------------------------------------------------

    // Configuración del formulario-------------------------------------------------------------------------------------
    private void addFieldsToForm(GridPane form, Label lblCodigo, TextField txtCodigo, Label lblDenominacion, TextField txtDenominacion) {
        // Agregar campos al formulario
        form.addRow(0, lblCodigo, txtCodigo);
        form.addRow(1, lblDenominacion, txtDenominacion);
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para crear botones---------------------------------------------------------------------------------------
    private HBox createButtonBox(Stage formStage, Instituto instituto, TextField txtCodigo, TextField txtDenominacion) {
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20px;");

        Button btnCancelar = createCancelButton(formStage);
        Button btnGuardar = createSaveButton(formStage, instituto, txtCodigo, txtDenominacion);

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

    private Button createSaveButton(Stage formStage, Instituto instituto, TextField txtCodigo, TextField txtDenominacion) {
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 10px 30px; -fx-font-weight: bold;");
        btnGuardar.setOnAction(e -> handleSaveAction(formStage, instituto, txtCodigo, txtDenominacion));
        return btnGuardar;
    }
    //------------------------------------------------------------------------------------------------------------------

    // Métodos para validación y guardado-------------------------------------------------------------------------------
    private void handleSaveAction(Stage formStage, Instituto instituto, TextField txtCodigo, TextField txtDenominacion) {
        if (validateFields(instituto, txtCodigo, txtDenominacion)) {
            saveInstituto(formStage, instituto, txtCodigo, txtDenominacion);
        }
    }

    private boolean validateFields(Instituto instituto, TextField txtCodigo, TextField txtDenominacion) {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        if (txtCodigo.getText().isEmpty() || !validation.isValidCodigoInstituto(txtCodigo.getText())) {
            isValid = false;
            errorMessage.append("- El código debe tener el formato AA123\n");
            ViewManager.getInstance().setErrorStyle(txtCodigo);
        } else if (validation.isCodigoInstitutoDuplicated(txtCodigo.getText(), instituto != null ? instituto.getId() : null)) {
            isValid = false;
            errorMessage.append("- Ya existe un instituto registrado con este código\n");
            ViewManager.getInstance().setErrorStyle(txtCodigo);
        }

        if (txtDenominacion.getText().isEmpty() || !validation.isValidTextAndNumbers(txtDenominacion.getText())) {
            isValid = false;
            errorMessage.append("- La denominación solo puede contener texto y números\n");
            ViewManager.getInstance().setErrorStyle(txtDenominacion);
        }

        if (!isValid) {
            ViewManager.getInstance().showError("Error de validación", "Por favor corrija los siguientes errores: ", errorMessage.toString());
        }
        return isValid;
    }

    private void saveInstituto(Stage formStage, Instituto instituto, TextField txtCodigo, TextField txtDenominacion) {
        try {
            Instituto institutoToSave = instituto != null ? instituto : new Instituto();
            institutoToSave.setCodigo(txtCodigo.getText());
            institutoToSave.setDenominacion(txtDenominacion.getText());

            if (instituto == null) {
                institutoToSave.setStatus(Status.activo);
                institutoController.save(institutoToSave);
            } else {
                institutoController.edit(institutoToSave);
            }

            handleSuccessfulSave(formStage, instituto);
        } catch (Exception ex) {
            ViewManager.getInstance().showError("Error al guardar", "Ocurrió un error al intentar guardar el docente: ", ex.getMessage());
        }
    }

    private void handleSuccessfulSave(Stage formStage, Instituto instituto) {
        ViewManager.getInstance().showSuccess("Éxito",
                instituto == null ? "Instituto registrado correctamente" : "Instituto actualizado correctamente");
        formStage.close();
        refreshTable();
    }
    //------------------------------------------------------------------------------------------------------------------

    // Método para configurar y mostrar la ventana ---------------------------------------------------------------------
    private void configureAndShowStage(Stage formStage, VBox mainContainer, HBox header, GridPane form, HBox buttonBox) {
        mainContainer.getChildren().addAll(header, form, buttonBox);

        Scene scene = new Scene(mainContainer);
        formStage.setScene(scene);
        formStage.setMinWidth(800);
        formStage.setMinHeight(700);
        formStage.show();
    }
    //------------------------------------------------------------------------------------------------------------------

    //Mostrar Docentes del Instituto------------------------------------------------------------------------------------
    public void showInstitutos(Docente docente, Stage owner, String title) {
        Stage institutoStage = ViewManager.getInstance().createNewStage(title);
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Institutos de docente - " + docente.getApellidos() + ", " + docente.getNombres());
        VBox contentContainer = ViewManager.getInstance().createContentContainer();

        // Crear y configurar la tabla
        TableView<Instituto> tableView = setupTableView(owner, docente);

        // Configurar el label de cantidad y obtener los datos
        List<Instituto> institutos = loadInstitutosData(docente);
        setupQuantityLabel(institutos.size());

        // Configurar contenedores
        setupContainers(contentContainer, mainContainer, header, tableView, institutoStage);

        // Configurar y mostrar la ventana
        setupStage(institutoStage, mainContainer, tableView);
    }
    //------------------------------------------------------------------------------------------------------------------

    //Activar e Inactivar Intituto--------------------------------------------------------------------------------------
    private void activateInstituto(Instituto instituto) {
        Alert confirmacion = ViewManager.getInstance().createAlert("Confirmar Alta", "¿Está seguro de dar de alta al instituto nuevamente?", "Esta acción cambiará el estado del instituto a activo.");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                institutoController.activate(instituto);
                refreshTable();
                ViewManager.getInstance().showSuccess("Éxito", "Instituto dado de alta correctamente");
            } catch (Exception ex) {
                ViewManager.getInstance().showError("Error", "No se pudo dar de alta al instituto: ", ex.getMessage());
            }
        }
    }

    private void deactivateInstituto(Instituto instituto) {
        try {
            boolean hasAssociatedData = institutoController.hasAssociatedData(instituto);
            if (hasAssociatedData) {
                Alert confirmacion = ViewManager.getInstance().createAlert("Confirmar Baja", "¿Está seguro de dar de baja al instituto?", "Este instituto tiene datos asociados. ¿Está seguro que desea eliminarlo? Se eliminarán todos los datos relacionados.");

                Optional<ButtonType> result = confirmacion.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        institutoController.deactivate(instituto);
                        refreshTable();
                        ViewManager.getInstance().showSuccess("Éxito", "Instituto dado de baja correctamente");

                    } catch (Exception ex) {
                        ViewManager.getInstance().showError("Error", "No se pudo dar de baja al instituto: ", ex.getMessage());
                    }
                }
            } else {
                institutoController.deactivate(instituto);
                refreshTable();

                ViewManager.getInstance().showSuccess("Éxito", "Instituto dado de baja correctamente");
            }
        } catch (Exception ex) {
            ViewManager.getInstance().showError("Error", "No se pudo verificar los datos asociados: ", ex.getMessage());
        }

    }
    //------------------------------------------------------------------------------------------------------------------

    //Desvincular Intituto-Docente--------------------------------------------------------------------------------------
    public void unlinkInstitutoFromDocente(Instituto instituto, Docente docente) {
        Alert confirmacion = ViewManager.getInstance().createAlert("Confirmar Desvinculación", "¿Está seguro que desea desvincular el instituto del docente?", "Este instituto y el docente compartendatos asociados.");

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                institutoController.unlinkInstitutoFromDocente(instituto, docente);
                refreshTable();

                ViewManager.getInstance().showSuccess("Éxito", "Instituto desvinculado del docente correctamente");

            } catch (Exception ex) {
                ViewManager.getInstance().showError("Error", "No se pudo desvincular el instituto del docente: ", ex.getMessage());
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Actualizar tabla--------------------------------------------------------------------------------------------------
    public static void refreshTable() {
        if (activeTableView != null) {
            if (currentDocente != null) {
                InstitutoController institutoController = new InstitutoController();
                List<Instituto> institutosDocente = institutoController.findActiveInstitutosByDocenteId(currentDocente.getId());
                activeTableView.setItems(FXCollections.observableArrayList(institutosDocente));
                cantidadInstitutos.setText("Total de Institutos: " + institutosDocente.size());
            } else {
            InstitutoController institutoController = new InstitutoController();
            activeTableView.setItems(FXCollections.observableArrayList(institutoController.findAll()));
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------

    //Informe instituto-------------------------------------------------------------------------------------------------
    public void showInformeInstituto(Instituto instituto) {
        Stage informeStage = ViewManager.getInstance().createInformeStage("Informe del Instituto - " + instituto.getDenominacion());

        // Contenedor principal con scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox mainContainer = ViewManager.getInstance().createInformeMainContainer();

        // Título principal
        Label titleLabel = new Label("INFORMACIÓN DEL INSTITUTO");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        mainContainer.getChildren().add(titleLabel);

        // Datos del Instituto
        VBox datosInstituto = new VBox(10);
        datosInstituto.setStyle("-fx-padding: 10px; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        ViewManager.getInstance().addInfoRow(datosInstituto, "Denominación:", instituto.getDenominacion());
        ViewManager.getInstance().addInfoRow(datosInstituto, "Código:", instituto.getCodigo());
        ViewManager.getInstance().addInfoRow(datosInstituto, "Estado:", instituto.getStatus().toString());

        mainContainer.getChildren().add(datosInstituto);

        // Sección de Docentes
        Label docentesTitle = new Label("STAFF DOCENTE");
        docentesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #283593;");
        mainContainer.getChildren().add(docentesTitle);

        DocenteController docenteController = new DocenteController();
        List<Docente> docentes = docenteController.findActiveDocentesByInstitutoId(instituto.getId());

        // Por cada docente
        for (Docente docente : docentes) {
            VBox docenteBox = ViewManager.getInstance().createDocenteBoxForInstituto(docente, instituto);
            mainContainer.getChildren().add(docenteBox);
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

    //Menú Contextual---------------------------------------------------------------------------------------------------
    public TableRow<Instituto> createContextMenuRow(TableView<Instituto> tableView, Stage owner) {
        TableRow<Instituto> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                Instituto instituto = row.getItem();
                row.setContextMenu(createContextMenu(instituto, owner));
            }
        });
        return row;
    }

    private ContextMenu createContextMenu(Instituto instituto, Stage owner) {
        ContextMenu contextMenu = new ContextMenu();

        if (instituto.getStatus() == Status.activo) {
            MenuItem informeItem = new MenuItem("Ver Detalles del Instituto");
            informeItem.setOnAction(e ->
                    showInformeInstituto(instituto)
            );

            MenuItem editarItem = new MenuItem("Editar Instituto");
            editarItem.setOnAction(e ->
                    openForm(instituto, owner, "Editar Instituto")
            );

            MenuItem eliminarItem = new MenuItem("Dar de Baja Instituto");
            eliminarItem.setOnAction(e ->
                    deactivateInstituto(instituto)
            );

            MenuItem verAsignaturasItem = new MenuItem("Ver Asignaturas");
            verAsignaturasItem.setOnAction(e ->
                    ViewManager.getInstance().showAsignaturas(instituto, owner, "Asignaturas del Instituto")
            );

            MenuItem verDocentesItem = new MenuItem("Ver Docentes");
            verDocentesItem.setOnAction(e ->
                    ViewManager.getInstance().showDocentes(instituto, owner, "Docentes del Instituto")
            );

            MenuItem verCargosItem = new MenuItem("Ver Cargos Docentes");
            verCargosItem.setOnAction(e ->
                    ViewManager.getInstance().showCargos(instituto, owner, "Cargos Docentes")
            );

            MenuItem registrarAsignaturaItem = new MenuItem("Registrar Asignatura");
            registrarAsignaturaItem.setOnAction(e ->
                    ViewManager.getInstance().openAsignaturaForm(owner, "Registrar Nueva Asignatura", instituto)
            );

            MenuItem registrarCargoItem = new MenuItem("Registrar Cargo Docente");
            registrarCargoItem.setOnAction(e ->
                    ViewManager.getInstance().openCargoDocenteForm(instituto, null, owner, "Registrar nuevo Cargo Docente")
            );

            // Separadores para organizar el menú
            SeparatorMenuItem separator1 = new SeparatorMenuItem();
            SeparatorMenuItem separator2 = new SeparatorMenuItem();

            // Agregar todos los items al menú contextual
            contextMenu.getItems().addAll(
                    informeItem,
                    editarItem,
                    eliminarItem,
                    separator1,
                    verAsignaturasItem,
                    verDocentesItem,
                    verCargosItem,
                    separator2,
                    registrarAsignaturaItem,
                    registrarCargoItem
            );
        } else {
            MenuItem altaItem = new MenuItem("Dar de Alta Instituto");
            altaItem.setOnAction(e -> activateInstituto(instituto));
            contextMenu.getItems().add(altaItem);
        }

        return contextMenu;
    }
    //------------------------------------------------------------------------------------------------------------------

    //Menú Contextual---------------------------------------------------------------------------------------------------
    public TableRow<Instituto> createContextMenuRow(TableView<Instituto> tableView, Docente docente, Stage owner) {
        TableRow<Instituto> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                Instituto instituto = row.getItem();
                row.setContextMenu(createContextMenu(instituto, docente, owner));
            }
        });
        return row;
    }

    public ContextMenu createContextMenu(Instituto instituto, Docente docente, Stage owner) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem desvincularItem = new MenuItem("Desvincular Instituto-Docente");
        desvincularItem.setOnAction(e -> unlinkInstitutoFromDocente(instituto, docente));

        MenuItem verInstitutoItem = new MenuItem("Ver Detalles del Instituto");
        verInstitutoItem.setOnAction(e -> {
            showInformeInstituto(instituto);
        });


        // Separadores para organizar el menú
        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        // Agregar todos los items al menú contextual
        contextMenu.getItems().addAll(
                desvincularItem,
                separator1,
                verInstitutoItem
        );
        return contextMenu;
    }
    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void start(Stage primaryStage) {
        // Configuración inicial
        ViewManager.getInstance().setupInitialStage(primaryStage, "Sistema de Gestión Académica - Institutos");

        // Crear contenedores principales
        VBox mainContainer = ViewManager.getInstance().createMainContainer();
        HBox header = ViewManager.getInstance().createHeader("Gestión de Institutos");

        // Crear y configurar la tabla
        TableView<Instituto> tableView = setupTableView(primaryStage, null);

        // Crear panel superior con botones
        HBox buttonPanel = createButtonPanel(primaryStage, "Nuevo Instituto", "Registrar Nuevo Instituto");

        // Crear el campo de búsqueda
        VBox searchContainer = ViewManager.getInstance().createSearchContainer();
        Label searchLabel = ViewManager.getInstance().createSearchLabel("Buscar instituto:");
        TextField searchField = ViewManager.getInstance().createSearchField("Ingrese código o denominación...");
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

    private List<Instituto> loadInstitutosData(Docente docente) {
        List<Instituto> institutos;
        if (docente != null) {
            institutos = institutoController.findActiveInstitutosByDocenteId(docente.getId());
        } else {
            institutos = institutoController.findAll();
        }
        if (activeTableView != null) {
            activeTableView.setItems(FXCollections.observableArrayList(institutos));
        }
        return institutos;
    }

    private void setupQuantityLabel(int cantidad) {
        cantidadInstitutos = new Label("Total de institutos: " + cantidad);
        cantidadInstitutos.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
    }

    private void setupContainers(VBox contentContainer, VBox mainContainer, HBox header,
                                 TableView<Instituto> tableView, Stage institutosStage) {
        HBox buttonBox = createCancelButtonBox(institutosStage);
        contentContainer.getChildren().addAll(cantidadInstitutos, tableView);
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
    private TableView<Instituto> setupTableView(Stage primaryStage, Docente docente) {
        TableView<Instituto> tableView = createInstitutoTable();

        // Guardar referencias
        activeTableView = tableView;
        currentDocente = docente;

        // Configurar columnas y menú contextual
        configureTableColumns(tableView, docente);
        if(currentDocente != null) {
            tableView.setRowFactory(tv -> createContextMenuRow(tableView, docente, primaryStage));
        } else {
            tableView.setRowFactory(tv -> createContextMenuRow(tableView, primaryStage));
        }

        return tableView;
    }

    // Método para crear la tabla base
    private TableView<Instituto> createInstitutoTable() {
        TableView<Instituto> tableView = new TableView<>();
        tableView.setStyle("-fx-font-size: 14px; -fx-background-color: white; -fx-border-color: #e0e0e0;");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableView;
    }

    // Método para configurar las columnas de la tabla
    private void configureTableColumns(TableView<Instituto> tableView, Docente docente) {
        // Columnas base
        TableColumn<Instituto, String> codigoCol = createColumn("Código", "codigo", 80);
        TableColumn<Instituto, String> denominacionCol = createColumn("Denominación", "denominacion", 100);
        tableView.getColumns().addAll(codigoCol, denominacionCol, createCargosColumn());

        // Si no hay docente, agregar las columnas extra
        if (docente == null) {
            tableView.getColumns().addAll(createDocentesColumn(), createAsignaturasColumn(), createColumn("Estado", "status", 80)
            );
        }
    }

    // Método para crear una columna genérica
    private <T> TableColumn<Instituto, T> createColumn(String title, String propertyName, double width) {
        TableColumn<Instituto, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        column.setPrefWidth(width);
        column.setMinWidth(width);
        return column;
    }

    // Método para crear la columna de cantidad de cargos
    private TableColumn<Instituto, Integer> createCargosColumn() {
        TableColumn<Instituto, Integer> cargosCol = new TableColumn<>("Cant. Cargos Docentes");
        cargosCol.setCellValueFactory(cellData -> {
            int cantidadCargos = institutoController.countCargosDocentes(cellData.getValue());
            return new SimpleIntegerProperty(cantidadCargos).asObject();
        });
        cargosCol.setStyle("-fx-alignment: CENTER;");
        cargosCol.setPrefWidth(60);
        cargosCol.setMinWidth(60);
        return cargosCol;
    }

    // Método para crear la columna de cantidad de docentes
    private TableColumn<Instituto, Integer> createDocentesColumn() {
        TableColumn<Instituto, Integer> docentesCol = new TableColumn<>("Cant. Docentes");
        docentesCol.setCellValueFactory(cellData -> {
            int cantidadDocentes = institutoController.countDocentes(cellData.getValue());
            return new SimpleIntegerProperty(cantidadDocentes).asObject();
        });
        docentesCol.setStyle("-fx-alignment: CENTER;");
        docentesCol.setPrefWidth(60);
        docentesCol.setMinWidth(60);
        return docentesCol;
    }

    // Método para crear la columna de cantidad de asignaturas
    private TableColumn<Instituto, Integer> createAsignaturasColumn() {
        TableColumn<Instituto, Integer> asignaturasCol = new TableColumn<>("Cant. Asignaturas");
        asignaturasCol.setCellValueFactory(cellData -> {
            int cantidadAsignaturas = institutoController.countAsignaturas(cellData.getValue());
            return new SimpleIntegerProperty(cantidadAsignaturas).asObject();
        });
        asignaturasCol.setStyle("-fx-alignment: CENTER;");
        asignaturasCol.setPrefWidth(60);
        asignaturasCol.setMinWidth(60);
        return asignaturasCol;
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
    private void setupTableFiltering(TableView<Instituto> tableView, TextField searchField) {
        // Cargar los datos
        institutos = FXCollections.observableArrayList(institutoController.findAll());
        filteredInstitutos = new FilteredList<>(institutos, p -> true);

        // Configurar el filtro basado en el texto de búsqueda
        setupSearchFilter(searchField);

        // Crear un SortedList basado en la FilteredList
        SortedList<Instituto> sortedData = new SortedList<>(filteredInstitutos);

        // Vincular el comparador de la lista ordenada con el comparador de la tabla
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        // Usar sortedData como fuente de datos para la tabla
        tableView.setItems(sortedData);
    }

    // Método para configurar el filtro de búsqueda
    private void setupSearchFilter(TextField searchField) {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredInstitutos.setPredicate(instituto -> filterDocente(instituto, newValue));
        });
    }

    // Método para filtrar un docente según el texto de búsqueda
    private boolean filterDocente(Instituto instituto, String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }
        String lowerCaseFilter = filterText.toLowerCase();
        return instituto.getCodigo().toLowerCase().contains(lowerCaseFilter) ||
                instituto.getDenominacion().toLowerCase().contains(lowerCaseFilter);
    }

    // Método para crear el contenedor de la tabla
    private VBox createTableContainer(TableView<Instituto> tableView) {
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

        Label totalInstitutos = createTotalLabel();
        infoPanel.getChildren().add(totalInstitutos);

        setupTotalLabelListener(totalInstitutos);

        return infoPanel;
    }

    // Método para crear la etiqueta del total
    private Label createTotalLabel() {
        Label totalInstitutos = new Label("Total de docentes: " + institutos.size());
        totalInstitutos.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return totalInstitutos;
    }

    // Método para configurar el listener de la etiqueta total
    private void setupTotalLabelListener(Label totalInstitutos) {
        filteredInstitutos.addListener((ListChangeListener<Instituto>) c -> {
            totalInstitutos.setText("Total de institutos mostrados: " + filteredInstitutos.size());
        });
    }

    private void setupStage(Stage institutosStage, VBox mainContainer, TableView<Instituto> tableView) {
        Scene scene = new Scene(mainContainer);
        institutosStage.setScene(scene);
        institutosStage.setMinWidth(800);
        institutosStage.setMinHeight(500);

        // Configurar el evento de cierre
        institutosStage.setOnCloseRequest(event -> cleanupOnClose(tableView));

        institutosStage.show();
    }

    private void cleanupOnClose(TableView<Instituto> tableView) {
        if (activeTableView == tableView) {
            activeTableView = null;
            currentDocente = null;
        }
    }

}