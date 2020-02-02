package steganography;

import com.google.common.primitives.Ints;
import encryption.EncryptionUtils;
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
@SuppressWarnings("DuplicatedCode")
public class SteganographyUtils {
    private static final String SIGNATURE = "ISLSB";
    private static final int LENGTH_BYTES = 4;

    /**
     * Loads an image (as a BufferedImage) from a file.
     * Throws a SteganographyException if the selected file is not an actual image.
     */
    public static BufferedImage loadImage(File imageFile) {
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
     * We create a copy of the cover image. The copy is required so that we
     * don't have any issues accessing and modifying the original image itself
     * (and also because we will display the original and the copy side by side).
     */
    public static BufferedImage createCopyOfImage(BufferedImage image) {
        BufferedImage coverImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = coverImage.createGraphics();
        graphics.drawRenderedImage(image, null);
        graphics.dispose();
        return coverImage;
    }

    /**
     * We convert an image to a byte array using a raster. This is done so that we
     * can access the pixel values themselves (and, therefore, encode data into them).
     */
    public static byte[] getImageAsPixelByteArray(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        return buffer.getData();
    }

    /**
     * We convert a generic file to a byte array. This is done so that we can
     * access the bytes that we will encode into an image.
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
     * Returns the file name with the appended extension, from a File object.
     */
    public static String getFileName(File file) {
        return FilenameUtils.getBaseName(file.getName()) + "." + FilenameUtils.getExtension(file.getName());
    }

    /**
     * This method encodes a file into an image, with the provided
     * 'bitsUsed' and the provided steganography method.
     * Parameter meaning:
     *
     */
    public static BufferedImage encodeFileInImageLSB(File selectedImage, File selectedFile,
                                                     int bitsUsed, String methodString,
                                                     String encryptionMethodString) {
        // load the image as a BufferedImage
        BufferedImage originalImage = loadImage(selectedImage);

        // create a copy of it, in which we will encode our hidden file
        BufferedImage coverImage = createCopyOfImage(originalImage);

        // obtain the pixels of the coverImage as a byte array
        byte[] coverImageBytes = getImageAsPixelByteArray(coverImage);

        // obtain the content of the selectedFile as a byte array
        byte[] selectedFileBytes = EncryptionUtils.encryptBytes(
                getFileAsByteArray(selectedFile), encryptionMethodString);

        // encode the signature in the first bytes
        byte[] signatureBytes = EncryptionUtils.encryptBytes(SIGNATURE.getBytes(), encryptionMethodString);
        SteganographyEncoding.encodeBytesLSB(coverImageBytes, signatureBytes, 0,
                bitsUsed, methodString);
        int currentOffsetInBytes = signatureBytes.length;

        // encode the length of the "file name + extension"
        String fileName = getFileName(selectedFile);
        byte[] fileNameLengthBytes = EncryptionUtils.encryptBytes(
                Ints.toByteArray(fileName.getBytes().length), encryptionMethodString);
        SteganographyEncoding.encodeBytesLSB(coverImageBytes, fileNameLengthBytes, currentOffsetInBytes,
                bitsUsed, methodString);
        currentOffsetInBytes += fileNameLengthBytes.length;

        // encode the file name + extension
        byte[] fileNameBytes = EncryptionUtils.encryptBytes(fileName.getBytes(), encryptionMethodString);
        SteganographyEncoding.encodeBytesLSB(coverImageBytes, fileNameBytes, currentOffsetInBytes,
                bitsUsed, methodString);
        currentOffsetInBytes += fileNameBytes.length;

        // encode the length of the file
        byte[] fileLengthBytes = EncryptionUtils.encryptBytes(
                Ints.toByteArray((int) selectedFile.length()), encryptionMethodString);
        SteganographyEncoding.encodeBytesLSB(coverImageBytes, fileLengthBytes, currentOffsetInBytes,
                bitsUsed, methodString);
        currentOffsetInBytes += fileLengthBytes.length;

        // encode the file bytes themselves
        SteganographyEncoding.encodeBytesLSB(coverImageBytes, selectedFileBytes, currentOffsetInBytes,
                bitsUsed, methodString);

        // return the image after encoding is done
        return coverImage;
    }

    /**
     * Having the bytes of an image, we validate the first few encoded bits to check
     * if they have the signature. The signature was encoded in the first bits of the
     * image, before encoding the actual file. If the signature is not present, at least
     * one of the following affirmations is true:
     * - we used the wrong decoding method (steganography method);
     * - we used the wrong decrypting method (cryptography method);
     * - the encoded file used a different signature (or none at all);
     * - there is no encoded file present in the image.
     *
     * This method throws a SteganographyException if the signature can not be verified.
     */
    private static void validateSignature(byte[] coverImageBytes, int bitsUsed,
                                          String methodString, String encryptionMethodString) {
        // encrypt the signature to obtain the number of bytes it has, encrypted
        byte[] signatureEncryptedBytes = EncryptionUtils.encryptBytes(
                SIGNATURE.getBytes(), encryptionMethodString);

        // decode the bytes and decrypt them
        byte[] signatureBytes = SteganographyDecoding.decodeBytesLSB(coverImageBytes, 0,
                signatureEncryptedBytes.length, bitsUsed, methodString);
        signatureBytes = EncryptionUtils.decryptBytes(signatureBytes, encryptionMethodString);

        // validate if the signature is the same
        try {
            String signature = new String(signatureBytes);
            if (!SIGNATURE.equals(signature)) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new SteganographyException(
                    "Decoding error!",
                    "There is no encoded file in the provided image."
            );
        }
    }

    /**
     * This method decodes a file from an image, with the provided
     * 'bitsUsed' and the provided steganography method.
     * Parameter meaning:
     *
     */
    public static RawDecodedFile decodeFileFromImageLSB(File selectedImage,
                                                        int bitsUsed, String methodString,
                                                        String encryptionMethodString) {
        // load the image as a BufferedImage
        BufferedImage originalImage = loadImage(selectedImage);

        // create a copy of it, which we will attempt to parse
        BufferedImage coverImage = createCopyOfImage(originalImage);

        // obtain the pixels of the coverImage as a byte array
        byte[] coverImageBytes = getImageAsPixelByteArray(coverImage);

        // obtain and validate the signature
        byte[] signatureEncryptedBytes = EncryptionUtils.encryptBytes(SIGNATURE.getBytes(), encryptionMethodString);
        validateSignature(coverImageBytes, bitsUsed, methodString, encryptionMethodString);
        int offsetInBytes = signatureEncryptedBytes.length;

        // obtain the file name length
        byte[] fileNameLengthBytes = SteganographyDecoding.decodeBytesLSB(coverImageBytes, offsetInBytes, LENGTH_BYTES,
                bitsUsed, methodString);
        fileNameLengthBytes = EncryptionUtils.decryptBytes(fileNameLengthBytes, encryptionMethodString);
        int fileNameLength = Ints.fromByteArray(fileNameLengthBytes);
        offsetInBytes += fileNameLengthBytes.length;

        // obtain the file name
        byte[] fileNameBytes = SteganographyDecoding.decodeBytesLSB(coverImageBytes, offsetInBytes, fileNameLength,
                bitsUsed, methodString);
        fileNameBytes = EncryptionUtils.decryptBytes(fileNameBytes, encryptionMethodString);
        String fileName = new String(fileNameBytes);
        offsetInBytes += fileNameBytes.length;

        // obtain the file length
        byte[] fileLengthBytes = SteganographyDecoding.decodeBytesLSB(coverImageBytes, offsetInBytes, LENGTH_BYTES,
                bitsUsed, methodString);
        fileLengthBytes = EncryptionUtils.decryptBytes(fileLengthBytes, encryptionMethodString);
        int fileLength = Ints.fromByteArray(fileLengthBytes);
        offsetInBytes += fileLengthBytes.length;

        // obtain the content of the selectedFile as a byte array
        byte[] fileBytes = SteganographyDecoding.decodeBytesLSB(coverImageBytes, offsetInBytes, fileLength,
                bitsUsed, methodString);
        fileBytes = EncryptionUtils.decryptBytes(fileBytes, encryptionMethodString);

        // return the file name & file bytes as a RawDecodedFile
        return new RawDecodedFile(fileName, fileBytes);
    }
}
