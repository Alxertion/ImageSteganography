package steganography;

/**
 * This class contains the actual encoding steganography methods, which involve
 * bit shifting and bitwise operations.
 *
 * For the decoding, see SteganographyDecoding.java.
 */
public class SteganographyEncoding {
    /**
     *
     */
    public static void encodeBytesLSB(byte[] coverImageBytes,
                                       byte[] addedBytes,
                                       int offset) {
        // TODO: check that the added bytes truly fit in the image
        // TODO: make this receive the number of bytes used and the method used

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
}
