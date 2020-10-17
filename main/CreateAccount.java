package main;

import account.Account;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateAccount {

    @FXML
    ToggleGroup accType, modeOfPayment;

    @FXML
    TextField nameField, initDeposit;

    @FXML
    Label nameLabel, accountType, paymentMethod;

    Stage loginStage;
    GridPane createAccount;

    public void createAccountButtonClicked(ActionEvent actionEvent) throws Exception{
        if (nameField.getText().length() < 5) {
            nameLabel.setText("Name must contain at least 5 characters");
            return;
        }

        RadioButton selectedType = (RadioButton) accType.getSelectedToggle();
        RadioButton selectedPayment = (RadioButton) modeOfPayment.getSelectedToggle();

        if (selectedType == null) {
            accountType.setText("Choose an account type");
            return;
        }

        if (selectedPayment == null) {
            paymentMethod.setText("Choose a method of payment");
            return;
        }

        if (Double.parseDouble(initDeposit.getText()) < 10000) {
            initDeposit.setText("Insufficient deposit");
            return;
        }

        Account.createAccount(nameField.getText(), selectedType.getText(), selectedPayment.getText(), Double.parseDouble(initDeposit.getText()));
        loginStage = Main.getPrimaryStage();

        createAccount = FXMLLoader.load(getClass().getResource("login.fxml"));
        loginStage.setScene(new Scene(createAccount));
    }

    public void loginButtonClicked(ActionEvent actionEvent) throws IOException {
        loginStage = Main.getPrimaryStage();

        createAccount = FXMLLoader.load(getClass().getResource("login.fxml"));
        loginStage.setScene(new Scene(createAccount));
    }
}
