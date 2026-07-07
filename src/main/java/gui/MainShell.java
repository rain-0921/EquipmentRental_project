package gui;

import model.user.User;
import model.user.UserType;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * Role-aware main shell with a sidebar on the left and a card
 * layout on the right. Students see Browse / My Rentals / My
 * Profile. Staff see Browse / Users / Rentals / New User.
 */
public class MainShell extends JFrame {

    public static final String KEY_BROWSE   = "browse";
    public static final String KEY_RENT     = "rent";
    public static final String KEY_RETURN   = "return";
    public static final String KEY_RENTALS  = "rentals";
    public static final String KEY_PROFILE  = "profile";
    public static final String KEY_USERS    = "users";
    public static final String KEY_NEW_USER = "newuser";
    public static final String KEY_ABOUT    = "about";

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final SidebarPanel sidebar;
    private final User currentUser;

    // Cached so RentPanel/ReturnPanel can refresh them after a write.
    private MyRentalsPanel myRentalsPanel;
    private ReturnPanel    returnPanel;
    private EquipmentPanel equipmentPanel;

    public MainShell(User user) {
        super("Smart Equipment Rental System");
        this.currentUser = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);

        String roleLabel = user.getType().getDisplayName();
        sidebar = new SidebarPanel(user.getFullName(), roleLabel, this::navigate);

        buildNavForRole(user.getType());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.SURFACE);
        root.add(sidebar, BorderLayout.WEST);
        content.setBackground(Theme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        // Land on the first available page.
        sidebar.getFirstButton().doClick();
        setVisible(true);
    }

    private void buildNavForRole(UserType type) {
        switch (type) {
            case STUDENT -> {
                sidebar.addNavButton("Browse Equipment", KEY_BROWSE);
                sidebar.addNavButton("Rent Equipment",   KEY_RENT);
                sidebar.addNavButton("Return Equipment", KEY_RETURN);
                sidebar.addNavButton("My Rentals",       KEY_RENTALS);
                sidebar.addNavButton("My Profile",       KEY_PROFILE);
                sidebar.addSeparator();
                sidebar.addNavButton("About",            KEY_ABOUT);
                sidebar.addFooterButton("Logout", e -> logout());

                Runnable refreshSiblings = () -> {
                    myRentalsPanel.refresh();
                    returnPanel.reloadRentals();
                    equipmentPanel.refresh();
                };

                equipmentPanel = new EquipmentPanel();
                content.add(wrap(equipmentPanel),                          KEY_BROWSE);
                content.add(wrap(new RentPanel(currentUser, refreshSiblings)), KEY_RENT);
                myRentalsPanel = new MyRentalsPanel(currentUser.getUserId());
                content.add(wrap(myRentalsPanel),                          KEY_RENTALS);
                returnPanel = new ReturnPanel(currentUser.getUserId());
                returnPanel.setOnReturnProcessed(refreshSiblings);
                content.add(wrap(returnPanel),                             KEY_RETURN);
                content.add(wrap(new ProfilePanel(currentUser)),           KEY_PROFILE);
                content.add(wrap(new AboutPanel()),                        KEY_ABOUT);
            }
            case STAFF -> {
                sidebar.addNavButton("Browse Equipment", KEY_BROWSE);
                sidebar.addNavButton("Rent Equipment",   KEY_RENT);
                sidebar.addNavButton("All Rentals",      KEY_RENTALS);
                sidebar.addNavButton("Return Equipment", KEY_RETURN);
                sidebar.addNavButton("Manage Users",     KEY_USERS);
                sidebar.addNavButton("New User",         KEY_NEW_USER);
                sidebar.addSeparator();
                sidebar.addNavButton("About",            KEY_ABOUT);
                sidebar.addFooterButton("Logout", e -> logout());

                Runnable refreshSiblings = () -> {
                    myRentalsPanel.refresh();
                    returnPanel.reloadRentals();
                    equipmentPanel.refresh();
                };
                UsersPanel usersPanel = new UsersPanel();
                Runnable refreshUsers = () -> {
                    usersPanel.refresh();
                    refreshSiblings.run();
                };

                equipmentPanel = new EquipmentPanel();
                content.add(wrap(equipmentPanel),                          KEY_BROWSE);
                content.add(wrap(new RentPanel(currentUser, refreshSiblings)), KEY_RENT);
                myRentalsPanel = new MyRentalsPanel(null); // null = show all
                content.add(wrap(myRentalsPanel),                          KEY_RENTALS);
                returnPanel = new ReturnPanel(null); // null = show all
                returnPanel.setOnReturnProcessed(refreshSiblings);
                content.add(wrap(returnPanel),                             KEY_RETURN);
                content.add(wrap(usersPanel),                              KEY_USERS);
                NewUserPanel newUserPanel = new NewUserPanel();
                newUserPanel.setOnCreated(u -> refreshUsers.run());
                content.add(wrap(newUserPanel),                            KEY_NEW_USER);
                content.add(wrap(new AboutPanel()),                        KEY_ABOUT);
            }
        }
    }

    private void navigate(String key) {
        cards.show(content, key);
    }

    private void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private static JPanel wrap(JPanel inner) {
        JPanel host = new JPanel(new BorderLayout());
        host.setBackground(Theme.SURFACE);
        host.add(inner, BorderLayout.CENTER);
        return host;
    }
}