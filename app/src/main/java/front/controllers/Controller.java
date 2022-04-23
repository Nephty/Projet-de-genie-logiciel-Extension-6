package front.controllers;

import app.Main;
import back.user.Portfolio;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import front.animation.FadeInTransition;
import front.animation.threads.FadeOutThread;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This mother Controller class gives access to a few useful methods to all children classes.
 * Having access to these methods provide shortcuts. For example :
 * The method formatCurrentTime() uses a <code>Calendar</code> object to return the current time in a defined format.
 * This allows every controller to have access to a method to format time, rather than defining it in all FXML controllers.
 * It also makes the code more readable.
 * @author Arnaud MOREAU
 */
public class Controller {
    /**
     * Returns the visibility of a <code>Node</code> using its opacity. If the node's opacity is 0, then it is
     * considered invisible. If the node's opacity is not 0, it is considered visible. This method provides an
     * alternative to the isVisible() method which returns whether the attribute "visible" is true or false.
     * This is mainly used with transitions, since these use the opacity of the node, and not it's core visibility.
     * It is not recommended using this method if you don't alter the node's opacity is such a way that it
     * purposely reaches 0 or 1 (for example, using fade in/fade out transitions). If you alter the node's visibility
     * to make it, let's say, 0.4, using the <code>visible</code> attribute of the node is probably a better way to go,
     * because you'd probably want to know if it is visible at its core, not if the opacity is other than 0.
     *
     * @param node - <code>Node</code> - The node to check
     * @return Whether the node's opacity is not 0 or not : that is, true if the node's opacity is not 0,
     * false otherwise.
     */
    public static boolean isVisibleUsingOpacity(Node node) {
        return node.getOpacity() != 0;
    }

    /**
     * Returns a <code>String</code> containing the current time and date of the given <code>Calendar</code>,
     * but formatted for display.
     *
     * @param calendar The calendar providing the desired time and date
     * @return A string containing the current time and date of the given <code>Calendar</code>, but formatted for
     * display.
     */
    public static String formatCurrentTime(Calendar calendar) {
        String res = "";
        if (calendar.get(Calendar.DAY_OF_MONTH) < 10) res += "0";
        res += calendar.get(Calendar.DAY_OF_MONTH);
        res += "-";
        if (calendar.get(Calendar.MONTH) + 1 < 10) res += "0";
        res += calendar.get(Calendar.MONTH) + 1;
        res += "-";
        res += calendar.get(Calendar.YEAR);
        res += " ~ ";
        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) res += "0";
        res += calendar.get(Calendar.HOUR_OF_DAY);
        res += ":";
        if (calendar.get(Calendar.MINUTE) < 10) res += "0";
        res += calendar.get(Calendar.MINUTE);
        res += ":";
        if (calendar.get(Calendar.SECOND) < 10) res += "0";
        res += calendar.get(Calendar.SECOND);
        return res;
    }

    /**
     * Returns a new style line with the darker background color to use when the mouse enters a button.
     *
     * @param button The entered button
     * @return The CSS line
     */
    public static String formatNewCSSLineMouseEntered(Button button) {
        String CSSLine = "";
        if (button.getPrefWidth() == 350) {
            // Big buttons
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -2.5; -fx-border-radius: 10;";
        } else if (button.getPrefWidth() == 120) {
            // Language & Back buttons
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 296) {
            // Change password button
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -2.5; -fx-border-radius: 10;";
        } else if (button.getPrefWidth() == 50) {
            // Square buttons for PIN
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -1.5; -fx-border-radius: 4;";
        } else if (button.getPrefWidth() == 200) {
            if (button.getText().toLowerCase().contains("path") || button.getText().toLowerCase().contains("choose")) {
                CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
            } else {
                // Confirm button for PIN
                CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -2.5; -fx-border-radius: 10;";
            }
        } else if (button.getPrefWidth() == 250) {
            // Export buttons
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 170) {
            // Right side buttons
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 75) {
            // Add button for language
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 135) {
            // Add/remove account for visualisation
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 100) {
            // Search button
            CSSLine = "-fx-background-color: rgb(190, 185, 180); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        }
        return CSSLine;
    }

    /**
     * Returns a new style line with the original background color to use when the mouse exits a button.
     *
     * @param button The exited button
     * @return The CSS line
     */
    public static String formatNewCSSLineMouseExited(Button button) {
        String CSSLine = "";
        if (button.getPrefWidth() == 350) {
            // Big buttons
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -2.5; -fx-border-radius: 10;";
        } else if (button.getPrefWidth() == 120) {
            // Language & Back buttons
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 296) {
            // Change password button
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -2.5; -fx-border-radius: 10;";
        } else if (button.getPrefWidth() == 50) {
            // Square buttons for PIN
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -1.5; -fx-border-radius: 4;";
        } else if (button.getPrefWidth() == 200) {
            if (button.getText().toLowerCase().contains("path") || button.getText().toLowerCase().contains("choose")) {
                CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
            } else {
                // Confirm button for PIN
                CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 2.5; -fx-border-insets: -2.5; -fx-border-radius: 10;";
            }
        } else if (button.getPrefWidth() == 250) {
            // Export buttons
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 170) {
            // Right side buttons
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 75) {
            // Add button for language
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 135) {
            // Add/remove account for visualisation
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        } else if (button.getPrefWidth() == 100) {
            // Search button
            CSSLine = "-fx-background-color: rgb(210, 205, 200); -fx-border-color: rgb(10, 10, 20); -fx-border-width: 1.5; -fx-border-insets: -1.5; -fx-border-radius: 5;";
        }
        return CSSLine;
    }

    /**
     * Checks if the given <code>String</code> is a valid amount.
     * Requirements :
     * - string must not be empty
     * - string must not be null
     * - string must only contain characters from 0-9
     * - string must contain at least one digit that is not 0
     * - string must contain at most one dot (.)
     * - string length must be at most 12
     *
     * @param amount - <code>String</code> - the amount to check
     * @return <code>boolean</code> - whether the given amount is a valid amount or not
     */
    public static boolean isValidAmount(String amount) {
        if (amount == null) return false;
        if (amount.equals("") || (!amount.matches("^[0-9.]*$")) || amount.length() > 12) return false;
        boolean hasOneDot = false;
        boolean hasOnlyZeros = true;
        for (int i = 0; i < amount.length(); i++) {
            if (amount.charAt(i) == '.' && hasOneDot) return false;
            if (amount.charAt(i) == '.') hasOneDot = true;
            if (amount.charAt(i) >= 49 && amount.charAt(i) <= 57) hasOnlyZeros = false;
        }
        return !hasOnlyZeros;
    }

    /**
     * Checks if the given <code>String</code> is a valid recipient.
     * Requirements :
     * - string must only contain characters from a-z and from A-Z
     *
     * @param recipient - <code>String</code> - the recipient to check
     * @return <code>boolean</code> - whether the given recipient is a valid recipient or not
     */
    public static boolean isValidRecipient(String recipient) {
        if (recipient == null) return true;
        return recipient.matches("^[a-zA-Z]*$");
    }

    /**
     * Checks if the given <code>String</code> is a valid IBAN.
     * Requirements :
     * - string must not be empty
     * - string must not be null
     * - string must only contain characters from a-z, from A-Z and from 0-9
     * - string must follow this format : AAXXXXXXXXXXXXXX where A is a letter and X is a digit
     *
     * @param IBAN - <code>String</code> - the IBAN to check
     * @return <code>boolean</code> - whether the given IBAN is a valid IBAN or not
     */
    public static boolean isValidIBAN(String IBAN) {
        if (IBAN == null) return false;
        if ((!IBAN.matches("^[a-zA-Z0-9]*$")) || !(IBAN.length() == 16))
            return false;  // IBAN.length() == 16 already checks IBAN != ""
        for (int i = 0; i < IBAN.length(); i++) {
            switch (i) {
                case 0:
                case 1:
                    if (!Character.isAlphabetic(IBAN.charAt(i))) return false;
                    break;
                default:
                    if (!Character.isDigit(IBAN.charAt(i))) return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given <code>String</code> is a valid message.
     * Requirements :
     * - string must only contain characters with ASCII code between 32 and 126 both included
     * - string length must be at most 256
     *
     * @param message - <code>String</code> - the message to check
     * @return <code>boolean</code> - whether the given message is a valid message or not
     */
    public static boolean isValidMessage(String message) {
        if (message == null) return true;
        if (message.equals("")) return true;
        if (message.length() > 256) return false;
        for (int i = 0; i < message.length(); i++) {
            if (!(message.charAt(i) >= 32 && message.charAt(i) <= 126)) return false;
        }
        return true;
    }

    /**
     * Checks if the given <code>String</code> is a valid date.
     * Requirements :
     * - string must only contain characters from 0-9 or dashes (-) or dots (.) (dashes and dots are mutually exclusive :
     * the string either contains dots or dashes, but not both)
     * - string must follow this format : XX.XX.XXXX or XX-XX-XXXX where X is a digit
     * - the year cannot be less than the current year and cannot be more than 1 year in the future
     *
     * @param date - <code>String</code> - the date to check
     * @return <code>boolean</code> - whether the given date is a valid date or not
     */
    public static boolean isValidDate(String date) {
        if (date == null) return true;
        if (date.equals("")) return true;
        if (!date.matches("^[0-9-.]*$") || !(date.length() == 10)) return false;
        boolean hasDot = false, hasDash = false;
        for (int i = 0; i < date.length(); i++) {
            switch (i) {
                case 0:
                case 1:
                case 3:
                case 4:
                case 6:
                case 7:
                case 8:
                case 9:
                    if (!Character.isDigit(date.charAt(i))) return false;
                    break;
                case 2:
                case 5:
                    if (date.charAt(i) == '.') hasDot = true;
                    if (date.charAt(i) == '-') hasDash = true;
                    if (date.charAt(i) == '.' && hasDash) return false;
                    if (date.charAt(i) == '-' && hasDot) return false;
                    break;
            }
        }
        int day = Integer.parseInt(date.charAt(0) + "" + date.charAt(1));
        int month = Integer.parseInt(date.charAt(3) + "" + date.charAt(4));
        int year = Integer.parseInt(date.charAt(6) + "" + date.charAt(7) + date.charAt(8) + date.charAt(9));
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (!(day <= 31 && day >= 1)) return false;
        if (!(month <= 12 && month >= 0)) return false;
        return year >= currentYear && year <= currentYear + 1;
    }

    /**
     * Checks if the given passwords match and are not empty.
     *
     * @param password             - <code>String</code> - the password
     * @param passwordConfirmation - <code>String</code> - the password confirmation
     * @return <code>boolean</code> - whether the two passwords match or not
     */
    public boolean passwordMatchesAndIsNotEmpty(String password, String passwordConfirmation) {
        return password.equals(passwordConfirmation) && !password.equals("");
    }

    @FXML
    void handleButtonMouseEntered(MouseEvent event) {
        Button buttonSource = (Button) event.getSource();
        buttonSource.setStyle(formatNewCSSLineMouseEntered(buttonSource));
    }

    @FXML
    void handleButtonMouseExited(MouseEvent event) {
        Button buttonSource = (Button) event.getSource();
        buttonSource.setStyle(formatNewCSSLineMouseExited(buttonSource));
    }

    /**
     * Fades in and out the given node using its opacity values for a pre-determined durations in the following order :
     * - fade in (opacity rises to a level of 1 over a period of 1 second)
     * - sleep (opacity remains at a level of 1)
     * - fade out (opacity decreases to a level of 0 over a period of 1 second)
     * Note that this method forces the fade in and fade out transitions to last for exactly 1 second.
     *
     * @param sleepDuration the time that should pass by after the fade in transition and before the fade out transition
     * @param node          the node which should fade in and out
     */
    public void fadeInAndOutNode(int sleepDuration, Node node) {
        int fadeInDuration = 1000, fadeOutDuration = 1000;
        FadeOutThread sleepAndFadeOutFadeThread;
        FadeInTransition.playFromStartOn(node, Duration.millis(fadeInDuration));
        sleepAndFadeOutFadeThread = new FadeOutThread();
        sleepAndFadeOutFadeThread.start(fadeOutDuration, sleepDuration + fadeInDuration, node);
    }

    /**
     * Using an ArrayList of Strings that are the read content of a JSON file, creates JSON objects and puts them in
     * an ArrayList
     * @param parsedContent an ArrayList of Strings that are the read content of a JSON file
     * @return An ArrayList of JSONObjects that are made of the given Strings
     */
    protected ArrayList<JSONObject> convertReadDueTransactionsToJSONObjects(ArrayList<String> parsedContent) {
        ArrayList<JSONObject> res = new ArrayList<>();
        for (String s : parsedContent) {
            while (s.charAt(0) != '{') {
                s = s.substring(1);
            }
            res.add(new JSONObject(s));
        }
        return res;
    }

    /**
     * Reads the given file and puts every read JSONObject in an ArrayList.
     * @param file the file to read
     * @return an ArrayList containing all the read JSONObjects
     */
    protected ArrayList<String> readDueTransactionsAsStrings(File file) {
        String readContent = "";

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
        return Portfolio.JSONArrayParser(readContent);
    }

    protected boolean isValidPassword(String password) {
        HttpResponse<String> response = null;
        try {
            response = Unirest.post("https://flns-spring-test.herokuapp.com/api/login")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .field("username", Main.getUser().getUsername())
                    .field("password", password)
                    .field("role", "ROLE_USER")
                    .asString();
            // Check the HTTP code status to inform the user if there is an error
            Main.errorCheck(response.getStatus());
        } catch (UnirestException e) {
            Main.ErrorManager(408);
        }

        if (response != null) {
            return response.getStatus() == 200;
        }
        return false;
    }
}
