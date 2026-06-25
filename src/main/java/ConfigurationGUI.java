import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ConfigurationGUI extends JFrame {

    private JSpinner pageSizeSpinner;
    private JSpinner cpuSpinner;
    private JSpinner quantumSpinner;
    private JButton saveBtn;
    private JButton closeBtn;

    public ConfigurationGUI() {
        super(KernelTheme.OS_NAME + " — Kernel Configuration");
        KernelTheme.init();
        buildUi();
        KernelTheme.applyToWindow(this);
        wireButtons();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        KernelTheme.stylePanel(root);

        KernelConfig cfg = KernelConfig.getInstance();
        JPanel card = UiLayout.formCard();

        UiLayout.addCardTitle(card, 0, "Kernel Settings");

        pageSizeSpinner = new JSpinner(new SpinnerNumberModel(cfg.getPageSizeBits(), 8, 16, 1));
        UiLayout.addAlignedFormRow(card, 1, 130, "Page Size (2^n bytes):", pageSizeSpinner);

        cpuSpinner = new JSpinner(new SpinnerNumberModel(cfg.getCpuCount(), 1, 16, 1));
        UiLayout.addAlignedFormRow(card, 2, "CPU Count:", cpuSpinner);

        quantumSpinner = new JSpinner(new SpinnerNumberModel(cfg.getTimeQuantum(), 1, 100, 1));
        UiLayout.addAlignedFormRow(card, 3, "Time Quantum:", quantumSpinner);

        saveBtn = new JButton("Save & Apply");
        closeBtn = new JButton("Back");
        KernelTheme.stylePrimaryButton(saveBtn, "Save & Apply");
        KernelTheme.styleSecondaryButton(closeBtn, "Back");

        for (JButton btn : new JButton[]{saveBtn, closeBtn}) {
            UiLayout.normalizeMenuButton(btn);
        }

        GridBagConstraints gbc = UiLayout.cardGbc(4);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 10, 2, 10);
        card.add(buildButtonColumn(saveBtn, closeBtn), gbc);

        UiLayout.mountCenteredCard(root, card);
        setContentPane(root);
        UiLayout.applyCompactWindow(this, 320, 280, 300, 240);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JPanel buildButtonColumn(JButton... buttons) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        int rowHeight = UiLayout.MENU_BUTTON.height + 18;
        for (int i = 0; i < buttons.length; i++) {
            UiLayout.normalizeMenuButton(buttons[i]);
            buttons[i].setMaximumSize(new Dimension(
                    UiLayout.MENU_BUTTON.width + 8, UiLayout.MENU_BUTTON.height + 14));
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            row.setOpaque(false);
            row.setPreferredSize(new Dimension(UiLayout.MENU_BUTTON.width, rowHeight));
            row.setMinimumSize(new Dimension(UiLayout.MENU_BUTTON.width, rowHeight));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
            row.add(buttons[i]);
            col.add(row);
            if (i < buttons.length - 1) {
                col.add(Box.createVerticalStrut(UiLayout.BUTTON_GAP));
            }
        }
        return col;
    }

    private void wireButtons() {
        ButtonWiring.bind(saveBtn, this::saveConfig);
        ButtonWiring.bind(closeBtn, () -> NavigationHelper.back(this));
    }

    private void saveConfig() {
        try {
            KernelConfig cfg = KernelConfig.getInstance();
            cfg.setPageSizeBits((Integer) pageSizeSpinner.getValue());
            cfg.setCpuCount((Integer) cpuSpinner.getValue());
            cfg.setTimeQuantum((Integer) quantumSpinner.getValue());
            if (cfg.saveToFile()) {
                JOptionPane.showMessageDialog(this,
                        "Configuration saved to page_size.txt",
                        KernelTheme.OS_NAME, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not save configuration file.",
                        KernelTheme.OS_NAME, JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Save failed: " + ex.getMessage(),
                    KernelTheme.OS_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }
}
