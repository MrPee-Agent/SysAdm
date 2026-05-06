package com.example.ParkingSimulator.gui;

import com.example.ParkingSimulator.core.Car;
import com.example.ParkingSimulator.core.Parking;
import com.example.ParkingSimulator.strategy.DefaultStrategy;
import com.example.ParkingSimulator.sync.MutexSync;
import com.example.ParkingSimulator.sync.SemaphoreSync;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private int NBR_PLACES = 9;
    private int NBR_CARS   = 15;

    private final List<CustomRectangle> spots = new ArrayList<>();
    private Parking parking;
    private Label   statusLabel;
    private Label   modeLabel;
    private Button  btnMutex;
    private Button  btnSemaphore;
    private Button  btnReset;
    private VBox    logBox;
    private ScrollPane logScroll;
    private GridPane grid;

    @Override
    public void start(Stage stage) {
        // ── Ask user for config ───────────────────────────────────────
        TextInputDialog placesDialog = new TextInputDialog("9");
        placesDialog.setTitle("Configuration");
        placesDialog.setHeaderText("Parking Simulator");
        placesDialog.setContentText("Nombre de places :");
        placesDialog.showAndWait().ifPresent(val -> {
            try { NBR_PLACES = Integer.parseInt(val); } catch (Exception ignored) {}
        });

        TextInputDialog carsDialog = new TextInputDialog("15");
        carsDialog.setTitle("Configuration");
        carsDialog.setHeaderText("Parking Simulator");
        carsDialog.setContentText("Nombre de voitures :");
        carsDialog.showAndWait().ifPresent(val -> {
            try { NBR_CARS = Integer.parseInt(val); } catch (Exception ignored) {}
        });

        // ── Root layout ──────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");

        // ── Header ───────────────────────────────────────────────────
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 20, 12, 20));

        Label title = new Label("PARKING SIMULATOR");
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 26));
        title.setTextFill(Color.web("#58a6ff"));

        Label config = new Label(NBR_PLACES + " places | " + NBR_CARS + " voitures");
        config.setFont(Font.font("Courier New", 12));
        config.setTextFill(Color.web("#f0883e"));

        modeLabel = new Label("Choisissez un mode de synchronisation");
        modeLabel.setFont(Font.font("Courier New", 13));
        modeLabel.setTextFill(Color.web("#8b949e"));

        header.getChildren().addAll(title, config, modeLabel);

        // ── Parking Grid ─────────────────────────────────────────────
        int cols = (int) Math.ceil(Math.sqrt(NBR_PLACES));
        grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));

        for (int i = 0; i < NBR_PLACES; i++) {
            CustomRectangle spot = new CustomRectangle(i);
            spots.add(spot);
            grid.add(spot, i % cols, i / cols);
        }

        // ── Status Label ─────────────────────────────────────────────
        statusLabel = new Label("En attente...");
        statusLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.web("#f0f6fc"));
        statusLabel.setPadding(new Insets(8, 0, 0, 0));

        VBox centerBox = new VBox(10, grid, statusLabel);
        centerBox.setAlignment(Pos.CENTER);

        // ── Buttons ───────────────────────────────────────────────────
        btnMutex     = createButton("▶  MUTEX",     "#238636", "#2ea043");
        btnSemaphore = createButton("▶  SEMAPHORE", "#1f6feb", "#388bfd");
        btnReset     = createButton("↺  RESET",     "#6e7681", "#8b949e");
        btnReset.setDisable(true);

        btnMutex.setOnAction(e -> startSimulation("MUTEX"));
        btnSemaphore.setOnAction(e -> startSimulation("SEMAPHORE"));
        btnReset.setOnAction(e -> resetSimulation());

        HBox btnBox = new HBox(12, btnMutex, btnSemaphore, btnReset);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(0, 20, 16, 20));

        // ── Log Panel ─────────────────────────────────────────────────
        logBox = new VBox(3);
        logBox.setPadding(new Insets(8));
        logBox.setStyle("-fx-background-color: #161b22;");

        logScroll = new ScrollPane(logBox);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(160);
        logScroll.setStyle("-fx-background: #161b22; -fx-border-color: #30363d;");

        Label logTitle = new Label("  JOURNAL D'ÉVÉNEMENTS");
        logTitle.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        logTitle.setTextFill(Color.web("#8b949e"));
        logTitle.setPadding(new Insets(6, 0, 4, 8));
        logTitle.setStyle("-fx-background-color: #21262d; -fx-border-color: #30363d; -fx-border-width: 0 0 1 0;");
        logTitle.setMaxWidth(Double.MAX_VALUE);

        VBox logPanel = new VBox(0, logTitle, logScroll);
        logPanel.setStyle("-fx-border-color: #30363d; -fx-border-width: 1 0 0 0;");

        // ── Assemble ──────────────────────────────────────────────────
        VBox top = new VBox(0, header, centerBox, btnBox);
        root.setCenter(top);
        root.setBottom(logPanel);

        Scene scene = new Scene(root, 800, 700);
        stage.setTitle("Parking Simulator");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    private Button createButton(String text, String bgNormal, String bgHover) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(150);
        btn.setPrefHeight(38);
        btn.setStyle(
                "-fx-background-color: " + bgNormal + ";" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + bgHover + ";" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + bgNormal + ";" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        ));
        return btn;
    }

    private void startSimulation(String mode) {
        btnMutex.setDisable(true);
        btnSemaphore.setDisable(true);
        btnReset.setDisable(true);

        resetSpots();
        clearLog();

        Platform.runLater(() -> {
            modeLabel.setText("Mode actif : " + mode);
            statusLabel.setText("Simulation en cours...");
        });

        if ("MUTEX".equals(mode)) {
            Object verrou = new Object();
            parking = new Parking(NBR_PLACES, new DefaultStrategy(), new MutexSync(verrou, true));
        } else {
            parking = new Parking(NBR_PLACES, new DefaultStrategy(), new SemaphoreSync(NBR_PLACES));
        }

        Parking p = parking;

        new Thread(() -> {
            List<Car> cars = new ArrayList<>();
            for (int i = 1; i <= NBR_CARS; i++) {
                final int carId = i;
                Car car = new Car(carId, p) {
                    @Override
                    public void run() {
                        try {
                            long arrivee = System.currentTimeMillis();
                            int place = p.entrer(getCarId());
                            if (place == -1) return;
                            long attente = System.currentTimeMillis() - arrivee;

                            Platform.runLater(() -> {
                                spots.get(place).setOccupied(true, getCarId());
                                addLog("🚗 Voiture-" + getCarId() + " → place " + place
                                        + "  (attente " + attente + "ms)", "#58a6ff");
                            });

                            Thread.sleep(2000 + (int)(Math.random() * 3000));

                            p.sortir(getCarId(), place);

                            Platform.runLater(() -> {
                                spots.get(place).setOccupied(false, -1);
                                addLog("✓ Voiture-" + getCarId() + " quitte place " + place, "#3fb950");
                            });

                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                };
                cars.add(car);
            }

            for (Car c : cars) {
                c.start();
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }

            for (Car c : cars) {
                try { c.join(); } catch (InterruptedException ignored) {}
            }

            Platform.runLater(() -> {
                statusLabel.setText("✅ Simulation terminée !");
                btnReset.setDisable(false);
                addLog("── Simulation terminée ──", "#f0883e");
            });

        }).start();
    }

    private void resetSimulation() {
        resetSpots();
        clearLog();
        btnMutex.setDisable(false);
        btnSemaphore.setDisable(false);
        btnReset.setDisable(true);
        modeLabel.setText("Choisissez un mode de synchronisation");
        statusLabel.setText("En attente...");
    }

    private void resetSpots() {
        Platform.runLater(() -> spots.forEach(s -> s.setOccupied(false, -1)));
    }

    private void clearLog() {
        Platform.runLater(() -> logBox.getChildren().clear());
    }

    private void addLog(String message, String color) {
        Label lbl = new Label(message);
        lbl.setFont(Font.font("Courier New", 11));
        lbl.setTextFill(Color.web(color));
        lbl.setPadding(new Insets(1, 6, 1, 6));
        logBox.getChildren().add(lbl);
        logScroll.setVvalue(1.0);
    }

    public static void main(String[] args) {
        launch();
    }
}