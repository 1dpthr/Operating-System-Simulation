import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class PHH2 extends javax.swing.JFrame {
    DefaultTableModel model;
    private DefaultTableModel pcbModel;
    private JLabel pcbEmptyHint;
    private final JComboBox<String> parentPidCombo = ProcessPicker.createCombo(true);
    private final JComboBox<String> processActionCombo = ProcessPicker.createCombo(false);
    private final JComboBox<String> createPriorityCombo = new JComboBox<>(new String[]{"High", "Medium", "Low"});
    private final ProcessRegistry registry = ProcessRegistry.getInstance();
    private JButton commBtn;
    private JButton configBtn;

    private int parsePriorityToInt(Object priorityCellValue) {
        String s = (priorityCellValue == null) ? "" : priorityCellValue.toString().trim();
        if (s.isEmpty()) return 2;

        switch (s.toLowerCase()) {
            case "high":
                return 1;
            case "medium":
                return 2;
            case "low":
                return 3;
            case "1":
            case "2":
            case "3":
                return Integer.parseInt(s);
            default:
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return 2;
                }
        }
    }

    public PHH2() {
        initComponents();
        jTable2.setModel(SharedTableModel.getInstance());
        model = (DefaultTableModel) jTable2.getModel();
        setupPcbTab();
        addExtraButtons();
        ProcessRegistry.setPcbRefreshCallback(this::refreshPcbTable);
        ProcessPicker.registerRefreshCallback(() -> {
            ProcessPicker.refresh(processActionCombo, false);
            ProcessPicker.refresh(parentPidCombo, true);
        });
        jButton11.setText("Back");
        fixProcessButtonLayout();
        fixFrameLayout();
        fixCreateProcessPanel();
        KernelTheme.applyToWindow(this);
        wireProcessToolbarButtons();
        wireToolsTabButtons();
        wireCreateProcessButton();
        setTitle(KernelTheme.OS_NAME + " — Process Management");
    }

    private void wireCreateProcessButton() {
        ButtonWiring.bind(jButton5, () -> jButton5ActionPerformed(
                new java.awt.event.ActionEvent(jButton5, java.awt.event.ActionEvent.ACTION_PERFORMED, "wire")));
    }

    private void openProcessCommunication() {
        ProcessCommunicationFxApp.launchWindow();
    }

    private void openKernelConfiguration() {
        ConfigurationGUI gui = new ConfigurationGUI();
        gui.setLocationRelativeTo(this);
        gui.setVisible(true);
        gui.toFront();
        gui.requestFocus();
    }

    private void openIoManagement() {
        interrupt gui = new interrupt();
        gui.setLocationRelativeTo(this);
        gui.setVisible(true);
        gui.toFront();
        gui.requestFocus();
    }

    private void wireProcessToolbarButtons() {
        rebindButton(jButton10, e -> jButton10ActionPerformed(e));
        rebindButton(jButton6, e -> jButton6ActionPerformed(e));
        rebindButton(jButton12, e -> jButton12ActionPerformed(e));
        rebindButton(jButton1, e -> jButton1ActionPerformed(e));
        rebindButton(jButton4, e -> jButton4ActionPerformed(e));
        rebindButton(jButton7, e -> jButton7ActionPerformed(e));
        rebindButton(jButton9, e -> jButton9ActionPerformed(e));
        rebindButton(jButton2, e -> openIoManagement());
        rebindButton(jButton8, e -> jButton8ActionPerformed(e));
        rebindButton(jButton11, e -> jButton11ActionPerformed(e));
    }

    private void wireToolsTabButtons() {
        if (commBtn == null || configBtn == null) {
            return;
        }
        rebindButton(commBtn, e -> openProcessCommunication());
        rebindButton(configBtn, e -> openKernelConfiguration());
    }

    private void rebindButton(JButton button, java.awt.event.ActionListener action) {
        for (java.awt.event.ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
        button.addActionListener(action);
        button.setEnabled(true);
    }

    private void fixFrameLayout() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(KernelTheme.BG);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        jPanel1.setPreferredSize(new Dimension(0, 112));
        jPanel1.setMinimumSize(new Dimension(0, 112));
        getContentPane().add(jPanel1, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jTabbedPane1, jScrollPane2);
        split.setDividerLocation(0.52);
        split.setResizeWeight(0.5);
        split.setBackground(KernelTheme.BG);
        getContentPane().add(split, BorderLayout.CENTER);

        UiLayout.applyWorkspaceWindow(this, 1000, 600, 800, 520);
    }

    private void fixCreateProcessPanel() {
        jPanel4.removeAll();
        jPanel4.setLayout(new BorderLayout());
        jPanel4.setBackground(KernelTheme.BG);

        JPanel card = UiLayout.formCard();
        UiLayout.addCardTitle(card, 0, "Create Process");

        UiLayout.addAlignedFormRow(card, 1, "Process Name:", jTextField3);
        UiLayout.addAlignedFormRow(card, 2, "Arrival Time:", jTextField1);
        UiLayout.addAlignedFormRow(card, 3, "Burst Time:", jTextField4);
        UiLayout.addAlignedFormRow(card, 4, "Process Size (KB):", jTextField2);
        createPriorityCombo.setSelectedItem("Medium");
        UiLayout.applyComboBoxSize(createPriorityCombo, UiLayout.FORM_FIELD_WIDE);
        UiLayout.addAlignedFormRow(card, 5, "Priority:", createPriorityCombo);
        UiLayout.addAlignedFormRow(card, 6, "Parent Process (optional):", parentPidCombo);

        GridBagConstraints gbc = UiLayout.cardGbc(7);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 4, 10);
        KernelTheme.stylePrimaryButton(jButton5, "Create Process");
        UiLayout.normalizeActionButton(jButton5);
        card.add(jButton5, gbc);

        UiLayout.mountCenteredCard(jPanel4, card);
    }

    private void fixProcessButtonLayout() {
        jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout(0, 8));
        jPanel1.setBackground(KernelTheme.BG);
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel selectorRow = new JPanel(new GridBagLayout());
        selectorRow.setOpaque(false);
        GridBagConstraints gbc = UiLayout.formGbc(0, 0, 0);
        JLabel selectLbl = new JLabel("Select Process:");
        KernelTheme.styleLabel(selectLbl);
        selectLbl.setPreferredSize(new Dimension(108, 26));
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4, 0, 4, 10);
        selectorRow.add(selectLbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        UiLayout.applyComboBoxSize(processActionCombo, UiLayout.COMBO_FIELD);
        selectorRow.add(processActionCombo, gbc);

        jButton2.setText("I/O");
        jButton9.setText("Priority");

        javax.swing.JButton[] toolbar = {
            jButton10, jButton6, jButton12, jButton1, jButton4,
            jButton7, jButton9, jButton2, jButton8, jButton11
        };
        String[] labels = {
            "Destroy", "Suspend", "Block", "Resume", "Wake Up",
            "Dispatch", "Priority", "I/O", "Scheduling", "Back"
        };
        for (int i = 0; i < toolbar.length; i++) {
            if ("Back".equals(labels[i])) {
                KernelTheme.styleSecondaryButton(toolbar[i], labels[i]);
            } else if ("Destroy".equals(labels[i])) {
                KernelTheme.styleAccentButton(toolbar[i], labels[i]);
            } else {
                KernelTheme.stylePrimaryButton(toolbar[i], labels[i]);
            }
        }
        jPanel1.add(selectorRow, BorderLayout.NORTH);
        jPanel1.add(UiLayout.toolbarButtonGrid(5, toolbar), BorderLayout.CENTER);
    }

    private void setupPcbTab() {
        String[] cols = {"PID", "State", "Owner", "Priority", "Parent", "Children",
            "Memory Req.", "Mem. Pointer", "CPU Registers", "Processor", "I/O State"};
        pcbModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable pcbTable = new JTable(pcbModel);
        KernelTheme.styleTable(pcbTable);
        pcbTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {55, 72, 80, 68, 58, 90, 92, 108, 130, 78, 96};
        for (int i = 0; i < widths.length && i < pcbTable.getColumnCount(); i++) {
            pcbTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        pcbEmptyHint = new JLabel(
                "No PCB data yet. Go to CreateProcess tab, fill the form, then click Create Process.",
                SwingConstants.CENTER);
        pcbEmptyHint.setFont(KernelTheme.bodyFont());
        pcbEmptyHint.setForeground(KernelTheme.TEXT_MUTED);
        pcbEmptyHint.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(pcbTable);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel pcbPanel = new JPanel(new BorderLayout());
        pcbPanel.setBackground(KernelTheme.BG_PANEL);
        pcbPanel.add(pcbEmptyHint, BorderLayout.NORTH);
        pcbPanel.add(scroll, BorderLayout.CENTER);
        jTabbedPane1.addTab("PCB Details", pcbPanel);
        refreshPcbTable();
    }

    private void addExtraButtons() {
        commBtn = new JButton();
        configBtn = new JButton();
        KernelTheme.stylePrimaryButton(commBtn, "Process Communication");
        KernelTheme.styleSecondaryButton(configBtn, "Kernel Configuration");

        Dimension btnSize = new Dimension(268, UiLayout.MENU_BUTTON.height);
        for (JButton btn : new JButton[]{commBtn, configBtn}) {
            btn.setPreferredSize(btnSize);
            btn.setMinimumSize(btnSize);
            btn.setMaximumSize(new Dimension(btnSize.width + 8, btnSize.height + 14));
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
        }

        JPanel tools = new JPanel(new GridBagLayout());
        tools.setBackground(KernelTheme.BG);
        tools.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, UiLayout.BUTTON_GAP, 0);
        JButton[] toolButtons = {commBtn, configBtn};
        for (int i = 0; i < toolButtons.length; i++) {
            gbc.gridy = i;
            gbc.insets = i < toolButtons.length - 1
                    ? new Insets(0, 0, UiLayout.BUTTON_GAP, 0)
                    : new Insets(0, 0, 0, 0);
            tools.add(toolButtons[i], gbc);
        }

        jTabbedPane1.addTab("Tools", tools);
    }

    private void refreshPcbTable() {
        if (pcbModel == null) {
            return;
        }
        pcbModel.setRowCount(0);
        for (ProcessControlBlock pcb : registry.getAll()) {
            pcbModel.addRow(pcb.toPcbDetailRow());
        }
        if (pcbEmptyHint != null) {
            pcbEmptyHint.setVisible(pcbModel.getRowCount() == 0);
        }
    }

    private ProcessControlBlock.ProcessState mapState(String status) {
        return switch (status.toLowerCase()) {
            case "running" -> ProcessControlBlock.ProcessState.RUNNING;
            case "ready" -> ProcessControlBlock.ProcessState.READY;
            case "blocked" -> ProcessControlBlock.ProcessState.BLOCKED;
            case "suspended" -> ProcessControlBlock.ProcessState.SUSPENDED;
            default -> ProcessControlBlock.ProcessState.TERMINATED;
        };
    }

    private void syncPcbState(int processId, String status, String priorityLabel) {
        registry.find(processId).ifPresent(pcb -> {
            pcb.setState(mapState(status));
            pcb.setPriority(switch (priorityLabel.toLowerCase()) {
                case "high" -> 1;
                case "low" -> 3;
                default -> 2;
            });
            if ("running".equalsIgnoreCase(status)) {
                pcb.captureCpuContext();
            }
        });
        ProcessRegistry.syncViews();
    }

   public boolean containsProcessID(String processID) {
        try {
            return registry.find(Integer.parseInt(processID.trim())).isPresent();
        } catch (NumberFormatException e) {
            return false;
        }
    }
public void changeProcessStatus(String processID, String status, String priority) {
    for (int i = 0; i < model.getRowCount(); i++) {
        String pid = model.getValueAt(i, 0).toString();
        if (pid.equals(processID)) {
            model.setValueAt(status, i, 4);
            model.setValueAt(priority, i, 5);
            break;
        }
    }
}
public boolean isInterruptedIn(String processID) {
    for (int i = 0; i < model.getRowCount(); i++) {
        if (processID.equals(model.getValueAt(i, 0).toString())) {
            String status = model.getValueAt(i, 4).toString();
            if (status.equals("blocked")) {
                return true;
            }
        }
    }
    return false;
}
    
private List<String[]> getRunningProcessesData()
{
    List<String[]> runningProcessesData = new ArrayList<>();
    for (ProcessControlBlock pcb : registry.getActiveProcesses()) {
        if (runningProcessesData.size() >= 10) {
            break;
        }
        runningProcessesData.add(new String[]{
            String.valueOf(pcb.getProcessId()),
            pcb.getProcessName(),
            String.valueOf(pcb.getArrivalTime()),
            String.valueOf(pcb.getBurstTime()),
            String.valueOf(pcb.getPriority())
        });
    }
    return runningProcessesData;
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new Color(245, 245, 250));

        jButton1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton1.setText("Resume");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton2.setText("IO management");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton3.setText("Create");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton4.setText("WakeUp");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton6.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton6.setText("Suspend");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton7.setText("Dispatch");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton8.setText("Scheduling");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton9.setText("ChangePriority");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton10.setText("Destroy");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton11.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton11.setText("Main Menu");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton12.setText("Block");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(38, 38, 38)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(37, 37, 37)
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(63, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        jTabbedPane1.setBackground(new Color(245, 245, 250));
        jTabbedPane1.setForeground(new Color(40, 44, 52));
        jTabbedPane1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N

        jPanel4.setBackground(new Color(245, 245, 250));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel1.setForeground(new Color(40, 44, 52));
        jLabel1.setText("No. of Process");

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel2.setForeground(new Color(40, 44, 52));
        jLabel2.setText("ProcessName");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setForeground(new Color(40, 44, 52));
        jLabel3.setText("ArrivalTime");

        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel4.setForeground(new Color(40, 44, 52));
        jLabel4.setText("BurstTime");

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jButton5.setText("Created");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1)
                    .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                    .addComponent(jTextField3)
                    .addComponent(jTextField4))
                .addGap(17, 17, 17))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addComponent(jButton5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addComponent(jButton5)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("CreateProcess", jPanel4);

        jTable2.setBackground(new Color(245, 245, 250));
        jTable2.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        jTable2.setForeground(new Color(40, 44, 52));
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ProcessID", "ProcessName", "ArrivalTime", "BurstTime", "Status", "Priority"
            }
        ));
        jTable2.setToolTipText("");
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        String processName = jTextField3.getText();
        String arrivalTime = jTextField1.getText();
        String burstTime = jTextField4.getText();
        String memoryKbText = jTextField2.getText().trim();

        if (processName.isEmpty() || arrivalTime.isEmpty() || burstTime.isEmpty() || memoryKbText.isEmpty()) {
            return;
        }

        try {
            int memoryKb = Integer.parseInt(memoryKbText);
            if (memoryKb <= 0) {
                return;
            }

            Integer parentId = ProcessPicker.getSelectedPid(parentPidCombo).orElse(null);

            ProcessControlBlock pcb = registry.create(
                    processName,
                    System.getProperty("user.name", "user"),
                    Integer.parseInt(arrivalTime),
                    Integer.parseInt(burstTime),
                    memoryKb,
                    ProcessControlBlock.priorityFromLabel(
                            (String) createPriorityCombo.getSelectedItem()),
                    parentId);

            jTextField1.setText("");
            jTextField3.setText("");
            jTextField4.setText("");
            jTextField2.setText("");
            createPriorityCombo.setSelectedItem("Medium");
            parentPidCombo.setSelectedItem(ProcessPicker.NONE);
            refreshPcbTable();
        } catch (NumberFormatException ignored) {
        } catch (IllegalArgumentException ignored) {
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        NavigationHelper.backToMain(this);
    }//GEN-LAST:event_jButton11ActionPerformed

    private Optional<Integer> selectedActionPid() {
        return ProcessPicker.getSelectedPid(processActionCombo);
    }

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        Optional<Integer> pidOpt = selectedActionPid();
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int pid = pidOpt.get();
        if (registry.remove(pid)) {
            refreshPcbTable();
            JOptionPane.showMessageDialog(this, "Process P" + pid + " destroyed.");
        } else {
            JOptionPane.showMessageDialog(this, "Process P" + pid + " not found.");
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        Optional<Integer> pidOpt = selectedActionPid();
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int pid = pidOpt.get();
        registry.find(pid).ifPresentOrElse(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.RUNNING
                    || pcb.getState() == ProcessControlBlock.ProcessState.READY) {
                pcb.setState(ProcessControlBlock.ProcessState.SUSPENDED);
                pcb.setPriority(3);
                ProcessRegistry.syncViews();
                refreshPcbTable();
                JOptionPane.showMessageDialog(this, "Process P" + pid + " suspended.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Process P" + pid + " cannot be suspended from state " + pcb.getState() + ".");
            }
        }, () -> JOptionPane.showMessageDialog(this, "Process P" + pid + " not found."));
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        Optional<Integer> pidOpt = selectedActionPid();
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int pid = pidOpt.get();
        registry.find(pid).ifPresentOrElse(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.RUNNING
                    || pcb.getState() == ProcessControlBlock.ProcessState.READY) {
                pcb.setState(ProcessControlBlock.ProcessState.BLOCKED);
                pcb.setPriority(3);
                pcb.setIoStateInfo("Blocked (manual)");
                ProcessRegistry.syncViews();
                refreshPcbTable();
                JOptionPane.showMessageDialog(this, "Process P" + pid + " blocked.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Process P" + pid + " cannot be blocked from state " + pcb.getState() + ".");
            }
        }, () -> JOptionPane.showMessageDialog(this, "Process P" + pid + " not found."));
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Optional<Integer> pidOpt = selectedActionPid();
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int pid = pidOpt.get();
        registry.find(pid).ifPresentOrElse(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.SUSPENDED) {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
                pcb.setPriority(2);
                ProcessRegistry.syncViews();
                refreshPcbTable();
                JOptionPane.showMessageDialog(this, "Process P" + pid + " resumed (READY).");
            } else {
                JOptionPane.showMessageDialog(this, "Process P" + pid + " is not suspended.");
            }
        }, () -> JOptionPane.showMessageDialog(this, "Process P" + pid + " not found."));
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Optional<Integer> pidOpt = selectedActionPid();
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int pid = pidOpt.get();
        registry.find(pid).ifPresentOrElse(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.BLOCKED) {
                pcb.setState(ProcessControlBlock.ProcessState.READY);
                pcb.setPriority(2);
                ProcessRegistry.syncViews();
                refreshPcbTable();
                JOptionPane.showMessageDialog(this, "Process P" + pid + " is READY.");
            } else {
                JOptionPane.showMessageDialog(this, "Process P" + pid + " is not blocked.");
            }
        }, () -> JOptionPane.showMessageDialog(this, "Process P" + pid + " not found."));
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        Optional<Integer> pidOpt = selectedActionPid();
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int pid = pidOpt.get();
        registry.find(pid).ifPresentOrElse(pcb -> {
            if (pcb.getState() == ProcessControlBlock.ProcessState.SUSPENDED) {
                JOptionPane.showMessageDialog(this, "Process P" + pid + " is suspended. Cannot dispatch.");
                return;
            }
            for (ProcessControlBlock running : registry.getByState(ProcessControlBlock.ProcessState.RUNNING)) {
                running.setState(ProcessControlBlock.ProcessState.READY);
                running.setPriority(2);
            }
            pcb.setState(ProcessControlBlock.ProcessState.RUNNING);
            pcb.setPriority(1);
            ProcessRegistry.syncViews();
            refreshPcbTable();
            JOptionPane.showMessageDialog(this, "Process P" + pid + " is RUNNING.");
        }, () -> JOptionPane.showMessageDialog(this, "Process P" + pid + " not found."));
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        if (registry.getActiveProcesses().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Pehle Process Management se kam az kam ek process create karein.",
                    "No Processes",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Schedulingg s = new Schedulingg(null);
        s.setLocationRelativeTo(this);
        s.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        Optional<Integer> pidOpt = selectedActionPid();
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int pid = pidOpt.get();
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        KernelTheme.styleComboBox(priorityCombo);
        int result = JOptionPane.showConfirmDialog(this, priorityCombo,
                "New priority for P" + pid, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }
        String newPriority = (String) priorityCombo.getSelectedItem();
        registry.find(pid).ifPresentOrElse(pcb -> {
            pcb.setPriority(ProcessControlBlock.priorityFromLabel(newPriority));
            ProcessRegistry.syncViews();
            refreshPcbTable();
        }, () -> JOptionPane.showMessageDialog(this, "Process P" + pid + " not found."));
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        openIoManagement();
    }//GEN-LAST:event_jButton2ActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PHH2().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
