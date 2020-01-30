package exceptions;

/**
 * Contains a title and a message; these can be used to display an alert
 * regarding the encountered problem. Used extensively in SteganographyUtils.
 */
public class SteganographyException extends RuntimeException {
    private String title;
    private String message;

    public SteganographyException(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
