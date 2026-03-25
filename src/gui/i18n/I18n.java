package gui.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class I18n {

    private static final String BUNDLE_NAME = "i18n.messages";

    private I18n() {
    }

    public static String tr(String key, String defaultValue) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return defaultValue;
        }
    }

    public static String trf(String key, String defaultValue, Object... args) {
        return MessageFormat.format(tr(key, defaultValue), args);
    }
}
