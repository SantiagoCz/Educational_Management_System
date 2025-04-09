package view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.entities.Asignatura;
import model.entities.CargoDocente;
import model.entities.Docente;
import model.entities.Instituto;
import controller.AsignaturaController;
import controller.CargoDocenteController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewManager {
    private static ViewManager instance;
    private Stage mainStage;

    // Cache de vistas
    private InstitutoView institutoView;
    private AsignaturaView asignaturaView;
    private DocenteView docenteView;
    private CargoDocenteView cargoDocenteView;
    private MainDashboard mainDashboard;
    private Stage mainDashboardStage;

    private String titleStyle = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e;";
    private String subtitleStyle = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #283593;";
    private String valueStyle = "-fx-font-weight: normal;";
    private String sectionStyle = "-fx-padding: 10px; -fx-background-color: #f5f5f5; -fx-background-radius: 5;";

    private String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;";
    private String fieldStyle = "-fx-pref-width: 300px; -fx-pref-height: 35px; -fx-font-size: 14px; " +
            "-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 4px; -fx-background-radius: 4px;";
    public String readOnlyStyle = fieldStyle + "-fx-background-color: #e9ecef;";

    // Mapa para mantener registro de ventanas abiertas
    private final Map<String, Stage> activeStages = new HashMap<>();

    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }
        return instance;
    }

    private ViewManager() {}

    // Getters privados con inicialización lazy y manejo de recursos
    private InstitutoView getInstitutoView() {
        if (institutoView == null) {
            institutoView = new InstitutoView();
        }
        return institutoView;
    }

    private AsignaturaView getAsignaturaView() {
        if (asignaturaView == null) {
            asignaturaView = new AsignaturaView();
        }
        return asignaturaView;
    }

    private DocenteView getDocenteView() {
        if (docenteView == null) {
            docenteView = new DocenteView();
        }
        return docenteView;
    }

    private CargoDocenteView getCargoDocenteView() {
        if (cargoDocenteView == null) {
            cargoDocenteView = new CargoDocenteView();
        }
        return cargoDocenteView;
    }

    private MainDashboard getMainDashboard() {
        if (mainDashboard == null) {
            mainDashboard = new MainDashboard();
        }
        return mainDashboard;
    }

    // Métodos para mostrar vistas
    public void showInstitutos(Docente docente, Stage owner, String title) {
        try {
            Stage stage = getOrCreateStage("institutos_" + docente.getId(), owner);
            getInstitutoView().showInstitutos(docente, stage, title);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir la vista de institutos: " + e.getMessage());
        }
    }

    public void showAsignaturas(Instituto instituto, Stage owner, String title) {
        try {
            Stage stage = getOrCreateStage("asignaturas_instituto_" + instituto.getId(), owner);
            getAsignaturaView().showAsignaturas(instituto, stage, title);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir la vista de asignaturas: " + e.getMessage());
        }
    }

    public void showAsignaturas(Docente docente, Stage owner, String title) {
        try {
            Stage stage = getOrCreateStage("asignaturas_docente_" + docente.getId(), owner);
            getAsignaturaView().showAsignaturas(docente, stage, title);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir la vista de asignaturas: " + e.getMessage());
        }
    }

    public void showDocentes(Instituto instituto, Stage owner, String title) {
        try {
            Stage stage = getOrCreateStage("docentes_" + instituto.getId(), owner);
            getDocenteView().showDocentes(instituto, stage, title);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir la vista de docentes: " + e.getMessage());
        }
    }

    public void showInformeDocente(Docente docente) {
        try {
            getDocenteView().showInformeDocente(docente);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir la vista de informe docente: " + e.getMessage());
        }
    }

    public void showInformeInstituto(Instituto instituto) {
        try {
            getInstitutoView().showInformeInstituto(instituto);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir la vista de informe docente: " + e.getMessage());
        }
    }

    public void showCargos(Instituto instituto, Stage owner, String title) {
        try {
            getCargoDocenteView().showCargos(instituto, owner, title);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir la vista de cargos: " + e.getMessage());
        }
    }

    public void openCargoDocenteForm(Instituto instituto, CargoDocente cargoDocente, Stage owner, String title) {
        try {
            getCargoDocenteView().openForm(instituto, cargoDocente, owner, title);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir el formulario de cargo docente: " + e.getMessage());
        }
    }

    public void openAsignaturaForm(Stage owner, String title, Docente docente) {
        try {
            getAsignaturaView().openForm(owner, title, docente);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir el formulario de asignatura: " + e.getMessage());
        }
    }

    public void openAsignaturaForm(Stage owner, String title, Instituto instituto) {
        try {
            getAsignaturaView().openForm(owner, title, instituto);
        } catch (Exception e) {
            showError("Error", null, "No se pudo abrir el formulario de asignatura: " + e.getMessage());
        }
    }

    // Utilidades para manejo de ventanas
    private Stage getOrCreateStage(String stageId, Stage owner) {
        Stage stage = activeStages.get(stageId);
        if (stage == null || !stage.isShowing()) {
            stage = createNewStage(owner);
            stage.setOnCloseRequest(event -> activeStages.remove(stageId));
            activeStages.put(stageId, stage);
        } else {
            stage.toFront();
        }
        return stage;
    }

    // Métodos para ver detalles de Instituto y Docente-----------------------------------------------------------------
    public Stage createInformeStage(String titulo) {
        Stage informeStage = new Stage();
        informeStage.initModality(Modality.WINDOW_MODAL);
        informeStage.setTitle(titulo);
        return informeStage;
    }

    public VBox createInformeMainContainer() {
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-padding: 20px; -fx-background-color: white;");
        mainContainer.setPrefWidth(600);
        return mainContainer;
    }

    public VBox createInstitutoBoxForDocente(Docente docente, Instituto instituto) {
        VBox institutoBox = new VBox(10);
        institutoBox.setStyle("-fx-padding: 10px; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // Nombre del instituto
        Label institutoLabel = new Label("• " + instituto.getDenominacion());
        institutoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        institutoBox.getChildren().add(institutoLabel);

        // Cargo docente
        institutoBox.getChildren().add(createCargoDocenteBox(docente, instituto));

        // Asignaturas
        institutoBox.getChildren().add(createAsignaturasBox(docente, instituto));

        return institutoBox;
    }

    public VBox createDocenteBoxForInstituto(Docente docente, Instituto instituto) {
        VBox docenteBox = new VBox(10);
        docenteBox.setStyle(sectionStyle);

        // Nombre del docente
        Label docenteLabel = new Label("• " + docente.getApellidos() + ", " + docente.getNombres());
        docenteLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        docenteBox.getChildren().add(docenteLabel);

        // DNI y Legajo del docente
        VBox infoDocenteBox = new VBox(5);
        infoDocenteBox.setStyle("-fx-padding: 0 0 0 20;");
        addInfoRow(infoDocenteBox, "DNI:", docente.getDni());
        addInfoRow(infoDocenteBox, "Legajo:", docente.getLegajo());
        docenteBox.getChildren().add(infoDocenteBox);

        // Cargo docente
        docenteBox.getChildren().add(createCargoDocenteBox(docente, instituto));

        // Asignaturas
        docenteBox.getChildren().add(createAsignaturasBox(docente, instituto));

        return docenteBox;
    }

    private VBox createCargoDocenteBox(Docente docente, Instituto instituto) {
        VBox cargoBox = new VBox(5);
        cargoBox.setStyle("-fx-padding: 0 0 0 20;");

        CargoDocenteController cargoDocenteController = new CargoDocenteController();

        Label cargoLabel = new Label("Cargo Docente:");
        cargoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #424242;");
        cargoBox.getChildren().add(cargoLabel);

        CargoDocente cargo = cargoDocenteController.findActiveCargoByDocenteAndInstituto(docente, instituto);
        if (cargo != null) {
            Label numeroCargoLabel = new Label("• Número de cargo: " + cargo.getNumeroCargo());
            Label horasSemanalesLabel = new Label("• Horas Semanales: " + cargo.getDedicacionHoras());

            numeroCargoLabel.setStyle("-fx-padding: 0 0 0 20;");
            horasSemanalesLabel.setStyle("-fx-padding: 0 0 0 20;");

            cargoBox.getChildren().addAll(numeroCargoLabel, horasSemanalesLabel);
        } else {
            Label sinCargoLabel = new Label("Sin cargo docente");
            sinCargoLabel.setStyle("-fx-padding: 0 0 0 20; -fx-font-style: italic; -fx-text-fill: #757575;");
            cargoBox.getChildren().add(sinCargoLabel);
        }

        return cargoBox;
    }

    private VBox createAsignaturasBox(Docente docente, Instituto instituto) {
        VBox asignaturasBox = new VBox(5);
        asignaturasBox.setStyle("-fx-padding: 0 0 0 20;");

        AsignaturaController asignaturaController = new AsignaturaController();

        Label asignaturasLabel = new Label("Asignaturas:");
        asignaturasLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #424242;");
        asignaturasBox.getChildren().add(asignaturasLabel);

        List<Asignatura> asignaturas = asignaturaController.findActiveAsignaturasByDocenteAndInstituto(docente, instituto);
        if (!asignaturas.isEmpty()) {
            for (Asignatura asignatura : asignaturas) {
                Label asignaturaLabel = new Label("• " + asignatura.getNombre());
                asignaturaLabel.setStyle("-fx-padding: 0 0 0 20;");
                asignaturasBox.getChildren().add(asignaturaLabel);
            }
        } else {
            Label sinAsignaturasLabel = new Label("Sin asignaturas");
            sinAsignaturasLabel.setStyle("-fx-padding: 0 0 0 20; -fx-font-style: italic; -fx-text-fill: #757575;");
            asignaturasBox.getChildren().add(sinAsignaturasLabel);
        }

        return asignaturasBox;
    }

    public void addInfoRow(VBox container, String label, String value) {
        HBox row = new HBox(10);
        Label labelNode = new Label(label);
        labelNode.setStyle(labelStyle);

        Label valueNode = new Label(value);
        valueNode.setStyle(valueStyle);
        valueNode.setWrapText(true);

        row.getChildren().addAll(labelNode, valueNode);
        container.getChildren().add(row);
    }

    public Button createCloseButton(Stage stage) {
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-padding: 10px 30px;");
        btnCerrar.setOnAction(e -> stage.close());
        return btnCerrar;
    }

    public void setupAndShowScene(Stage stage, ScrollPane scrollPane) {
        Scene scene = new Scene(scrollPane, 650, 700);
        stage.setScene(scene);
        stage.show();
    }
    //------------------------------------------------------------------------------------------------------------------

    private Stage createNewStage(Stage owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setResizable(false);
        return stage;
    }

    public Stage createNewStage(String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setResizable(false);
        return stage;
    }

    // Método para crear contenedor
    public VBox createContentContainer() {
        VBox contentContainer = new VBox(20);
        contentContainer.setStyle("-fx-background-color: white; -fx-padding: 40px; -fx-border-color: #e0e0e0;");
        return contentContainer;
    }

    // Método para mostrar alerta de error
    public void showError(String title, String headerText, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(headerText);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Metodo para mostrar alerta exitosa
    public void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Aplicar estilo de error a un campo
    public void setErrorStyle(Node field) {
        field.setStyle(field.getStyle() + "; -fx-border-color: red;");
    }

    // Método para restablecer estilo normal
    public void resetStyle(Node field) {
        field.setStyle(fieldStyle);
    }

    // Método para crear etiqueta
    public Label createLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(labelStyle);
        return lbl;
    }

    // Método para crear campo de texto
    public TextField createTextField(String text, String prompt) {
        TextField textField = new TextField(text);
        textField.setStyle(fieldStyle);
        textField.setPromptText(prompt);
        textField.setFocusTraversable(false);
        return textField;
    }

    // Método para crear datePicker
    public DatePicker createDatePicker(Docente docente, String prompt) {
        DatePicker datePicker = new DatePicker();
        if (docente != null && docente.getFechaNacimiento() != null) {
            datePicker.setValue(docente.getFechaNacimiento().toLocalDate());
        }
        datePicker.setStyle(fieldStyle);
        datePicker.setPromptText(prompt);
        datePicker.setFocusTraversable(false);
        return datePicker;
    }

    // Método para crear área de texto
    public TextArea createTextArea(String text, String prompt) {
        TextArea textArea = new TextArea(text);
        textArea.setStyle(fieldStyle);
        textArea.setPromptText(prompt);
        textArea.setWrapText(true);
        textArea.setFocusTraversable(false);
        return textArea;
    }

    // Método para crear un comboBox de instituto
    public ComboBox<Instituto> createInstitutoComboBox() {
        ComboBox<Instituto> comboBox = new ComboBox<>();
        comboBox.setStyle(fieldStyle);
        comboBox.setPromptText("Seleccione un instituto");
        return comboBox;
    }

    // Método para crear un comboBox de docente
    public ComboBox<Docente> createDocenteComboBox() {
        ComboBox<Docente> comboBox = new ComboBox<>();
        comboBox.setStyle(fieldStyle);
        comboBox.setPromptText("Seleccione un docente");
        return comboBox;
    }

    // Método para crear alerta de confirmación
    public Alert createAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    // Método para crear vista de formulario
    public Stage createFormStage(Stage primaryStage, String title) {
        Stage formStage = new Stage();
        formStage.initModality(Modality.WINDOW_MODAL);
        formStage.initOwner(primaryStage);
        formStage.setTitle(title);
        formStage.setResizable(false);
        return formStage;
    }

    public void setupPrimaryStage(Stage primaryStage, String title) {
        primaryStage.setTitle(title);
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        primaryStage.setMaximized(true);
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(600);
    }

    // Método para crear contenedor principal
    public VBox createMainContainer() {
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");
        mainContainer.setFillWidth(true);
        return mainContainer;
    }

    // Método para crear encabezado
    public HBox createHeader(String title) {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1a237e; -fx-padding: 20px; -fx-alignment: center;");
        header.setPrefHeight(80);

        Label headerTitle = new Label(title);
        headerTitle.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        header.getChildren().add(headerTitle);
        return header;
    }

    // Método para crear grid
    public GridPane createFormGrid() {
        GridPane form = new GridPane();
        form.setStyle("-fx-background-color: white; -fx-padding: 40px; -fx-border-color: #e0e0e0;");
        form.setHgap(20);
        form.setVgap(20);
        form.setAlignment(Pos.CENTER);
        return form;
    }

    // Método para crear botón de menú principal
    public Button createMainMenuButton(Stage primaryStage) {
        Button btnMenuPrincipal = new Button("Menú Principal");
        btnMenuPrincipal.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; " + "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnMenuPrincipal.setOnAction(e -> {
            if (mainDashboardStage != null) {
                mainDashboardStage.show();
                primaryStage.close();
            } else {
                try {
                    new MainDashboard().start(new Stage());
                    primaryStage.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return btnMenuPrincipal;
    }

    // Método para configurar el stage inicial
    public void setupInitialStage(Stage primaryStage, String title) {
        ViewManager.getInstance().setupPrimaryStage(primaryStage, title);
    }

    // Método para crear contenedor de búsqueda
    public VBox createSearchContainer() {
        VBox searchContainer = new VBox(5);
        searchContainer.setMaxWidth(400);
        return searchContainer;
    }

    // Método para crear top panel
    public VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-border-color: #e0e0e0;");
        return topPanel;
    }

    // Método para crear el contenedor derecho de botón
    public HBox createRightButtonContainer(Stage primaryStage) {
        HBox rightContainer = new HBox();
        rightContainer.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);

        Button btnMenuPrincipal = createMainMenuButton(primaryStage);
        rightContainer.getChildren().add(btnMenuPrincipal);
        return rightContainer;
    }

    // Método para crear la etiqueta de búsqueda
    public Label createSearchLabel(String text) {
        Label searchLabel = new Label(text);
        searchLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #424242;");
        return searchLabel;
    }

    // Método para crear campo de búsqueda
    public TextField createSearchField(String prompt) {
        TextField searchField = new TextField();
        searchField.setPromptText(prompt);
        searchField.setStyle("-fx-pref-height: 35px; -fx-font-size: 14px; " + "-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0;");
        searchField.setPrefWidth(300);
        searchField.setMaxWidth(300);
        return searchField;
    }
}