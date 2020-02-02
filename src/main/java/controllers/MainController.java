package controllers;

import com.jfoenix.controls.*;
import exceptions.SteganographyException;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import steganography.RawDecodedFile;
import utils.AlertUtils;
import utils.GUIUtils;
import steganography.SteganographyUtils;
import utils.TextFieldLimitListener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@SuppressWarnings("DuplicatedCode")
public class MainController {
    // THE MAIN STAGE
    Stage mainStage;

    // STEGANOGRAPHY METHOD - Radio Buttons & Toggle Group
    @FXML
    private JFXRadioButton LSBRadioButton;
    @FXML
    private ToggleGroup methodToggleGroup;

    // LEAST SIGNIFICANT BIT (LSB) - Parameters
    @FXML
    private JFXSlider LSBBitsUsedSlider;
    @FXML
    private JFXTextField everyNPixelsTextField;
    @FXML
    private JFXRadioButton randomPatternRadioButton;
    @FXML
    private JFXRadioButton everyNPixelsRadioButton;
    @FXML
    private JFXTextField randomSeedTextField;
    @FXML
    private JFXTextField randomLowerBoundTextField;
    @FXML
    private JFXTextField randomUpperBoundTextField;
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
    private String selectedImageName;
    private File selectedFile;

    public void initialize() {
        // Set the radio button colors
        LSBRadioButton.setSelectedColor(GUIUtils.PRIMARY_COLOR);
        everyNPixelsRadioButton.setSelectedColor(GUIUtils.PRIMARY_COLOR);
        randomPatternRadioButton.setSelectedColor(GUIUtils.PRIMARY_COLOR);

        // Set the text field colors
        resetTextFieldColors();

        // Set the checkbox colors
        useEncryptionCheckbox.setCheckedColor(GUIUtils.PRIMARY_COLOR);

        // Text alignment to center for the text fields
        everyNPixelsTextField.setAlignment(Pos.CENTER);
        randomSeedTextField.setAlignment(Pos.CENTER);
        randomLowerBoundTextField.setAlignment(Pos.CENTER);
        randomUpperBoundTextField.setAlignment(Pos.CENTER);

        // Limit the number of characters in the input text fields
        everyNPixelsTextField.textProperty().addListener(
                new TextFieldLimitListener(everyNPixelsTextField, 3, true));
        randomSeedTextField.textProperty().addListener(
                new TextFieldLimitListener(randomSeedTextField, 8, true));
        randomLowerBoundTextField.textProperty().addListener(
                new TextFieldLimitListener(randomLowerBoundTextField, 3, true));
        randomUpperBoundTextField.textProperty().addListener(
                new TextFieldLimitListener(randomUpperBoundTextField, 3, true));

        // Set the initial directory of the image and file choosers
        imageChooserDirectory = new File(System.getProperty("user.home"));
        fileChooserDirectory = new File(System.getProperty("user.home"));

        // Parametrize the image view
        imageView.setPreserveRatio(true);

        // Set a listener on the slider to modify the maximum file size and unload the
        // file if too few bits were selected. We set similar listeners on the other
        // input fields in the current method
        ChangeListener<Object> genericMaxFileSizeUpdater = (observable, oldValue, newValue) -> {
            if (imageView.getImage() != null) {
                updateMaxFileSize(imageView.getImage(), (int) LSBBitsUsedSlider.getValue());
            }
        };
        ChangeListener<Number> sliderMaxFileSizeUpdater = (observable, oldValue, newValue) -> {
            if (imageView.getImage() != null) {
                updateMaxFileSize(imageView.getImage(), newValue.intValue());
            }
        };

        LSBBitsUsedSlider.valueProperty().addListener(sliderMaxFileSizeUpdater);
        everyNPixelsTextField.textProperty().addListener(genericMaxFileSizeUpdater);
        randomSeedTextField.textProperty().addListener(genericMaxFileSizeUpdater);
        randomLowerBoundTextField.textProperty().addListener(genericMaxFileSizeUpdater);
        randomUpperBoundTextField.textProperty().addListener(genericMaxFileSizeUpdater);
        randomPatternRadioButton.selectedProperty().addListener(genericMaxFileSizeUpdater);
        everyNPixelsRadioButton.selectedProperty().addListener(genericMaxFileSizeUpdater);

        // Initialize the help text area with the help information
        GUIUtils.initializeHelpTextArea(helpTextArea);
    }

    public void resetTextFieldColors() {
        // Set the text field colors when focused
        everyNPixelsTextField.setFocusColor(GUIUtils.PRIMARY_COLOR);
        randomSeedTextField.setFocusColor(GUIUtils.PRIMARY_COLOR);
        randomLowerBoundTextField.setFocusColor(GUIUtils.PRIMARY_COLOR);
        randomUpperBoundTextField.setFocusColor(GUIUtils.PRIMARY_COLOR);

        // Set the text field colors when unfocused
        everyNPixelsTextField.setUnFocusColor(GUIUtils.UNFOCUSED_COLOR);
        randomSeedTextField.setUnFocusColor(GUIUtils.UNFOCUSED_COLOR);
        randomLowerBoundTextField.setUnFocusColor(GUIUtils.UNFOCUSED_COLOR);
        randomUpperBoundTextField.setUnFocusColor(GUIUtils.UNFOCUSED_COLOR);
    }

    public void validateSteganographyMethod() {
        resetTextFieldColors();
        if (everyNPixelsRadioButton.isSelected()) {
            GUIUtils.validateEveryNPixelsParameters(everyNPixelsTextField);
        } else if (randomPatternRadioButton.isSelected()) {
            GUIUtils.validateRandomParameters(randomSeedTextField,
                    randomLowerBoundTextField, randomUpperBoundTextField);
        }
    }

    public boolean updateMaxFileSize(Image image, int bitsUsed) {
        try {
            validateSteganographyMethod();
        } catch (SteganographyException exception) {
            GUIUtils.setErrorMaxFileSize(maxFileSizeLabel);
            return true;
        }

        long maxFileSize = GUIUtils.getMaxFileSize(image, bitsUsed,
                everyNPixelsRadioButton.isSelected(), GUIUtils.getNumericTextFieldValue(everyNPixelsTextField),
                randomPatternRadioButton.isSelected(), GUIUtils.getNumericTextFieldValue(randomLowerBoundTextField),
                GUIUtils.getNumericTextFieldValue(randomUpperBoundTextField));

        if (selectedFile != null && selectedFile.length() > maxFileSize) {
            GUIUtils.resetFileNameText(fileNameLabel);
            encodeButton.setDisable(true);
            selectedFile = null;
            AlertUtils.showNotificationAlert(mainStage,
                    "The loaded file size is too big!",
                    "The file was unloaded.");
            return false;
        } else {
            maxBytesSize = GUIUtils.setMaxFileSize(image, maxFileSizeLabel, bitsUsed,
                    everyNPixelsRadioButton.isSelected(), GUIUtils.getNumericTextFieldValue(everyNPixelsTextField),
                    randomPatternRadioButton.isSelected(), GUIUtils.getNumericTextFieldValue(randomLowerBoundTextField),
                    GUIUtils.getNumericTextFieldValue(randomUpperBoundTextField));
            return true;
        }
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @FXML
    private void encodeButtonHandler(ActionEvent event) {
        if (LSBRadioButton.isSelected()) {
            try {
                validateSteganographyMethod();
                String methodString = GUIUtils.getSteganographyMethod(
                        everyNPixelsRadioButton, everyNPixelsTextField,
                        randomPatternRadioButton, randomSeedTextField,
                        randomLowerBoundTextField, randomUpperBoundTextField);

                BufferedImage coverImage = SteganographyUtils.encodeFileInImageLSB(selectedImage, selectedFile,
                        (int) LSBBitsUsedSlider.getValue(), methodString);

                GUIUtils.showSaveImageDialog(mainStage, selectedImageName, imageView.getImage(), coverImage,
                        (int) LSBBitsUsedSlider.getValue(), selectedImage);
            } catch (SteganographyException exception) {
                AlertUtils.showNotificationAlert(mainStage, exception.getTitle(), exception.getMessage());
            }
        }
    }

    @FXML
    private void decodeButtonHandler(ActionEvent event) {
        if (LSBRadioButton.isSelected()) {
            try {
                validateSteganographyMethod();
                String methodString = GUIUtils.getSteganographyMethod(
                        everyNPixelsRadioButton, everyNPixelsTextField,
                        randomPatternRadioButton, randomSeedTextField,
                        randomLowerBoundTextField, randomUpperBoundTextField);

                RawDecodedFile decodedFile = SteganographyUtils.decodeFileFromImageLSB(selectedImage,
                        (int) LSBBitsUsedSlider.getValue(), methodString);
                GUIUtils.showSaveFileDialog(mainStage, decodedFile);

                AlertUtils.showNotificationAlert(mainStage,
                        "Saving successful!",
                        "The decoded file was saved successfully.");
            } catch (SteganographyException exception) {
                AlertUtils.showNotificationAlert(mainStage, exception.getTitle(), exception.getMessage());
            } catch (RuntimeException ignored) {
            }
        }
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
        File encodedFile = fileChooser.showOpenDialog(mainStage);
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
            AlertUtils.showNotificationAlert(mainStage,
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
        File imageFile = fileChooser.showOpenDialog(mainStage);
        if (imageFile == null) {
            return;
        }

        // remember the path, and check if the selected file was an existing file
        imageChooserDirectory = imageFile.getParentFile();
        if (!imageFile.exists() || !imageFile.isFile()) {
            return;
        }

        // check if it's an actual image
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage.getWidth() == 0 && bufferedImage.getHeight() == 0) {
                throw new Exception();
            }
        } catch (Exception exception) {
            AlertUtils.showNotificationAlert(mainStage,
                    "The chosen image is invalid!",
                    "Please select an image.");
            return;
        }

        // check if the file size is not too large
        Image image = new Image(imageFile.toURI().toString());
        boolean successful = updateMaxFileSize(image, (int) LSBBitsUsedSlider.getValue());
        if (!successful) {
            return;
        }

        // load it in the imageView and update the imageName/maxFileSize labels with the correct information
        selectedImage = imageFile;
        selectedImageName = FilenameUtils.getBaseName(selectedImage.getName());
        loadFileButton.setDisable(false);
        decodeButton.setDisable(false);
        imageView.setImage(image);
        GUIUtils.setImageNameText(imageFile, image, imageNameLabel);
        GUIUtils.centerImage(imageView);
    }
}
