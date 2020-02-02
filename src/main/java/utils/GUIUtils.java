package utils;

import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import controllers.SaveImageController;
import exceptions.SteganographyException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import steganography.RawDecodedFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class GUIUtils {
    // COLOR CONSTANTS
    public static final Color PRIMARY_COLOR = new Color(33 / 255d, 150 / 255d, 243 / 255d, 1);
    public static final Color SECONDARY_COLOR = new Color(50 / 255d, 50 / 255d, 50 / 255d, 1);
    public static final Color UNFOCUSED_COLOR = new Color(0.3019608d, 0.3019608d, 0.3019608d, 1);
    public static final Color ERROR_COLOR = new Color(243 / 255d, 80 / 255d, 75 / 255d, 1);

    // STRING CONSTANTS
    private static final String FILE_NAME_PREFIX = "File name: ";
    private static final String MAX_FILE_SIZE_PREFIX = "Max. file size: ";
    private static final String APPROX_MAX_FILE_SIZE_PREFIX = "Approx. max. file size: ";

    /**
     * Centers the image in the imageView by doing some calculations using the coordinates.
     * To be called after the image is placed in the imageView.
     */
    public static void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();

            double reducingCoefficient = Math.min(ratioX, ratioY);

            double w = img.getWidth() * reducingCoefficient;
            double h = img.getHeight() * reducingCoefficient;

            imageView.setX((imageView.getFitWidth() - w) / 2);
            imageView.setY((imageView.getFitHeight() - h) / 2);
        }
    }

    /**
     * Formats a string so that only the first 10 characters remain,
     * adding 3 dots after ("...") if necessary (applying ellipsis).
     */
    public static String formatStringWithEllipsis(String value) {
        if (value.length() > 10) {
            value = value.substring(0, 10) + "...";
        }
        return value;
    }

    /**
     * Sets the image name in the corresponding image name label, taking it from the image file.
     * Also appends the image resolution at the end.
     * Before calling, the imageFile must be validated as an actual, existing file.
     */
    public static void setImageNameText(File imageFile, Image image, Label imageNameLabel) {
        String imageName = formatStringWithEllipsis(imageFile.getName());
        imageNameLabel.setText("Image name: "
                + imageName
                + " ("
                + (int) image.getWidth()
                + " x "
                + (int) image.getHeight()
                + " px)");
    }

    /**
     * Receiving a number of bytes as a parameter, the method formats the value
     * in B, KB or MB depending on the byte count.
     */
    public static String formatBytesValue(long byteCount) {
        double kiloBytes = Math.round((byteCount / 1024d) * 100.0) / 100.0;
        double megaBytes = Math.round((kiloBytes / 1024d) * 100.0) / 100.0;
        if (kiloBytes >= 1) {
            if (megaBytes >= 1) {
                return megaBytes + " MBytes";
            }
            return kiloBytes + " KBytes";
        }
        return byteCount + " Bytes";
    }

    /**
     * Returns the maximum file size possible that can be encoded in an image,
     * based on the image width & height, and the number of least significant
     * bits that are used.
     * It also takes into account the pixel choosing pattern (every N pixels,
     * or random).
     */
    public static long getMaxFileSize(Image image, int bitsUsed,
                                      boolean everyNPixelsEnabled, int everyNPixelsValue,
                                      boolean randomEnabled, int lowerBound, int upperBound) {
        // calculate the bytes based on the 'bitsUsed' value
        double bytes = image.getWidth() * image.getHeight(); // number of pixels
        bytes *= 3; // max number of encoded bits if we use 1 bit
        bytes *= bitsUsed; // max number of encoded bits if we use 'bitsUsed' bits
        bytes /= 8; // max number of encoded bytes

        // use the pixel choosing pattern as a factor
        if (everyNPixelsEnabled) {
            bytes /= everyNPixelsValue;
        } else if (randomEnabled) {
            bytes /= (lowerBound + upperBound) / 2.0;
        }
        return (long) bytes;
    }

    /**
     * Calculates the maximum file size possible that can be encoded in an image,
     * based on the image width & height, and sets that value in the label,
     * formatted as bytes, KBytes or MBytes.
     * It returns the amount of bytes that can be encoded.
     */
    public static long setMaxFileSize(Image image, Label maxFileSizeLabel, int bitsUsed,
                                      boolean everyNPixelsEnabled, int everyNPixelsValue,
                                      boolean randomEnabled, int lowerBound, int upperBound) {
        long bytes = getMaxFileSize(image, bitsUsed,
                everyNPixelsEnabled, everyNPixelsValue,
                randomEnabled, lowerBound, upperBound);

        String formattedBytesValue = formatBytesValue(bytes);
        maxFileSizeLabel.setTextFill(SECONDARY_COLOR);
        if (randomEnabled) {
            maxFileSizeLabel.setText(APPROX_MAX_FILE_SIZE_PREFIX + formattedBytesValue);
        } else {
            maxFileSizeLabel.setText(MAX_FILE_SIZE_PREFIX + formattedBytesValue);
        }

        return bytes;
    }

    /**
     * Sets the file name text, with ellipsis, and the file size as well, formatted.
     */
    public static void setFileNameText(File encodedFile, Label fileNameLabel) {
        String fileName = formatStringWithEllipsis(encodedFile.getName());
        String fileSize = formatBytesValue(encodedFile.length());
        fileNameLabel.setText(FILE_NAME_PREFIX + fileName + " (" + fileSize + ")");
    }

    /**
     * Resets the file name text, if an image that is too small was loaded, for example.
     */
    public static void resetFileNameText(Label fileNameLabel) {
        fileNameLabel.setText(FILE_NAME_PREFIX);
    }

    /**
     * Returns the method used for encryption as a String. Throws a SteganographyException
     * if the currently selected method is invalid.
     */
    public static String getSteganographyMethod(JFXRadioButton everyNPixelsRadioButton,
                                                JFXTextField everyNPixelsTextField,
                                                JFXRadioButton randomPatternRadioButton,
                                                JFXTextField randomSeedTextField,
                                                JFXTextField randomLowerBoundTextField,
                                                JFXTextField randomUpperBoundTextField) {
        String methodString = "";
        if (everyNPixelsRadioButton.isSelected()) {
            try {
                Integer.parseInt(everyNPixelsTextField.getText());
                methodString = everyNPixelsTextField.getText();
            } catch (NumberFormatException exception) {
                throw new SteganographyException(
                        "Invalid number value!",
                        "'Every n pixels' value should be a number!");
            }
        } else if (randomPatternRadioButton.isSelected()) {
            methodString = "random" + ","
                    + randomSeedTextField.getText() + ","
                    + randomLowerBoundTextField.getText() + ","
                    + randomUpperBoundTextField.getText();
        }
        return methodString;
    }

    /**
     * Shows a dialog that displays the original and the cover image side by side, and
     * allows the user to either save the image or cancel the operation altogether.
     */
    public static void showSaveImageDialog(Stage mainStage, String originalImageFileName,
                                           Image originalImage, BufferedImage coverImage) {
        try {
            // load the new window and the components
            FXMLLoader fxmlLoader = new FXMLLoader(GUIUtils.class.getResource("../view/SaveImageView.fxml"));
            Parent root = fxmlLoader.load();

            // send the images to the controller so that they can be displayed
            SaveImageController controller = fxmlLoader.getController();
            controller.setMainStage(mainStage);
            controller.setOriginalImageFileName(originalImageFileName);
            controller.setOriginalImage(originalImage);
            controller.setCoverImage(coverImage);

            // show the new window on the screen, modal
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            controller.setCurrentStage(stage);
            stage.setTitle("Original and Cover Images");
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

    /**
     * Shows a file chooser, which allows the user to select a folder and a file name
     * to save the decoded file. The file chooser is populated with the file name and
     * extension that were decoded from the cover image.
     */
    public static void showSaveFileDialog(Stage mainStage, RawDecodedFile decodedFile) {
        // save the actual decoded file
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save decoded file as...");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            fileChooser.setInitialFileName(decodedFile.getFileName());

            File savedFile = fileChooser.showSaveDialog(mainStage);
            if (savedFile != null) {
                Files.write(savedFile.toPath(), decodedFile.getFileBytes());
            } else {
                throw new RuntimeException();
            }
        } catch (IOException exception) {
            AlertUtils.showNotificationAlert(mainStage,
                    "Saving error!",
                    "The decoded file could not be saved.");
        }
    }

    /**
     * Returns the value of a text field as an int. Returns a -1 value
     * if the text field cannot be converted to an int.
     */
    public static int getNumericTextFieldValue(JFXTextField numericTextField) {
        try {
            return Integer.parseInt(numericTextField.getText());
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Validates the random parameters: the seed/lower bound/upper bound are not empty,
     * the lower bound/upper bound are >= 1, and the lower bound < upper bound. Throws
     * a SteganographyException if any of the aforementioned conditions are not true.
     */
    public static void validateRandomParameters(JFXTextField seedTextField,
                                                JFXTextField lowerBoundTextField,
                                                JFXTextField upperBoundTextField) {
        int seedValue = getNumericTextFieldValue(seedTextField);
        int lowerBound = getNumericTextFieldValue(lowerBoundTextField);
        int upperBound = getNumericTextFieldValue(upperBoundTextField);

        if (seedValue < 0) {
            seedTextField.setFocusColor(ERROR_COLOR);
            seedTextField.setUnFocusColor(ERROR_COLOR);
            throw new SteganographyException(
                    "Random seed error!",
                    "The seed must not be empty.");
        }
        if (lowerBound < 0 || upperBound < 0) {
            if (lowerBound < 0) {
                lowerBoundTextField.setFocusColor(ERROR_COLOR);
                lowerBoundTextField.setUnFocusColor(ERROR_COLOR);
            } else {
                upperBoundTextField.setFocusColor(ERROR_COLOR);
                upperBoundTextField.setUnFocusColor(ERROR_COLOR);
            }
            throw new SteganographyException(
                    "Random bound error!",
                    "The bounds must not be empty.");
        }
        if (lowerBound < 1 || upperBound < 1) {
            if (lowerBound < 1) {
                lowerBoundTextField.setFocusColor(ERROR_COLOR);
                lowerBoundTextField.setUnFocusColor(ERROR_COLOR);
            } else {
                upperBoundTextField.setFocusColor(ERROR_COLOR);
                upperBoundTextField.setUnFocusColor(ERROR_COLOR);
            }
            throw new SteganographyException(
                    "Random bound error!",
                    "The bounds must be >= 1.");
        }
        if (lowerBound >= upperBound) {
            lowerBoundTextField.setFocusColor(ERROR_COLOR);
            lowerBoundTextField.setUnFocusColor(ERROR_COLOR);
            upperBoundTextField.setFocusColor(ERROR_COLOR);
            upperBoundTextField.setUnFocusColor(ERROR_COLOR);
            throw new SteganographyException(
                    "Random bound error!",
                    "The lower bound must be < the upper bound.");
        }
    }

    /**
     * Validates the everyNPixels parameter: everyNPixels text field is >= 1.
     * Throws a SteganographyException if the previous condition is not true.
     */
    public static void validateEveryNPixelsParameters(JFXTextField everyNPixelsTextField) {
        int everyNPixelsValue = getNumericTextFieldValue(everyNPixelsTextField);

        if (everyNPixelsValue < 1) {
            everyNPixelsTextField.setFocusColor(ERROR_COLOR);
            everyNPixelsTextField.setUnFocusColor(ERROR_COLOR);
            throw new SteganographyException(
                    "Pixel pattern error!",
                    "The pixel pattern must be >= 1.");
        }
    }

    /**
     * If the currently selected steganography method is erroneous, we display that instead
     * of the max. file size (which can not be computed). We also change the color to a
     * light red.
     */
    public static void setErrorMaxFileSize(Label maxFileSizeLabel) {
        maxFileSizeLabel.setTextFill(ERROR_COLOR);
        maxFileSizeLabel.setText("Current steganography method is erroneous!");
    }
}
