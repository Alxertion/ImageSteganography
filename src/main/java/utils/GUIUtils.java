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

    // STRING CONSTANTS
    private static final String FILE_NAME_PREFIX = "File name: ";
    private static final String MAX_FILE_SIZE_PREFIX = "Max. file size: ";

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
     */
    public static long getMaxFileSize(Image image, int bitsUsed) {
        double bytes = image.getWidth() * image.getHeight(); // number of pixels
        bytes *= 3; // max number of encoded bits if we use 1 bit
        bytes *= bitsUsed; // max number of encoded bits if we use 'bitsUsed' bits
        bytes /= 8; // max number of encoded bytes
        return (long) bytes;
    }

    /**
     * Calculates the maximum file size possible that can be encoded in an image,
     * based on the image width & height, and sets that value in the label,
     * formatted as bytes, KBytes or MBytes.
     * It returns the amount of bytes that can be encoded.
     */
    public static long setMaxFileSize(Image image, Label maxFileSizeLabel, int bitsUsed) {
        long bytes = getMaxFileSize(image, bitsUsed);

        String formattedBytesValue = formatBytesValue(bytes);
        maxFileSizeLabel.setText(MAX_FILE_SIZE_PREFIX + formattedBytesValue);

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
                                                JFXTextField everyNPixelsTextField) {
        String methodString;
        if (everyNPixelsRadioButton.isSelected()) {
            try {
                Integer.parseInt(everyNPixelsTextField.getText());
                methodString = everyNPixelsTextField.getText();
            } catch (NumberFormatException exception) {
                throw new SteganographyException(
                        "Invalid number value!",
                        "'Every n pixels' value should be a number!");
            }
        } else {
            methodString = "fibonacci";
        }
        return methodString;
    }

    /**
     * Shows a dialog that displays the original and the cover image side by side, and
     * allows the user to either save the image or cancel the operation altogether.
     */
    public static void showSaveImageDialog(Stage mainStage,
                                           Image originalImage,
                                           BufferedImage coverImage) {
        try {
            // load the new window and the components
            FXMLLoader fxmlLoader = new FXMLLoader(GUIUtils.class.getResource("../view/SaveImageView.fxml"));
            Parent root = fxmlLoader.load();

            // send the images to the controller so that they can be displayed
            SaveImageController controller = fxmlLoader.getController();
            controller.setMainStage(mainStage);
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
                AlertUtils.showNotificationAlert(mainStage,
                        "Saving successful!",
                        "The decoded file was saved successfully.");
            }
        } catch (IOException exception) {
            AlertUtils.showNotificationAlert(mainStage,
                    "Saving error!",
                    "The decoded file could not be saved.");
        }
    }
}
