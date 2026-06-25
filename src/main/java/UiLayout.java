import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class UiLayout {

    public static final int BUTTON_GAP = 10;
    public static final int PANEL_PADDING = 14;
    public static final int SIDEBAR_WIDTH = 220;
    public static final int CARD_MAX_WIDTH = 380;
    
    public static final Dimension MENU_BUTTON = new Dimension(240, 32);
    
    public static final Dimension WIDE_MENU_BUTTON = MENU_BUTTON;
    
    public static final Dimension ACTION_BUTTON = new Dimension(160, 32);
    
    @Deprecated
    public static final Dimension COMPACT_BUTTON = ACTION_BUTTON;
    
    public static final Dimension TOOLBAR_BUTTON = new Dimension(88, 28);
    
    public static final Dimension INLINE_BUTTON = new Dimension(96, 30);
    /** Three-level scheduler actions — wide enough for full labels. */
    public static final Dimension SCHEDULER_LEVEL_BUTTON = new Dimension(250, 36);
    public static final Dimension FORM_FIELD = new Dimension(130, 28);
    public static final Dimension FORM_FIELD_WIDE = new Dimension(190, 28);
    /** Process / parent dropdowns — wide enough for "P9999 - ProcessName". */
    public static final Dimension COMBO_FIELD = new Dimension(280, 28);
    public static final String COMBO_PROTOTYPE = "P9999 - SampleProcess";
    
    public static final Dimension FORM_SPINNER = new Dimension(130, 30);
    public static final int FORM_LABEL_WIDTH = 130;

    private UiLayout() {
    }

    public static Rectangle usableScreenBounds() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }

    public static int screenWidth() {
        return usableScreenBounds().width;
    }

    public static int screenHeight() {
        return usableScreenBounds().height;
    }

    public static int scaledSidebarWidth() {
        int w = screenWidth();
        if (w < 1100) {
            return 200;
        }
        if (w < 1400) {
            return SIDEBAR_WIDTH;
        }
        return 250;
    }

    public static int scaledCardMaxWidth() {
        int w = screenWidth();
        return Math.min(CARD_MAX_WIDTH, Math.max(300, (int) (w * 0.38)));
    }

    public static Dimension fitWindow(int preferredWidth, int preferredHeight,
            int minWidth, int minHeight, double maxWidthFraction, double maxHeightFraction) {
        Rectangle screen = usableScreenBounds();
        int maxW = (int) (screen.width * maxWidthFraction);
        int maxH = (int) (screen.height * maxHeightFraction);
        int width = Math.min(Math.max(preferredWidth, minWidth), maxW);
        int height = Math.min(Math.max(preferredHeight, minHeight), maxH);
        width = Math.min(width, screen.width);
        height = Math.min(height, screen.height);
        return new Dimension(width, height);
    }

    public static void applyWindowBounds(JFrame frame, int preferredWidth, int preferredHeight,
            int minWidth, int minHeight, double maxWidthFraction, double maxHeightFraction) {
        Dimension size = fitWindow(preferredWidth, preferredHeight, minWidth, minHeight,
                maxWidthFraction, maxHeightFraction);
        frame.setMinimumSize(new Dimension(Math.min(minWidth, size.width), Math.min(minHeight, size.height)));
        frame.setSize(size);
        frame.setLocationRelativeTo(null);
    }

    public static void applyCompactWindow(JFrame frame, int preferredWidth, int preferredHeight,
            int minWidth, int minHeight) {
        applyWindowBounds(frame, preferredWidth, preferredHeight, minWidth, minHeight, 0.42, 0.88);
    }

    public static void applyWorkspaceWindow(JFrame frame, int preferredWidth, int preferredHeight,
            int minWidth, int minHeight) {
        applyWindowBounds(frame, preferredWidth, preferredHeight, minWidth, minHeight, 0.95, 0.92);
    }

    public static GridBagConstraints formGbc(int x, int y, double weightx) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = weightx;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    public static void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
            String labelText, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(4, 8, 4, 8);
        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new Dimension(FORM_LABEL_WIDTH, 24));
        lbl.setHorizontalAlignment(JLabel.RIGHT);
        KernelTheme.styleLabel(lbl);
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 8);
        if (field instanceof JTextField tf) {
            applyFormFieldSize(tf, FORM_FIELD);
            KernelTheme.styleTextField(tf);
        } else if (field instanceof JSpinner spinner) {
            applyFormFieldSize(spinner, FORM_SPINNER);
            spinner.setFont(KernelTheme.bodyFont());
        } else if (field instanceof JComboBox<?> combo) {
            applyComboBoxSize(combo, FORM_FIELD_WIDE);
        } else {
            applyFormFieldSize(field, FORM_FIELD_WIDE);
        }
        panel.add(field, gbc);
    }

    public static JPanel menuButtonColumn(JButton... buttons) {
        return buttonColumn(buttons, MENU_BUTTON);
    }

    
    public static JPanel centeredMenuColumn(JButton... buttons) {
        return buttonColumn(buttons, COMPACT_BUTTON);
    }

    
    public static JPanel wideMenuColumn(JButton... buttons) {
        return buttonColumn(buttons, WIDE_MENU_BUTTON);
    }

    private static JPanel buttonColumn(JButton[] buttons, Dimension size) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));
        int rowHeight = size.height + 18;
        for (int i = 0; i < buttons.length; i++) {
            normalizeButton(buttons[i], size);
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            row.setOpaque(false);
            row.setPreferredSize(new Dimension(size.width, rowHeight));
            row.setMinimumSize(new Dimension(size.width, rowHeight));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
            row.add(buttons[i]);
            panel.add(row);
            if (i < buttons.length - 1) {
                panel.add(Box.createVerticalStrut(BUTTON_GAP));
            }
        }
        return panel;
    }

    
    public static JPanel centeredMenuScreen(JLabel title, JButton... buttons) {
        JPanel screen = new JPanel(new BorderLayout(0, 16));
        screen.setOpaque(false);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBorder(new EmptyBorder(PANEL_PADDING, 0, 0, 0));
        screen.add(title, BorderLayout.NORTH);
        screen.add(centeredMenuColumn(buttons), BorderLayout.CENTER);
        return screen;
    }

    
    public static void hideTabBar(JTabbedPane pane) {
        pane.putClientProperty("JTabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
        pane.putClientProperty("JTabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        pane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return 0;
            }

            @Override
            protected int calculateTabHeight(int tabPlacement, int fontHeight, int maxIconHeight) {
                return 0;
            }
        });
    }

    public static JPanel sidebar(String title, JButton... buttons) {
        JPanel sidebar = new JPanel(new BorderLayout(0, 8));
        sidebar.setBackground(KernelTheme.BG);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, KernelTheme.BORDER),
                new EmptyBorder(0, 0, 0, 0)));
        sidebar.setPreferredSize(new Dimension(scaledSidebarWidth(), 0));

        JLabel heading = new JLabel(title, JLabel.CENTER);
        KernelTheme.styleLabel(heading);
        heading.setFont(KernelTheme.headingFont());
        heading.setBorder(new EmptyBorder(PANEL_PADDING, 8, 4, 8));
        JPanel buttonCol = menuButtonColumn(buttons);
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(buttonCol, BorderLayout.CENTER);
        sidebar.add(top, BorderLayout.NORTH);
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        sidebar.add(filler, BorderLayout.CENTER);
        return sidebar;
    }

    
    public static void mountCenteredCard(JPanel host, JPanel card) {
        host.removeAll();
        host.setLayout(new BorderLayout());
        host.setBackground(KernelTheme.BG);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        card.setMaximumSize(new Dimension(scaledCardMaxWidth(), Integer.MAX_VALUE));
        wrapper.add(card, gbc);
        host.add(wrapper, BorderLayout.CENTER);
    }

    public static void addAlignedFormRow(JPanel panel, int row, int labelWidth,
            String labelText, JComponent field) {
        GridBagConstraints gbc = cardGbc(row);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 10, 3, 6);
        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new Dimension(labelWidth, 26));
        lbl.setHorizontalAlignment(JLabel.RIGHT);
        KernelTheme.styleLabel(lbl);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 0, 3, 10);
        if (field instanceof JSpinner spinner) {
            applyFormFieldSize(spinner, FORM_SPINNER);
            spinner.setFont(KernelTheme.bodyFont());
            gbc.fill = GridBagConstraints.HORIZONTAL;
        } else if (field instanceof JComboBox<?> combo) {
            applyComboBoxSize(combo, FORM_FIELD_WIDE);
            gbc.fill = GridBagConstraints.HORIZONTAL;
        } else {
            applyFormFieldSize(field, FORM_FIELD);
            gbc.fill = GridBagConstraints.NONE;
            if (field instanceof JTextField tf) {
                KernelTheme.styleTextField(tf);
            }
        }
        panel.add(field, gbc);
    }

    /** Label above field — for narrow scheduler cards where side-by-side rows overlap. */
    public static void addStackedFormRow(JPanel panel, int row, String labelText, JComponent field) {
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        KernelTheme.styleLabel(lbl);
        stack.add(lbl);
        stack.add(Box.createVerticalStrut(4));

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JComboBox<?> combo) {
            applyComboBoxSize(combo, FORM_FIELD_WIDE);
        } else if (field instanceof JTextField tf) {
            applyFormFieldSize(tf, FORM_FIELD_WIDE);
            KernelTheme.styleTextField(tf);
        } else {
            applyFormFieldSize(field, FORM_FIELD_WIDE);
        }
        stack.add(field);

        GridBagConstraints gbc = cardGbc(row);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 10, 4, 10);
        panel.add(stack, gbc);
    }

    public static void applyFormFieldSize(JComponent field, Dimension size) {
        field.setPreferredSize(size);
        field.setMinimumSize(new Dimension(size.width, size.height));
        if (field instanceof JSpinner) {
            field.setMaximumSize(new Dimension(size.width + 28, size.height + 6));
        } else if (field instanceof JComboBox<?>) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height + 2));
        } else {
            field.setMaximumSize(new Dimension(size.width, size.height + 2));
        }
    }

    @SuppressWarnings("unchecked")
    public static void applyComboBoxSize(JComboBox<?> combo, Dimension size) {
        ((JComboBox<Object>) combo).setPrototypeDisplayValue(COMBO_PROTOTYPE);
        applyFormFieldSize(combo, size);
        KernelTheme.styleComboBox(combo);
    }

    public static void addAlignedFormRow(JPanel panel, int row, String labelText, JComponent field) {
        addAlignedFormRow(panel, row, FORM_LABEL_WIDTH, labelText, field);
    }

    public static void addCardTitle(JPanel card, int row, String text) {
        GridBagConstraints gbc = cardGbc(row);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(2, 10, 6, 10);
        JLabel title = new JLabel(text, JLabel.CENTER);
        title.setFont(KernelTheme.headingFont());
        KernelTheme.styleLabel(title);
        card.add(title, gbc);
    }

    public static void addCardSection(JPanel card, int row, String text) {
        GridBagConstraints gbc = cardGbc(row);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 10, 2, 10);
        JLabel section = new JLabel(text, JLabel.CENTER);
        section.setFont(KernelTheme.headingFont());
        KernelTheme.styleLabel(section);
        card.add(section, gbc);
    }

    public static JPanel formCard() {
        JPanel card = new JPanel(new GridBagLayout());
        KernelTheme.styleCard(card);
        return card;
    }

    public static GridBagConstraints cardGbc(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    
    public static void mountSidebarFrame(JFrame frame, JPanel sidebar, JComponent main,
            int width, int height) {
        frame.getContentPane().removeAll();
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(KernelTheme.BG);
        main.setBorder(new EmptyBorder(12, 12, 12, 16));
        frame.getContentPane().add(sidebar, BorderLayout.WEST);
        frame.getContentPane().add(main, BorderLayout.CENTER);
        applyWorkspaceWindow(frame, width, height, Math.min(width, 720), Math.min(height, 420));
    }

    public static JPanel buttonRow(JButton... buttons) {
        JPanel row = new JPanel(new GridLayout(1, buttons.length, BUTTON_GAP, 0));
        row.setOpaque(false);
        for (JButton b : buttons) {
            row.add(b);
        }
        return row;
    }

    
    public static JPanel centeredButtonRow(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, BUTTON_GAP, 0));
        row.setOpaque(false);
        for (JButton b : buttons) {
            normalizeCompactButton(b);
            row.add(b);
        }
        return row;
    }

    
    public static JPanel inlineButtonRow(JButton... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, BUTTON_GAP, 0));
        row.setOpaque(false);
        for (JButton b : buttons) {
            normalizeButton(b, INLINE_BUTTON);
            row.add(b);
        }
        return row;
    }

    public static void addCenteredActionButton(JPanel panel, GridBagConstraints gbc, int row, JButton button) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 8, 8, 8);
        normalizeCompactButton(button);
        panel.add(button, gbc);
    }

    public static void normalizeMenuButton(JButton button) {
        normalizeButton(button, MENU_BUTTON);
    }

    public static void normalizeActionButton(JButton button) {
        normalizeButton(button, ACTION_BUTTON);
    }

    public static void normalizeCompactButton(JButton button) {
        normalizeButton(button, ACTION_BUTTON);
    }

    public static void normalizeToolbarButton(JButton button) {
        normalizeButton(button, TOOLBAR_BUTTON);
    }

    public static void normalizeSchedulerLevelButton(JButton button) {
        normalizeButton(button, SCHEDULER_LEVEL_BUTTON);
    }

    public static void normalizeWideButton(JButton button) {
        normalizeButton(button, WIDE_MENU_BUTTON);
    }

    
    public static JPanel toolbarButtonGrid(int columns, JButton... buttons) {
        JPanel panel = new JPanel(new GridLayout(2, columns, BUTTON_GAP, BUTTON_GAP));
        panel.setOpaque(false);
        for (JButton button : buttons) {
            normalizeToolbarButton(button);
            panel.add(button);
        }
        return panel;
    }

    private static void normalizeButton(JButton button, Dimension size) {
        if (button == null) {
            return;
        }
        button.setPreferredSize(size);
        button.setMinimumSize(new Dimension(size.width, size.height));
        button.setMaximumSize(new Dimension(Math.max(size.width + 24, 280), size.height + 14));
    }

    public static void normalizePrimaryButtons(JButton... buttons) {
        for (JButton b : buttons) {
            normalizeMenuButton(b);
        }
    }

    public static JPanel cardPanel(JComponent content, String title) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        KernelTheme.styleCard(card);
        if (title != null && !title.isBlank()) {
            JLabel lbl = new JLabel(title);
            lbl.setFont(KernelTheme.headingFont());
            lbl.setForeground(KernelTheme.TEXT);
            card.add(lbl, BorderLayout.NORTH);
        }
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    public static void spacer(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        panel.add(new JPanel(), gbc);
        gbc.weighty = 0;
    }
}
