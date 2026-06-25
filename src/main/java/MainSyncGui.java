
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class MainSyncGui extends javax.swing.JFrame {

    private final Color PRIMARY_COLOR = KernelTheme.PRIMARY;
    private final Color SECONDARY_COLOR = KernelTheme.PRIMARY_HOVER;
    private final Color SUCCESS_COLOR = KernelTheme.SUCCESS;
    private final Color RUNNING_COLOR = KernelTheme.ACCENT;
    private final Color BACKGROUND_COLOR = KernelTheme.BG;
    private final Color CARD_COLOR = KernelTheme.CARD;
    private final Color TEXT_COLOR = KernelTheme.TEXT;

    StringBuilder readyProcess;
    String runningProcess = "Empty";
    StringBuilder finishedProcess;
    private int processCount = 0;

    public Queue<String> readyQueue = new ConcurrentLinkedQueue<>();
    public Queue<String> finishedQueue = new ConcurrentLinkedQueue<>();

    public MainSyncGui() {
        initComponents();
        readyProcess = new StringBuilder();
        finishedProcess = new StringBuilder();
        fixLayout();
        wireSyncButtons();
        KernelTheme.applyToWindow(this);
        polishLayout();
        setTitle(KernelTheme.OS_NAME + " — Process Synchronization");
    }

    private void wireSyncButtons() {
        ButtonWiring.bind(submit, () -> submitActionPerformed(
                new java.awt.event.ActionEvent(submit, java.awt.event.ActionEvent.ACTION_PERFORMED, "wire")));
    }

    private void fixLayout() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);

        jLabel1.setText("Process Synchronization");
        jLabel1.setFont(KernelTheme.headingFont());
        jLabel1.setForeground(TEXT_COLOR);
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setBorder(new EmptyBorder(20, 20, 8, 20));
        getContentPane().add(jLabel1, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(16, 16, 16, 16);
        gbc.anchor = GridBagConstraints.NORTH;
        body.add(buildInputCard(), gbc);

        gbc.gridx = 1;
        body.add(buildStateCard(), gbc);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(body, gbc);

        getContentPane().add(wrapper, BorderLayout.CENTER);
        UiLayout.applyWorkspaceWindow(this, 660, 440, 600, 380);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        NavigationHelper.addBackBar(this, () -> NavigationHelper.back(this));
    }

    private JPanel buildInputCard() {
        JPanel card = UiLayout.formCard();

        GridBagConstraints gbc = UiLayout.cardGbc(0);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JLabel heading = new JLabel("Process Setup", SwingConstants.CENTER);
        heading.setFont(KernelTheme.headingFont());
        KernelTheme.styleLabel(heading);
        card.add(heading, gbc);

        gbc = UiLayout.cardGbc(1);
        gbc.gridwidth = 2;
        jLabel8.setText("Number of Processes (1–19):");
        KernelTheme.styleLabel(jLabel8);
        card.add(jLabel8, gbc);

        gbc = UiLayout.cardGbc(2);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        noOfProcess.setColumns(8);
        noOfProcess.setPreferredSize(UiLayout.FORM_FIELD);
        noOfProcess.setMinimumSize(UiLayout.FORM_FIELD);
        noOfProcess.setMaximumSize(UiLayout.FORM_FIELD);
        KernelTheme.styleTextField(noOfProcess);
        card.add(noOfProcess, gbc);

        gbc = UiLayout.cardGbc(3);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(18, 12, 4, 12);
        card.add(submit, gbc);

        return card;
    }

    private JPanel buildStateCard() {
        JPanel card = UiLayout.formCard();

        GridBagConstraints gbc = UiLayout.cardGbc(0);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JLabel heading = new JLabel("Process States", SwingConstants.CENTER);
        heading.setFont(KernelTheme.headingFont());
        KernelTheme.styleLabel(heading);
        card.add(heading, gbc);

        addStateLane(card, 1, jLabel10, "Ready", SUCCESS_COLOR, ready);
        addStateLane(card, 2, jLabel12, "Running", RUNNING_COLOR, running);
        addStateLane(card, 3, jLabel14, "Finished", KernelTheme.TEXT_MUTED, finished);

        return card;
    }

    private void addStateLane(JPanel card, int row, JLabel titleLabel, String title,
            Color titleColor, JLabel stateBox) {
        GridBagConstraints gbc = UiLayout.cardGbc(row * 2 - 1);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        titleLabel.setText(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(titleColor);
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(titleLabel, gbc);

        gbc = UiLayout.cardGbc(row * 2);
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 12, row < 3 ? 14 : 4, 12);
        styleStateBox(stateBox);
        card.add(stateBox, gbc);
    }

    private void styleStateBox(JLabel box) {
        box.setHorizontalAlignment(SwingConstants.CENTER);
        box.setVerticalAlignment(SwingConstants.CENTER);
        box.setFont(KernelTheme.bodyFont());
        box.setForeground(TEXT_COLOR);
        box.setBackground(CARD_COLOR);
        box.setOpaque(true);
        Dimension lane = new Dimension(260, 40);
        box.setPreferredSize(lane);
        box.setMinimumSize(lane);
        box.setMaximumSize(lane);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(KernelTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
    }

    private void polishLayout() {
        KernelTheme.stylePrimaryButton(submit, "Start Simulation");
        UiLayout.normalizeActionButton(submit);
        styleStateBox(ready);
        styleStateBox(running);
        styleStateBox(finished);
        jLabel10.setForeground(SUCCESS_COLOR);
        jLabel12.setForeground(RUNNING_COLOR);
        jLabel14.setForeground(KernelTheme.TEXT_MUTED);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        noOfProcess = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        submit = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        ready = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        running = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        finished = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new Color(245, 245, 250));
        jPanel1.setForeground(new Color(40, 44, 52));

        jLabel1.setFont(new java.awt.Font("Bookman Old Style", 1, 30));
        jLabel1.setForeground(new Color(40, 44, 52));
        jLabel1.setText("Synchronization");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel2.setForeground(new Color(40, 44, 52));

        noOfProcess.addActionListener(evt -> noOfProcessActionPerformed(evt));

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 14));
        jLabel8.setForeground(new Color(40, 44, 52));
        jLabel8.setText("Enter Number of Processes");

        submit.setBackground(new Color(245, 245, 250));
        submit.setFont(new java.awt.Font("Times New Roman", 0, 12));
        submit.setForeground(new Color(40, 44, 52));
        submit.setText("Submit");
        submit.addActionListener(evt -> submitActionPerformed(evt));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(noOfProcess, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addGap(63, 63, 63)
                                                .addComponent(submit)))
                                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noOfProcess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(submit)
                                .addContainerGap(15, Short.MAX_VALUE))
        );

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12));
        jLabel10.setText("Ready");

        ready.setFont(new java.awt.Font("Segoe UI", 1, 16));
        ready.setText("Empty");
        ready.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        ready.setPreferredSize(new java.awt.Dimension(33, 24));

        jLabel12.setFont(new java.awt.Font("Times New Roman", 1, 24));
        jLabel12.setForeground(new Color(40, 44, 52));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Running");

        running.setBackground(new java.awt.Color(255, 255, 255));
        running.setFont(new java.awt.Font("Segoe UI", 1, 12));
        running.setForeground(new java.awt.Color(255, 51, 51));
        running.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        running.setText("Empty");
        running.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        running.setPreferredSize(new java.awt.Dimension(33, 24));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12));
        jLabel14.setText("Finished");

        finished.setFont(new java.awt.Font("Segoe UI", 1, 16));
        finished.setText("Empty");
        finished.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(ready, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(finished, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addContainerGap(143, Short.MAX_VALUE)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(running, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel12))
                                .addGap(141, 141, 141))
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ready, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(47, 47, 47)
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(running, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(finished, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(64, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(247, 247, 247)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                                .addGap(156, 156, 156))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(35, 35, 35)
                                                .addComponent(jLabel2))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(25, 25, 25)
                                                .addComponent(jLabel1)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(58, 58, 58)
                                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(50, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void noOfProcessActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void submitActionPerformed(java.awt.event.ActionEvent evt) {

        try {
            processCount = Integer.parseInt(noOfProcess.getText());
        } catch (NumberFormatException e) {
            return;
        }

        if (processCount <= 20 && processCount > 0) {

            readyQueue.clear();
            finishedQueue.clear();
            runningProcess = "Empty";

            final ExecutorService exService = Executors.newFixedThreadPool(processCount);
            final Printer printer = new Printer();

            for (int i = 1; i <= processCount; i++) {
                exService.execute(new Job(printer, "" + i, this));
            }

            exService.shutdown();

        } else {
            return;
        }
    }

    public void updateGui() {

        SwingUtilities.invokeLater(() -> {

            readyProcess.setLength(0);
            finishedProcess.setLength(0);

            for (String p : readyQueue) {
                readyProcess.append("P").append(p).append("|");
            }

            for (String p : finishedQueue) {
                finishedProcess.append("P").append(p).append("|");
            }

            if (processCount == finishedQueue.size()) {
                runningProcess = "Empty";
            }

            ready.setText(readyProcess.length() == 0 ? "Empty" : readyProcess.toString());
            running.setText(runningProcess);
            finished.setText(finishedProcess.length() == 0 ? "Empty" : finishedProcess.toString());
        });
    }

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> new MainSyncGui().setVisible(true));
    }

    private javax.swing.JLabel finished;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField noOfProcess;
    private javax.swing.JLabel ready;
    private javax.swing.JLabel running;
    private javax.swing.JButton submit;
}
