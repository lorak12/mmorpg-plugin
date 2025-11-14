package org.nakii.mmorpg.util;

public final class FormattingUtils {

    // Private constructor to prevent instantiation
    private FormattingUtils() {}

    /**
     * Converts an integer into its Roman numeral representation.
     * Supports numbers from 1 to 3999.
     *
     * @param number The integer to convert.
     * @return The Roman numeral as a String.
     */
    public static String toRoman(int number) {
        if (number < 1 || number > 3999) {
            return String.valueOf(number);
        }

        int[] values =    {1000, 900, 500, 400, 100,  90,  50,  40,  10,   9,   5,   4,   1};
        String[] numerals = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                result.append(numerals[i]);
            }
        }

        return result.toString();
    }

    /**
     * Generates a MiniMessage-formatted progress bar string.
     *
     * @param current The current progress value.
     * @param max The maximum progress value.
     * @param totalChars The total number of characters in the bar.
     * @return A formatted progress bar string.
     */
    public static String generateProgressBar(double current, double max, int totalChars) {
        if (max <= 0) {
            return "<dark_gray>" + "─".repeat(totalChars) + "</dark_gray>";
        }
        double percent = Math.min(1.0, current / max);
        int greenChars = (int) (totalChars * percent);
        int grayChars = totalChars - greenChars;
        return "<green>" + "■".repeat(greenChars) + "</green><dark_gray>" + "■".repeat(grayChars) + "</dark_gray>";
    }

    /**
     * Overloaded method for a default progress bar of 17 characters.
     */
    public static String generateProgressBar(double current, double max) {
        return generateProgressBar(current, max, 17);
    }
}