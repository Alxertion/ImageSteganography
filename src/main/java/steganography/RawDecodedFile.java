package steganography;

/**
 * This class is used when decoding a file from a cover image.
 * We store all the information decoded from the file, which are:
 * - the decoded file's name (as a String);
 * - the file bytes themselves.
 */
public class RawDecodedFile {
    private String fileName;
    private byte[] fileBytes;

    public RawDecodedFile(String fileName, byte[] fileBytes) {
        this.fileName = fileName;
        this.fileBytes = fileBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }
}
