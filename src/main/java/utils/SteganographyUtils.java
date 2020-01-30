package utils;

import com.google.common.primitives.Longs;
import exceptions.SteganographyException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SteganographyUtils {
    private static final int BITS_FOR_FILE_LENGTH = 64;

    /**
     * @param imageFile
     * @return
     */
    private static BufferedImage loadImage(File imageFile) {
        BufferedImage image;
        try {
            image = ImageIO.read(imageFile);
            if (image.getWidth() == 0 && image.getHeight() == 0) {
                throw new Exception();
            }
            return image;
        } catch (Exception exception) {
            throw new SteganographyException(
                    "The chosen image is invalid!",
                    "Please select an image.");
        }
    }

    /**
     * @param image
     * @return
     */
    private static BufferedImage createCopyOfImage(BufferedImage image) {
        BufferedImage coverImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = coverImage.createGraphics();
        graphics.drawRenderedImage(image, null);
        graphics.dispose();
        return coverImage;
    }

    /**
     * @param image
     * @return
     */
    private static byte[] getImageAsPixelByteArray(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        return buffer.getData();
    }

    /**
     * @param file
     * @return
     */
    private static byte[] getFileAsByteArray(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new SteganographyException(
                    "The file can not be read!",
                    "Please select a different file.");
        }
    }

    /**
     * @param coverImageBytes
     * @param addedBytes
     * @param offset
     */
    private static void encodeBytesLSB(byte[] coverImageBytes,
                                       byte[] addedBytes,
                                       int offset) {
        // TODO: check that the added bytes truly fit in the image

        // loop through all the bytes to be added
        for (int i = 0; i < addedBytes.length; i++) {
            // get the current 8 bits that we must add
            int bitsToAdd = addedBytes[i];

            // loop through the bits of the current byte, one at a time
            for (int currentBit = 7; currentBit >= 0; currentBit--, offset++) {
                // get the bit that we must add (shift by 'currentBit' bits and then isolate it)
                int bitToAdd = (bitsToAdd >>> currentBit) & 0x01;

                // AND the current bit with 0x1111 1110 (0xFE) to make the last bit 0, and then
                // OR with the bit to be added, to place it in that empty spot (which we zeroed)
                coverImageBytes[offset] = (byte) ((coverImageBytes[offset] & 0xFE) | bitToAdd);
            }
        }
    }

    /**
     * @param selectedImage
     * @param selectedFile
     * @param bitsUsed
     * @param methodString
     */
    public static BufferedImage encodeFileInImageLSB(File selectedImage, File selectedFile,
                                                     int bitsUsed, String methodString) {
        // load the image as a BufferedImage
        BufferedImage originalImage = loadImage(selectedImage);

        // create a copy of it, in which we will encode our hidden file
        BufferedImage coverImage = createCopyOfImage(originalImage);

        // obtain the pixels of the coverImage as a byte array
        byte[] coverImageBytes = getImageAsPixelByteArray(coverImage);

        // obtain the content of the selectedFile as a byte array
        byte[] selectedFileBytes = getFileAsByteArray(selectedFile);

        // encode the length of the file in the first 8 bytes (as a long)
        byte[] lengthBytes = Longs.toByteArray(selectedFile.length());
        encodeBytesLSB(coverImageBytes, lengthBytes, 0);

        // encode the file bytes themselves
        encodeBytesLSB(coverImageBytes, selectedFileBytes, BITS_FOR_FILE_LENGTH);

        // return the image after encoding is done
        return coverImage;
    }

    /**
     * @param imageBytes
     * @return
     */
    private static byte[] decodeBytesLSB(byte[] imageBytes) {
        int length = 0;
        int offset = BITS_FOR_FILE_LENGTH;
        // Loop through 64 bytes of data to determine text length
        for (int i = 0; i < offset; ++i) {
            length = (length << 1) | (imageBytes[i] & 1);
        }

        byte[] result = new byte[length];

        // Loop through each byte of text
        for (int b = 0; b < result.length; b++) {
            // Loop through each bit within a byte of text
            for (int i = 0; i < 8; i++, offset++) {
                // Assign bit: [(new byte value) << 1] OR [(text byte) AND 1]
                result[b] = (byte) ((result[b] << 1) | (imageBytes[offset] & 1));
            }
        }
        return result;
    }

    /**
     * @param selectedImage
     * @param bitsUsed
     * @param methodString
     */
    public static byte[] decodeFileFromImageLSB(File selectedImage,
                                                int bitsUsed, String methodString) {
        // load the image as a BufferedImage
        BufferedImage originalImage = loadImage(selectedImage);

        // create a copy of it, which we will attempt to parse
        BufferedImage coverImage = createCopyOfImage(originalImage);

        // obtain the pixels of the coverImage as a byte array
        byte[] coverImageBytes = getImageAsPixelByteArray(coverImage);

        // obtain the content of the selectedFile as a byte array and return it
        return decodeBytesLSB(coverImageBytes);
    }


    public static void applyPVD(File selectedImage, File selectedFile) {

    }
}
