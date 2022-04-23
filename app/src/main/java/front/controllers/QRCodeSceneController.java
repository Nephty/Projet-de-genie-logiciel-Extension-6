package front.controllers;

import app.Main;
import front.navigation.Flow;
import front.navigation.navigators.BackButtonNavigator;
import javafx.scene.input.MouseEvent;
import org.json.JSONObject;

public class QRCodeSceneController extends Controller implements BackButtonNavigator {
    @Override
    public void handleBackButtonNavigation(MouseEvent mouseEvent) {
        Main.setScene(Flow.back());
    }

    @Override
    public void emulateBackButtonClicked() {
        // handleBackButtonClicked();
    }
}
