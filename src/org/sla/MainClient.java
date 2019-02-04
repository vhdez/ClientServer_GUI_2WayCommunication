package org.sla;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application {
    private TwoWayCommunicationController myController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Load View from xml description
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TwoWayCommunication.fxml"));
        Parent root = loader.load();

        Thread.currentThread().setName("TwoWayCommunicationController MainClient GUI Thread");

        // Display the scene
        primaryStage.setTitle("TwoWayCommunicationController CLIENT");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        myController = loader.getController();
        myController.setClientMode();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
