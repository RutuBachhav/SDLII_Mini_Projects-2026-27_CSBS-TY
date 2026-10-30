package hackmate;

/**
 * Small helper to escape text before it is written into an HTML page.
 *
 * <p>Prevents cross site scripting when a value that came from a user, for
 * example a full name or a skill list, is shown again in a form.</p>
 */
public final class HtmlUtil {

    private HtmlUtil() {
    }

    /**
     * Escapes the five characters that are unsafe inside HTML text and
     * inside a double quoted HTML attribute.
     *
     * @param value the text to escape, may be null
     * @return the escaped text, never null
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#39;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }
}
