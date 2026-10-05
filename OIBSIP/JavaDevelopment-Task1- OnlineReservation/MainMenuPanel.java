

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main menu shown after login.
 * Usage: new MainMenuPanel(username, onBook, onCancel, onLogout)
 * The three Runnables are provided by MainApp (open dialogs / go back to login).
 */
public class MainMenuPanel extends JPanel {

    private static final Color DARK = new Color(0x1E3A8A);
    private static final Color LIGHT = new Color(0x3B82F6);
    private static final Color GREEN = new Color(0x059669);
    private static final Color RED = new Color(0xDC2626);
    private static final Color TEXT = new Color(0x111827);
    private static final Color MUTED = new Color(0x6B7280);
    private static final String FONT = "Segoe UI";

    public MainMenuPanel(String username, Runnable onBook, Runnable onCancel, Runnable onLogout) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(28, 36, 28, 36));

        // ---------- header ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel welcome = new JPanel();
        welcome.setOpaque(false);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        JLabel hello = new JLabel("Welcome, " + username);
        hello.setFont(new Font(FONT, Font.BOLD, 26));
        hello.setForeground(Color.WHITE);
        JLabel sub = new JLabel("What would you like to do today?");
        sub.setFont(new Font(FONT, Font.PLAIN, 14));
        sub.setForeground(new Color(255, 255, 255, 200));
        welcome.add(hello);
        welcome.add(Box.createVerticalStrut(4));
        welcome.add(sub);

        JPanel logoutWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        logoutWrap.setOpaque(false);
        logoutWrap.add(createLogoutButton(onLogout));

        header.add(welcome, BorderLayout.WEST);
        header.add(logoutWrap, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ---------- cards ----------
        JPanel cards = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        cards.setOpaque(false);
        cards.add(new MenuCard("Book Ticket", "Reserve a seat and\nget your PNR number",
                GREEN, true, onBook));
        cards.add(new MenuCard("Cancel Ticket", "Cancel a booking\nusing your PNR number",
                RED, false, onCancel));

        JPanel center = new JPanel(new GridBagLayout());   // centers the cards vertically
        center.setOpaque(false);
        center.add(cards);
        add(center, BorderLayout.CENTER);
    }

    private JButton createLogoutButton(Runnable onLogout) {
        JButton b = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, getModel().isRollover() ? 70 : 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(255, 255, 255, 150));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font(FONT, Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 38));
        b.addActionListener(e -> onLogout.run());
        return b;
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

    // ---------- big clickable card ----------
    private static class MenuCard extends JPanel {
        private final String title;
        private final String[] descLines;
        private final Color accent;
        private final boolean isBook;
        private boolean hover = false;

        MenuCard(String title, String desc, Color accent, boolean isBook, Runnable action) {
            this.title = title;
            this.descLines = desc.split("\n");
            this.accent = accent;
            this.isBook = isBook;
            setOpaque(false);
            setPreferredSize(new Dimension(250, 250));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    if (contains(e.getPoint())) action.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int dy = hover ? -5 : 0;                 // card lifts on hover
            int x = 12, y = 12 + dy, cw = w - 24, ch = h - 36;

            // soft shadow
            for (int i = 0; i < 10; i++) {
                g2.setColor(new Color(0, 0, 0, hover ? 8 : 5));
                g2.fillRoundRect(x - i, y - i + 5, cw + 2 * i, ch + 2 * i, 26 + i, 26 + i);
            }
            // card
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, cw, ch, 26, 26);

            // icon circle
            int cx = w / 2, cy = y + 62;
            g2.setColor(accent);
            g2.fillOval(cx - 34, cy - 34, 68, 68);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (isBook) {                            // plus sign
                g2.drawLine(cx - 15, cy, cx + 15, cy);
                g2.drawLine(cx, cy - 15, cx, cy + 15);
            } else {                                 // cross
                g2.drawLine(cx - 12, cy - 12, cx + 12, cy + 12);
                g2.drawLine(cx - 12, cy + 12, cx + 12, cy - 12);
            }

            // title
            g2.setFont(new Font(FONT, Font.BOLD, 20));
            g2.setColor(TEXT);
            FontMetrics fm = g2.getFontMetrics();
            int ty = y + 135;
            g2.drawString(title, cx - fm.stringWidth(title) / 2, ty);

            // description
            g2.setFont(new Font(FONT, Font.PLAIN, 13));
            g2.setColor(MUTED);
            fm = g2.getFontMetrics();
            int dyText = ty + 28;
            for (String line : descLines) {
                g2.drawString(line, cx - fm.stringWidth(line) / 2, dyText);
                dyText += 20;
            }

            // accent bar at the bottom of the card
            g2.setColor(hover ? accent : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 90));
            g2.fillRoundRect(cx - 24, y + ch - 22, 48, 5, 5, 5);

            g2.dispose();
        }
    }
}