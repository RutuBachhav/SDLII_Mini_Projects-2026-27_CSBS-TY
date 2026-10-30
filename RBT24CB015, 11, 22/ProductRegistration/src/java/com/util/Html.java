package com.util;

/**
 * Small HTML helpers: escaping (prevents XSS) and shared page layout so every
 * servlet response uses the same improved theme.
 */
public final class Html {

    private Html() {
    }

    public static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    public static String header(String title, String active) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<title>" + esc(title) + " - Product Registration</title>"
                + "<link rel='stylesheet' href='css/style.css'>"
                + "</head><body>"
                + "<div class='sidebar'>"
                + "<div class='logo'><h2>ABC Technologies</h2><p>Product Registration</p></div>"
                + "<nav class='menu'>"
                + nav("index.html", "Dashboard", active)
                + nav("addCategory.html", "Add Category", active)
                + nav("addProduct.html", "Add Product", active)
                + nav("viewProducts.html", "View Products", active)
                + nav("updateProduct.html", "Update Product", active)
                + nav("deleteProduct.html", "Delete Product", active)
                + "</nav></div>"
                + "<div class='main'><div class='topbar'><h2>" + esc(title) + "</h2>"
                + "<div class='admin'>Welcome, Admin</div></div>"
                + "<div class='content'>";
    }

    private static String nav(String href, String label, String active) {
        String cls = href.equals(active) ? " class='active'" : "";
        return "<a href='" + href + "'" + cls + ">" + esc(label) + "</a>";
    }

    public static String footer() {
        return "</div><div class='footer'>&copy; 2026 ABC Technologies - Product Registration</div>"
                + "</div></body></html>";
    }

    public static String alert(String kind, String msg) {
        return "<div class='alert " + kind + "'>" + esc(msg) + "</div>";
    }
}
