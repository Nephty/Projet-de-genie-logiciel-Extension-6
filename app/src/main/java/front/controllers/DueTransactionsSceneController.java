package front.controllers;

import app.Main;
import back.user.Currencies;
import back.user.Portfolio;
import back.user.SubAccount;
import back.user.Transaction;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import front.animation.FadeInTransition;
import front.animation.threads.FadeOutThread;
import front.navigation.Flow;
import front.navigation.navigators.BackButtonNavigator;
import front.scenes.SceneLoader;
import front.scenes.Scenes;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @author Arnaud MOREAU
 */
public class DueTransactionsSceneController extends Controller implements BackButtonNavigator {
    @FXML
    Label lastUpdateTimeLabel;
    @FXML
    Label loadingTransactionsHistoryLabel, recipientIBANNotFoundLabel, insufficientBalanceLabel, transferExecutedLabel;
    @FXML
    TableColumn<Transaction, Double> amountColumn;
    @FXML
    TableColumn<Transaction, String> receiverNameColumn, receiverIBANColumn;
    @FXML
    TableView<Transaction> dueTransactionsTableView;
    @FXML
    Button backButton, payButton, payWithoutContactButton, chooseFileButton;

    private ObservableList<Transaction> data = FXCollections.observableArrayList();
    static SubAccount selectedSubAccount;

    final FileChooser fileChooser = new FileChooser();

    public void initialize() {
        receiverNameColumn.setCellValueFactory(new PropertyValueFactory<>("receiverName"));
        receiverIBANColumn.setCellValueFactory(new PropertyValueFactory<>("receiverIBAN"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dueTransactionsTableView.setPlaceholder(new Label("No due transaction."));
    }

    private void updateDueTransactions(File file) {
        if (loadingTransactionsHistoryLabel.getOpacity() == 0.0) {
            int fadeInDuration = 1000;
            int sleepDuration = 1000;
            FadeOutThread sleepAndFadeOutLoadingTransactionsLabelFadeThread;
            // Fade the label "updating history..." in to 1.0 opacity
            FadeInTransition.playFromStartOn(loadingTransactionsHistoryLabel, Duration.millis(fadeInDuration));
            // We use a new Thread, so we can sleep the method for a few hundreds of milliseconds so that the label
            // doesn't instantly go away when the notifications are retrieved.
            sleepAndFadeOutLoadingTransactionsLabelFadeThread = new FadeOutThread();
            // Save actual time and date
            Calendar c = Calendar.getInstance();
            // Update lastUpdateLabel with the new time and date
            lastUpdateTimeLabel.setText("Last update : " + formatCurrentTime(c));
            // Fetch transactions and put them in the listview
            // Fade the label "updating history..." out to 0.0 opacity

            ArrayList<String> parsedContent = readDueTransactionsAsStrings(file);
            for (JSONObject jsonObject : convertReadDueTransactionsToJSONObjects(parsedContent)) {
                String today = Date.valueOf(LocalDate.now()).toString();
                today = today.substring(8) + "-" + today.substring(5, 7) + "-" + today.substring(0, 4);
                data.add(new Transaction(Main.getUser().getFirstName() + " " + Main.getUser().getLastName(),
                        selectedSubAccount.getIBAN(), jsonObject.getString("recipient_name"),
                        jsonObject.getString("recipient_IBAN"), jsonObject.getDouble("amount"),
                        today, Currencies.EUR, jsonObject.getString("message")));
            }
            dueTransactionsTableView.setItems(FXCollections.observableArrayList(data));
            sleepAndFadeOutLoadingTransactionsLabelFadeThread.start(fadeInDuration, sleepDuration + fadeInDuration, loadingTransactionsHistoryLabel);
        }

    }

    @Override
    public void handleBackButtonNavigation(MouseEvent mouseEvent) {
        Main.setScene(Flow.back());
    }

    @Override
    public void emulateBackButtonClicked() {
        handleBackButtonClicked(null);
        recipientIBANNotFoundLabel.setVisible(false);
        insufficientBalanceLabel.setVisible(false);
    }

    @FXML
    void handleComponentKeyReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            emulateBackButtonClicked();
        }
        keyEvent.consume();
    }

    @FXML
    void handleBackButtonClicked(MouseEvent mouseEvent) {
        handleBackButtonNavigation(mouseEvent);
    }

    @FXML
    void handlePayButtonMouseClicked(MouseEvent mouseEvent) {
        if (dueTransactionsTableView.getSelectionModel().getSelectedItems().size() == 1) {
            TransferSceneController.comesFromDuePaymentsScene = true;
            TransferSceneController.duePayment = dueTransactionsTableView.getSelectionModel().getSelectedItems().get(0);
            TransferSceneController.selectedAccountAmount = selectedSubAccount.getAmount();
            Scenes.TransferScene = SceneLoader.load("TransferScene.fxml", Main.appLocale);
            Main.setScene(Flow.forward(Scenes.TransferScene));
            data.remove(dueTransactionsTableView.getSelectionModel().getSelectedItems().get(0));
            dueTransactionsTableView.setItems(data);
        }
    }

    @FXML
    void handlePayWithoutContactButtonMouseClicked(MouseEvent mouseEvent) {
        if (dueTransactionsTableView.getSelectionModel().getSelectedItems().size() == 1) {
            Transaction selectedTransaction = dueTransactionsTableView.getSelectionModel().getSelectedItems().get(0);
            try {
                int fadeInDuration = 1000;
                int sleepDuration = 1000;
                FadeOutThread sleepAndFadeOutLoadingTransactionsLabelFadeThread;
                // Fade the label "updating history..." in to 1.0 opacity
                FadeInTransition.playFromStartOn(transferExecutedLabel, Duration.millis(fadeInDuration));
                // We use a new Thread, so we can sleep the method for a few hundreds of milliseconds so that the label
                // doesn't instantly go away when the notifications are retrieved.
                sleepAndFadeOutLoadingTransactionsLabelFadeThread = new FadeOutThread();


                HttpResponse<String> getRecipientIBANResponse = Unirest.get("https://flns-spring-test.herokuapp.com/api/account/" + selectedTransaction.getReceiverIBAN())
                        .header("Authorization", "Bearer " + Main.getToken())
                        .header("Content-Type", "application/json").asString();
                boolean doesIBANExist = getRecipientIBANResponse.getStatus() == 200;
                if (!doesIBANExist) {
                    if (!recipientIBANNotFoundLabel.isVisible()) recipientIBANNotFoundLabel.setVisible(true);
                    return;
                }
                if (recipientIBANNotFoundLabel.isVisible()) recipientIBANNotFoundLabel.setVisible(false);
                if (selectedSubAccount.getAmount() < selectedTransaction.getAmount()) {
                    if (!insufficientBalanceLabel.isVisible()) insufficientBalanceLabel.setVisible(true);
                    return;
                }
                Unirest.post("https://flns-spring-test.herokuapp.com/api/transaction")
                        .header("Authorization", "Bearer " + Main.getToken())
                        .header("Content-Type", "application/json")
                        .body("{\r\n    \"transactionTypeId\": 1,\r\n    \"senderIban\": \"" + selectedTransaction.getSenderIBAN() + "\",\r\n    \"recipientIban\": \"" + selectedTransaction.getReceiverIBAN() + "\",\r\n    \"currencyId\": 0,\r\n    \"transactionAmount\": " + selectedTransaction.getAmount() + ",\r\n    \"comments\": \"" + selectedTransaction.getMessage() + "\"\r\n}")
                        .asString();
                if (insufficientBalanceLabel.isVisible()) insufficientBalanceLabel.setVisible(false);
                data.remove(selectedTransaction);
                dueTransactionsTableView.setItems(data);

                sleepAndFadeOutLoadingTransactionsLabelFadeThread.start(fadeInDuration, sleepDuration + fadeInDuration, transferExecutedLabel);
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    void handleChooseFileButtonClicked(MouseEvent mouseEvent) {
        File selectedFile = fileChooser.showOpenDialog(Main.getStage());
        updateDueTransactions(selectedFile);
    }
}
