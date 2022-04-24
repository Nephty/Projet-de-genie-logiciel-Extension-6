package front.controllers;

import app.Main;
import back.user.*;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import front.navigation.Flow;
import front.navigation.navigators.BackButtonNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;

public class ReadQRCodeSceneController extends Controller implements BackButtonNavigator {
    @FXML
    ComboBox<Account> accountsComboBox;
    @FXML
    Button backButton, choosePathButton, scanButton;
    @FXML
    Label chooseFileLabel, fileScannedSuccessfully, noPathSelectedLabel, imageFileLabel, insufficientBalanceLabel, chooseAccountLabel, pleaseSelectAnAccountLabel;

    private boolean fileScanned = false;
    private File selectedFile;
    private boolean fileChosen = false;
    private FileChooser QRCodeFileChooser;

    public void initialize() {
        QRCodeFileChooser = new FileChooser();
        ExtensionFilter PNGFilter = new ExtensionFilter("PNG files (*.png)", "*.png");
        QRCodeFileChooser.getExtensionFilters().add(PNGFilter);

        ObservableList<Account> values = FXCollections.observableArrayList();
        for (Wallet wallet : Main.getPortfolio().getWalletList()) {
            values.addAll(wallet.getAccountList());
        }
        accountsComboBox.setItems(values);
    }

    @Override
    public void handleBackButtonNavigation(MouseEvent event) {
        Main.setScene(Flow.back());
    }

    @Override
    public void emulateBackButtonClicked() {
        handleBackButtonClicked(null);
    }

    @FXML
    void handleBackButtonClicked(MouseEvent event) {
        handleBackButtonNavigation(event);
        if (fileScanned) {
            selectedFile = null;
            imageFileLabel.setText("QR code file not set.");
            fileScanned = false;
            fileChosen = false;
        }
    }

    @FXML
    void handleChooseFileButtonClicked(MouseEvent event) {
        selectedFile = QRCodeFileChooser.showOpenDialog(Main.getStage());
        if (selectedFile != null) {
            // File chosen (in file variable)
            // We don't need to test if it equals null : when we arrive on the scene and if the user doesn't select any
            // file, the label is already saying "no file selected", and when the user selects a file, then re-opens
            // the file chooser and closes it (returning null), we want to keep the previously selected file
            imageFileLabel.setText("Image chosen : " + selectedFile.getName());
            fileChosen = true;
            // If we imported a file and choose another file, we reset import done so that going back won't reset the form
            fileScanned = false;
        }
    }

    @FXML
    void handleScanButtonClicked(MouseEvent event) {
        scanQRCode();
    }

    /**
     * Checks if every input is correct, and reads the selected QR code and execute the transfer if so.
     */
    private void scanQRCode() {
        if (!fileChosen) {
            if (!noPathSelectedLabel.isVisible()) noPathSelectedLabel.setVisible(true);
            return;
        }
        if (accountsComboBox.getValue() == null) {
            if (!pleaseSelectAnAccountLabel.isVisible()) pleaseSelectAnAccountLabel.setVisible(true);
            return;
        }
        if (pleaseSelectAnAccountLabel.isVisible()) pleaseSelectAnAccountLabel.setVisible(false);
        if (noPathSelectedLabel.isVisible()) noPathSelectedLabel.setVisible(false);
        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(selectedFile.getAbsolutePath())))));
            Result result = new MultiFormatReader().decode(binaryBitmap);

            ArrayList<String> transaction = Portfolio.JSONArrayParser(result.getText().substring(1, result.getText().length() - 1));
            String amountString = transaction.get(0), IBANString = transaction.get(1), nameString = transaction.get(2), messageString = transaction.get(3);
            double amount = Double.parseDouble(amountString.substring(amountString.indexOf(":") + 2, amountString.length() - 1));
            if (accountsComboBox.getValue().getSubAccountList().get(0).getAmount() < amount && !insufficientBalanceLabel.isVisible()) {
                insufficientBalanceLabel.setVisible(true);
                return;
            }
            if (insufficientBalanceLabel.isVisible()) insufficientBalanceLabel.setVisible(false);
            String IBAN = IBANString.substring(IBANString.indexOf(":") + 2, IBANString.length() - 1);
            String name = nameString.substring(nameString.indexOf(":") + 2, nameString.length() - 1);
            String message = messageString.substring(messageString.indexOf(":") + 2, messageString.length() - 1);

            HttpResponse<String> response = ErrorHandler.handlePossibleError(() -> {
                HttpResponse<String> rep = null;
                try {
                    rep = Unirest.post("https://flns-spring-test.herokuapp.com/api/transaction")
                            .header("Authorization", "Bearer " + Main.getToken())
                            .header("Content-Type", "application/json")
                            .body("{\r\n    \"transactionTypeId\": 1,\r\n    \"senderIban\": \"" + accountsComboBox.getValue().getIBAN() + "\",\r\n    \"recipientIban\": \"" + IBAN + "\",\r\n    \"currencyId\": 0,\r\n    \"transactionAmount\": " + amount + ",\r\n    \"comments\": \"" + message + "\"\r\n}")
                            .asString();
                } catch (UnirestException e) {
                    throw new RuntimeException(e);
                }
                return rep;
            });

            selectedFile = null;
            fileChosen = false;
            fileScanned = false;
            accountsComboBox.setValue(null);
            imageFileLabel.setText("No image selected.");

            fadeInAndOutNode(1000, fileScannedSuccessfully);
        } catch (NotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleComponentKeyReleased(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            emulateBackButtonClicked();
        }
        keyEvent.consume();
    }

    @FXML
    void handleAccountsComboBoxMouseClicked(MouseEvent event) {
        fileScanned = false;
    }
}

