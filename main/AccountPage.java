package main;

import Exceptions.*;
import account.Account;
import account.Current;
import account.Savings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.poi.ss.formula.functions.T;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

public class AccountPage implements Initializable {
    static Account account;
    boolean countDeposit = false, countWithdraw = false, countStatement = false;

    @FXML
    VBox vBox;

    @FXML
    Label nameLabel, dateLabel, accTypeLabel, cusTypeLabel, balanceLabel;

    TextField amtField, amtField1, start, end;
    RadioButton cash, cheque;
    Label errorDate;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (Account.accType.equals("Savings")) {
            account = new Savings();
        } else {
            account = new Current();
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        nameLabel.setText(account.getName());
        dateLabel.setText(df.format(account.getOpeningDate()));
        accTypeLabel.setText(account.getAccType());
        cusTypeLabel.setText(account.getCusType());
        balanceLabel.setText(Double.toString(account.getBalance())); }

    public void depositClicked(ActionEvent actionEvent) {
        if (countDeposit) {
            vBox.getChildren().removeAll(amtField, cash, cheque);
            countDeposit = false;
            return;
        }
        amtField = new TextField();
        amtField.setPromptText("Amount");
        amtField.setMaxWidth(200);
        amtField.setMaxHeight(60);

        ToggleGroup payment = new ToggleGroup();
        cash = new RadioButton("Cash");
        cash.setToggleGroup(payment);
        cheque = new RadioButton("Cheque");
        cheque.setToggleGroup(payment);

        vBox.getChildren().add(1, cash);
        vBox.getChildren().add(2, cheque);
        vBox.getChildren().add(3, amtField);

        amtField.setOnAction(e -> {
            RadioButton selectedPayment = (RadioButton) payment.getSelectedToggle();
            account.deposit(Double.parseDouble(amtField.getText()), selectedPayment.getText());
            vBox.getChildren().removeAll(cash, cheque, amtField);
        });
        countDeposit = true;
    }

    public void withdrawClicked(ActionEvent actionEvent) {
        if (countWithdraw) {
            vBox.getChildren().removeAll(amtField1);
            countWithdraw = false;
            return;
        }
        amtField1 = new TextField();
        amtField1.setPromptText("Amount");
        amtField1.setMaxWidth(200);
        amtField1.setMaxHeight(60);

        vBox.getChildren().add(2, amtField1);

        amtField1.setOnAction(e -> {
            Label errorLabel = new Label();
            try {
                account.withdraw(Double.parseDouble(amtField1.getText()));
            } catch (MaximumWithdrawalsException exception) {
                errorLabel.setText("You have reached today's withdrawal limit: " + account.getWithdrawalsToday() + ". Cannot complete transaction");
            } catch (WithdrawalLimitException exception) {
                errorLabel.setText("Cannot withdraw " + amtField1.getText() + " from a " + account.getCusType() + " account.");
            } catch (BelowMinimumBalanceException exception) {
                errorLabel.setText("Account balance is below the minimum balance for a " + account.getCusType() + " account");
            } catch (BelowZeroBalanceException exception) {
                errorLabel.setText("Balance after transaction is less than 0. Cannot complete transaction");
            } catch (AccountException exception) {
                exception.printStackTrace();
            }

            Stage errorStage = new Stage();
            errorStage.setTitle("Error");
            errorStage.setScene(new Scene((new StackPane(errorLabel)), 600, 200));
            errorStage.show();

            vBox.getChildren().removeAll(amtField1);
        });
        countWithdraw = true;
    }

    public void genStatementClicked(ActionEvent actionEvent) {
        if (countStatement) {
            vBox.getChildren().removeAll(start, end, errorDate);
            countStatement = false;
            return;
        }
        start = new TextField();
        end = new TextField();
        errorDate = new Label("Enter date in dd/mm/yyyy format");

        vBox.getChildren().addAll(start, end, errorDate);

        start.setPromptText("Start Date");
        end.setPromptText("End Date");

        start.setMaxWidth(250);
        end.setMaxWidth(250);

        end.setOnAction(e -> {
            try {
                Date startDate = new SimpleDateFormat("dd/MM/yyyy").parse(start.getText());
                Date endDate = new SimpleDateFormat("dd/MM/yyyy").parse(end.getText());

                Stage stage = new Stage();

                stage.setScene(new Scene(new StackPane(getStatement(startDate, endDate))));
                stage.show();

                start.clear();
                end.clear();

            } catch (ParseException exception) {
                errorDate.setText("Invalid date entry");
            }
            countStatement = true;
        });
    }

    public TableView<String[]> getStatement(Date start, Date end) {
        ObservableList<String[]> statementList = FXCollections.observableArrayList();
        String[][] statement = account.generateStatement(start, end);
        statementList.addAll(Arrays.asList(statement));

        TableView<String[]> table = new TableView<>();

        for (int i = 0; i < statement[0].length; i++) {
            TableColumn tc = new TableColumn(statement[0][i]);
            final int colNo = i;
            tc.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String[], String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<String[], String> p) {
                    return new SimpleStringProperty((p.getValue()[colNo]));
                }
            });
            tc.setPrefWidth(300);
            table.getColumns().add(tc);
        }
        table.setItems(statementList);
        return table;
    }

}

