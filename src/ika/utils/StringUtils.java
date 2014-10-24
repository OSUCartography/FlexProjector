/*
 * StringUtils.java
 *
 * Created on February 17, 2006, 1:56 PM
 *
 */
package ika.utils;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * @author jenny
 */
public class StringUtils {

    /**
     * Replace substring by other string.
     * http://javaalmanac.com/egs/java.lang/ReplaceString.html
     */
    static public String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     * Convert a string of reals to a double array.
     * This will not throw any exception if the string is not well formatted.
     * @param str String of numbers to convert
     * @param fieldSeparator String that separates each number
     * @return an array of doubles
     */
    public static double[] splitDoubles(String str, String fieldSeparator) {
        ArrayList array = new ArrayList();

        // split the string into tokens
        StringTokenizer tokenizer = new StringTokenizer(str, fieldSeparator);
        while (tokenizer.hasMoreTokens()) {
            array.add(tokenizer.nextToken());
        }

        // convert the tokens to double values and store them in values[]
        double[] values = new double[array.size()];
        for (int i = 0; i < values.length; i++) {
            try {
                values[i] = Double.parseDouble((String) (array.get(i)));
            } catch (Exception exc) {
                break;
            }
        }

        return values;
    }

    /**
     * Test whether a string contains a valid double value, i.e. it can be
     * converted to a double with Double.valueof(str).
     * From the API doc of the Double class of Java 5.
     * @param str The string to test.
     * @return True if the string str can be converted to a double using
     * Double.valueof(str)
     */
    public static boolean isDouble(String str) {
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex =
                ("[\\x00-\\x20]*" + // Optional leading "whitespace"
                "[+-]?(" + // Optional sign character
                "NaN|" + // "NaN" string
                "Infinity|" + // "Infinity" string

                // A decimal floating-point string representing a finite positive
                // number without a leading sign has at most five basic pieces:
                // Digits . Digits ExponentPart FloatTypeSuffix
                //
                // Since this method allows integer-only strings as input
                // in addition to strings of floating-point literals, the
                // two sub-patterns below are simplifications of the grammar
                // productions from the Java Language Specification, 2nd
                // edition, section 3.10.2.

                // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +
                // . Digits ExponentPart_opt FloatTypeSuffix_opt
                "(\\.(" + Digits + ")(" + Exp + ")?)|" +
                // Hexadecimal strings
                "((" +
                // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "(\\.)?)|" +
                // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +
                ")[pP][+-]?" + Digits + "))" +
                "[fFdD]?))" +
                "[\\x00-\\x20]*");// Optional trailing "whitespace"

        return Pattern.matches(fpRegex, str);

    }

    /**
     * Converts a chunk of bytes into a String. Stops when 0x0 is found.
     * Uses a specified character set for the conversion. If the bytes cannot
     * be converted with the specified character set, the default character set
     * is used.
     * @param bytes The raw bytes containing the string, one byte per character.
     * @maxLength Don't convert more bytes than maxLength.
     * @charsetName The name of the encoding of the character set.
     * @return A new String.
     */
    public static String bytesToString(byte[] bytes, int maxLength,
            String charsetName) {

        // find the number of valid characters
        int nbrValidChars = 0;
        maxLength = Math.min(maxLength, bytes.length);
        for (int i = 0; i < maxLength; i++) {
            if (bytes[i] == 0x0) {
                break;
            }
            nbrValidChars++;
        }

        try {
            // try encoding with the passed character set.
            return new String(bytes, 0, nbrValidChars, charsetName);
        } catch (UnsupportedEncodingException exc) {
            // The string could not be encoded with the passed character set.
            // The character set possibly does not exist on this machine.
            // Use the default character set instead.
            return new String(bytes, 0, nbrValidChars);
        }

    }

    /**
     * Converts a version string, such as "1.12.5 alpha" to a number.
     * @param versionStr A string of type "X.Y.Z suffix". The suffix is ignored,
     * Y and Z are optional. Y and Z must not be larger than 99.
     * @return A number. For the example string "1.12.5 alpha", this is 
     * 1 * 100 * 100 + 12 * 100 + 5;
     */
    public static int versionStringToNumber(String versionStr) {
        // remove " alpha" at the end of the string
        String[] strs = versionStr.trim().split(" ");

        // split into numbers
        strs = strs[0].split(".");

        // the following could be a little more elegant...
        if (strs.length == 1) {
            return Integer.parseInt(strs[0]) * 100 * 100;
        }

        if (strs.length == 2) {
            if (Integer.parseInt(strs[1]) > 99) {
                throw new IllegalArgumentException("subversion too large");
            }
            return Integer.parseInt(strs[0]) * 100 * 100 + Integer.parseInt(strs[1]) * 100;
        }

        if (strs.length == 3) {
            if (Integer.parseInt(strs[1]) > 99 || Integer.parseInt(strs[2]) > 99) {
                throw new IllegalArgumentException("subversion too large");
            }
            return Integer.parseInt(strs[0]) * 100 * 100 + Integer.parseInt(strs[1]) * 100 + Integer.parseInt(strs[2]);
        }

        return 0;
    }
}