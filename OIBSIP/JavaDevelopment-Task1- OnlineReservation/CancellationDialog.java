

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cancellation form: enter PNR -> Fetch -> review details -> Confirm Cancellation.
 * Usage: new CancellationDialog(parentFrame).setVisible(true);
 */
public class CancellationDialog extends JDialog {

    private static final Color DARK = new Color(0x991B1B);
    private static final Color LIGHT = new Color(0xEF4444);
    private static final Color PRIMARY = new Color(0x2563EB);
    private static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
    private static final Color DANGER = new Color(0xDC2626);
    private static final Color DANGER_HOVER = new Color(0xB91C1C);
    private static final Color SUCCESS = new Color(0x059669);
    private static final Color TEXT = new Color(0x111827);
    private static final Color MUTED = new Color(0x6B7280);
    private static final Color BORDER_IDLE = new Color(0xD1D5DB);
    private static final Color PANEL_BG = new Color(0xF3F4F6);
    private static final String FONT = "Segoe UI";

    private static final String[] KEYS = {"PNR", "Passenger", "Train", "Class", "Journey date", "From", "To"};

    private final JTextField pnrField = new JTextField();
    private final JLabel[] values = new JLabel[KEYS.length];
    private final JLabel statusLabel = new JLabel(" ");
    private JButton confirmBtn;
    private String fetchedPnr = null;   // PNR currently shown in the details panel

    public CancellationDialog(Window owner) {
        super(owner, "Cancel Ticket", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
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
        JLabel title = new JLabel("Cancel a Ticket");
        title.setFont(new Font(FONT, Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Enter your PNR number to find the booking");
        sub.setFont(new Font(FONT, Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 215));
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(Box.createVerticalStrut(3));
        texts.add(sub);
        header.add(texts, BorderLayout.WEST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(22, 30, 6, 30));

        // ---- PNR row ----
        JPanel top = new JPanel(new BorderLayout(10, 5));
        top.setOpaque(false);
        JLabel l = new JLabel("PNR number");
        l.setFont(new Font(FONT, Font.BOLD, 12));
        l.setForeground(TEXT);

        pnrField.setFont(new Font(FONT, Font.PLAIN, 15));
        pnrField.setForeground(TEXT);
        pnrField.setPreferredSize(new Dimension(250, 42));
        pnrField.setBorder(fieldBorder(BORDER_IDLE));
        pnrField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { pnrField.setBorder(fieldBorder(PRIMARY)); }
            @Override public void focusLost(FocusEvent e) { pnrField.setBorder(fieldBorder(BORDER_IDLE)); }
        });
        pnrField.addActionListener(e -> fetch());
        pnrField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onPnrEdited(); }
            public void removeUpdate(DocumentEvent e) { onPnrEdited(); }
            public void changedUpdate(DocumentEvent e) { onPnrEdited(); }
        });

        JButton fetchBtn = makeButton("Fetch", PRIMARY, PRIMARY_HOVER, Color.WHITE, 100);
        fetchBtn.addActionListener(e -> fetch());

        top.add(l, BorderLayout.NORTH);
        top.add(pnrField, BorderLayout.CENTER);
        top.add(fetchBtn, BorderLayout.EAST);
        body.add(top, BorderLayout.NORTH);

        // ---- details card ----
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PANEL_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        for (int i = 0; i < KEYS.length; i++) {
            JLabel key = new JLabel(KEYS[i]);
            key.setFont(new Font(FONT, Font.PLAIN, 13));
            key.setForeground(MUTED);

            values[i] = new JLabel("-");
            values[i].setFont(new Font(FONT, Font.BOLD, 14));
            values[i].setForeground(TEXT);

            GridBagConstraints g = new GridBagConstraints();
            g.gridy = i;
            g.anchor = GridBagConstraints.WEST;
            g.insets = new Insets(5, 0, 5, 24);
            g.gridx = 0;
            card.add(key, g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(5, 0, 5, 0);
            card.add(values[i], g);
        }
        card.setPreferredSize(new Dimension(360, 250));
        body.add(card, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(10, 30, 22, 30));

        statusLabel.setFont(new Font(FONT, Font.BOLD, 12));
        statusLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        footer.add(statusLabel, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        JButton close = makeButton("Close", Color.WHITE, PANEL_BG, TEXT, 110);
        close.addActionListener(e -> dispose());

        confirmBtn = makeButton("Confirm Cancellation", DANGER, DANGER_HOVER, Color.WHITE, 200);
        confirmBtn.setEnabled(false);
        confirmBtn.addActionListener(e -> cancelBooking());

        buttons.add(close);
        buttons.add(confirmBtn);
        footer.add(buttons, BorderLayout.CENTER);
        return footer;
    }

    private CompoundBorder fieldBorder(Color c) {
        return new CompoundBorder(new LineBorder(c, 2, true), new EmptyBorder(0, 12, 0, 12));
    }

    private JButton makeButton(String text, Color bg, Color hoverBg, Color fg, int width) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(new Color(0xD1D5DB));
                } else {
                    g2.setColor(getModel().isRollover() ? hoverBg : bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (bg.equals(Color.WHITE)) {
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
        b.setPreferredSize(new Dimension(width, 42));
        return b;
    }

    // ================= logic =================

    /** If the user edits the PNR after a fetch, clear the old details. */
    private void onPnrEdited() {
        if (fetchedPnr != null && !pnrField.getText().trim().equals(fetchedPnr)) {
            resetDetails();
        }
    }

    private void resetDetails() {
        fetchedPnr = null;
        confirmBtn.setEnabled(false);
        for (JLabel v : values) v.setText("-");
    }

    private void fetch() {
        String pnr = pnrField.getText().trim();
        resetDetails();

        if (pnr.isEmpty()) {
            showStatus("Please enter a PNR number.", DANGER);
            return;
        }
        if (!pnr.matches("\\d+")) {
            showStatus("PNR must contain digits only.", DANGER);
            return;
        }

        String sql = "SELECT pnr, passenger_name, train_no, train_name, class_type, "
                + "journey_date, source, destination FROM reservations WHERE pnr = ?";
        try (Connection c = DatabaseSetup.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    showStatus("No booking found for this PNR.", DANGER);
                    return;
                }
                values[0].setText(rs.getString("pnr"));
                values[1].setText(rs.getString("passenger_name"));
                values[2].setText(rs.getInt("train_no") + " - " + rs.getString("train_name"));
                values[3].setText(rs.getString("class_type"));
                values[4].setText(rs.getString("journey_date"));
                values[5].setText(rs.getString("source"));
                values[6].setText(rs.getString("destination"));
            }
            fetchedPnr = pnr;
            confirmBtn.setEnabled(true);
            showStatus("Booking found. Review the details, then confirm.", SUCCESS);
        } catch (SQLException ex) {
            ex.printStackTrace();
            showStatus("Database error while fetching the booking.", DANGER);
        }
    }

    private void cancelBooking() {
        if (fetchedPnr == null) return;

        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel the booking with PNR " + fetchedPnr + "?\nThis cannot be undone.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try (Connection c = DatabaseSetup.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM reservations WHERE pnr = ?")) {
            ps.setString(1, fetchedPnr);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                String done = fetchedPnr;
                resetDetails();
                pnrField.setText("");
                showStatus("Booking " + done + " cancelled successfully.", SUCCESS);
                JOptionPane.showMessageDialog(this, "Your booking (PNR " + done + ") has been cancelled.",
                        "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStatus("Booking was already cancelled.", DANGER);
                resetDetails();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showStatus("Database error while cancelling.", DANGER);
        }
    }

    private void showStatus(String msg, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(msg);
    }
}
