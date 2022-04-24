package front.controllers;

import app.Main;
import back.user.ErrorHandler;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
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
import javafx.stage.DirectoryChooser;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class GenerateQRCodeSceneController extends Controller implements BackButtonNavigator {
    @FXML
    Button backButton, generateButton, choosePathButton;
    @FXML
    TextField amountField, recipientField, IBANField, messageField;
    @FXML
    Label invalidAmountLabel, invalidRecipientLabel, invalidIBAN, invalidMessageLabel,
            QRCodeGeneratedLabel, charactersLeftLabel, invalidPasswordLabel, noPathSelectedLabel, choosePathLabel, exportLocationLabel;

    private File selectedDirectory;
    final DirectoryChooser directoryChooser = new DirectoryChooser();
    private boolean directoryChosen = false;
    private boolean QRCodeGenerated = false;

    public void initialize() {
        QRCodeGenerated = false;
        directoryChosen = false;
        selectedDirectory = null;
    }

    @Override
    public void handleBackButtonNavigation(MouseEvent mouseEvent) {
        Main.setScene(Flow.back());
    }

    @Override
    public void emulateBackButtonClicked() {
        handleBackButtonNavigation(null);
    }

    @FXML
    void handleBackButtonClicked(MouseEvent event) {
        handleBackButtonNavigation(event);
        QRCodeGeneratedLabel.setVisible(false);
        if (QRCodeGenerated) clearAllTextFields();
        if (QRCodeGenerated) hideAllLabels();
        QRCodeGenerated = false;
    }

    private void hideAllLabels() {
        invalidPasswordLabel.setVisible(false);
        charactersLeftLabel.setVisible(false);
        invalidAmountLabel.setVisible(false);
        invalidMessageLabel.setVisible(false);
        invalidRecipientLabel.setVisible(false);
    }

    /**
     * Checks if every input is correct, and creates a new QR code if so at the given location.
     */
    private void generateQRCode() {
        String amount = amountField.getText(), recipient = recipientField.getText(), IBAN = IBANField.getText(),
                message = messageField.getText();

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

        if (noLabelVisible() && directoryChosen) {
            // Creates the transfer if everything is correct
            createQRCodeFromText("{\"amount\":\"" + amount + "\",\"recipient_IBAN\":\"" + IBAN + "\",\"recipient_name\":\"" + recipient + "\",\"message\":\"" + message + "\"}", selectedDirectory);
            QRCodeGenerated = true;
            Main.setScene(Flow.back());
            exportLocationLabel.setText("Location not set.");
            clearAllTextFields();
        }
    }

    /**
     * Generates a QR code with the given text and saves it in the given directory.
     * @param text the text to put in the QR code
     * @param directory the directory where to put the QR code
     */
    public void createQRCodeFromText(String text, File directory) {
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (matrix != null) {
            try {
                MatrixToImageWriter.writeToPath(matrix, "png", Paths.get(new File(directory + "/generated_qr_code_" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now()) + ".png").getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean noLabelVisible() {
        return !invalidRecipientLabel.isVisible() && !invalidIBAN.isVisible()
                && !invalidMessageLabel.isVisible() && !invalidPasswordLabel.isVisible();
    }

    @FXML
    void handleGenerateButtonClicked(MouseEvent event) {
        generateQRCode();
    }

    private void clearAllTextFields() {
        amountField.setText("");
        recipientField.setText("");
        IBANField.setText("");
        messageField.setText("");
    }

    @FXML
    void handleComponentKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            emulateBackButtonClicked();
        } else if (event.getCode() == KeyCode.ENTER) {
            generateQRCode();
        }
        event.consume();
    }

    @FXML
    void handleChoosePathButtonClicked(MouseEvent event) {
        selectedDirectory = directoryChooser.showDialog(Main.getStage());
        if (selectedDirectory != null) {
            exportLocationLabel.setText("Selected path : " + selectedDirectory.getPath());
            directoryChosen = true;
            QRCodeGenerated = false;
        }
    }
}
