import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public final class KernelTheme {

    public static final String OS_NAME = "SimulationOS";
    public static final String OS_FULL_NAME = "SimulationOS";
    private static final String THEME_KEY = "simulationos.themed";

    public static final Color BG = new Color(198, 192, 176);
    public static final Color BG_GRADIENT_END = new Color(175, 168, 152);
    public static final Color BG_PANEL = new Color(212, 206, 192);
    public static final Color PRIMARY = new Color(95, 130, 82);
    public static final Color PRIMARY_HOVER = new Color(78, 112, 68);
    public static final Color SECONDARY = new Color(148, 172, 134);
    public static final Color ACCENT = new Color(180, 105, 78);
    public static final Color ACCENT_HOVER = new Color(160, 88, 65);
    public static final Color SUCCESS = new Color(78, 140, 98);
    public static final Color CARD = new Color(228, 224, 212);
    public static final Color TEXT = new Color(42, 38, 34);
    public static final Color TEXT_MUTED = new Color(95, 88, 78);
    public static final Color BORDER = new Color(162, 155, 138);
    public static final Color TABLE_HEADER = new Color(185, 195, 172);
    public static final Color HOVER_SOFT = new Color(200, 194, 178);

    private KernelTheme() {
    }

    public static void init() {
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("Panel.background", BG);
        UIManager.put("RootPane.background", BG);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.contentAreaColor", BG_PANEL);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", bodyFont());
        UIManager.put("Button.font", buttonFont());
        UIManager.put("TextField.background", CARD);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextArea.background", CARD);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("ComboBox.background", CARD);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("Table.background", CARD);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("Viewport.background", BG_PANEL);
        UIManager.put("RadioButton.foreground", TEXT);
        UIManager.put("RadioButton.background", BG_PANEL);
        UIManager.put("Spinner.background", CARD);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", TEXT);
    }

    public static Font titleFont() {
        return new Font("Segoe UI", Font.BOLD, 26);
    }

    public static Font headingFont() {
        return new Font("Segoe UI", Font.BOLD, 16);
    }

    public static Font bodyFont() {
        return new Font("Segoe UI", Font.PLAIN, 13);
    }

    public static Font buttonFont() {
        return new Font("Segoe UI", Font.BOLD, 13);
    }

    public static Font smallFont() {
        return new Font("Segoe UI", Font.PLAIN, 12);
    }

    
    public static void applyToContainer(Container container) {
        if (container == null) {
            return;
        }
        container.setBackground(BG_PANEL);
        applyRecursively(container);
    }
    
    public static void applyToWindow(JFrame frame) {
        if (frame == null) {
            return;
        }
        frame.getContentPane().setBackground(BG);
        applyRecursively(frame.getContentPane());
        if (frame.getTitle() == null || frame.getTitle().isBlank()) {
            frame.setTitle(OS_NAME);
        }
        frame.repaint();
    }

    public static void applyToWindow(Window window) {
        if (window instanceof JFrame frame) {
            applyToWindow(frame);
        }
    }

    public static void stylePanel(JPanel panel) {
        panel.setBackground(BG);
    }

    public static void styleCard(JPanel panel) {
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
    }

    public static void styleTitle(JLabel label, String text) {
        label.setText(text);
        label.setFont(titleFont());
        label.setForeground(TEXT);
    }

    public static void styleLabel(JLabel label) {
        if (label.getFont() != null && label.getFont().getSize() >= 24) {
            label.setFont(headingFont());
        } else if (label.getFont() != null && label.getFont().getSize() >= 18) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        } else {
            label.setFont(bodyFont());
        }
        label.setForeground(TEXT);
        label.setBackground(BG_PANEL);
        label.setOpaque(false);
    }

    public static void stylePrimaryButton(JButton button, String text) {
        button.putClientProperty(THEME_KEY, Boolean.TRUE);
        button.setText(text);
        applyPrimaryButtonLook(button);
    }

    public static void styleSecondaryButton(JButton button, String text) {
        button.putClientProperty(THEME_KEY, Boolean.TRUE);
        button.setText(text);
        button.setFont(buttonFont());
        button.setForeground(TEXT);
        button.setBackground(CARD);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addHover(button, CARD, HOVER_SOFT);
    }

    public static void styleAccentButton(JButton button, String text) {
        button.putClientProperty(THEME_KEY, Boolean.TRUE);
        button.setText(text);
        button.setFont(buttonFont());
        button.setForeground(Color.BLACK);
        button.setBackground(ACCENT);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addHover(button, ACCENT, ACCENT_HOVER);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(bodyFont());
        field.setForeground(TEXT);
        field.setBackground(CARD);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(bodyFont());
        area.setForeground(TEXT);
        area.setBackground(CARD);
        area.setCaretColor(TEXT);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    public static void styleTable(JTable table) {
        table.setFont(bodyFont());
        table.setRowHeight(24);
        table.setGridColor(BORDER);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 80));
        table.setSelectionForeground(TEXT);
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            table.getTableHeader().setBackground(TABLE_HEADER);
            table.getTableHeader().setForeground(TEXT);
        }
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(bodyFont());
        combo.setForeground(TEXT);
        combo.setBackground(CARD);
    }

    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(bodyFont());
    }

    public static void styleTitledBorder(TitledBorder border) {
        border.setTitleColor(TEXT);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private static void applyRecursively(Component component) {
        if (component instanceof JPanel panel) {
            panel.setBackground(BG_PANEL);
            panel.setForeground(TEXT);
            if (panel.getBorder() instanceof TitledBorder tb) {
                styleTitledBorder(tb);
            }
        } else if (component instanceof JTabbedPane tabs) {
            styleTabbedPane(tabs);
        } else if (component instanceof JLabel label) {
            styleLabel(label);
        } else if (component instanceof JButton button) {
            if (!Boolean.TRUE.equals(button.getClientProperty(THEME_KEY))) {
                styleButtonAuto(button);
            }
        } else if (component instanceof JTextField field) {
            styleTextField(field);
        } else if (component instanceof JTextArea area) {
            styleTextArea(area);
        } else if (component instanceof JTable table) {
            styleTable(table);
        } else if (component instanceof JComboBox<?> combo) {
            styleComboBox(combo);
        } else if (component instanceof JRadioButton radio) {
            radio.setFont(bodyFont());
            radio.setForeground(TEXT);
            radio.setBackground(BG_PANEL);
        } else if (component instanceof JScrollPane scroll) {
            scroll.setBackground(BG);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
            scroll.getViewport().setBackground(BG_PANEL);
        } else if (component instanceof JSpinner spinner) {
            spinner.setFont(bodyFont());
            spinner.setBackground(CARD);
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyRecursively(child);
            }
        }
    }

    private static void styleButtonAuto(JButton button) {
        button.putClientProperty(THEME_KEY, Boolean.TRUE);
        String text = button.getText() == null ? "" : button.getText().toLowerCase();

        if (text.contains("exit") || text.contains("destroy") || text.contains("clear")
                || text.contains("remove") || text.contains("stop")) {
            styleAccentButton(button, button.getText());
        } else if (text.contains("main menu") || text.contains("back") || text.contains("close")) {
            styleSecondaryButton(button, button.getText());
        } else {
            applyPrimaryButtonLook(button);
        }
    }

    private static void applyPrimaryButtonLook(JButton button) {
        button.setFont(buttonFont());
        button.setForeground(Color.BLACK);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY.darker(), 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addHover(button, PRIMARY, PRIMARY_HOVER);
    }

    private static void addHover(JButton button, Color normal, Color hover) {
        if (Boolean.TRUE.equals(button.getClientProperty("kernelTheme.hover"))) {
            return;
        }
        button.putClientProperty("kernelTheme.hover", Boolean.TRUE);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }
}
