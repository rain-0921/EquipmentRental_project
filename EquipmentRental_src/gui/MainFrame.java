package gui;

import system.RentalSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, SidebarButton> navButtons = new LinkedHashMap<>();

    public MainFrame() {
        super("Smart Equipment Rental & Billing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.CONTENT_BG);

        RentalSystem system = new RentalSystem();

        ManageUserPanel manageUserPanel = new ManageUserPanel(system);
        EquipmentPanel equipmentPanel = new EquipmentPanel(system);
        BillingPanel billingPanel = new BillingPanel();
        RentalPanel rentalPanel = new RentalPanel(system, equipmentPanel, billingPanel);

        // Keep the Rental tab's dropdowns in sync whenever Equipment or Manage User change.
        equipmentPanel.setOnChange(rentalPanel::refreshAll);
        manageUserPanel.setOnChange(rentalPanel::refreshAll);

        // Load any bills already persisted in the database (from a previous session)
        for (var rental : system.getAllRentals()) {
            if (rental.getBill() != null) {
                billingPanel.addBill(rental.getBill());
            }
        }

        contentPanel.setBackground(UIStyle.CONTENT_BG);
        contentPanel.add(scrollWrap(manageUserPanel), "manageUser");
        contentPanel.add(scrollWrap(equipmentPanel), "equipment");
        contentPanel.add(scrollWrap(rentalPanel), "rental");
        contentPanel.add(scrollWrap(billingPanel), "billing");

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showCard("manageUser");
    }

    private JScrollPane scrollWrap(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIStyle.CONTENT_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(232, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        sidebar.add(buildBrand());
        sidebar.add(separator());
        sidebar.add(Box.createVerticalStrut(10));

        addNavButton(sidebar, "manageUser", "Manage User");
        addNavButton(sidebar, "equipment", "Equipment");
        addNavButton(sidebar, "rental", "Rental & Return");
        addNavButton(sidebar, "billing", "Billing History");

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("<html>CCP6224 OOAD<br>Final Assignment</html>");
        footer.setFont(UIStyle.FONT_BODY);
        footer.setForeground(UIStyle.SIDEBAR_TEXT_MUTED);
        footer.setBorder(new EmptyBorder(16, 20, 20, 20));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private JPanel buildBrand() {
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.X_AXIS));
        brand.setBorder(new EmptyBorder(26, 20, 26, 20));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JComponent mark = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIStyle.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(UIStyle.FONT_TITLE.getFamily(), Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String letter = "C";
                int tx = (getWidth() - fm.stringWidth(letter)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(letter, tx, ty);
                g2.dispose();
            }
        };
        mark.setPreferredSize(new Dimension(36, 36));
        mark.setMaximumSize(new Dimension(36, 36));

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel title = new JLabel("CampusRental");
        title.setFont(new Font(UIStyle.FONT_TITLE.getFamily(), Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("Equipment & Billing");
        tagline.setFont(UIStyle.FONT_BODY);
        tagline.setForeground(UIStyle.SIDEBAR_TEXT_MUTED);

        textBox.add(title);
        textBox.add(Box.createVerticalStrut(2));
        textBox.add(tagline);

        brand.add(mark);
        brand.add(textBox);
        return brand;
    }

    private JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UIStyle.SIDEBAR_DIVIDER);
        sep.setBackground(UIStyle.SIDEBAR_BG);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void addNavButton(JPanel sidebar, String key, String label) {
        SidebarButton btn = new SidebarButton(label);
        btn.addActionListener(e -> showCard(key));
        navButtons.put(key, btn);
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(2));
    }

    private void showCard(String key) {
        cardLayout.show(contentPanel, key);
        navButtons.forEach((k, btn) -> btn.setActive(k.equals(key)));
    }
}
