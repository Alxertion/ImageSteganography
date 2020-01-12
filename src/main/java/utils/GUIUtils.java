package utils;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class GUIUtils {
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
        long kiloBytes = byteCount / 1024;
        long megaBytes = kiloBytes / 1024;
        if (kiloBytes != 0) {
            if (megaBytes != 0) {
                return megaBytes + " MBytes";
            }
            return kiloBytes + " KBytes";
        }
        return byteCount + " Bytes";
    }

    /**
     * Returns the maximum file size possible that can be encoded in an image,
     * based on the image width & height.
     */
    public static long getMaxFileSize(Image image) {
        double bytes = image.getWidth() * image.getHeight(); // number of pixels
        bytes *= 3; // max number of encoded bits
        bytes /= 8; // max number of encoded bytes
        return (long) bytes;
    }

    /**
     * Calculates the maximum file size possible that can be encoded in an image,
     * based on the image width & height, and sets that value in the label,
     * formatted as bytes, KBytes or MBytes.
     * It returns the amount of bytes that can be encoded.
     */
    public static long setMaxFileSize(Image image, Label maxFileSizeLabel) {
        long bytes = getMaxFileSize(image);

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
}
