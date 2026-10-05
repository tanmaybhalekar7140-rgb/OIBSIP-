

import javax.swing.*;
import java.awt.*;

/**
 * Entry point. Creates the database, shows the login screen,
 * then switches to the main menu after a successful login.
 */
public class MainApp extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public MainApp() {
        super("Online Reservation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(root);
        setSize(920, 680);
        setMinimumSize(new Dimension(760, 640));
        setLocationRelativeTo(null);
        showLogin();
    }

    /** Fresh login screen (also used by the Logout button). */
    private void showLogin() {
        show(new loginPanel(this::showMainMenu), "login");
    }

    private void showMainMenu(String username) {
        MainMenuPanel menu = new MainMenuPanel(
                username,
                () -> new ReservationDialog(this).setVisible(true),    // Book Ticket
                () -> new CancellationDialog(this).setVisible(true),   // Cancel Ticket
                this::showLogin);                                       // Logout
        show(menu, "menu");
    }

    private void show(JPanel panel, String name) {
        root.removeAll();
        root.add(panel, name);
        cards.show(root, name);
        root.revalidate();
        root.repaint();
    }

    public static void main(String[] args) {
        try {
            DatabaseSetup.init();   // creates tables, admin user and sample trains
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null,
                    "Could not set up the database.\nIs the SQLite JDBC jar added to the project?\n\n"
                            + ex.getMessage(),
                    "Startup error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}