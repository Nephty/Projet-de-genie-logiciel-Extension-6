package front.controllers;

import app.Main;
import back.user.Currencies;
import back.user.Portfolio;
import back.user.Transaction;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import front.animation.FadeInTransition;
import front.animation.threads.FadeOutThread;
import front.navigation.Flow;
import front.navigation.navigators.BackButtonNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;

public class DueTransactionsSceneController extends Controller implements BackButtonNavigator {
    @FXML
    Label lastUpdateTimeLabel;
    @FXML
    Label loadingTransactionsHistoryLabel;
    @FXML
    TableColumn<Transaction, Double> amountColumn;
    @FXML
    TableColumn<Transaction, String> receiverNameColumn, receiverIBANColumn;
    @FXML
    TableView<Transaction> dueTransactionsTableView;
    @FXML
    Button backButton, payButton, payWithoutContactButton;

    private ObservableList<Transaction> data = FXCollections.observableArrayList();
    static String selectedAccountIBAN;

    public void initialize() {
        receiverNameColumn.setCellValueFactory(new PropertyValueFactory<>("receiverName"));
        receiverIBANColumn.setCellValueFactory(new PropertyValueFactory<>("receiverIBAN"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dueTransactionsTableView.setPlaceholder(new Label("No due transaction."));
        updateDueTransactions();
    }

    private void updateDueTransactions() {
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

            String readContent = "";
            File file = null;
            try {
                file = new File(new URL(Main.class.getResource("/client/duetransactions.json").toString()).toURI());
            } catch (URISyntaxException | MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                if (file != null) {
                    StringBuilder bobTheBuilder = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            bobTheBuilder.append(line).append("\n");
                        }
                    }
                    readContent = bobTheBuilder.toString();
                } else {
                    throw new FileNotFoundException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            readContent = readContent.replace("\n", "");
            readContent = readContent.replaceAll("\\s\\s", "");
            readContent = readContent.substring(1, readContent.length() - 1);
            ArrayList<String> parsedContent = Portfolio.JSONArrayParser(readContent);
            for (String s : parsedContent) {
                while (s.charAt(0) != '{') {
                    s = s.substring(1);
                }
                JSONObject obj = new JSONObject(s);
                if (!obj.getBoolean("done")) {
                    data.add(new Transaction(obj.getLong("id"),
                            Main.getUser().getFirstName() + " " + Main.getUser().getLastName(), selectedAccountIBAN,
                            obj.getString("recipient_name"), obj.getString("recipient_IBAN"), obj.getDouble("amount"),
                            Date.valueOf(LocalDate.now()).toString(), Currencies.EUR, obj.getString("message")));
                }
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

    }

    @FXML
    void handlePayWithoutContactButtonMouseClicked(MouseEvent mouseEvent) {
        if (dueTransactionsTableView.getSelectionModel().getSelectedItems().size() == 1) {
            Transaction selectedTransaction = dueTransactionsTableView.getSelectionModel().getSelectedItems().get(0);
            try {
                // TODO : check if sufficient balance (other thing to check to see if transaction is realizable ?)
                Unirest.post("https://flns-spring-test.herokuapp.com/api/transaction")
                        .header("Authorization", "Bearer " + Main.getToken())
                        .header("Content-Type", "application/json")
                        .body("{\r\n    \"transactionTypeId\": 1,\r\n    \"senderIban\": \"" + selectedTransaction.getSenderIBAN() + "\",\r\n    \"recipientIban\": \"" + selectedTransaction.getReceiverIBAN() + "\",\r\n    \"currencyId\": 0,\r\n    \"transactionAmount\": " + selectedTransaction.getAmount() + ",\r\n    \"comments\": \"" + selectedTransaction.getMessage() + "\"\r\n}")
                        .asString();
                data.remove(selectedTransaction);
                dueTransactionsTableView.setItems(data);
                // TODO : set transaction as done in JSON file
            } catch (UnirestException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
