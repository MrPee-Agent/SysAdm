package com.example.ParkingSimulator.gui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class CustomRectangle extends StackPane {

    private static final int    WIDTH  = 80;
    private static final int    HEIGHT = 60;

    private final Rectangle rect;
    private final Text      spotNumber;
    private final Text      carLabel;
    private final int       index;

    public CustomRectangle(int index) {
        this.index = index;

        rect = new Rectangle(WIDTH, HEIGHT);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        setFree();

        spotNumber = new Text("P" + (index + 1));
        spotNumber.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
        spotNumber.setFill(Color.web("#8b949e"));
        StackPane.setAlignment(spotNumber, Pos.TOP_LEFT);

        carLabel = new Text("");
        carLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
        carLabel.setFill(Color.WHITE);

        getChildren().addAll(rect, spotNumber, carLabel);
        setPrefSize(WIDTH, HEIGHT);
    }

    public void setOccupied(boolean occupied, int carId) {
        FadeTransition ft = new FadeTransition(Duration.millis(250), this);
        ft.setFromValue(0.5);
        ft.setToValue(1.0);
        ft.play();

        if (occupied) {
            rect.setFill(Color.web("#da3633"));
            rect.setStroke(Color.web("#f85149"));
            rect.setStrokeWidth(2);
            carLabel.setText("🚗 " + carId);
            spotNumber.setFill(Color.web("#ff7b72"));
        } else {
            setFree();
            carLabel.setText("");
            spotNumber.setFill(Color.web("#8b949e"));
        }
    }

    private void setFree() {
        rect.setFill(Color.web("#1a2332"));
        rect.setStroke(Color.web("#238636"));
        rect.setStrokeWidth(1.5);
    }

    public int getIndex() {
        return index;
    }
}