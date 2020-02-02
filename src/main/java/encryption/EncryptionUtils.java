package encryption;

/**
 * This class handles all the encryption methods, providing methods to
 * encrypt and decrypt bytes.
 */
public class EncryptionUtils {
    /**
     * This method encrypts a byte array and returns it, encrypted.
     */
    public static byte[] encryptBytes(byte[] bytes, String methodString) {
        if (methodString.contains("caesar")) {
            return encryptBytesCaesar(bytes, Integer.parseInt(methodString.substring(8)));
        } else if (methodString.contains("vigenere")) {
            return encryptBytesVigenere(bytes, methodString.substring(8));
        } else {
            return bytes;
        }
    }

    /**
     * This method decrypts a byte array and returns it, decrypted.
     */
    public static byte[] decryptBytes(byte[] bytes, String methodString) {
        if (methodString.contains("caesar")) {
            return decryptBytesCaesar(bytes, Integer.parseInt(methodString.substring(8)));
        } else if (methodString.contains("vigenere")) {
            return decryptBytesVigenere(bytes, methodString.substring(8));
        } else {
            return bytes;
        }
    }

    /**
     * Given a byte, the method returns the byte rotated by 'shift' positions to the left.
     */
    public static byte rotateLeft(byte bits, int shift) {
        return (byte) (((bits & 0xff) << shift) | ((bits & 0xff) >>> (8 - shift)));
    }

    /**
     * Given a byte, the method returns the byte rotated by 'shift' positions to the right.
     */
    public static byte rotateRight(byte bits, int shift) {
        return (byte) (((bits & 0xff) >>> shift) | ((bits & 0xff) << (8 - shift)));
    }

    /**
     * Encrypts the given bytes, using the given key, returning the encrypted bytes.
     * The encryption method is the Vigenere cypher.
     */
    private static byte[] encryptBytesVigenere(byte[] bytes, String key) {
        byte[] result = new byte[bytes.length];
        int keyPosition = 0;
        for (int i = 0; i < bytes.length; i++) {
            result[i] = rotateLeft(bytes[i], ((int) key.charAt(keyPosition)) % 7 + 1);
            keyPosition++;
            if (keyPosition >= key.length()) {
                keyPosition = 0;
            }
        }
        return result;
    }

    /**
     * Decrypts the given bytes, using the given key, returning the decrypted bytes.
     * The decryption method is the Vigenere cypher.
     */
    private static byte[] decryptBytesVigenere(byte[] bytes, String key) {
        byte[] result = new byte[bytes.length];
        int keyPosition = 0;
        for (int i = 0; i < bytes.length; i++) {
            result[i] = rotateRight(bytes[i], ((int) key.charAt(keyPosition)) % 7 + 1);
            keyPosition++;
            if (keyPosition >= key.length()) {
                keyPosition = 0;
            }
        }
        return result;
    }

    /**
     * Encrypts the given bytes, using the given key, returning the encrypted bytes.
     * The encryption method is the Caesar cypher.
     */
    private static byte[] encryptBytesCaesar(byte[] bytes, int shiftCount) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = rotateLeft(bytes[i], shiftCount);
        }
        return result;
    }

    /**
     * Decrypts the given bytes, using the given key, returning the decrypted bytes.
     * The decryption method is the Caesar cypher.
     */
    private static byte[] decryptBytesCaesar(byte[] bytes, int shiftCount) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = rotateRight(bytes[i], shiftCount);
        }
        return result;
    }
}
