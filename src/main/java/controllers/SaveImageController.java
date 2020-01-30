package controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.AlertUtils;
import utils.GUIUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SaveImageController {
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView coverImageView;

    private Stage mainStage;
    private Stage currentStage;

    private Image originalImage;
    private BufferedImage coverImage;

    @FXML
    private void initialize() {
        // Parametrize the image views
        originalImageView.setPreserveRatio(true);
        coverImageView.setPreserveRatio(true);
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void setOriginalImage(Image originalImage) {
        this.originalImage = originalImage;
        originalImageView.setImage(originalImage);
        GUIUtils.centerImage(originalImageView);
    }

    public void setCoverImage(BufferedImage coverImage) {
        this.coverImage = coverImage;
        coverImageView.setImage(SwingFXUtils.toFXImage(coverImage, null));
        GUIUtils.centerImage(coverImageView);
    }

    @FXML
    private void saveImageButtonHandler(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save cover image as...");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("All Images", "*.*")
            );

            File coverImageFile = fileChooser.showSaveDialog(currentStage);
            if (coverImageFile != null) {
                ImageIO.write(coverImage, "png", coverImageFile);
                AlertUtils.showNotificationAlert(mainStage,
                        "Saving successful!",
                        "The cover image was saved successfully.");
                closeWindow();
            }
        } catch (IOException exception) {
            AlertUtils.showNotificationAlert(currentStage,
                    "Saving error!",
                    "The cover image could not be saved.");
        }
    }

    @FXML
    private void cancelButtonHandler(ActionEvent actionEvent) {
        closeWindow();
    }

    private void closeWindow() {
        currentStage.close();
    }
}
