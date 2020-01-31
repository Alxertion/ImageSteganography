package steganography;

/**
 * This class contains the actual decoding steganography methods, which involve
 * bit shifting and bitwise operations.
 *
 * For the encoding, see SteganographyEncoding.java.
 */
public class SteganographyDecoding {
    /**
     *
     */
    public static byte[] decodeBytesLSB(byte[] imageBytes, int offset, int length) {
        // TODO: check that the decoded bytes will not go out of bounds
        // TODO: make this receive the number of bytes used and the method used

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
}
