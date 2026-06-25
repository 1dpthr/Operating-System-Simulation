import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PHH1 extends javax.swing.JFrame {

    public PHH1() {
        initComponents();
        polishMainMenu();
        KernelTheme.applyToWindow(this);
        wireMainMenuButtons();
        jButton6.setToolTipText("Kernel settings — page size, max processes");
        for (JButton btn : new JButton[]{jButton1, jButton2, jButton3, jButton4, jButton6, jButton5}) {
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
        }
    }

    private void wireMainMenuButtons() {
        ButtonWiring.bind(jButton1, () -> ButtonWiring.openScreen(this, PHH2::new));
        ButtonWiring.bind(jButton2, () -> ButtonWiring.openScreen(this, Memoryyyy::new));
        ButtonWiring.bind(jButton3, () -> ButtonWiring.openScreen(this, interrupt::new));
        ButtonWiring.bind(jButton4, () -> ButtonWiring.openScreen(this, OtherOperationsGUI::new));
        ButtonWiring.bind(jButton5, () -> System.exit(0));
        ButtonWiring.bind(jButton6, () -> ButtonWiring.openScreen(this, ConfigurationGUI::new));
    }

    private void openIoManagement() {
        interrupt gui = new interrupt();
        gui.setLocationRelativeTo(this);
        gui.setVisible(true);
        gui.toFront();
        gui.requestFocus();
    }

    private void rebind(JButton button, java.awt.event.ActionListener action) {
        for (java.awt.event.ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }
        button.addActionListener(action);
        button.setEnabled(true);
    }

    private void polishMainMenu() {
        setTitle(KernelTheme.OS_FULL_NAME);
        getContentPane().setBackground(KernelTheme.BG);

        jLabel1.setFont(KernelTheme.titleFont());
        jLabel1.setForeground(KernelTheme.TEXT);
        jLabel1.setText(KernelTheme.OS_FULL_NAME);

        jLabel2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jLabel2.setForeground(KernelTheme.TEXT_MUTED);
        jLabel2.setText("Lightweight Operating System Simulation");

        KernelTheme.stylePrimaryButton(jButton1, "Process Management");
        KernelTheme.stylePrimaryButton(jButton2, "Memory Management");
        KernelTheme.stylePrimaryButton(jButton3, "I/O Management");
        KernelTheme.stylePrimaryButton(jButton4, "Other Operations");
        KernelTheme.styleSecondaryButton(jButton6, "Configuration");
        KernelTheme.styleAccentButton(jButton5, "Exit");

        for (JButton btn : new JButton[]{jButton1, jButton2, jButton3, jButton4, jButton6, jButton5}) {
            UiLayout.normalizeMenuButton(btn);
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        jPanel1 = new GradientPanel();
        jLabel1 = new JLabel("", JLabel.CENTER);
        jLabel2 = new JLabel("", JLabel.CENTER);
        jButton1 = new JButton();
        jButton2 = new JButton();
        jButton3 = new JButton();
        jButton4 = new JButton();
        jButton5 = new JButton();
        jButton6 = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(KernelTheme.BG);

        jButton1.addActionListener(evt -> jButton1ActionPerformed(evt));
        jButton2.addActionListener(evt -> jButton2ActionPerformed(evt));
        jButton3.addActionListener(evt -> jButton3ActionPerformed(evt));
        jButton4.addActionListener(evt -> jButton4ActionPerformed(evt));
        jButton5.addActionListener(evt -> jButton5ActionPerformed(evt));
        jButton6.addActionListener(evt -> jButton6ActionPerformed(evt));

        JPanel buttonPanel = UiLayout.menuButtonColumn(jButton1, jButton2, jButton3, jButton4, jButton6, jButton5);

        jPanel1.setLayout(new BorderLayout(0, 16));
        jPanel1.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 6));
        titleBlock.setOpaque(false);
        titleBlock.add(jLabel1);
        titleBlock.add(jLabel2);
        jPanel1.add(titleBlock, BorderLayout.NORTH);
        jPanel1.add(buttonPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        UiLayout.applyCompactWindow(this, 360, 580, 320, 480);
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        new PHH2().setVisible(true);
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        new Memoryyyy().setVisible(true);
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        openIoManagement();
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        new OtherOperationsGUI().setVisible(true);
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        new ConfigurationGUI().setVisible(true);
    }

    public static void main(String[] args) {
        KernelTheme.init();
        ProcessRegistry.getInstance();
        java.awt.EventQueue.invokeLater(() -> new PHH1().setVisible(true));
    }

    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;

    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, KernelTheme.BG, 0, getHeight(), KernelTheme.BG_GRADIENT_END);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
