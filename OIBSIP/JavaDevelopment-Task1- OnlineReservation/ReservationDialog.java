

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Booking form.
 * Usage: new ReservationDialog(parentFrame).setVisible(true);
 */
public class ReservationDialog extends JDialog {

    private static final Color DARK = new Color(0x1E3A8A);
    private static final Color LIGHT = new Color(0x3B82F6);
    private static final Color PRIMARY = new Color(0x2563EB);
    private static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
    private static final Color TEXT = new Color(0x111827);
    private static final Color MUTED = new Color(0x6B7280);
    private static final Color BORDER_IDLE = new Color(0xD1D5DB);
    private static final Color ERROR = new Color(0xDC2626);
    private static final Color READONLY_BG = new Color(0xF3F4F6);
    private static final String FONT = "Segoe UI";

    private final JTextField nameField = new JTextField();
    private final JTextField trainNoField = new JTextField();
    private final JTextField trainNameField = new JTextField();
    private final JComboBox<String> classBox = new JComboBox<>(new String[]{"Sleeper", "3AC", "2AC", "1AC"});
    private final JTextField dateField = new JTextField(LocalDate.now().plusDays(1).toString());
    private final JTextField sourceField = new JTextField();
    private final JTextField destField = new JTextField();
    private final JLabel errorLabel = new JLabel(" ");

    private String foundTrainName = null;   // null = train number not found / not entered

    public ReservationDialog(Window owner) {
        super(owner, "Book Ticket", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // ================= UI =================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, DARK, getWidth(), getHeight(), LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(18, 30, 18, 30));
        JLabel title = new JLabel("Book a Ticket");
        title.setFont(new Font(FONT, Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Fill in the details to get your PNR number");
        sub.setFont(new Font(FONT, Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 205));
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(3));
        texts.add(sub);
        header.add(texts, BorderLayout.WEST);
        return header;
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(22, 30, 6, 30));

        styleField(nameField);
        styleField(trainNoField);
        styleField(dateField);
        styleField(sourceField);
        styleField(destField);
        styleCombo(classBox);

        trainNameField.setEditable(false);
        trainNameField.setFont(new Font(FONT, Font.PLAIN, 14));
        trainNameField.setBackground(READONLY_BG);
        trainNameField.setBorder(fieldBorder(BORDER_IDLE));
        trainNameField.setPreferredSize(new Dimension(210, 42));

        // auto-populate train name from train number
        trainNoField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { lookupTrain(); }
        });
        trainNoField.addActionListener(e -> lookupTrain());

        addField(p, "Passenger name", nameField, 0, 0, 2);
        addField(p, "Train number", trainNoField, 1, 0, 1);
        addField(p, "Train name (auto)", trainNameField, 1, 1, 1);
        addField(p, "Class type", classBox, 2, 0, 1);
        addField(p, "Date of journey (yyyy-MM-dd)", dateField, 2, 1, 1);
        addField(p, "Source station", sourceField, 3, 0, 1);
        addField(p, "Destination station", destField, 3, 1, 1);
        return p;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(6, 30, 22, 30));

        errorLabel.setFont(new Font(FONT, Font.BOLD, 12));
        errorLabel.setForeground(ERROR);
        errorLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        footer.add(errorLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = makeButton("Close", Color.WHITE, new Color(0xF3F4F6), TEXT, true);
        JButton book = makeButton("Book Ticket", PRIMARY, PRIMARY_HOVER, Color.WHITE, false);
        cancel.addActionListener(e -> dispose());
        book.addActionListener(e -> book());
        buttons.add(cancel);
        buttons.add(book);
        footer.add(buttons, BorderLayout.CENTER);

        getRootPane().setDefaultButton(book);
        return footer;
    }

    private void addField(JPanel p, String label, JComponent comp, int row, int col, int span) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = col;
        g.gridwidth = span;
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        int left = (col == 1) ? 8 : 0;
        int right = (col == 0 && span == 1) ? 8 : 0;

        JLabel l = new JLabel(label);
        l.setFont(new Font(FONT, Font.BOLD, 12));
        l.setForeground(TEXT);
        g.gridy = row * 2;
        g.insets = new Insets(row == 0 ? 0 : 14, left, 5, right);
        p.add(l, g);

        g.gridy = row * 2 + 1;
        g.insets = new Insets(0, left, 0, right);
        p.add(comp, g);
    }

    private void styleField(JTextField f) {
        f.setFont(new Font(FONT, Font.PLAIN, 14));
        f.setForeground(TEXT);
        f.setPreferredSize(new Dimension(210, 42));
        f.setBorder(fieldBorder(BORDER_IDLE));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(fieldBorder(PRIMARY)); }
            @Override public void focusLost(FocusEvent e) { f.setBorder(fieldBorder(BORDER_IDLE)); }
        });
    }

    private void styleCombo(JComboBox<String> c) {
        c.setFont(new Font(FONT, Font.PLAIN, 14));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(210, 42));
    }

    private CompoundBorder fieldBorder(Color c) {
        return new CompoundBorder(new LineBorder(c, 2, true), new EmptyBorder(0, 12, 0, 12));
    }

    private JButton makeButton(String text, Color bg, Color hoverBg, Color fg, boolean outline) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hoverBg : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (outline) {
                    g2.setColor(BORDER_IDLE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font(FONT, Font.BOLD, 14));
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 42));
        return b;
    }

    // ================= logic =================

    /** Fills the train name when a valid train number is typed. */
    private void lookupTrain() {
        String no = trainNoField.getText().trim();
        foundTrainName = null;
        trainNameField.setForeground(TEXT);
        if (no.isEmpty()) {
            trainNameField.setText("");
            return;
        }
        if (!no.matches("\\d{1,9}")) {
            trainNameField.setForeground(ERROR);
            trainNameField.setText("Numbers only");
            return;
        }
        try {
            foundTrainName = findTrainName(Integer.parseInt(no));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        if (foundTrainName == null) {
            trainNameField.setForeground(ERROR);
            trainNameField.setText("Train not found");
        } else {
            trainNameField.setText(foundTrainName);
        }
    }

    private void book() {
        String name = nameField.getText().trim();
        String trainNo = trainNoField.getText().trim();
        String dateText = dateField.getText().trim();
        String src = sourceField.getText().trim();
        String dst = destField.getText().trim();

        // ---- validation ----
        if (name.isEmpty() || trainNo.isEmpty() || dateText.isEmpty() || src.isEmpty() || dst.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        if (!trainNo.matches("\\d{1,9}")) {
            showError("Train number must be numeric.");
            return;
        }
        lookupTrain();
        if (foundTrainName == null) {
            showError("Train number not found. Try 12951, 12301, 12627, 12009 or 12723.");
            return;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateText);
        } catch (DateTimeParseException ex) {
            showError("Invalid date. Use yyyy-MM-dd (e.g. 2026-12-25).");
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            showError("Journey date cannot be in the past.");
            return;
        }
        if (src.equalsIgnoreCase(dst)) {
            showError("Source and destination must be different.");
            return;
        }

        // ---- save ----
        String cls = (String) classBox.getSelectedItem();
        try {
            String pnr = saveReservation(name, Integer.parseInt(trainNo), foundTrainName,
                    cls, date.toString(), src, dst);
            showConfirmation(pnr, name, trainNo, foundTrainName, cls, date.toString(), src, dst);
            dispose();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database error: could not save the booking.");
        }
    }

    private String findTrainName(int trainNo) throws SQLException {
        try (Connection c = DatabaseSetup.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT train_name FROM trains WHERE train_no = ?")) {
            ps.setInt(1, trainNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Inserts the booking and returns the auto-generated unique PNR. */
    private String saveReservation(String name, int trainNo, String trainName, String cls,
                                   String date, String src, String dst) throws SQLException {
        try (Connection c = DatabaseSetup.getConnection()) {
            String pnr;
            do {
                pnr = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
            } while (pnrExists(c, pnr));

            String sql = "INSERT INTO reservations(pnr, passenger_name, train_no, train_name, "
                    + "class_type, journey_date, source, destination) VALUES(?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, pnr);
                ps.setString(2, name);
                ps.setInt(3, trainNo);
                ps.setString(4, trainName);
                ps.setString(5, cls);
                ps.setString(6, date);
                ps.setString(7, src);
                ps.setString(8, dst);
                ps.executeUpdate();
            }
            return pnr;
        }
    }

    private boolean pnrExists(Connection c, String pnr) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM reservations WHERE pnr = ?")) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void showConfirmation(String pnr, String name, String trainNo, String trainName,
                                  String cls, String date, String src, String dst) {
        String details = "PNR NUMBER:    " + pnr + "\n\n"
                + "Passenger:     " + name + "\n"
                + "Train:         " + trainNo + " - " + trainName + "\n"
                + "Class:         " + cls + "\n"
                + "Journey date:  " + date + "\n"
                + "From:          " + src + "\n"
                + "To:            " + dst + "\n\n"
                + "Keep your PNR safe. You need it to cancel.";
        JTextArea area = new JTextArea(details);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, area, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}