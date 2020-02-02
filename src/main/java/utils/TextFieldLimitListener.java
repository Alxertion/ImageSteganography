package utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * This listener can be added to a TextField (on the 'textProperty'), and it
 * effectively limits the max character count allowed in it, as well as
 * giving the option of restricting the TextField input to numbers only.
 */
public class TextFieldLimitListener implements ChangeListener<String> {
    private final TextField textField;
    private final int maxCharacters;
    private final boolean allowOnlyNumbers;

    public TextFieldLimitListener(TextField textField, int maxCharacters, boolean allowOnlyNumbers) {
        this.textField = textField;
        this.maxCharacters = maxCharacters;
        this.allowOnlyNumbers = allowOnlyNumbers;
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        boolean shouldSetValue = false;

        // check for non-number characters
        if (allowOnlyNumbers && !newValue.matches("\\d*")) {
            newValue = newValue.replaceAll("[^\\d]", "");
            shouldSetValue = true;
        }

        // restrict the length
        if (newValue.length() > maxCharacters) {
            newValue = newValue.substring(0, maxCharacters);
            shouldSetValue = true;
        }

        // set the value if all is good
        if (shouldSetValue) {
            textField.setText(newValue);
        }
    }
}
