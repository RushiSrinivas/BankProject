package main;

import account.Account;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Login {

    @FXML
    TextField accNoField, passField;

    @FXML
    Label errorLabel;

    @FXML
    Button createAccountButton;

    public void loginButtonClicked(ActionEvent actionEvent) throws Exception {
        boolean success = Account.login(Integer.parseInt(accNoField.getText()));
        if (!success) {
            errorLabel.setText("Invalid credentials");
            return;
        }

        Stage loginStage = Main.getPrimaryStage();

        HBox createAccount = FXMLLoader.load(getClass().getResource("account_page.fxml"));
        loginStage.setScene(new Scene(createAccount));
    }

    public void createAccountButtonClicked(ActionEvent actionEvent) throws Exception {
        Stage loginStage = Main.getPrimaryStage();

        GridPane createAccount = FXMLLoader.load(getClass().getResource("create_account.fxml"));
        loginStage.setScene(new Scene(createAccount));
    }
}
