module VehicleRecognitionSystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;  // optional
    requires java.desktop;  // required by javafx.swing
    requires javafx.graphics;
    requires opencv;
    opens com.vehicle to javafx.fxml;
    exports com.vehicle;
}
