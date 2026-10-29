package com.lostlink;

import com.lostlink.Models.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/** Swing views only: all disk/database work and matching run through SwingWorker. */
public final class MainFrame extends JFrame {
  Repository repository;
  PhotoStore photos;
  private List<Report> reports = List.of();
  private List<Claim> claims = List.of();
  private List<Match> matches = List.of();
  private final CardLayout cards = new CardLayout();
  private final JPanel pages = new JPanel(cards);
  private final JLabel status = new JLabel("Starting LostLink…"), pathLabel = new JLabel(" ");
  private final JLabel[] counts = {
    new JLabel("0"), new JLabel("0"), new JLabel("0"), new JLabel("0")
  };
  private final DefaultTableModel
      reportModel = model("ID", "Type", "Item", "Category", "Colour", "Location", "Date", "Status"),
      recentModel = model("ID", "Type", "Item", "Location", "Date", "Status"),
      matchModel = model("Lost report", "Found report", "Similarity", "Why these match"),
      claimModel =
          model(
              "ID",
              "Found report",
              "Linked lost",
              "Claimant",
              "Contact",
              "Status",
              "Submitted (UTC)"),
      recoveryModel =
          model("Recovery", "Claim", "Found", "Lost", "Verification notes", "Recorded (UTC)");
  private final JTable reportTable = table(reportModel),
      recentTable = table(recentModel),
      matchTable = table(matchModel),
      claimTable = table(claimModel),
      recoveryTable = table(recoveryModel);
  private final JTextField search = new JTextField();
  private final JComboBox<String>
      typeFilter = new JComboBox<>(new String[] {"All types", "LOST", "FOUND"}),
      categoryFilter = new JComboBox<>(),
      statusFilter = new JComboBox<>(new String[] {"All statuses", "ACTIVE", "RECOVERED"});
  private final List<JButton> dbButtons = new ArrayList<>();
  private int tasks = 0;
  private long refreshVersion = 0;

  public MainFrame() {
    super("LostLink · Campus Lost & Found");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(1050, 700));
    setSize(1320, 860);
    setLocationRelativeTo(null);
    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(new Color(247, 249, 251));
    setContentPane(root);
    JPanel sidebar = new JPanel(new BorderLayout(0, 25));
    sidebar.setBackground(new Color(232, 241, 238));
    sidebar.setBorder(BorderFactory.createEmptyBorder(30, 20, 24, 20));
    sidebar.setPreferredSize(new Dimension(225, 0));
    JPanel brand = new JPanel(new GridLayout(2, 1, 0, 8));
    brand.setOpaque(false);
    JLabel logo = new JLabel("LostLink");
    logo.setFont(logo.getFont().deriveFont(Font.BOLD, 29));
    logo.setForeground(new Color(23, 89, 73));
    brand.add(logo);
    brand.add(new JLabel("Campus lost & found"));
    sidebar.add(brand, BorderLayout.NORTH);
    JPanel nav = new JPanel();
    nav.setOpaque(false);
    nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
    navigation(nav, "Dashboard", "dashboard");
    navigation(nav, "Browse reports", "reports");
    navigation(nav, "Suggested matches", "matches");
    navigation(nav, "Claims & recovery", "claims");
    nav.add(Box.createVerticalStrut(24));
    JButton add = button("+ Report an item", () -> new ReportDialog(this, null).setVisible(true));
    add.setMaximumSize(new Dimension(200, 44));
    nav.add(add);
    sidebar.add(nav, BorderLayout.CENTER);
    JPanel extras = new JPanel(new GridLayout(0, 1, 0, 9));
    extras.setOpaque(false);
    extras.add(button("Load demo records", this::demo));
    extras.add(button("Refresh", this::refresh));
    JLabel local = new JLabel("Local storage · SQLite");
    local.setForeground(new Color(70, 104, 94));
    extras.add(local);
    sidebar.add(extras, BorderLayout.SOUTH);
    root.add(sidebar, BorderLayout.WEST);
    pages.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
    pages.setBackground(new Color(247, 249, 251));
    pages.add(dashboard(), "dashboard");
    pages.add(browse(), "reports");
    pages.add(matches(), "matches");
    pages.add(claims(), "claims");
    root.add(pages, BorderLayout.CENTER);
    JPanel footer = new JPanel(new BorderLayout(15, 0));
    footer.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    footer.add(status, BorderLayout.WEST);
    pathLabel.setFont(pathLabel.getFont().deriveFont(11f));
    footer.add(pathLabel, BorderLayout.EAST);
    root.add(footer, BorderLayout.SOUTH);
  }

  void initialize(Database db) {
    repository = new Repository(db);
    photos = new PhotoStore(db.directory());
    pathLabel.setText(db.directory().toString());
    refresh();
  }

  private void navigation(JPanel panel, String text, String card) {
    JButton b = new JButton(text);
    b.setHorizontalAlignment(SwingConstants.LEFT);
    b.setMaximumSize(new Dimension(200, 44));
    b.addActionListener(e -> cards.show(pages, card));
    panel.add(b);
    panel.add(Box.createVerticalStrut(9));
  }

  private JButton button(String text, Runnable action) {
    JButton b = new JButton(text);
    dbButtons.add(b);
    b.setEnabled(false);
    b.addActionListener(e -> action.run());
    return b;
  }

  private static DefaultTableModel model(String... columns) {
    return new DefaultTableModel(columns, 0) {
      @Override
      public boolean isCellEditable(int r, int c) {
        return false;
      }
    };
  }

  private JTable table(DefaultTableModel model) {
    JTable t =
        new JTable(model) {
          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0) {
              String empty =
                  model == matchModel
                      ? "No compatible pairs yet. Add active lost and found reports."
                      : model == claimModel
                          ? "No claims yet. Select a found report to submit a claim."
                          : model == recoveryModel
                              ? "No verified returns yet. Completed recoveries appear here."
                              : "No reports to show. Add an item, load demo records, or clear"
                                    + " filters.";
              g.setColor(new Color(102, 116, 128));
              FontMetrics fm = g.getFontMetrics();
              g.drawString(empty, Math.max(16, (getWidth() - fm.stringWidth(empty)) / 2), 70);
            }
          }
        };
    t.setRowHeight(40);
    t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    t.setAutoCreateRowSorter(true);
    t.setFillsViewportHeight(true);
    t.getTableHeader().setReorderingAllowed(false);
    return t;
  }

  private JPanel page(String title, String subtitle) {
    JPanel p = new JPanel(new BorderLayout(0, 22));
    p.setOpaque(false);
    JPanel header = new JPanel(new GridLayout(2, 1, 0, 9));
    header.setOpaque(false);
    JLabel h = new JLabel(title);
    h.setFont(h.getFont().deriveFont(Font.BOLD, 28));
    header.add(h);
    JLabel sub = new JLabel(subtitle);
    sub.setForeground(new Color(94, 107, 120));
    header.add(sub);
    p.add(header, BorderLayout.NORTH);
    return p;
  }

  private JPanel dashboard() {
    JPanel p =
        page(
            "A little help. A happy reunion.",
            "Report belongings, discover possible matches, and record verified returns.");
    JPanel body = new JPanel(new BorderLayout(0, 28));
    body.setOpaque(false);
    JPanel metrics = new JPanel(new GridLayout(1, 4, 14, 0));
    metrics.setOpaque(false);
    String[] labels = {"Lost reports", "Found reports", "Active reports", "Recovered pairs"};
    for (int i = 0; i < 4; i++) {
      JPanel metric = new JPanel(new BorderLayout(0, 14));
      metric.setBackground(Color.WHITE);
      metric.setBorder(
          BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(new Color(224, 230, 234)),
              BorderFactory.createEmptyBorder(20, 20, 20, 20)));
      metric.add(new JLabel(labels[i]), BorderLayout.NORTH);
      counts[i].setFont(counts[i].getFont().deriveFont(Font.BOLD, 35));
      counts[i].setForeground(new Color(26, 112, 91));
      metric.add(counts[i], BorderLayout.CENTER);
      metrics.add(metric);
    }
    body.add(metrics, BorderLayout.NORTH);
    JPanel recent = new JPanel(new BorderLayout(0, 12));
    recent.setOpaque(false);
    JLabel label = new JLabel("Recent reports");
    label.setFont(label.getFont().deriveFont(Font.BOLD, 18));
    recent.add(label, BorderLayout.NORTH);
    recent.add(new JScrollPane(recentTable), BorderLayout.CENTER);
    recent.add(
        new JLabel("Start with “Report an item”, or load the clearly labelled demo records."),
        BorderLayout.SOUTH);
    body.add(recent, BorderLayout.CENTER);
    p.add(body, BorderLayout.CENTER);
    return p;
  }

  private JPanel browse() {
    JPanel p =
        page(
            "Browse reports",
            "Search by item, description, location, reporter or report number. Select a row for"
                + " actions.");
    JPanel body = new JPanel(new BorderLayout(0, 15));
    body.setOpaque(false);
    JPanel filters = new JPanel(new BorderLayout(10, 8));
    filters.setOpaque(false);
    search.putClientProperty("JTextField.placeholderText", "Search reports…");
    filters.add(search, BorderLayout.CENTER);
    JPanel combos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    combos.setOpaque(false);
    categoryFilter.addItem("All categories");
    for (String c : Models.CATEGORIES) categoryFilter.addItem(c);
    combos.add(typeFilter);
    combos.add(categoryFilter);
    combos.add(statusFilter);
    filters.add(combos, BorderLayout.EAST);
    body.add(filters, BorderLayout.NORTH);
    body.add(new JScrollPane(reportTable), BorderLayout.CENTER);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    actions.setOpaque(false);
    actions.add(button("View details", () -> selectedReport().ifPresent(this::details)));
    actions.add(
        button(
            "Edit",
            () ->
                selectedReport()
                    .ifPresent(
                        r -> {
                          if (r.status() == Status.RECOVERED) {
                            error(new IllegalArgumentException("Recovered reports are locked."));
                            return;
                          }
                          new ReportDialog(this, r).setVisible(true);
                        })));
    actions.add(button("Delete…", this::delete));
    actions.add(
        button("Claim found item", () -> selectedReport().ifPresent(r -> claimDialog(r, null))));
    body.add(actions, BorderLayout.SOUTH);
    p.add(body, BorderLayout.CENTER);
    search
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              public void insertUpdate(DocumentEvent e) {
                filter();
              }

              public void removeUpdate(DocumentEvent e) {
                filter();
              }

              public void changedUpdate(DocumentEvent e) {
                filter();
              }
            });
    typeFilter.addActionListener(e -> filter());
    categoryFilter.addActionListener(e -> filter());
    statusFilter.addActionListener(e -> filter());
    return p;
  }

  private JPanel matches() {
    JPanel p =
        page(
            "Suggested matches",
            "Weighted similarity, not a probability. A person must verify ownership before any"
                + " return.");
    JPanel body = new JPanel(new BorderLayout(0, 14));
    body.setOpaque(false);
    body.add(new JScrollPane(matchTable), BorderLayout.CENTER);
    matchTable.getColumnModel().getColumn(3).setPreferredWidth(500);
    JPanel south = new JPanel(new BorderLayout(0, 12));
    south.setOpaque(false);
    south.add(
        new JLabel(
            "Category 25% · Name 25% · Colour 15% · Location 15% · Date 10% · Description 10%"),
        BorderLayout.NORTH);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    actions.setOpaque(false);
    actions.add(
        button("View lost report", () -> selectedMatch().ifPresent(m -> details(m.lost()))));
    actions.add(
        button("View found report", () -> selectedMatch().ifPresent(m -> details(m.found()))));
    actions.add(
        button(
            "Submit linked claim",
            () -> selectedMatch().ifPresent(m -> claimDialog(m.found(), m.lost()))));
    south.add(actions, BorderLayout.SOUTH);
    body.add(south, BorderLayout.SOUTH);
    p.add(body, BorderLayout.CENTER);
    return p;
  }

  private JPanel claims() {
    JPanel p =
        page(
            "Claims & recovery",
            "Review identifying details, manually verify ownership, then record the return.");
    JPanel body = new JPanel(new BorderLayout(0, 14));
    body.setOpaque(false);
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Claims", new JScrollPane(claimTable));
    tabs.addTab("Recovery history", new JScrollPane(recoveryTable));
    body.add(tabs, BorderLayout.CENTER);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    actions.setOpaque(false);
    actions.add(button("Review claim", () -> selectedClaim().ifPresent(this::claimDetails)));
    actions.add(button("Verify & recover…", this::recover));
    actions.add(button("Reject claim…", this::reject));
    body.add(actions, BorderLayout.SOUTH);
    p.add(body, BorderLayout.CENTER);
    return p;
  }

  private void filter() {
    @SuppressWarnings("unchecked")
    TableRowSorter<DefaultTableModel> sorter =
        (TableRowSorter<DefaultTableModel>) reportTable.getRowSorter();
    String query = search.getText().trim().toLowerCase(Locale.ROOT);
    String type = (String) typeFilter.getSelectedItem(),
        category = (String) categoryFilter.getSelectedItem(),
        state = (String) statusFilter.getSelectedItem();
    sorter.setRowFilter(
        new RowFilter<>() {
          public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
            long id = ((Number) e.getValue(0)).longValue();
            Report r = reports.stream().filter(x -> x.id() == id).findFirst().orElse(null);
            if (r == null) return false;
            return ReportSearch.includes(r, query, type, category, state);
          }
        });
  }

  private Optional<Report> selectedReport() {
    int row = reportTable.getSelectedRow();
    if (row < 0) {
      message("Select a report first.");
      return Optional.empty();
    }
    long id =
        ((Number) reportModel.getValueAt(reportTable.convertRowIndexToModel(row), 0)).longValue();
    return reports.stream().filter(r -> r.id() == id).findFirst();
  }

  private Optional<Match> selectedMatch() {
    int row = matchTable.getSelectedRow();
    if (row < 0) {
      message("Select a suggested match first.");
      return Optional.empty();
    }
    return Optional.of(matches.get(matchTable.convertRowIndexToModel(row)));
  }

  private Optional<Claim> selectedClaim() {
    int row = claimTable.getSelectedRow();
    if (row < 0) {
      message("Select a claim in the Claims tab first.");
      return Optional.empty();
    }
    return Optional.of(claims.get(claimTable.convertRowIndexToModel(row)));
  }

  void details(Report r) {
    JDialog dialog = new JDialog(this, "Report #" + r.id() + " · " + r.name(), true);
    JPanel content = new JPanel(new BorderLayout(16, 16));
    content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    JTextArea text =
        new JTextArea(
            "REPORT #"
                + r.id()
                + "  ·  "
                + r.type()
                + "  ·  "
                + r.status()
                + "\n\nItem: "
                + r.name()
                + "\nCategory: "
                + r.category()
                + "\nColour: "
                + blank(r.colour())
                + "\nLocation: "
                + r.location()
                + "\nIncident date: "
                + r.date()
                + "\n\nDescription\n"
                + blank(r.description())
                + "\n\nReporter: "
                + r.reporter()
                + "\nContact: "
                + r.contact());
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setCaretPosition(0);
    content.add(new JScrollPane(text), BorderLayout.CENTER);
    if (!r.photo().isBlank()) {
      JLabel photo = new JLabel("Loading photo…", SwingConstants.CENTER);
      photo.setPreferredSize(new Dimension(260, 200));
      content.add(photo, BorderLayout.SOUTH);
      work(
          "Loading photo…",
          () -> photos.preview(r.photo(), 440, 200),
          icon -> {
            photo.setText("");
            photo.setIcon(icon);
          });
    }
    dialog.setContentPane(content);
    dialog.setSize(600, 650);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  private static String blank(String s) {
    return s == null || s.isBlank() ? "Not provided" : s;
  }

  private void delete() {
    selectedReport()
        .ifPresent(
            r -> {
              if (JOptionPane.showConfirmDialog(
                      this,
                      "Delete report #" + r.id() + " (" + r.name() + ")? This cannot be undone.",
                      "Delete report",
                      JOptionPane.OK_CANCEL_OPTION,
                      JOptionPane.WARNING_MESSAGE)
                  == JOptionPane.OK_OPTION)
                work(
                    "Deleting report…",
                    () -> {
                      repository.delete(r.id());
                      return true;
                    },
                    v -> {
                      refresh();
                      message("Report deleted.");
                    });
            });
  }

  private void claimDialog(Report found, Report linked) {
    if (found.type() != Models.Type.FOUND || found.status() != Status.ACTIVE) {
      error(new IllegalArgumentException("Claims can only be submitted for active found items."));
      return;
    }
    JTextField claimant = new JTextField(), contact = new JTextField();
    JTextArea identifying = new JTextArea(4, 30);
    identifying.setLineWrap(true);
    identifying.setWrapStyleWord(true);
    JComboBox<Object> lost = new JComboBox<>();
    lost.addItem("No linked lost report");
    for (Report r : reports)
      if (r.type() == Models.Type.LOST
          && r.status() == Status.ACTIVE
          && r.category().equals(found.category())
          && !r.date().isAfter(found.date())) lost.addItem(r);
    if (linked != null) lost.setSelectedItem(linked);
    JPanel form = new JPanel();
    form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
    form.add(new JLabel("Found #" + found.id() + ": " + found.name()));
    form.add(new JLabel("Claimant name *"));
    form.add(claimant);
    form.add(new JLabel("Contact details *"));
    form.add(contact);
    form.add(new JLabel("Linked lost report (optional)"));
    form.add(lost);
    form.add(new JLabel("Identifying details only the owner would know *"));
    form.add(new JScrollPane(identifying));
    form.add(new JLabel("Submitting a claim does not confirm ownership."));
    while (JOptionPane.showConfirmDialog(
            this,
            form,
            "Submit ownership claim",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE)
        == JOptionPane.OK_OPTION) {
      try {
        String n = Validation.required(claimant.getText(), "Claimant name"),
            c = Validation.required(contact.getText(), "Contact details"),
            d = Validation.required(identifying.getText(), "Identifying details");
        Long id = lost.getSelectedItem() instanceof Report r ? r.id() : null;
        work(
            "Submitting claim…",
            () -> {
              repository.claim(found.id(), id, n, c, d);
              return true;
            },
            v -> {
              refresh();
              message("Claim submitted. It is pending manual verification.");
            });
        return;
      } catch (IllegalArgumentException e) {
        error(e);
      }
    }
  }

  private void claimDetails(Claim c) {
    String found =
        reports.stream()
            .filter(r -> r.id() == c.foundId())
            .map(Report::name)
            .findFirst()
            .orElse("Unknown");
    JTextArea text =
        new JTextArea(
            "Claim #"
                + c.id()
                + " · "
                + c.status()
                + "\nFound #"
                + c.foundId()
                + ": "
                + found
                + "\nLinked lost report: "
                + (c.lostId() == null ? "None" : c.lostId())
                + "\nClaimant: "
                + c.claimant()
                + "\nContact: "
                + c.contact()
                + "\n\nIdentifying details\n"
                + c.details()
                + "\n\nSubmitted: "
                + c.created()
                + " UTC");
    text.setEditable(false);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    JScrollPane scroll = new JScrollPane(text);
    scroll.setPreferredSize(new Dimension(530, 320));
    JOptionPane.showMessageDialog(this, scroll, "Review claim", JOptionPane.PLAIN_MESSAGE);
  }

  private void recover() {
    selectedClaim()
        .ifPresent(
            c -> {
              if (c.status() != ClaimStatus.PENDING) {
                error(new IllegalArgumentException("Only pending claims can be verified."));
                return;
              }
              claimDetails(c);
              JTextArea notes = new JTextArea(4, 35);
              notes.setLineWrap(true);
              notes.setWrapStyleWord(true);
              JCheckBox verified =
                  new JCheckBox(
                      "I manually verified the claimant's ownership and the item return.");
              JPanel form = new JPanel(new BorderLayout(0, 12));
              form.add(
                  new JLabel("Record how you verified ownership (required):"), BorderLayout.NORTH);
              form.add(new JScrollPane(notes), BorderLayout.CENTER);
              form.add(verified, BorderLayout.SOUTH);
              while (JOptionPane.showConfirmDialog(
                      this,
                      form,
                      "Confirm verified recovery",
                      JOptionPane.OK_CANCEL_OPTION,
                      JOptionPane.PLAIN_MESSAGE)
                  == JOptionPane.OK_OPTION) {
                try {
                  if (!verified.isSelected())
                    throw new IllegalArgumentException(
                        "Manual verification must be confirmed before recovery.");
                  String value = Validation.required(notes.getText(), "Verification notes");
                  work(
                      "Recording verified recovery…",
                      () -> {
                        repository.recover(c.id(), value);
                        return true;
                      },
                      v -> {
                        refresh();
                        message(
                            "Recovery recorded. Related reports and claims were updated together.");
                      });
                  return;
                } catch (IllegalArgumentException e) {
                  error(e);
                }
              }
            });
  }

  private void reject() {
    selectedClaim()
        .ifPresent(
            c -> {
              if (JOptionPane.showConfirmDialog(
                      this,
                      "Reject claim #" + c.id() + "?",
                      "Reject claim",
                      JOptionPane.OK_CANCEL_OPTION)
                  == JOptionPane.OK_OPTION)
                work(
                    "Rejecting claim…",
                    () -> {
                      repository.reject(c.id());
                      return true;
                    },
                    v -> {
                      refresh();
                      message("Claim rejected.");
                    });
            });
  }

  private void demo() {
    if (JOptionPane.showConfirmDialog(
            this,
            "Add four labelled demo reports? Existing records are kept. Demo records can be loaded"
                + " once per database.",
            "Load demo records",
            JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION)
      work(
          "Loading demo records…",
          () -> {
            repository.loadDemo();
            return true;
          },
          v -> {
            refresh();
            message("Four demo reports added. Explore Suggested matches next.");
          });
  }

  private record Snapshot(
      List<Report> reports,
      List<Claim> claims,
      List<Match> matches,
      int pairs,
      List<String[]> recoveries) {}

  void refresh() {
    if (repository == null) return;
    long version = ++refreshVersion;
    work(
        "Refreshing reports…",
        () -> {
          List<Report> r = repository.reports();
          return new Snapshot(
              r,
              repository.claims(),
              new MatchingService().suggest(r),
              repository.recoveredPairs(),
              repository.recoveries());
        },
        s -> {
          if (version != refreshVersion) return;
          reports = s.reports();
          claims = s.claims();
          matches = s.matches();
          counts[0].setText(
              "" + reports.stream().filter(r -> r.type() == Models.Type.LOST).count());
          counts[1].setText(
              "" + reports.stream().filter(r -> r.type() == Models.Type.FOUND).count());
          counts[2].setText("" + reports.stream().filter(r -> r.status() == Status.ACTIVE).count());
          counts[3].setText("" + s.pairs());
          reportModel.setRowCount(0);
          recentModel.setRowCount(0);
          for (Report r : reports) {
            reportModel.addRow(
                new Object[] {
                  r.id(),
                  r.type(),
                  r.name(),
                  r.category(),
                  r.colour(),
                  r.location(),
                  r.date(),
                  r.status()
                });
            if (recentModel.getRowCount() < 10)
              recentModel.addRow(
                  new Object[] {r.id(), r.type(), r.name(), r.location(), r.date(), r.status()});
          }
          matchModel.setRowCount(0);
          for (Match m : matches)
            matchModel.addRow(
                new Object[] {
                  m.lost().toString(),
                  m.found().toString(),
                  String.format(Locale.ROOT, "%.1f%%", m.score()),
                  m.explanation()
                });
          claimModel.setRowCount(0);
          for (Claim c : claims)
            claimModel.addRow(
                new Object[] {
                  c.id(),
                  c.foundId(),
                  c.lostId() == null ? "—" : c.lostId(),
                  c.claimant(),
                  c.contact(),
                  c.status(),
                  c.created()
                });
          recoveryModel.setRowCount(0);
          for (String[] r : s.recoveries()) recoveryModel.addRow(r);
          filter();
          reportTable.setToolTipText(
              reports.isEmpty()
                  ? "No reports yet. Add an item or load demo records."
                  : "Select a row to view, edit or claim an item.");
          matchTable.setToolTipText(
              matches.isEmpty()
                  ? "No compatible active pairs. Add lost and found reports with the same category"
                        + " and valid dates."
                  : "Similarity is a comparison score, not proof of ownership.");
          status.setText(
              reports.isEmpty()
                  ? "No reports yet · Report an item or load demo records to begin."
                  : reports.size()
                      + " reports · "
                      + matches.size()
                      + " suggested matches · "
                      + claims.size()
                      + " claims");
        });
  }

  <T> void work(String label, Callable<T> task, Consumer<T> success) {
    work(label, task, success, () -> {});
  }

  <T> void work(String label, Callable<T> task, Consumer<T> success, Runnable finished) {
    tasks++;
    status.setText(label);
    setButtons(false);
    new SwingWorker<T, Void>() {
      protected T doInBackground() throws Exception {
        return task.call();
      }

      protected void done() {
        try {
          success.accept(get());
        } catch (Exception e) {
          Throwable cause = e instanceof java.util.concurrent.ExecutionException ? e.getCause() : e;
          error(cause);
        } finally {
          tasks--;
          setButtons(tasks == 0 && repository != null);
          finished.run();
        }
      }
    }.execute();
  }

  private void setButtons(boolean enabled) {
    for (JButton b : dbButtons) b.setEnabled(enabled);
  }

  void message(String text) {
    status.setText(text);
    JOptionPane.showMessageDialog(this, text, "LostLink", JOptionPane.INFORMATION_MESSAGE);
  }

  void error(Throwable e) {
    e.printStackTrace();
    String text = e.getMessage();
    if (text == null || text.isBlank()) text = e.getClass().getSimpleName();
    status.setText("Action failed · " + text);
    JOptionPane.showMessageDialog(
        this,
        text
            + "\n\nYour previously saved records are kept. Correct the input or refresh and retry.",
        "Unable to complete action",
        JOptionPane.ERROR_MESSAGE);
  }
}
