package controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;
import utils.GUIUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("DuplicatedCode")
public class SaveImageController {
    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView coverImageView;

    private Stage mainStage;
    private Stage currentStage;

    private String originalImageFileName;
    private Image originalImage;
    private BufferedImage coverImage;
    private int bitsUsed;
    private File originalImageFile;

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

    public void setOriginalImageFileName(String originalImageFileName) {
        this.originalImageFileName = originalImageFileName;
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

    public void setBitsUsed(int bitsUsed) {
        this.bitsUsed = bitsUsed;
    }

    public void setOriginalImageFile(File originalImageFile) {
        this.originalImageFile = originalImageFile;
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
            fileChooser.setInitialFileName(originalImageFileName + ".png");

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
    private void showImageDifferenceButtonHandler(ActionEvent actionEvent) {
        try {
            // load the new window and the components
            FXMLLoader fxmlLoader = new FXMLLoader(GUIUtils.class.getResource("../view/ImageDifferenceView.fxml"));
            Parent root = fxmlLoader.load();

            // send the images to the controller so that they can be displayed
            ImageDifferenceController controller = fxmlLoader.getController();
            controller.setImages(originalImageFile, coverImage, bitsUsed);

            // show the new window on the screen, modal
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Image Difference");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            scene.getRoot().getStylesheets().add(GUIUtils.class.getResource("../css/styles.css").toExternalForm());
            stage.showAndWait();
        } catch (Exception e) {
            AlertUtils.showNotificationAlert(mainStage,
                    "Saving error!",
                    "The cover image can not be displayed.");
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
