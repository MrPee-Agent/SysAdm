
module com.example.ParkingSimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.example.ParkingSimulator to javafx.fxml;
}