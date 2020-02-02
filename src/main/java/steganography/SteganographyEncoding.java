package steganography;

import exceptions.SteganographyException;

import java.util.Random;

/**
 * This class contains the actual encoding steganography methods, which involve
 * bit shifting and bitwise operations.
 * <p>
 * For the decoding, see SteganographyDecoding.java.
 * <p>
 * Partly inspired by https://www.dreamincode.net/forums/topic/27950-steganography/
 */
@SuppressWarnings("DuplicatedCode")
public class SteganographyEncoding {
    /**
     * This method does LSB steganography encoding, using 'bitsUsed' least significant bits.
     * The method can be:
     * - "fibonacci" (in which case the bytes used in the coverImage are selected using the fibonacci function:
     * byte 0, byte 1, byte 2, byte 3, byte 5, byte 8, byte 13 etc);
     * - "i", where i is an int (in which case we take every 'i' byte and use it in encoding).
     */
    public static void encodeBytesLSB(byte[] coverImageBytes,
                                      byte[] addedBytes,
                                      int offset,
                                      int bitsUsed,
                                      String method) {
        try {
            // obtain the byte and bit offset according to how many bits we encoded beforehand
            int byteOffset = (offset * 8) / bitsUsed;
            int bitOffset = (offset * 8) % bitsUsed;

            // calculate the offset according to the current encoding method
            int byteIncrement = 1;
            Random randomGenerator = new Random();
            int lowerBoundRandom = 0;
            int upperBoundRandom = 0;
            if (method.contains("random")) {
                String[] split = method.split(",");
                randomGenerator.setSeed(Long.parseLong(split[1]));
                lowerBoundRandom = Integer.parseInt(split[2]);
                upperBoundRandom = Integer.parseInt(split[3]);
                int value = 0;
                for (int i = 0; i <= byteOffset; i++) {
                    value += randomGenerator.nextInt(upperBoundRandom - lowerBoundRandom) + lowerBoundRandom;
                }
                byteOffset = value;
            } else {
                byteIncrement = Integer.parseInt(method);
                byteOffset *= byteIncrement;
            }

            // loop through all the bytes to be added
            for (int i = 0; i < addedBytes.length; i++) {
                // get the current 8 bits that we must add
                int bitsToAdd = addedBytes[i];

                // loop through the bits of the current byte, one at a time
                for (int currentBit = 7; currentBit >= 0; currentBit--) {
                    // get the bit that we must add (shift by 'currentBit' bits and then isolate it)
                    int bitToAdd = (bitsToAdd >>> currentBit) & 0x01;

                    // if we encoded the max. number of bits in the current byte,
                    // we take the next byte from the coverImage according to the used method
                    if (bitOffset == bitsUsed) {
                        if (method.contains("random")) {
                            byteOffset += randomGenerator.nextInt(upperBoundRandom - lowerBoundRandom) + lowerBoundRandom;
                        } else {
                            byteOffset += byteIncrement;
                        }
                        bitOffset = 0;

                        // we shift the current byte to the right by 'bitsUsed' times
                        coverImageBytes[byteOffset] = (byte) (coverImageBytes[byteOffset] >>> bitsUsed);
                    }

                    // we encode a bit by shifting to the left and applying OR with the new bit
                    coverImageBytes[byteOffset] = (byte) (coverImageBytes[byteOffset] << 1 | bitToAdd);

                    // and move on to the next one
                    bitOffset++;
                }
            }
        } catch (Exception e) {
            throw new SteganographyException(
                    "The loaded file size is too big!",
                    "Please change the file or the encoding method.");
        }
    }

    /**
     * This method does LSB steganography encoding, using only 1 least significant bit.
     * It is not currently used in the application.
     */
    public static void encodeBytesLSB(byte[] coverImageBytes,
                                      byte[] addedBytes,
                                      int offset) {
        try {
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
        } catch (Exception e) {
            throw new SteganographyException(
                    "The loaded file size is too big!",
                    "Please change the file or the encoding method.");
        }
    }
}
