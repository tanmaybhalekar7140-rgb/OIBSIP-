

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
import java.util.function.Consumer;

/**
 * Modern login screen: gradient background + centered white card.
 * Usage:  new loginPanel(username -> { ...open main menu... })
 */
public class loginPanel extends JPanel {

    private static final Color DARK = new Color(0x1E3A8A);
    private static final Color LIGHT = new Color(0x3B82F6);
    private static final Color PRIMARY = new Color(0x2563EB);
    private static final Color PRIMARY_HOVER = new Color(0x1D4ED8);
    private static final Color TEXT = new Color(0x111827);
    private static final Color MUTED = new Color(0x6B7280);
    private static final Color BORDER_IDLE = new Color(0xD1D5DB);
    private static final Color ERROR = new Color(0xDC2626);
    private static final String FONT = "Segoe UI";

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JLabel errorLabel = new JLabel(" ");
    private final Consumer<String> onSuccess;

    public loginPanel(Consumer<String> onSuccess) {
        this.onSuccess = onSuccess;
        setLayout(new GridBagLayout());

        // ---------- card ----------
        JPanel card = new RoundedCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(48, 52, 44, 52));

        JComponent logo = new Logo();
        logo.setPreferredSize(new Dimension(64, 64));
        card.add(wrap(logo, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(14));

        card.add(wrap(text("Online Reservation", 22, Font.BOLD, TEXT, true), BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(4));
        card.add(wrap(text("Sign in to book or cancel tickets", 13, Font.PLAIN, MUTED, true), BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(26));

        // username
        card.add(wrap(text("Username", 12, Font.BOLD, TEXT, false), BorderLayout.WEST));
        card.add(Box.createVerticalStrut(6));
        styleField(userField);
        card.add(wrap(userField, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(16));

        // password
        card.add(wrap(text("Password", 12, Font.BOLD, TEXT, false), BorderLayout.WEST));
        card.add(Box.createVerticalStrut(6));
        styleField(passField);
        card.add(wrap(passField, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(8));

        final char defaultEcho = passField.getEchoChar();
        JCheckBox show = new JCheckBox("Show password");
        show.setOpaque(false);
        show.setFocusPainted(false);
        show.setFont(new Font(FONT, Font.PLAIN, 12));
        show.setForeground(MUTED);
        show.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        show.addActionListener(e -> passField.setEchoChar(show.isSelected() ? (char) 0 : defaultEcho));
        card.add(wrap(show, BorderLayout.WEST));
        card.add(Box.createVerticalStrut(10));

        // error message (inline)
        errorLabel.setFont(new Font(FONT, Font.BOLD, 12));
        errorLabel.setForeground(ERROR);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(wrap(errorLabel, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(10));

        // login button
        JButton loginBtn = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? PRIMARY_HOVER : PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginBtn.setFont(new Font(FONT, Font.BOLD, 15));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setRolloverEnabled(true);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setPreferredSize(new Dimension(300, 44));
        loginBtn.addActionListener(e -> doLogin());
        card.add(wrap(loginBtn, BorderLayout.CENTER));
        card.add(Box.createVerticalStrut(18));

        card.add(wrap(text("Demo login:  admin / admin123", 11, Font.PLAIN, MUTED, true), BorderLayout.CENTER));

        add(card);

        // Enter key: username -> password -> login
        userField.addActionListener(e -> passField.requestFocusInWindow());
        passField.addActionListener(e -> doLogin());
    }

    // ================= login logic =================

    private void doLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }
        try {
            if (authenticate(username, password)) {
                errorLabel.setText(" ");
                passField.setText("");
                onSuccess.accept(username);
            } else {
                showError("Access Denied: invalid credentials.");
                passField.setText("");
                passField.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            showError("Database error. Check the SQLite driver.");
            ex.printStackTrace();
        }
    }

    private boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password_hash = ?";
        try (Connection c = DatabaseSetup.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, DatabaseSetup.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    // ================= UI helpers =================

    private JLabel text(String s, int size, int style, Color color, boolean center) {
        JLabel l = new JLabel(s);
        l.setFont(new Font(FONT, style, size));
        l.setForeground(color);
        if (center) l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    /** Wraps a component so BoxLayout stretches it to full card width. */
    private JPanel wrap(JComponent c, String position) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, position);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font(FONT, Font.PLAIN, 14));
        f.setForeground(TEXT);
        f.setPreferredSize(new Dimension(300, 42));
        f.setBorder(fieldBorder(BORDER_IDLE));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(fieldBorder(PRIMARY)); }
            @Override public void focusLost(FocusEvent e) { f.setBorder(fieldBorder(BORDER_IDLE)); }
        });
    }

    private CompoundBorder fieldBorder(Color c) {
        return new CompoundBorder(new LineBorder(c, 2, true), new EmptyBorder(0, 12, 0, 12));
    }

    // ---------- gradient background ----------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, DARK, getWidth(), getHeight(), LIGHT));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    // ---------- white rounded card with soft shadow ----------
    private static class RoundedCard extends JPanel {
        RoundedCard() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 10; i++) {
                g2.setColor(new Color(0, 0, 0, 7));
                g2.fillRoundRect(i, i + 4, getWidth() - 2 * i, getHeight() - 2 * i - 4, 34, 34);
            }
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 26, 26);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Insets getInsets() {
            Insets i = super.getInsets();
            return new Insets(i.top + 10, i.left + 10, i.bottom + 10, i.right + 10);
        }
    }

    // ---------- small train logo ----------
    private static class Logo extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2, cy = getHeight() / 2;
            g2.setPaint(new GradientPaint(cx - 30, cy - 30, LIGHT, cx + 30, cy + 30, DARK));
            g2.fillOval(cx - 30, cy - 30, 60, 60);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(cx - 14, cy - 15, 28, 24, 8, 8);   // train body
            g2.setColor(PRIMARY);
            g2.fillRoundRect(cx - 10, cy - 11, 9, 9, 3, 3);     // windows
            g2.fillRoundRect(cx + 1, cy - 11, 9, 9, 3, 3);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx - 11, cy + 8, 7, 7);                 // wheels
            g2.fillOval(cx + 4, cy + 8, 7, 7);
            g2.dispose();
        }
    }
}
