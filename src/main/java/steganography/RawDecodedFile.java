package steganography;

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
