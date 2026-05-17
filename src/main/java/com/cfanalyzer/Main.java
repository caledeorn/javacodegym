package com.cfanalyzer;

import com.cfanalyzer.service.SchedulerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private final SchedulerService schedulerService = new SchedulerService();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        primaryStage.setTitle("Codeforces Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Start background scheduler
        schedulerService.start();
    }

    @Override
    public void stop() throws Exception {
        schedulerService.stop();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
