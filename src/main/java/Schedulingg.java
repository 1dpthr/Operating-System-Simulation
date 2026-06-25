import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class Schedulingg extends javax.swing.JFrame {
     private List<String[]> processData;
     private JTabbedPane schedulingTabs;
     private JComboBox<String> hlsProcessCombo;
     private JLabel hlsProcessInfoLabel;
     private JComboBox<String> mlsProcessCombo;
     private JComboBox<String> llsAlgorithmCombo;
     private JLabel jobQueueLabel;
     private JLabel readyQueueLabel;
     private JLabel suspendQueueLabel;
     private JLabel runningQueueLabel;
     private JTextArea summaryArea;
     private int jobCounter;
     private JPanel simulationQuantumPanel;
     private final SchedulingLiveSimulator liveSimulator = new SchedulingLiveSimulator();
     private JButton liveDemoBtn;
     private static final String[] LLS_ALGORITHMS = {
         "FCFS",
         "SJF(PREEMPTIVE)",
         "SJF(NON_PREEMPTIVE)",
         "PRIORITY(PREEMPTIVE)",
         "PRIORITY(NON_PREEMPTIVE)",
         "ROUND ROBIN"
     };
     
    private void wireSchedulingButtons() {
        rebindButton(jButton1, e -> jButton1ActionPerformed(e));
        rebindButton(jButton2, e -> jButton2ActionPerformed(e));
        rebindButton(jButton3, e -> jButton3ActionPerformed(e));
    }

    private void rebindButton(JButton button, java.awt.event.ActionListener action) {
        for (java.awt.event.ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
        button.addActionListener(action);
        button.setEnabled(true);
    }

    private void fixSchedulingMainPanel() {
        jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout(12, 12));
        jPanel1.setBackground(KernelTheme.BG_PANEL);
        jPanel1.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        jLabel1.setText("Algorithm Simulation (Gantt + Waiting Time)");
        jLabel1.setFont(KernelTheme.headingFont());
        jLabel1.setForeground(KernelTheme.TEXT);
        jPanel1.add(jLabel1, BorderLayout.NORTH);

        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        GridBagConstraints gbc = UiLayout.formGbc(0, 0, 0);
        UiLayout.addFormRow(topBar, gbc, 0, "Algorithm:", jComboBox1);
        KernelTheme.styleComboBox(jComboBox1);
        while (jComboBox1.getItemCount() > 0 && " ".equals(String.valueOf(jComboBox1.getItemAt(jComboBox1.getItemCount() - 1)).trim())) {
            jComboBox1.removeItemAt(jComboBox1.getItemCount() - 1);
        }

        simulationQuantumPanel = new JPanel(new GridBagLayout());
        simulationQuantumPanel.setOpaque(false);
        GridBagConstraints qGbc = UiLayout.formGbc(0, 0, 0);
        UiLayout.addFormRow(simulationQuantumPanel, qGbc, 0, "Time Quantum:", jTextField4);
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topBar.add(simulationQuantumPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 8, 4, 8);
        KernelTheme.stylePrimaryButton(jButton1, "Run");
        KernelTheme.styleSecondaryButton(jButton2, "Clear");
        KernelTheme.styleSecondaryButton(jButton3, "Back");
        topBar.add(schedulingButtonRow(jButton1, jButton2, jButton3), gbc);

        jComboBox1.addActionListener(e -> updateQuantumVisibility());
        updateQuantumVisibility();

        JPanel stats = new JPanel(new GridBagLayout());
        stats.setOpaque(false);
        stats.setBorder(BorderFactory.createTitledBorder("Averages"));
        jLabel2.setText("Avg Completion:");
        jLabel4.setText("Avg Waiting:");
        jLabel5.setText("Avg Turnaround:");
        KernelTheme.styleLabel(jLabel2);
        KernelTheme.styleLabel(jLabel4);
        KernelTheme.styleLabel(jLabel5);

        addStatsRow(stats, 0, jLabel2, jTextField1);
        addStatsRow(stats, 1, jLabel4, jTextField2);
        addStatsRow(stats, 2, jLabel5, jTextField3);

        for (JTextField tf : new JTextField[]{jTextField1, jTextField2, jTextField3, jTextField4}) {
            tf.setEditable(false);
            KernelTheme.styleTextField(tf);
            UiLayout.applyFormFieldSize(tf, UiLayout.FORM_FIELD);
        }
        jTextField4.setEditable(true);
        UiLayout.applyFormFieldSize(jTextField4, UiLayout.FORM_FIELD);
        jTextField4.setText(String.valueOf(KernelConfig.getInstance().getTimeQuantum()));

        JPanel west = new JPanel(new BorderLayout(0, 12));
        west.setOpaque(false);
        JPanel westContent = new JPanel();
        westContent.setLayout(new javax.swing.BoxLayout(westContent, javax.swing.BoxLayout.Y_AXIS));
        westContent.setOpaque(false);
        westContent.add(topBar);
        westContent.add(javax.swing.Box.createVerticalStrut(8));
        westContent.add(stats);
        west.add(westContent, BorderLayout.NORTH);
        int westWidth = Math.min(330, Math.max(280, UiLayout.screenWidth() / 3));
        west.setPreferredSize(new Dimension(westWidth, 0));
        west.setMinimumSize(new Dimension(westWidth, 0));

        KernelTheme.styleTable(jTable1);
        configureSchedulingTable();

        JPanel tableHost = new JPanel();
        tableHost.setLayout(new javax.swing.BoxLayout(tableHost, javax.swing.BoxLayout.X_AXIS));
        tableHost.setOpaque(false);
        tableHost.add(jScrollPane1);
        tableHost.add(javax.swing.Box.createHorizontalGlue());

        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);
        center.add(west, BorderLayout.WEST);
        center.add(tableHost, BorderLayout.CENTER);
        jPanel1.add(center, BorderLayout.CENTER);

        jLabel7.setText("Timeline:");
        jLabel7.setFont(KernelTheme.bodyFont());
        jLabel7.setForeground(KernelTheme.TEXT);
        KernelTheme.styleTextField(jTextField5);
        UiLayout.applyFormFieldSize(jTextField5, UiLayout.FORM_FIELD_WIDE);
        JPanel gantt = new JPanel(new BorderLayout(8, 0));
        gantt.setOpaque(false);
        gantt.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));
        gantt.add(jLabel7, BorderLayout.WEST);
        gantt.add(jTextField5, BorderLayout.CENTER);

        summaryArea = new JTextArea(10, 50);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setText("Run a scheduling algorithm to view the detailed summary here.");
        KernelTheme.styleTextArea(summaryArea);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(KernelTheme.BG_PANEL);
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(KernelTheme.BORDER, 1),
                "Detailed Summary",
                0, 0,
                KernelTheme.bodyFont(),
                KernelTheme.TEXT));
        summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        JSplitPane southSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gantt, summaryPanel);
        southSplit.setResizeWeight(0.22);
        southSplit.setDividerLocation(52);
        southSplit.setOpaque(false);
        southSplit.setBorder(null);

        jPanel1.add(southSplit, BorderLayout.SOUTH);
    }

    private void updateQuantumVisibility() {
        if (simulationQuantumPanel == null || jComboBox1 == null) {
            return;
        }
        String algo = (String) jComboBox1.getSelectedItem();
        simulationQuantumPanel.setVisible("ROUND ROBIN".equals(algo));
        simulationQuantumPanel.getParent().revalidate();
        simulationQuantumPanel.getParent().repaint();
    }

    private void setupSchedulerPanel() {
        schedulingTabs = new JTabbedPane();
        schedulingTabs.setFont(KernelTheme.bodyFont());
        schedulingTabs.addTab("Queue Management", buildSchedulingLevelsPanel());
        schedulingTabs.addTab("Algorithm Simulation", jPanel1);
        schedulingTabs.addChangeListener(e -> {
            if (schedulingTabs.getSelectedIndex() == 1) {
                reloadSimulationTableFromRegistry();
            }
        });

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(KernelTheme.BG);
        getContentPane().add(schedulingTabs, BorderLayout.CENTER);
        UiLayout.applyWorkspaceWindow(this, 960, 680, 800, 560);
        refreshQueueDisplay();
    }

    private JPanel buildSchedulingLevelsPanel() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(KernelTheme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel columns = new JPanel(new GridLayout(1, 3, 12, 0));
        columns.setOpaque(false);
        columns.add(buildHlsPanel());
        columns.add(buildMlsPanel());
        columns.add(buildLlsPanel());
        root.add(columns, BorderLayout.NORTH);

        JPanel queueState = new JPanel(new GridLayout(2, 2, 10, 8));
        queueState.setOpaque(false);
        queueState.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(KernelTheme.BORDER),
                "Queue State",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                KernelTheme.headingFont(),
                KernelTheme.TEXT));

        jobQueueLabel = queueLineLabel("(empty)");
        readyQueueLabel = queueLineLabel("(empty)");
        suspendQueueLabel = queueLineLabel("(empty)");
        runningQueueLabel = queueLineLabel("(empty)");

        queueState.add(wrapQueueCard("Job Queue", new Color(180, 150, 60), jobQueueLabel));
        queueState.add(wrapQueueCard("Ready Queue", KernelTheme.SUCCESS, readyQueueLabel));
        queueState.add(wrapQueueCard("Suspend Queue", new Color(130, 90, 160), suspendQueueLabel));
        queueState.add(wrapQueueCard("Running (CPU)", new Color(70, 120, 190), runningQueueLabel));

        root.add(queueState, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildHlsPanel() {
        JPanel card = UiLayout.formCard();
        addBoldLevelTitle(card, 0, "High-Level (Job Admission)");
        addLevelHint(card, 1, "Select process from Process Management → Job Queue → Ready");

        hlsProcessCombo = ProcessPicker.createCombo(false);
        hlsProcessInfoLabel = new JLabel(" ", SwingConstants.CENTER);
        hlsProcessInfoLabel.setFont(KernelTheme.smallFont());
        hlsProcessInfoLabel.setForeground(KernelTheme.TEXT_MUTED);
        hlsProcessCombo.addActionListener(e -> updateHlsProcessInfo());

        UiLayout.addStackedFormRow(card, 2, "Select Process:", hlsProcessCombo);
        GridBagConstraints infoGbc = UiLayout.cardGbc(3);
        infoGbc.gridwidth = 2;
        card.add(hlsProcessInfoLabel, infoGbc);

        JButton createJobBtn = new JButton();
        KernelTheme.stylePrimaryButton(createJobBtn, "Add Selected Process to Job Queue");
        UiLayout.normalizeSchedulerLevelButton(createJobBtn);
        createJobBtn.addActionListener(e -> createJobForQueue());

        JButton admitBtn = new JButton();
        KernelTheme.styleSecondaryButton(admitBtn, "Admit Job to Ready Queue");
        UiLayout.normalizeSchedulerLevelButton(admitBtn);
        admitBtn.addActionListener(e -> {
            if (MultiLevelScheduler.admitNextJob()) {
                refreshQueueDisplay();
                reloadSimulationTableFromRegistry();
            }
        });

        GridBagConstraints gbc = UiLayout.cardGbc(4);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 10, 4, 10);
        card.add(createJobBtn, gbc);
        gbc = UiLayout.cardGbc(5);
        card.add(admitBtn, gbc);
        return card;
    }

    private void updateHlsProcessInfo() {
        if (hlsProcessInfoLabel == null) {
            return;
        }
        ProcessPicker.getSelectedPid(hlsProcessCombo).flatMap(
                pid -> ProcessRegistry.getInstance().find(pid)).ifPresentOrElse(
                pcb -> hlsProcessInfoLabel.setText(String.format(
                        "Arrival: %d | Burst: %d | Size: %d KB | Priority: %d",
                        pcb.getArrivalTime(), pcb.getBurstTime(),
                        pcb.getMemoryRequirementKb(), pcb.getPriority())),
                () -> hlsProcessInfoLabel.setText(" "));
    }

    private JPanel buildMlsPanel() {
        JPanel card = UiLayout.formCard();
        addBoldLevelTitle(card, 0, "Medium-Level (Memory)");
        addLevelHint(card, 1, "Suspend / Resume process");

        mlsProcessCombo = ProcessPicker.createCombo(false);
        UiLayout.addStackedFormRow(card, 2, "Select Process:", mlsProcessCombo);

        JButton suspendBtn = new JButton();
        KernelTheme.styleAccentButton(suspendBtn, "Suspend Selected Process");
        UiLayout.normalizeSchedulerLevelButton(suspendBtn);
        suspendBtn.addActionListener(e -> runMlsAction(true));

        JButton resumeBtn = new JButton();
        KernelTheme.stylePrimaryButton(resumeBtn, "Resume Selected Process");
        UiLayout.normalizeSchedulerLevelButton(resumeBtn);
        resumeBtn.addActionListener(e -> runMlsAction(false));

        GridBagConstraints gbc = UiLayout.cardGbc(3);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 4, 10);
        card.add(suspendBtn, gbc);
        gbc = UiLayout.cardGbc(4);
        gbc.insets = new Insets(4, 10, 4, 10);
        card.add(resumeBtn, gbc);
        return card;
    }

    private void runMlsAction(boolean suspend) {
        ProcessPicker.getSelectedPid(mlsProcessCombo).ifPresentOrElse(pid -> {
            boolean ok = suspend
                    ? MultiLevelScheduler.suspendProcess(pid)
                    : MultiLevelScheduler.resumeProcess(pid);
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        suspend ? "Process READY/RUNNING honi chahiye suspend ke liye."
                                : "Process SUSPENDED honi chahiye resume ke liye.",
                        "Invalid State", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshQueueDisplay();
        }, () -> JOptionPane.showMessageDialog(this, "Please select a process.",
                "Select Process", JOptionPane.WARNING_MESSAGE));
    }

    private JPanel buildLlsPanel() {
        JPanel card = UiLayout.formCard();
        addBoldLevelTitle(card, 0, "Low-Level (CPU Dispatch)");
        addLevelHint(card, 1, "Dispatch one ready process to CPU per click");

        llsAlgorithmCombo = new JComboBox<>(LLS_ALGORITHMS);
        KernelTheme.styleComboBox(llsAlgorithmCombo);
        UiLayout.addStackedFormRow(card, 2, "Dispatch Order:", llsAlgorithmCombo);

        JButton dispatchBtn = new JButton();
        KernelTheme.stylePrimaryButton(dispatchBtn, "Dispatch One Process to CPU");
        UiLayout.normalizeSchedulerLevelButton(dispatchBtn);
        dispatchBtn.addActionListener(e -> {
            String algo = (String) llsAlgorithmCombo.getSelectedItem();
            if (algo == null) {
                return;
            }
            if (!MultiLevelScheduler.dispatchNext(algo)) {
                JOptionPane.showMessageDialog(this,
                        "Ready queue is empty. Admit a job first.",
                        "Dispatch", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshQueueDisplay();
        });

        liveDemoBtn = new JButton();
        updateLiveDemoButtonLabel();
        KernelTheme.styleSecondaryButton(liveDemoBtn, liveDemoBtn.getText());
        UiLayout.normalizeSchedulerLevelButton(liveDemoBtn);
        llsAlgorithmCombo.addActionListener(e -> updateLiveDemoButtonLabel());
        liveDemoBtn.addActionListener(e -> startLiveDemo());

        GridBagConstraints gbc = UiLayout.cardGbc(3);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 4, 10);
        card.add(dispatchBtn, gbc);
        gbc = UiLayout.cardGbc(4);
        gbc.insets = new Insets(4, 10, 4, 10);
        card.add(liveDemoBtn, gbc);
        return card;
    }

    private void updateLiveDemoButtonLabel() {
        if (liveDemoBtn == null || llsAlgorithmCombo == null) {
            return;
        }
        String algo = (String) llsAlgorithmCombo.getSelectedItem();
        String label = "Auto-Run " + (algo == null ? "FCFS" : algo) + " Live Demo";
        liveDemoBtn.setText(label);
    }

    private JTextField levelField(String value) {
        JTextField field = new JTextField();
        field.setText(value);
        KernelTheme.styleTextField(field);
        UiLayout.applyFormFieldSize(field, UiLayout.FORM_FIELD);
        return field;
    }

    private void addBoldLevelTitle(JPanel card, int row, String text) {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(KernelTheme.headingFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(KernelTheme.TEXT);
        GridBagConstraints gbc = UiLayout.cardGbc(row);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(2, 10, 6, 10);
        card.add(title, gbc);
    }

    private void addLevelHint(JPanel card, int row, String text) {
        JLabel hint = new JLabel(text, SwingConstants.CENTER);
        hint.setFont(KernelTheme.smallFont());
        hint.setForeground(KernelTheme.TEXT_MUTED);
        GridBagConstraints gbc = UiLayout.cardGbc(row);
        gbc.gridwidth = 2;
        card.add(hint, gbc);
    }

    private JLabel queueLineLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(KernelTheme.bodyFont());
        label.setForeground(KernelTheme.TEXT);
        return label;
    }

    private JPanel wrapQueueCard(String title, Color accent, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setOpaque(true);
        panel.setBackground(KernelTheme.CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(KernelTheme.bodyFont());
        titleLabel.setForeground(accent);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private void createJobForQueue() {
        ProcessPicker.getSelectedPid(hlsProcessCombo).ifPresentOrElse(pid -> {
            if (MultiLevelScheduler.enqueueFromRegistry(pid)) {
                refreshQueueDisplay();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Process P" + pid + " is already in job queue or not found.",
                        "Job Queue", JOptionPane.INFORMATION_MESSAGE);
            }
        }, () -> JOptionPane.showMessageDialog(this, "Please select a process.",
                "Select Process", JOptionPane.WARNING_MESSAGE));
    }

    private List<String[]> buildProcessDataFromRegistry() {
        List<String[]> rows = new ArrayList<>();
        for (ProcessControlBlock pcb : ProcessRegistry.getInstance().getAll()) {
            rows.add(new String[]{
                String.valueOf(pcb.getProcessId()),
                pcb.getProcessName(),
                String.valueOf(pcb.getArrivalTime()),
                String.valueOf(pcb.getBurstTime()),
                String.valueOf(pcb.getPriority())
            });
        }
        if (rows.isEmpty()) {
            rows = readProcessDataFromTable();
        }
        return rows;
    }

    private List<String[]> readProcessDataFromTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object pid = model.getValueAt(i, 0);
            Object name = model.getValueAt(i, 1);
            Object arrival = model.getValueAt(i, 2);
            Object burst = model.getValueAt(i, 3);
            if (arrival == null || burst == null
                    || arrival.toString().isBlank() || burst.toString().isBlank()) {
                continue;
            }
            rows.add(new String[]{
                pid == null ? String.valueOf(i + 1) : pid.toString(),
                name == null ? "P" + (i + 1) : name.toString(),
                arrival.toString(),
                burst.toString(),
                "2"
            });
        }
        return rows;
    }

    private void reloadSimulationTableFromRegistry() {
        processData = buildProcessDataFromRegistry();
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        tableModel.setRowCount(0);
        for (String[] row : processData) {
            tableModel.addRow(new Object[]{
                row[0], row[1], row[2], row[3], "", "", ""
            });
        }
    }

    private void configureSchedulingTable() {
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        tableModel.setColumnIdentifiers(new String[]{
            "PID", "Name", "Arrival", "Burst", "Completion", "Waiting", "Turnaround"
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        int[] widths = {52, 68, 58, 52, 88, 62, 92};
        int tableWidth = 18;
        for (int i = 0; i < widths.length && i < jTable1.getColumnCount(); i++) {
            jTable1.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            tableWidth += widths[i];
        }
        Dimension tableSize = new Dimension(tableWidth, 240);
        jScrollPane1.setPreferredSize(tableSize);
        jScrollPane1.setMinimumSize(tableSize);
        jScrollPane1.setMaximumSize(tableSize);
        jScrollPane1.setBorder(BorderFactory.createTitledBorder("Scheduling Results"));
    }

    private JPanel schedulingButtonRow(JButton... buttons) {
        Dimension[] sizes = {
            new Dimension(76, 30),
            new Dimension(80, 30),
            new Dimension(118, 30)
        };
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, UiLayout.BUTTON_GAP, 0));
        row.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            Dimension size = sizes[Math.min(i, sizes.length - 1)];
            buttons[i].setPreferredSize(size);
            buttons[i].setMinimumSize(new Dimension(size.width, size.height));
            buttons[i].setMaximumSize(new Dimension(size.width + 4, Integer.MAX_VALUE));
            row.add(buttons[i]);
        }
        return row;
    }

    private void addStatsRow(JPanel panel, int row, JLabel label, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
        gbc.weightx = 0;
    }

    private void refreshQueueDisplay() {
        if (jobQueueLabel == null) {
            return;
        }
        MultiLevelScheduler.QueueState state = MultiLevelScheduler.getQueueState();
        jobQueueLabel.setText(state.jobQueue);
        readyQueueLabel.setText(state.readyQueue);
        suspendQueueLabel.setText(state.suspendQueue);
        runningQueueLabel.setText(state.runningQueue);
    }

    private void startLiveDemo() {
        String algo = (String) llsAlgorithmCombo.getSelectedItem();
        if (algo == null) {
            algo = "FCFS";
        }
        final String selected = algo;
        liveSimulator.start(selected, new SchedulingLiveSimulator.Listener() {
            @Override
            public void onTick(int time, String message) {
                SwingUtilities.invokeLater(() -> refreshQueueDisplay());
            }

            @Override
            public void onFinished(String summary) {
                SwingUtilities.invokeLater(() -> {
                    refreshQueueDisplay();
                    JOptionPane.showMessageDialog(Schedulingg.this, summary,
                            "Live Demo — " + selected, JOptionPane.INFORMATION_MESSAGE);
                });
            }
        });
    }
    
        public String getSelectedAlgorithm() {
        return (String) jComboBox1.getSelectedItem();
    }
    
public Schedulingg(List<String[]> processData) {
    this.processData = processData != null ? processData : buildProcessDataFromRegistry();
    initComponents();
    fixSchedulingMainPanel();
    setupSchedulerPanel();
    KernelTheme.applyToWindow(this);
    wireSchedulingButtons();
    setTitle(KernelTheme.OS_NAME + " — Process Scheduling");
    ProcessPicker.registerRefreshCallback(() -> {
        ProcessPicker.refresh(hlsProcessCombo, false);
        ProcessPicker.refresh(mlsProcessCombo, false);
        reloadSimulationTableFromRegistry();
    });
    reloadSimulationTableFromRegistry();
    refreshQueueDisplay();
}

private void updateGanttChart(List<int[]> ganttChartData) {
    StringBuilder ganttChart = new StringBuilder();
    for (int[] data : ganttChartData) {
        int processId = data[0];
        int startTime = data[1];
        int endTime = data[2];
        ganttChart.append("| P").append(processId)
                  .append(" [").append(startTime).append(" - ").append(endTime).append("] ");
    }
    ganttChart.append("|");
    jTextField5.setText(ganttChart.toString());
}

private void publishSchedulingSummary(String algorithmName, String extraInfo,
        double avgCompletion, double avgWaiting, double avgTurnaround,
        List<int[]> ganttChartData) {
    DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
    StringBuilder sb = new StringBuilder();
    sb.append("=== ").append(algorithmName).append(" - Scheduling Summary ===\n\n");
    sb.append("Total Processes: ").append(tableModel.getRowCount()).append("\n");
    if (extraInfo != null && !extraInfo.isBlank()) {
        sb.append(extraInfo).append("\n");
    }
    sb.append(String.format("Average Completion Time: %.2f%n", avgCompletion));
    sb.append(String.format("Average Waiting Time: %.2f%n", avgWaiting));
    sb.append(String.format("Average Turnaround Time: %.2f%n%n", avgTurnaround));

    sb.append("--- Process Scheduling Details ---\n");
    for (int i = 0; i < tableModel.getRowCount(); i++) {
        sb.append(String.format(
                "Process %s (%s) [Arrival: %s, Burst: %s, Completion: %s, Waiting: %s, Turnaround: %s]%n",
                tableModel.getValueAt(i, 0),
                tableModel.getValueAt(i, 1),
                tableModel.getValueAt(i, 2),
                tableModel.getValueAt(i, 3),
                tableModel.getValueAt(i, 4),
                tableModel.getValueAt(i, 5),
                tableModel.getValueAt(i, 6)));
    }

    sb.append("\n--- Gantt Chart Timeline ---\n");
    if (ganttChartData == null || ganttChartData.isEmpty()) {
        sb.append("Time-sliced execution — see completion times in the table above.\n");
    } else {
        for (int[] data : ganttChartData) {
            sb.append(String.format("P%d [%d - %d]  ", data[0], data[1], data[2]));
        }
        sb.append("\n");
    }

    if (summaryArea != null) {
        summaryArea.setText(sb.toString());
        summaryArea.setCaretPosition(0);
    }
}

private void clearSchedulingSummary() {
    if (summaryArea != null) {
        summaryArea.setText("Run a scheduling algorithm to view the detailed summary here.");
        summaryArea.setCaretPosition(0);
    }
    jTextField5.setText("|");
}

private void performFCFSScheduling() {
    int n = processData.size();
    List<int[]> ganttChartData = new ArrayList<>();
    double avgCompletionTime = 0;
    double avgTurnaroundTime = 0;
    double avgWaitingTime = 0;
    int[] arrivalTime = new int[n];
    int[] burstTime = new int[n];
    int[] waitingTime = new int[n];
    int[] turnaroundTime = new int[n];
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    for (int i = 0; i < n; i++) {
        arrivalTime[i] = Integer.parseInt(processData.get(i)[2]);
        burstTime[i] = Integer.parseInt(processData.get(i)[3]);
    }

    List<int[]> processList = new ArrayList<>();
    for (int i = 0; i < n; i++) {
        processList.add(new int[]{arrivalTime[i], burstTime[i], i});
    }

    Collections.sort(processList, new Comparator<int[]>() {
        public int compare(int[] a, int[] b) {
            return Integer.compare(a[0], b[0]);
        }
    });

    int[] completionTime = new int[n];
    int currentTime = 0;

    for (int i = 0; i < n; ++i) {
        int index = processList.get(i)[2];

        if (processList.get(i)[0] > currentTime) {
            currentTime = processList.get(i)[0];
            i--;
            continue;
        }

        int startTime = currentTime;
        completionTime[index] = currentTime + processList.get(i)[1];
        currentTime = completionTime[index];
        int endTime = currentTime;
        turnaroundTime[index] = completionTime[index] - processList.get(i)[0];
        waitingTime[index] = turnaroundTime[index] - processList.get(i)[1];

        model.addRow(new Object[]{
            processData.get(index)[0],
            processData.get(index)[1],
            processList.get(i)[0],
            processList.get(i)[1],
            completionTime[index],
            waitingTime[index],
            turnaroundTime[index]
        });

        ganttChartData.add(new int[]{
            Integer.parseInt(processData.get(index)[0].toString()),
            startTime,
            endTime
        });
    }

    for (int j = 0; j < n; j++) {
        avgCompletionTime += completionTime[j];
        avgTurnaroundTime += turnaroundTime[j];
        avgWaitingTime += waitingTime[j];
    }
    avgCompletionTime /= n;
    avgTurnaroundTime /= n;
    avgWaitingTime /= n;

    jTextField2.setText(String.valueOf(avgWaitingTime));
    jTextField3.setText(String.valueOf(avgTurnaroundTime));
    jTextField1.setText(String.valueOf(avgCompletionTime));

    updateGanttChart(ganttChartData);
    publishSchedulingSummary("FCFS", null, avgCompletionTime, avgWaitingTime, avgTurnaroundTime, ganttChartData);
    refreshQueueDisplay();
}

private void performSJFNonPreemptiveScheduling() {
    int n = processData.size();
    double avgCompletionTime = 0;
    double avgTurnaroundTime = 0;
    double avgWaitingTime = 0;

    int[] arrivalTime = new int[n];
    int[] burstTime = new int[n];
    int[] completionTime = new int[n];
    int[] waitingTime = new int[n];
    int[] turnaroundTime = new int[n];
    List<int[]> ganttChartData = new ArrayList<>();
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    for (int i = 0; i < n; i++) {
        arrivalTime[i] = Integer.parseInt(processData.get(i)[2]);
        burstTime[i] = Integer.parseInt(processData.get(i)[3]);
    }

    int currentTime = 0;
    int completedProcesses = 0;
    boolean[] isCompleted = new boolean[n];
    
    while (completedProcesses < n) {
        int shortestJobIndex = -1;
        int shortestJobBurst = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            if (arrivalTime[i] <= currentTime && !isCompleted[i] && burstTime[i] < shortestJobBurst) {
                shortestJobIndex = i;
                shortestJobBurst = burstTime[i];
            }
        }

        if (shortestJobIndex == -1) {
            currentTime++;
        } else {
            int index = shortestJobIndex;
            int startTime = currentTime;
            currentTime += burstTime[index];
            completionTime[index] = currentTime;
            turnaroundTime[index] = completionTime[index] - arrivalTime[index];
            waitingTime[index] = turnaroundTime[index] - burstTime[index];
            isCompleted[index] = true;
            completedProcesses++;

            model.addRow(new Object[]{
                processData.get(index)[0],
                processData.get(index)[1],
                arrivalTime[index],
                burstTime[index],
                completionTime[index],
                waitingTime[index],
                turnaroundTime[index]
            });

            ganttChartData.add(new int[]{index + 1, startTime, currentTime});
        }
    }

    for (int j = 0; j < n; j++) {
        avgCompletionTime += completionTime[j];
        avgTurnaroundTime += turnaroundTime[j];
        avgWaitingTime += waitingTime[j];
    }
    avgCompletionTime /= n;
    avgTurnaroundTime /= n;
    avgWaitingTime /= n;

    jTextField2.setText(String.valueOf(avgWaitingTime));
    jTextField3.setText(String.valueOf(avgTurnaroundTime));
    jTextField1.setText(String.valueOf(avgCompletionTime));

    updateGanttChart(ganttChartData);
    publishSchedulingSummary("SJF (Non-Preemptive)", null,
            avgCompletionTime, avgWaitingTime, avgTurnaroundTime, ganttChartData);
    refreshQueueDisplay();
}
private void performSJFPreemptiveScheduling() {
    int n = processData.size();
    double avgCompletionTime = 0;
    double avgTurnaroundTime = 0;
    double avgWaitingTime = 0;

    int[] arrivalTime = new int[n];
    int[] burstTime = new int[n];
    int[] remainingBurstTime = new int[n];
    int[] completionTime = new int[n];
    int[] waitingTime = new int[n];
    int[] turnaroundTime = new int[n];
    List<int[]> ganttChartData = new ArrayList<>();
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    for (int i = 0; i < n; i++) {
        arrivalTime[i] = Integer.parseInt(processData.get(i)[2]);
        burstTime[i] = Integer.parseInt(processData.get(i)[3]);
        remainingBurstTime[i] = burstTime[i];
    }

    int currentTime = 0;
    int completedProcesses = 0;
    int previousProcess = -1;

    while (completedProcesses < n) {
        int shortestJobIndex = -1;
        int shortestJobBurst = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            if (arrivalTime[i] <= currentTime && remainingBurstTime[i] < shortestJobBurst && remainingBurstTime[i] > 0) {
                shortestJobIndex = i;
                shortestJobBurst = remainingBurstTime[i];
            }
        }

        if (shortestJobIndex == -1) {
            currentTime++;
        } else {
            int index = shortestJobIndex;
            int startTime = currentTime;

            remainingBurstTime[index]--;
            currentTime++;

            if (previousProcess != index) {
                ganttChartData.add(new int[]{index + 1, startTime, currentTime});
                previousProcess = index;
            } else {
                ganttChartData.get(ganttChartData.size() - 1)[2] = currentTime;
            }

            if (remainingBurstTime[index] == 0) {
                completedProcesses++;
                completionTime[index] = currentTime;
                turnaroundTime[index] = completionTime[index] - arrivalTime[index];
                waitingTime[index] = turnaroundTime[index] - burstTime[index];

                model.addRow(new Object[]{
                    processData.get(index)[0],
                    processData.get(index)[1],
                    arrivalTime[index],
                    burstTime[index],
                    completionTime[index],
                    waitingTime[index],
                    turnaroundTime[index]
                });
            }
        }
    }

    for (int j = 0; j < n; j++) {
        avgCompletionTime += completionTime[j];
        avgTurnaroundTime += turnaroundTime[j];
        avgWaitingTime += waitingTime[j];
    }
    avgCompletionTime /= n;
    avgTurnaroundTime /= n;
    avgWaitingTime /= n;

    jTextField2.setText(String.valueOf(avgWaitingTime));
    jTextField3.setText(String.valueOf(avgTurnaroundTime));
    jTextField1.setText(String.valueOf(avgCompletionTime));

    updateGanttChart(ganttChartData);
    publishSchedulingSummary("SJF (Preemptive)", null,
            avgCompletionTime, avgWaitingTime, avgTurnaroundTime, ganttChartData);
    refreshQueueDisplay();
}

private int getPriorityValue(int i) {
    int p = 999;
    try {
        p = Integer.parseInt(processData.get(i)[4]);
    } catch (Exception ignored) {
    }
    return p;
}

private void performPriorityNonPreemptiveScheduling() {
    int n = processData.size();

    int[] arrivalTime = new int[n];
    int[] burstTime = new int[n];
    int[] priority = new int[n];

    int[] completionTime = new int[n];
    int[] waitingTime = new int[n];
    int[] turnaroundTime = new int[n];

    List<int[]> ganttChartData = new ArrayList<>();
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    for (int i = 0; i < n; i++) {
        arrivalTime[i] = Integer.parseInt(processData.get(i)[2]);
        burstTime[i] = Integer.parseInt(processData.get(i)[3]);
        priority[i] = getPriorityValue(i);
    }

    int currentTime = 0;
    int completedProcesses = 0;
    boolean[] isCompleted = new boolean[n];

    while (completedProcesses < n) {
        int chosen = -1;

        for (int i = 0; i < n; i++) {
            if (!isCompleted[i] && arrivalTime[i] <= currentTime) {
                if (chosen == -1) {
                    chosen = i;
                } else {
                    if (priority[i] < priority[chosen]) {
                        chosen = i;
                    }
                }
            }
        }

        if (chosen == -1) {
            currentTime++;
            continue;
        }

        int startTime = currentTime;
        currentTime += burstTime[chosen];
        completionTime[chosen] = currentTime;
        turnaroundTime[chosen] = completionTime[chosen] - arrivalTime[chosen];
        waitingTime[chosen] = turnaroundTime[chosen] - burstTime[chosen];
        isCompleted[chosen] = true;
        completedProcesses++;

        model.addRow(new Object[]{
            processData.get(chosen)[0],
            processData.get(chosen)[1],
            arrivalTime[chosen],
            burstTime[chosen],
            completionTime[chosen],
            waitingTime[chosen],
            turnaroundTime[chosen]
        });

        ganttChartData.add(new int[]{chosen + 1, startTime, currentTime});
    }

    double avgCompletionTime = 0;
    double avgTurnaroundTime = 0;
    double avgWaitingTime = 0;

    for (int i = 0; i < n; i++) {
        avgCompletionTime += completionTime[i];
        avgTurnaroundTime += turnaroundTime[i];
        avgWaitingTime += waitingTime[i];
    }

    avgCompletionTime /= n;
    avgTurnaroundTime /= n;
    avgWaitingTime /= n;

    jTextField2.setText(String.valueOf(avgWaitingTime));
    jTextField3.setText(String.valueOf(avgTurnaroundTime));
    jTextField1.setText(String.valueOf(avgCompletionTime));

    updateGanttChart(ganttChartData);
    publishSchedulingSummary("Priority (Non-Preemptive)", null,
            avgCompletionTime, avgWaitingTime, avgTurnaroundTime, ganttChartData);
    refreshQueueDisplay();
}

private void performPriorityPreemptiveScheduling() {
    int n = processData.size();

    int[] arrivalTime = new int[n];
    int[] burstTime = new int[n];
    int[] priority = new int[n];

    int[] remainingBurstTime = new int[n];
    int[] completionTime = new int[n];
    int[] waitingTime = new int[n];
    int[] turnaroundTime = new int[n];

    List<int[]> ganttChartData = new ArrayList<>();
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    for (int i = 0; i < n; i++) {
        arrivalTime[i] = Integer.parseInt(processData.get(i)[2]);
        burstTime[i] = Integer.parseInt(processData.get(i)[3]);
        priority[i] = getPriorityValue(i);
        remainingBurstTime[i] = burstTime[i];
    }

    int currentTime = 0;
    int completedProcesses = 0;
    int previousProcess = -1;

    while (completedProcesses < n) {
        int chosen = -1;

        for (int i = 0; i < n; i++) {
            if (arrivalTime[i] <= currentTime && remainingBurstTime[i] > 0) {
                if (chosen == -1 || priority[i] < priority[chosen]) {
                    chosen = i;
                }
            }
        }

        if (chosen == -1) {
            currentTime++;
            continue;
        }

        int startTime = currentTime;

        remainingBurstTime[chosen]--;
        currentTime++;

        if (previousProcess != chosen) {
            ganttChartData.add(new int[]{chosen + 1, startTime, currentTime});
            previousProcess = chosen;
        } else {
            ganttChartData.get(ganttChartData.size() - 1)[2] = currentTime;
        }

        if (remainingBurstTime[chosen] == 0) {
            completedProcesses++;
            completionTime[chosen] = currentTime;
            turnaroundTime[chosen] = completionTime[chosen] - arrivalTime[chosen];
            waitingTime[chosen] = turnaroundTime[chosen] - burstTime[chosen];

            model.addRow(new Object[]{
                processData.get(chosen)[0],
                processData.get(chosen)[1],
                arrivalTime[chosen],
                burstTime[chosen],
                completionTime[chosen],
                waitingTime[chosen],
                turnaroundTime[chosen]
            });
        }
    }

    double avgCompletionTime = 0;
    double avgTurnaroundTime = 0;
    double avgWaitingTime = 0;

    for (int i = 0; i < n; i++) {
        avgCompletionTime += completionTime[i];
        avgTurnaroundTime += turnaroundTime[i];
        avgWaitingTime += waitingTime[i];
    }

    avgCompletionTime /= n;
    avgTurnaroundTime /= n;
    avgWaitingTime /= n;

    jTextField2.setText(String.valueOf(avgWaitingTime));
    jTextField3.setText(String.valueOf(avgTurnaroundTime));
    jTextField1.setText(String.valueOf(avgCompletionTime));

    updateGanttChart(ganttChartData);
    publishSchedulingSummary("Priority (Preemptive)", null,
            avgCompletionTime, avgWaitingTime, avgTurnaroundTime, ganttChartData);
    refreshQueueDisplay();
}

 private void performRoundRobinScheduling(String timeQuantum) {
    int n = processData.size();
    int tQuantum = Integer.parseInt(timeQuantum);
    int[] arrivalTime = new int[n];
    int[] burstTime = new int[n];
    int[] remainingBurstTime = new int[n];
    int[] waitingTime = new int[n];
    int[] turnaroundTime = new int[n];
    int[] completionTime = new int[n];
    boolean[] completed = new boolean[n];

    double avgCompletionTime = 0;
    double avgTurnaroundTime = 0;
    double avgWaitingTime = 0;
    List<int[]> ganttChartData = new ArrayList<>();

    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    for (int i = 0; i < n; i++) {
        arrivalTime[i] = Integer.parseInt(processData.get(i)[2]);
        burstTime[i] = Integer.parseInt(processData.get(i)[3]);
        remainingBurstTime[i] = burstTime[i];
    }

    int currentTime = 0;
    int completedCount = 0;
  java.util.Queue<Integer> readyQueue = new java.util.LinkedList<>();

    while (completedCount < n) {
        for (int i = 0; i < n; i++) {
            if (!completed[i] && arrivalTime[i] <= currentTime && !readyQueue.contains(i)) {
                readyQueue.add(i);
            }
        }
        if (readyQueue.isEmpty()) {
            currentTime++;
            continue;
        }
        int idx = readyQueue.poll();
        if (remainingBurstTime[idx] <= 0 || completed[idx]) {
            continue;
        }
        int slice = Math.min(tQuantum, remainingBurstTime[idx]);
        int start = currentTime;
        currentTime += slice;
        remainingBurstTime[idx] -= slice;
        ganttChartData.add(new int[]{Integer.parseInt(processData.get(idx)[0]), start, currentTime});

        if (remainingBurstTime[idx] == 0) {
            completed[idx] = true;
            completedCount++;
            completionTime[idx] = currentTime;
            turnaroundTime[idx] = completionTime[idx] - arrivalTime[idx];
            waitingTime[idx] = turnaroundTime[idx] - burstTime[idx];
            model.addRow(new Object[]{
                processData.get(idx)[0],
                processData.get(idx)[1],
                arrivalTime[idx],
                burstTime[idx],
                completionTime[idx],
                waitingTime[idx],
                turnaroundTime[idx]
            });
        } else {
            for (int i = 0; i < n; i++) {
                if (!completed[i] && arrivalTime[i] <= currentTime && i != idx && !readyQueue.contains(i)
                        && remainingBurstTime[i] > 0) {
                    readyQueue.add(i);
                }
            }
            readyQueue.add(idx);
        }
    }

    for (int j = 0; j < n; j++) {
        avgCompletionTime += completionTime[j];
        avgTurnaroundTime += turnaroundTime[j];
        avgWaitingTime += waitingTime[j];
    }

    avgCompletionTime /= n;
    avgTurnaroundTime /= n;
    avgWaitingTime /= n;

    jTextField2.setText(String.valueOf(avgWaitingTime));
    jTextField3.setText(String.valueOf(avgTurnaroundTime));
    jTextField1.setText(String.valueOf(avgCompletionTime));
    updateGanttChart(ganttChartData);
    publishSchedulingSummary("Round Robin", "Time Quantum: " + tQuantum,
            avgCompletionTime, avgWaitingTime, avgTurnaroundTime, ganttChartData);
    refreshQueueDisplay();
}

     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jTextField5 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new Color(245, 245, 250));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 36)); // NOI18N
        jLabel1.setForeground(new Color(40, 44, 52));
        jLabel1.setText("Scheduling Algorithms");

jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Choose", "FCFS", "SJF(PREEMPTIVE)", "SJF(NON_PREEMPTIVE)", "PRIORITY(PREEMPTIVE)", "PRIORITY(NON_PREEMPTIVE)", "ROUND ROBIN", " " }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel2.setForeground(new Color(40, 44, 52));
        jLabel2.setText("Average Completion Time");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setForeground(new Color(40, 44, 52));
        jLabel3.setText("Selected Algorithm");

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setForeground(new Color(40, 44, 52));
        jLabel4.setText("Average Waiting Time");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel5.setForeground(new Color(40, 44, 52));
        jLabel5.setText("Average Turnaround Time");

        jTable1.setBackground(new Color(245, 245, 250));
        jTable1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jTable1.setForeground(new Color(40, 44, 52));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "processID", "processName", "ArrivalTime", "BrustTime", "completionTime", "WaitingTime", "TurnaroundTime"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton1.setText("Scheduling");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel6.setForeground(new Color(40, 44, 52));
        jLabel6.setText("Time Quantum");

        jButton2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton2.setText("Reset");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jButton3.setText("MainMenu");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jTextField5.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jTextField5.setText("|");

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("GanttChart");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel5))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(23, 23, 23)
                                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(118, 118, 118)
                                .addComponent(jButton2))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(88, 88, 88)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jButton3)
                                    .addComponent(jButton1))))
                        .addGap(27, 27, 27)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(226, 226, 226)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(116, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jLabel1)
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(38, 38, 38)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(40, 40, 40)
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 84, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(48, 48, 48))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    String selectedAlgorithm = (String) jComboBox1.getSelectedItem();
    if (selectedAlgorithm == null || "Choose".equals(selectedAlgorithm)
            || selectedAlgorithm.trim().isEmpty()) {
        return;
    }
    processData = buildProcessDataFromRegistry();
    if (processData.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Pehle Process Management se kam az kam ek process create karein,\n"
                        + "phir Algorithm Simulation tab mein processes table check karein.",
                "No Processes", JOptionPane.WARNING_MESSAGE);
        return;
    }
    reloadSimulationTableFromRegistry();
    try {
        switch (selectedAlgorithm.trim()) {
        case "FCFS":
            performFCFSScheduling();
            break;
        case "ROUND ROBIN":
            String timeQuantum = jTextField4.getText().trim();
            if (timeQuantum.isEmpty()) {
                timeQuantum = String.valueOf(KernelConfig.getInstance().getTimeQuantum());
            }
            performRoundRobinScheduling(timeQuantum);
            jTextField4.setText(timeQuantum);
            break;
        case "SJF(PREEMPTIVE)":
            performSJFPreemptiveScheduling();
            break;
        case "SJF(NON_PREEMPTIVE)":
            performSJFNonPreemptiveScheduling();
            break;
        case "PRIORITY(PREEMPTIVE)":
            performPriorityPreemptiveScheduling();
            break;
        case "PRIORITY(NON_PREEMPTIVE)":
            performPriorityNonPreemptiveScheduling();
            break;
        default:
            JOptionPane.showMessageDialog(this,
                    "Algorithm select karein: FCFS, SJF, Priority, ya Round Robin.",
                    "Select Algorithm", JOptionPane.WARNING_MESSAGE);
            break;
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
                "Scheduling error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
    refreshQueueDisplay();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
           jTextField4.setText(String.valueOf(KernelConfig.getInstance().getTimeQuantum()));
           jTextField1.setText("");
           jTextField2.setText("");  
           jTextField3.setText(""); 
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        clearSchedulingSummary();
    }

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        NavigationHelper.back(this);
    }//GEN-LAST:event_jButton3ActionPerformed

    public static void main(String args[]) {
        KernelTheme.init();
        java.awt.EventQueue.invokeLater(() -> new PHH1().setVisible(true));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
