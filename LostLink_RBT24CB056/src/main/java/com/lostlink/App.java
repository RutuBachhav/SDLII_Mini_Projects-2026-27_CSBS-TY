package com.lostlink;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import javax.swing.*;

public final class App {
  public static void main(String[] args) {
    FlatLightLaf.setup();
    UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 14));
    UIManager.put("Component.arc", 12);
    UIManager.put("Button.arc", 12);
    UIManager.put("TextComponent.arc", 10);
    UIManager.put("Table.rowHeight", 40);
    UIManager.put("Table.showHorizontalLines", true);
    UIManager.put("Table.selectionBackground", new Color(218, 238, 233));
    UIManager.put("Table.selectionForeground", new Color(23, 64, 57));
    SwingUtilities.invokeLater(
        () -> {
          MainFrame frame = new MainFrame();
          frame.setVisible(true);
          frame.work(
              "Opening local database…",
              () -> new Database(Database.defaultDirectory()),
              frame::initialize);
        });
  }
}
