package front.controllers;

import app.Main;
import back.user.ErrorHandler;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import front.navigation.Flow;
import front.navigation.navigators.BackButtonNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.Objects;

/**
 * @author Arnaud MOREAU
 */
public class TransferSceneController extends Controller implements BackButtonNavigator {
    @FXML
    PasswordField passwordField;
    @FXML
    Button backButton, transferButton;
    @FXML
    TextField amountField, recipientField, IBANField, messageField, dateField;
    @FXML
    Label invalidAmountLabel, invalidRecipientLabel, invalidIBAN, invalidMessageLabel, invalidDateLabel,
            transferExecutedLabel, charactersLeftLabel, invalidPasswordLabel, insufficientBalanceLabel;

    private boolean transferExecuted = false;

    @Override
    public void handleBackButtonNavigation(MouseEvent event) {
        Main.setScene(Flow.back());
    }

    @Override
    public void emulateBackButtonClicked() {
        handleBackButtonNavigation(null);
    }

    @FXML
    void handleBackButtonClicked(MouseEvent event) {
        handleBackButtonNavigation(event);
        transferExecutedLabel.setVisible(false);
        if (transferExecuted) clearAllTextFields();
        if (transferExecuted) hideAllLabels();
        transferExecuted = false;
    }

    private void hideAllLabels() {
        invalidPasswordLabel.setVisible(false);
        charactersLeftLabel.setVisible(false);
        invalidAmountLabel.setVisible(false);
        invalidDateLabel.setVisible(false);
        invalidMessageLabel.setVisible(false);
        invalidRecipientLabel.setVisible(false);
        insufficientBalanceLabel.setVisible(false);
    }

    /**
     * Checks if every field is properly filled in. Initializes the transfer process.
     */
    public void transfer() {
        String amount = amountField.getText(), recipient = recipientField.getText(), IBAN = IBANField.getText(),
                message = messageField.getText(), date = dateField.getText(), password = passwordField.getText();

        // Manage the invalid "xxxx" labels visibility
        if (!isValidAmount(amount) && !invalidAmountLabel.isVisible()) invalidAmountLabel.setVisible(true);
        else if (isValidAmount(amount) && invalidAmountLabel.isVisible()) invalidAmountLabel.setVisible(false);
        if (!isValidRecipient(recipient) && !invalidRecipientLabel.isVisible()) invalidRecipientLabel.setVisible(true);
        else if (isValidRecipient(recipient) && invalidRecipientLabel.isVisible())
            invalidRecipientLabel.setVisible(false);
        if (!isValidIBAN(IBAN) && !invalidIBAN.isVisible()) invalidIBAN.setVisible(true);
        else if (isValidIBAN(IBAN) && invalidIBAN.isVisible()) invalidIBAN.setVisible(false);
        if (!isValidMessage(message) && !invalidMessageLabel.isVisible()) invalidMessageLabel.setVisible(true);
        else if (isValidMessage(message) && invalidMessageLabel.isVisible()) invalidMessageLabel.setVisible(false);
        if (!isValidDate(date) && !invalidDateLabel.isVisible()) invalidDateLabel.setVisible(true);
        else if (isValidDate(date) && invalidDateLabel.isVisible()) invalidDateLabel.setVisible(false);
        if (!isValidPassword(password) && !invalidPasswordLabel.isVisible()) invalidPasswordLabel.setVisible(true);
        else if (isValidPassword(password) && invalidPasswordLabel.isVisible()) invalidPasswordLabel.setVisible(false);
        if (!sufficientBalance(amount) && !insufficientBalanceLabel.isVisible()) insufficientBalanceLabel.setVisible(true);
        else if (sufficientBalance(amount) && insufficientBalanceLabel.isVisible()) insufficientBalanceLabel.setVisible(false);

        String recipientActual = Main.getCurrentAccount().getIBAN();
        if (!Objects.equals(recipientActual, IBAN)) {
            if (noLabelVisible()) {
                // Creates the transfer if everything is correct
                if (insufficientBalanceLabel.isVisible()) insufficientBalanceLabel.setVisible(false);
                HttpResponse<String> response = ErrorHandler.handlePossibleError(() -> {
                    HttpResponse<String> rep = null;
                    try {
                        rep = Unirest.post("https://flns-spring-test.herokuapp.com/api/transaction")
                                .header("Authorization", "Bearer " + Main.getToken())
                                .header("Content-Type", "application/json")
                                .body("{\r\n    \"transactionTypeId\": 1,\r\n    \"senderIban\": \"" + recipientActual + "\",\r\n    \"recipientIban\": \"" + IBAN + "\",\r\n    \"currencyId\": 0,\r\n    \"transactionAmount\": " + amount + ",\r\n    \"comments\": \"" + message + "\"\r\n}")
                                .asString();
                    } catch (UnirestException e) {
                        throw new RuntimeException(e);
                    }
                    return rep;
                });

                transferExecuted = true;
                Main.setScene(Flow.back());
                Main.setScene(Flow.back());
                Main.setScene(Flow.back());
                clearAllTextFields();
            }
        }
    }

    private boolean sufficientBalance(String amount) {
        return Main.getCurrentAccount().getSubAccountList().get(0).getAmount() >= Double.parseDouble(amount);
    }

    public boolean noLabelVisible() {
        return !invalidDateLabel.isVisible() && !invalidRecipientLabel.isVisible() && !invalidIBAN.isVisible()
                && !invalidMessageLabel.isVisible() && !invalidDateLabel.isVisible() && !invalidPasswordLabel.isVisible()
                && !insufficientBalanceLabel.isVisible();
    }

    @FXML
    void handleTransferButtonClicked(MouseEvent event) {
        transfer();
    }

    private void clearAllTextFields() {
        amountField.setText("");
        recipientField.setText("");
        IBANField.setText("");
        messageField.setText("");
        dateField.setText("");
        passwordField.setText("");
    }

    @FXML
    void handleComponentKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            emulateBackButtonClicked();
        } else if (event.getCode() == KeyCode.ENTER) {
            transfer();
        }
        event.consume();
    }

}
