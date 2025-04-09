package view;

import controller.AsignaturaController;
import controller.DocenteController;
import controller.InstitutoController;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainDashboard extends Application {

    private final InstitutoController institutoController;
    private final DocenteController docenteController;
    private final AsignaturaController asignaturaController;

    public MainDashboard() {
        this.institutoController = new InstitutoController();
        this.docenteController = new DocenteController();
        this.asignaturaController = new AsignaturaController();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Gestión Académica");

        // Configurar la ventana para que ocupe toda la pantalla-----------------------------
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(600);

        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");
        mainContainer.setFillWidth(true);

        // Header----------------------------------------------------------------------------
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1a237e; -fx-padding: 20px; -fx-alignment: center;");
        header.setPrefHeight(80);

        Label title = new Label("Sistema de Gestión Académica");
        title.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        header.getChildren().add(title);

        // Módulos Grid----------------------------------------------------------------------
        GridPane modulesGrid = new GridPane();
        modulesGrid.setStyle("-fx-padding: 20px; -fx-background-color: white;");
        modulesGrid.setHgap(20);
        modulesGrid.setVgap(20);
        modulesGrid.setAlignment(Pos.CENTER);

        // Módulo Docentes-------------------------------------------------------------------
        Button btnDocentes = createModuleButton("Docentes", "Administrar Personal Docente", e -> {
            DocenteView docenteView = new DocenteView();
            try {
                docenteView.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        modulesGrid.add(btnDocentes, 0, 0);

        // Módulo Institutos-----------------------------------------------------------------
        Button btnInstitutos = createModuleButton("Institutos", "Gestión de Institutos", e -> {
            InstitutoView institutoView = new InstitutoView();
            try {
                institutoView.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        modulesGrid.add(btnInstitutos, 1, 0);

        // Módulo Asignaturas----------------------------------------------------------------
        Button btnAsignaturas = createModuleButton("Asignaturas", "Administrar Asignaturas", e -> {
            AsignaturaView asignaturaView = new AsignaturaView();
            try {
                asignaturaView.start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        modulesGrid.add(btnAsignaturas, 2, 0);

        // Panel de Información y Estadísticas------------------------------------------------
        HBox statsPanel = createStatsPanel();

        mainContainer.getChildren().addAll(header, modulesGrid, statsPanel);
        VBox.setVgrow(modulesGrid, Priority.ALWAYS);

        Scene scene = new Scene(mainContainer);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private Button createModuleButton(String moduleName, String description, EventHandler<ActionEvent> action) {
        VBox moduleContainer = new VBox(10);
        moduleContainer.setAlignment(Pos.CENTER);
        moduleContainer.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-padding: 20px;"
        );

        Label nameLabel = new Label(moduleName);
        nameLabel.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #1a237e;"
        );

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-text-fill: #757575;"
        );

        moduleContainer.getChildren().addAll(nameLabel, descLabel);

        Button moduleButton = new Button();
        moduleButton.setGraphic(moduleContainer);
        moduleButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-cursor: hand;"
        );
        moduleButton.setOnAction(action);

        return moduleButton;
    }

    private HBox createStatsPanel() {
        HBox statsPanel = new HBox(20);
        statsPanel.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 20px; " +
                        "-fx-alignment: center;"
        );

        Label docentesStats = createStatLabel(
                "Docentes",
                String.valueOf(docenteController.findAllActives().size())
        );
        Label institutosStats = createStatLabel(
                "Institutos",
                String.valueOf(institutoController.findAllActives().size())
        );
        Label asignaturasStats = createStatLabel(
                "Asignaturas",
                String.valueOf(asignaturaController.findAllActives().size())
        );

        statsPanel.getChildren().addAll(docentesStats, institutosStats, asignaturasStats);
        return statsPanel;
    }

    private Label createStatLabel(String title, String value) {
        VBox statContainer = new VBox(5);
        statContainer.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #1a237e;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #424242;"
        );

        statContainer.getChildren().addAll(titleLabel, valueLabel);
        return new Label("", statContainer);
    }

    public static void main(String[] args) {
        launch(args);
    }
}