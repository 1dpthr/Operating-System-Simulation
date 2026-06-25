import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class OtherOperationsGUI extends JFrame {

    public OtherOperationsGUI() {
        super(KernelTheme.OS_NAME + " — Other Operations");
        buildUi();
        KernelTheme.applyToWindow(this);
        wireButtons();
        UiLayout.applyCompactWindow(this, 320, 440, 300, 380);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JButton syncBtn;
    private JButton ipcBtn;
    private JButton configBtn;

    private void buildUi() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(KernelTheme.BG);

        JLabel title = new JLabel("Other Operations", SwingConstants.CENTER);
        KernelTheme.styleTitle(title, "Other Operations");
        title.setBorder(new EmptyBorder(16, 16, 0, 16));

        syncBtn = new JButton();
        KernelTheme.stylePrimaryButton(syncBtn, "Synchronization (Semaphores)");
        ipcBtn = new JButton();
        KernelTheme.stylePrimaryButton(ipcBtn, "Process Communication");
        configBtn = new JButton();
        KernelTheme.styleSecondaryButton(configBtn, "Configuration");
        JButton backBtn = NavigationHelper.createBackButton(this, () -> NavigationHelper.backToMain(this));

        JPanel screen = new JPanel(new BorderLayout(0, 12));
        screen.setOpaque(false);
        screen.add(title, BorderLayout.NORTH);
        screen.add(UiLayout.menuButtonColumn(syncBtn, ipcBtn, configBtn, backBtn), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(screen, gbc);

        getContentPane().add(wrapper, BorderLayout.CENTER);
    }

    private void wireButtons() {
        ButtonWiring.bind(syncBtn, () -> ButtonWiring.openScreen(this, MainSyncGui::new));
        ButtonWiring.bind(ipcBtn, () -> ProcessCommunicationFxApp.launchWindow());
        ButtonWiring.bind(configBtn, () -> ButtonWiring.openScreen(this, ConfigurationGUI::new));
    }
}
