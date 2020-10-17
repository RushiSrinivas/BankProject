package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        setPrimaryStage(primaryStage);
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene login = new Scene(root, 650, 300);
        primaryStage.setTitle("Bank");
        primaryStage.setScene(login);
        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return Main.primaryStage;
    }

    private void setPrimaryStage(Stage primaryStage) {
        Main.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
