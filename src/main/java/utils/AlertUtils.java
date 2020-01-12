package utils;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertUtils {
    public static void showNotificationAlert(Stage stage, String titleString, String contentString) {
        JFXAlert<Void> alert = new JFXAlert<>(stage);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setOverlayClose(false);

        JFXDialogLayout layout = new JFXDialogLayout();
        Label title = new Label(titleString);
        title.setStyle("-fx-font-size: 24px");
        layout.setHeading(title);

        ImageView icon = new ImageView(new Image("images/notificationIcon.png"));
        title.setGraphic(icon);
        title.setGraphicTextGap(10);

        Label content = new Label(contentString);
        content.setStyle("-fx-font-size: 18px");
        layout.setBody(content);

        JFXButton closeButton = new JFXButton("Close");
        closeButton.setPrefSize(400, 70);
        closeButton.setStyle("-fx-background-color: #2196F3; " +
                "-fx-text-fill: white; " +
                "-jfx-button-type: RAISED; " +
                "-fx-font-size: 18px");
        closeButton.setOnAction(e -> alert.hideWithAnimation());
        layout.setActions(closeButton);
        alert.setContent(layout);

        alert.show();
    }
}
