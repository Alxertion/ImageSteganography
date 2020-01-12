package controllers;

import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import utils.AlertUtils;
import utils.GUIUtils;

import java.io.File;

@SuppressWarnings("DuplicatedCode")
public class MainController {
    // COLORS
    Color primaryColor = new Color(33 / 255d, 150 / 255d, 243 / 255d, 1);

    // STEGANOGRAPHY METHOD - Radio Buttons & Toggle Group
    @FXML
    private JFXRadioButton LSBRadioButton;
    @FXML
    private JFXRadioButton PVDRadioButton;
    @FXML
    private ToggleGroup methodToggleGroup;

    // LEAST SIGNIFICANT BIT (LSB) - Parameters
    @FXML
    private JFXSlider LSBBitsUsedSlider;
    @FXML
    private JFXTextField everyNPixelsTextField;
    @FXML
    private JFXRadioButton fibonacciPatternRadioButton;
    @FXML
    private JFXRadioButton everyNPixelsRadioButton;
    @FXML
    private ToggleGroup patternToggleGroup;

    // ENCRYPTION
    @FXML
    private JFXCheckBox useEncryptionCheckbox;

    // THE LOADED IMAGE
    @FXML
    private ImageView imageView;
    @FXML
    private Label imageNameLabel;
    @FXML
    private Label maxFileSizeLabel;

    // THE TEXT FILE
    @FXML
    private JFXButton loadFileButton;
    @FXML
    private Label fileNameLabel;

    // THE ACTIONS
    @FXML
    private JFXButton encodeButton;
    @FXML
    private JFXButton decodeButton;

    // The HELP TEXT AREA - Contains all the info on how to use the application
    @FXML
    private JFXTextArea helpTextArea;

    // the directories where the file choosers will open
    private File imageChooserDirectory;
    private File fileChooserDirectory;

    // the maximum file size in bytes of the encoded file
    private long maxBytesSize;

    // the selected image / file
    private File selectedImage;
    private File selectedFile;

    public void initialize() {
        // Set the radio button colors
        LSBRadioButton.setSelectedColor(primaryColor);
        PVDRadioButton.setSelectedColor(primaryColor);
        everyNPixelsRadioButton.setSelectedColor(primaryColor);
        fibonacciPatternRadioButton.setSelectedColor(primaryColor);

        // Set the checkbox colors
        useEncryptionCheckbox.setCheckedColor(primaryColor);

        // Text alignment to center for the pixel text field
        everyNPixelsTextField.setAlignment(Pos.CENTER);

        // Limit the number of characters to 3 and allow only numbers
        everyNPixelsTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean shouldSetValue = false;
            if (!newValue.matches("\\d*")) {
                newValue = newValue.replaceAll("[^\\d]", "");
                shouldSetValue = true;
            }
            if (newValue.length() > 3) {
                newValue = newValue.substring(0, 3);
                shouldSetValue = true;
            }
            if (shouldSetValue) {
                everyNPixelsTextField.setText(newValue);
            }
        });

        // Set the initial directory of the image and file choosers
        imageChooserDirectory = new File(System.getProperty("user.home"));
        fileChooserDirectory = new File(System.getProperty("user.home"));

        // Parametrize the image view
        imageView.setPreserveRatio(true);
    }

    @FXML
    private void encodeButtonHandler(ActionEvent event) {

    }

    @FXML
    private void decodeButtonHandler(ActionEvent event) {

    }

    @FXML
    private void loadFileButtonHandler(ActionEvent event) {
        // configure the encoded file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to encode...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        fileChooser.setInitialDirectory(fileChooserDirectory);

        // let the user pick a file and check if a file was really picked
        Window ownerWindow = ((Node) event.getTarget()).getScene().getWindow();
        File encodedFile = fileChooser.showOpenDialog(ownerWindow);
        if (encodedFile == null) {
            return;
        }

        // remember the path, and check if the selected file was an existing file
        fileChooserDirectory = encodedFile.getParentFile();
        if (!encodedFile.exists() || !encodedFile.isFile()) {
            return;
        }

        // check the file length (in bytes)
        if (encodedFile.length() > maxBytesSize) {
            AlertUtils.showNotificationAlert((Stage) ownerWindow,
                    "File too big!",
                    "Its size must be less than " + GUIUtils.formatBytesValue(maxBytesSize) + ".");
            return;
        }

        // load the file and display all the necessary information in the labels
        selectedFile = encodedFile;
        encodeButton.setDisable(false);
        GUIUtils.setFileNameText(encodedFile, fileNameLabel);
    }

    @FXML
    private void loadImageButtonHandler(ActionEvent event) {
        // configure the image file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an image (RGB)...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );
        fileChooser.setInitialDirectory(imageChooserDirectory);

        // let the user pick a file and check if a file was really picked
        Window ownerWindow = ((Node) event.getTarget()).getScene().getWindow();
        File imageFile = fileChooser.showOpenDialog(ownerWindow);
        if (imageFile == null) {
            return;
        }

        // remember the path, and check if the selected file was an existing file
        imageChooserDirectory = imageFile.getParentFile();
        if (!imageFile.exists() || !imageFile.isFile()) {
            return;
        }

        // check if the file size is not too large
        Image image = new Image(imageFile.toURI().toString());
        if (selectedFile != null && selectedFile.length() > GUIUtils.getMaxFileSize(image)) {
            GUIUtils.resetFileNameText(fileNameLabel);
            encodeButton.setDisable(true);
            selectedFile = null;
            AlertUtils.showNotificationAlert((Stage) ownerWindow,
                    "The loaded file size is too big!",
                    "The file was unloaded.");
        }

        // load it in the imageView and update the imageName/maxFileSize labels with the correct information
        selectedImage = imageFile;
        loadFileButton.setDisable(false);
        decodeButton.setDisable(false); // TODO - check if there's actually any info encoded in the image before enabling the button
        imageView.setImage(image);
        GUIUtils.setImageNameText(imageFile, image, imageNameLabel);
        maxBytesSize = GUIUtils.setMaxFileSize(image, maxFileSizeLabel);
        GUIUtils.centerImage(imageView);
    }
}
