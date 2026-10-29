package com.lostlink;

import com.lostlink.Models.*;
import java.awt.*;
import java.nio.file.Path;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

final class ReportDialog extends JDialog {
  private final MainFrame owner;
  private final Report existing;
  private final JComboBox<Models.Type> type = new JComboBox<>(Models.Type.values());
  private final JComboBox<String> category = new JComboBox<>(Models.CATEGORIES);
  private final JTextField name = new JTextField(),
      colour = new JTextField(),
      location = new JTextField(),
      date = new JTextField(LocalDate.now().toString()),
      reporter = new JTextField(),
      contact = new JTextField();
  private final JTextArea description = new JTextArea(4, 30);
  private final JLabel preview = new JLabel("No photo selected", SwingConstants.CENTER);
  private String photo = "";

  ReportDialog(MainFrame owner, Report existing) {
    super(owner, existing == null ? "Report an item" : "Edit report #" + existing.id(), true);
    this.owner = owner;
    this.existing = existing;
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    JPanel content = new JPanel(new BorderLayout(20, 20));
    content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
    JLabel heading =
        new JLabel(existing == null ? "Help an item find its way home" : "Update report details");
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22));
    content.add(heading, BorderLayout.NORTH);
    JPanel form = new JPanel(new GridBagLayout());
    int row = 0;
    row = field(form, row, "Report type *", type);
    row = field(form, row, "Item name *", name);
    row = field(form, row, "Category *", category);
    row = field(form, row, "Colour", colour);
    row = field(form, row, "Location *", location);
    row = field(form, row, "Incident date *", date);
    date.setToolTipText("YYYY-MM-DD; today or earlier");
    description.setLineWrap(true);
    description.setWrapStyleWord(true);
    row = field(form, row, "Description", new JScrollPane(description));
    row = field(form, row, "Reporter name *", reporter);
    row = field(form, row, "Contact details *", contact);
    JPanel photos = new JPanel(new BorderLayout(8, 8));
    preview.setPreferredSize(new Dimension(210, 140));
    preview.setBorder(BorderFactory.createLineBorder(new Color(220, 226, 231)));
    photos.add(preview, BorderLayout.CENTER);
    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    JButton choose = new JButton("Choose photo…"), remove = new JButton("Remove");
    controls.add(choose);
    controls.add(remove);
    photos.add(controls, BorderLayout.SOUTH);
    field(form, row, "Optional photo", photos);
    choose.addActionListener(
        e -> {
          JFileChooser chooser = new JFileChooser();
          chooser.setFileFilter(
              new FileNameExtensionFilter("Images (PNG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
          if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path path = chooser.getSelectedFile().toPath();
            owner.work(
                "Copying photo…",
                () -> owner.photos.copy(path),
                value -> {
                  photo = value;
                  loadPreview();
                });
          }
        });
    remove.addActionListener(
        e -> {
          photo = "";
          preview.setIcon(null);
          preview.setText("No photo selected");
        });
    JScrollPane scroll = new JScrollPane(form);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(18);
    content.add(scroll, BorderLayout.CENTER);
    JPanel bottom = new JPanel(new BorderLayout());
    bottom.add(new JLabel("* Required fields · Date format: YYYY-MM-DD"), BorderLayout.NORTH);
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton cancel = new JButton("Cancel"), save = new JButton("Save report");
    save.putClientProperty("JButton.buttonType", "roundRect");
    buttons.add(cancel);
    buttons.add(save);
    bottom.add(buttons, BorderLayout.SOUTH);
    content.add(bottom, BorderLayout.SOUTH);
    cancel.addActionListener(e -> dispose());
    save.addActionListener(e -> save(save));
    getRootPane().setDefaultButton(save);
    if (existing != null) {
      type.setSelectedItem(existing.type());
      name.setText(existing.name());
      category.setSelectedItem(existing.category());
      colour.setText(existing.colour());
      location.setText(existing.location());
      date.setText(existing.date().toString());
      description.setText(existing.description());
      reporter.setText(existing.reporter());
      contact.setText(existing.contact());
      photo = existing.photo();
      loadPreview();
    }
    setContentPane(content);
    setSize(670, 800);
    setMinimumSize(new Dimension(530, 540));
    setLocationRelativeTo(owner);
  }

  private int field(JPanel p, int row, String label, JComponent input) {
    GridBagConstraints a = new GridBagConstraints();
    a.gridx = 0;
    a.gridy = row;
    a.anchor = GridBagConstraints.NORTHWEST;
    a.insets = new Insets(8, 0, 8, 16);
    p.add(new JLabel(label), a);
    a.gridx = 1;
    a.weightx = 1;
    a.fill = GridBagConstraints.HORIZONTAL;
    a.insets = new Insets(5, 0, 5, 0);
    p.add(input, a);
    return row + 1;
  }

  private void loadPreview() {
    if (photo.isBlank()) return;
    String requested = photo;
    owner.work(
        "Loading photo…",
        () -> owner.photos.preview(requested, 290, 140),
        icon -> {
          if (photo.equals(requested)) {
            preview.setIcon(icon);
            preview.setText("");
          }
        });
  }

  private void save(JButton button) {
    try {
      Report r =
          new Report(
              existing == null ? 0 : existing.id(),
              (Models.Type) type.getSelectedItem(),
              name.getText().trim(),
              (String) category.getSelectedItem(),
              colour.getText().trim(),
              location.getText().trim(),
              Validation.date(date.getText()),
              description.getText().trim(),
              reporter.getText().trim(),
              contact.getText().trim(),
              photo,
              Status.ACTIVE);
      Validation.report(r);
      button.setEnabled(false);
      owner.work(
          "Saving report…",
          () -> owner.repository.save(r),
          id -> {
            dispose();
            owner.refresh();
            owner.message("Report #" + id + " saved.");
          },
          () -> button.setEnabled(true));
    } catch (IllegalArgumentException ex) {
      owner.error(ex);
    }
  }
}
