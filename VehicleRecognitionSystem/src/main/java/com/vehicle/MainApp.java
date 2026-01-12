package com.vehicle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vehicle/main-view.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Vehicle Recognition System");
        stage.setScene(scene);
        stage.show();
    }
}
