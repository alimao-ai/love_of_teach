package com.loveofteach.platform.common;

/**
 * Escapes CSV cells and neutralizes spreadsheet formula prefixes.
 */
public final class CsvEscaper {

    private CsvEscaper() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        String safe = startsWithFormula(value) ? "'" + value : value;
        if (safe.contains(",") || safe.contains("\"")
                || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    private static boolean startsWithFormula(String value) {
        int index = 0;
        while (index < value.length() && value.charAt(index) == ' ') {
            index++;
        }
        return index < value.length()
                && "=+-@\t\r".indexOf(value.charAt(index)) >= 0;
    }
}
