package controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import steganography.SteganographyUtils;
import utils.GUIUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageDifferenceController {
    @FXML
    private ImageView imageDifferenceView;

    @FXML
    private void initialize() {
        // Parametrize the image view
        imageDifferenceView.setPreserveRatio(true);
    }

    public void setImages(File originalImageFile, BufferedImage coverBufferedImage, int bitsUsed) {
        // convert original image to buffered image
        BufferedImage originalBufferedImage = SteganographyUtils.loadImage(originalImageFile);

        // convert the cover image to a byte array
        byte[] coverByteImage = SteganographyUtils.getImageAsPixelByteArray(coverBufferedImage);

        // create the image difference as a byte array
        BufferedImage differenceBufferedImage = SteganographyUtils.createCopyOfImage(originalBufferedImage);
        byte[] differenceByteImage = SteganographyUtils.getImageAsPixelByteArray(differenceBufferedImage);
        for (int i = 0; i < differenceByteImage.length; i++) {
            // do the difference itself
            differenceByteImage[i] = (byte) (Math.abs(differenceByteImage[i] - coverByteImage[i]));

            // shift the bits to increase the difference magnitude, so it can be seen with the naked eye
            differenceByteImage[i] = (byte) (differenceByteImage[i] << (8 - bitsUsed));
        }

        // display the image in the imageDifferenceView
        imageDifferenceView.setImage(SwingFXUtils.toFXImage(differenceBufferedImage, null));
        GUIUtils.centerImage(imageDifferenceView);
    }
}
