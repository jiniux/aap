package xyz.jiniux.aap.support;

import java.util.regex.Pattern;

public class ISBNCleaner {
    private static final Pattern NOT_DIGITS_OR_NOT_X = Pattern.compile("[^\\dX]");

    public static String clean(String isbn) {
        return NOT_DIGITS_OR_NOT_X.matcher(isbn).replaceAll("");
    }
}
