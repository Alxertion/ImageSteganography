package steganography;

/**
 * This class contains the actual decoding steganography methods, which involve
 * bit shifting and bitwise operations.
 * <p>
 * For the encoding, see SteganographyEncoding.java.
 */
@SuppressWarnings("DuplicatedCode")
public class SteganographyDecoding {
    /**
     * This method does LSB steganography decoding, using 'bitsUsed' least significant bits.
     * The method can be:
     * - "fibonacci" (in which case the bytes used in the coverImage are selected using the fibonacci function:
     * byte 0, byte 1, byte 2, byte 3, byte 5, byte 8, byte 13 etc);
     * - "i", where i is an int (in which case we take every 'i' byte and use it when decoding).
     */
    public static byte[] decodeBytesLSB(byte[] imageBytes,
                                        int offset,
                                        int length,
                                        int bitsUsed,
                                        String method) {
        // TODO: check that the decoded bytes will not go out of bounds

        // obtain the byte and bit offset according to how many bits we encoded beforehand
        int byteOffset = (offset * 8) / bitsUsed;
        int bitOffset = (offset * 8) % bitsUsed;

        // calculate the offset according to the current encoding method
        int byteIncrement;
        if ("fibonacci".equals(method)) {
            // TODO: implement fibonacci
            byteIncrement = 1;
        } else {
            byteIncrement = Integer.parseInt(method);
            byteOffset *= byteIncrement;
        }

        // declare an array to store our decoded result
        byte[] result = new byte[length];

        // iterate over all the result bytes
        for (int currentResultByte = 0; currentResultByte < length; currentResultByte++) {
            // iterate over all the result bits of the current result byte
            for (int currentResultBit = 0; currentResultBit < 8; currentResultBit++) {
                // if we decoded all the bits from the current byte,
                // we take the next byte from the coverImage according to the used method
                if (bitOffset == bitsUsed) {
                    if ("fibonacci".equals(method)) {
                        // TODO: implement fibonacci
                        byteOffset += byteIncrement;
                    } else {
                        byteOffset += byteIncrement;
                    }
                    bitOffset = 0;
                }

                // isolate the current bit from the coverImage
                byte currentCoverImageByte = (byte) (imageBytes[byteOffset] >> (bitsUsed - bitOffset - 1) & 0x01);

                // store the isolated bit in the result
                result[currentResultByte] = (byte) (result[currentResultByte] << 1 | currentCoverImageByte);

                // move on to the next bit in the result
                bitOffset++;
            }
        }

        return result;
    }

    /**
     * This method does LSB steganography decoding, using only 1 least significant bit.
     */
    public static byte[] decodeBytesLSB(byte[] imageBytes, int offset, int length) {
        // TODO: check that the decoded bytes will not go out of bounds

        // multiply the offset by 8 to obtain the number of bits we offset with
        offset *= 8;

        // declare an array to store our result
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
