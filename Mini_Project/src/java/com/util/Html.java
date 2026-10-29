package com.util;

public final class Html {

    private Html() {
    }

    public static String esc(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value);
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Accept only #rrggbb so a poster color can be used in CSS safely. */
    public static String color(String raw) {
        if (raw != null && raw.matches("#[0-9a-fA-F]{6}")) {
            return raw;
        }
        return "#3d5a80";
    }
}
