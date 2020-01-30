package steganography;

import com.google.common.primitives.Ints;
import exceptions.SteganographyException;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This class contains methods that deal with Image Least Significant Bit (LSB)
 * Steganography, both encoding and decoding.
 *
 * An encoded file will be found in the Least Significant Bits of an image, with
 * the following structure:
 * - first 40 bits (5 bytes): characters ISLSB (ImageSteganographyLeastSignificantBit,
 *                            a signature to know the image actually contains a file);
 * - next 32 bits (4 bytes): the length of the file name;
 * - next bytes: all the file name bytes (according to the length previously encoded);
 * - next 32 bits (4 bytes): the length of the file in bytes (an 'int';
 *                           max. length of a file is around 2 GB);
 * - next bytes: all the file bytes (we know how many because of the file length),
 *               encoded using the selected method (either using every byte, or
 *               every n byte, or a function etc.).
 *
 * Note that if an image was encoded with a different steganography tool,
 * the decoder will not work (as the signature will not be present).
 *
 * All the encoded bytes (signature, file length & file bytes) are encrypted first using
 * the selected encryption method (if a different encryption method / key is used when
 * decoding, it will not work).
 */
public class SteganographyUtils {
    private static final String SIGNATURE = "ISLSB";
    private static final int LENGTH_BYTES = 4;

    /**
     *
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
     *
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
     *
     */
    private static byte[] getImageAsPixelByteArray(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        return buffer.getData();
    }

    /**
     *
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
     *
     */
    private static void encodeBytesLSB(byte[] coverImageBytes,
                                       byte[] addedBytes,
                                       int offset) {
        // TODO: check that the added bytes truly fit in the image

        // multiply the offset by 8 to obtain the number of bits we offset with
        offset *= 8;

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
     * Returns the file name with the appended extension, from a File object.
     */
    public static String getFileName(File file) {
        return FilenameUtils.getBaseName(file.getName()) + "." + FilenameUtils.getExtension(file.getName());
    }

    /**
     *
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

        // encode the signature in the first bytes
        encodeBytesLSB(coverImageBytes, SIGNATURE.getBytes(), 0);
        int currentOffsetInBytes = SIGNATURE.getBytes().length;

        // encode the length of the "file name + extension"
        String fileName = getFileName(selectedFile);
        byte[] fileNameLengthBytes = Ints.toByteArray(fileName.getBytes().length);
        encodeBytesLSB(coverImageBytes, fileNameLengthBytes, currentOffsetInBytes);
        currentOffsetInBytes += fileNameLengthBytes.length;

        // encode the file name + extension
        encodeBytesLSB(coverImageBytes, fileName.getBytes(), currentOffsetInBytes);
        currentOffsetInBytes += fileName.getBytes().length;

        // encode the length of the file
        byte[] fileLengthBytes = Ints.toByteArray((int) selectedFile.length());
        encodeBytesLSB(coverImageBytes, fileLengthBytes, currentOffsetInBytes);
        currentOffsetInBytes += fileLengthBytes.length;

        // encode the file bytes themselves
        encodeBytesLSB(coverImageBytes, selectedFileBytes, currentOffsetInBytes);

        // return the image after encoding is done
        return coverImage;
    }

    /**
     *
     */
    private static byte[] decodeBytesLSB(byte[] imageBytes, int offset, int length) {
        // multiply the offset by 8 to obtain the number of bits we offset with
        offset *= 8;

        // declare an array to store our result and then return it
        byte[] result = new byte[length];

        // loop through each byte of the encoded value
        for (int b = 0; b < result.length; b++) {
            // loop through each bit of the current byte
            for (int i = 0; i < 8; i++, offset++) {
                // assign bit: [(new byte value) << 1] OR [(text byte) AND 1]
                result[b] = (byte) ((result[b] << 1) | (imageBytes[offset] & 1));
            }
        }
        return result;
    }

    /**
     *
     */
    private static void validateSignature(byte[] coverImageBytes) {
        byte[] signatureBytes = decodeBytesLSB(coverImageBytes, 0, SIGNATURE.getBytes().length);
        String signature = new String(signatureBytes);
        if (!SIGNATURE.equals(signature)) {
            throw new SteganographyException(
                    "Decoding error!",
                    "There is no encoded file in the provided image."
            );
        }
    }

    /**
     *
     */
    public static RawDecodedFile decodeFileFromImageLSB(File selectedImage,
                                                        int bitsUsed, String methodString) {
        // load the image as a BufferedImage
        BufferedImage originalImage = loadImage(selectedImage);

        // create a copy of it, which we will attempt to parse
        BufferedImage coverImage = createCopyOfImage(originalImage);

        // obtain the pixels of the coverImage as a byte array
        byte[] coverImageBytes = getImageAsPixelByteArray(coverImage);

        // obtain and validate the signature
        validateSignature(coverImageBytes);
        int offsetInBytes = SIGNATURE.getBytes().length;

        // obtain the file name length
        byte[] fileNameLengthBytes = decodeBytesLSB(coverImageBytes, offsetInBytes, LENGTH_BYTES);
        int fileNameLength = Ints.fromByteArray(fileNameLengthBytes);
        offsetInBytes += fileNameLengthBytes.length;

        // obtain the file name
        byte[] fileNameBytes = decodeBytesLSB(coverImageBytes, offsetInBytes, fileNameLength);
        String fileName = new String(fileNameBytes);
        offsetInBytes += fileNameBytes.length;

        // obtain the file length
        byte[] fileLengthBytes = decodeBytesLSB(coverImageBytes, offsetInBytes, LENGTH_BYTES);
        int fileLength = Ints.fromByteArray(fileLengthBytes);
        offsetInBytes += fileLengthBytes.length;

        // obtain the content of the selectedFile as a byte array
        byte[] fileBytes = decodeBytesLSB(coverImageBytes, offsetInBytes, fileLength);

        // return the file name & file bytes as a RawDecodedFile
        return new RawDecodedFile(fileName, fileBytes);
    }

    public static void applyPVD(File selectedImage, File selectedFile) {
        // TODO: maybe try to do this method?
    }
}
