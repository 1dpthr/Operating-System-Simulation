import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ContiguosMemory extends javax.swing.JFrame {
    
    private JTextField blockSizesField;
    private JLabel processesInfoLabel;
    private JPanel inputPanel;
    
    public ContiguosMemory() {
        initComponents();
        setupInputPanel();
        fixWindowLayout();
        KernelTheme.applyToWindow(this);
        wireSidebarButtons();
        setTitle(KernelTheme.OS_NAME + " — Contiguous Memory");
    }

    private void wireSidebarButtons() {
        KernelTheme.stylePrimaryButton(jButton4, "Best Fit");
        KernelTheme.stylePrimaryButton(jButton5, "Worst Fit");
        KernelTheme.stylePrimaryButton(jButton2, "First Fit");
        KernelTheme.styleSecondaryButton(jButton3, "Back");
        ButtonWiring.bind(jButton4, () -> {
            jTabbedPane1.setSelectedIndex(0);
            executeBestFit();
        });
        ButtonWiring.bind(jButton5, () -> {
            jTabbedPane1.setSelectedIndex(1);
            executeWorstFit();
        });
        ButtonWiring.bind(jButton2, () -> {
            jTabbedPane1.setSelectedIndex(2);
            executeFirstFit();
        });
        ButtonWiring.bind(jButton3, () -> NavigationHelper.back(this));
    }

    private void fixWindowLayout() {
        JPanel sidebar = UiLayout.sidebar("Contiguous Memory", jButton4, jButton5, jButton2, jButton3);
        jTabbedPane1.setFont(KernelTheme.bodyFont());
        UiLayout.hideTabBar(jTabbedPane1);

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(KernelTheme.BG);
        getContentPane().add(inputPanel, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setOpaque(false);
        body.setBackground(KernelTheme.BG);
        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 16));
        body.add(sidebar, BorderLayout.WEST);
        body.add(jTabbedPane1, BorderLayout.CENTER);
        getContentPane().add(body, BorderLayout.CENTER);

        jButton3.setText("Back");
        UiLayout.applyWorkspaceWindow(this, 900, 560, 760, 480);
    }
    
    private void setupInputPanel() {
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(KernelTheme.BG_PANEL);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel blockLabel = new JLabel("Memory Block Sizes (comma-separated):");
        KernelTheme.styleLabel(blockLabel);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        inputPanel.add(blockLabel, gbc);
        
        blockSizesField = new JTextField(30);
        blockSizesField.setText("100,500,200,300");
        KernelTheme.styleTextField(blockSizesField);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        inputPanel.add(blockSizesField, gbc);
        
        JLabel processLabel = new JLabel("Loaded Processes (from Process Management):");
        KernelTheme.styleLabel(processLabel);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(processLabel, gbc);

        processesInfoLabel = new JLabel("No processes created yet.");
        KernelTheme.styleLabel(processesInfoLabel);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        inputPanel.add(processesInfoLabel, gbc);

        refreshProcessList();
    }

    private void refreshProcessList() {
        List<ProcessControlBlock> processes = ProcessRegistry.getInstance().getAll();
        if (processes.isEmpty()) {
            processesInfoLabel.setText("No processes — create them in Process Management (PHH2) first.");
            return;
        }
        StringBuilder sb = new StringBuilder("<html>");
        for (ProcessControlBlock pcb : processes) {
            sb.append("P").append(pcb.getProcessId()).append(" ")
                    .append(pcb.getProcessName()).append(" — ")
                    .append(pcb.getMemoryRequirementKb()).append(" KB<br>");
        }
        sb.append("</html>");
        processesInfoLabel.setText(sb.toString());
    }

    private boolean loadProcessesFromRegistry(int[][] blockAndProcess, int[][] processIdsOut) {
        List<ProcessControlBlock> processes = ProcessRegistry.getInstance().getAll();
        if (processes.isEmpty()) {
            return false;
        }
        blockAndProcess[1] = new int[processes.size()];
        processIdsOut[0] = new int[processes.size()];
        for (int i = 0; i < processes.size(); i++) {
            ProcessControlBlock pcb = processes.get(i);
            processIdsOut[0][i] = pcb.getProcessId();
            blockAndProcess[1][i] = pcb.getMemoryRequirementKb();
        }
        return true;
    }
    
    private int[] parseInput(String input) throws NumberFormatException {
        String[] parts = input.trim().split(",");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Integer.parseInt(parts[i].trim());
        }
        return values;
    }
    
    private boolean getUserInput(int[][] blockAndProcess, int[][] processIdsOut) {
        try {
            refreshProcessList();
            String blockInput = blockSizesField.getText().trim();
            if (blockInput.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Memory block sizes enter karein (comma-separated).\nExample: 100,500,200,300",
                        "Input Required", javax.swing.JOptionPane.WARNING_MESSAGE);
                blockSizesField.requestFocus();
                return false;
            }
            if (!loadProcessesFromRegistry(blockAndProcess, processIdsOut)) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Pehle Process Management se processes create karein.",
                        "No Processes", javax.swing.JOptionPane.WARNING_MESSAGE);
                return false;
            }
            blockAndProcess[0] = parseInput(blockInput);
            for (int size : blockAndProcess[0]) {
                if (size <= 0) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Block sizes positive honi chahiye.",
                            "Invalid Input", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Block sizes numbers honi chahiye (comma-separated).",
                    "Invalid Input", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    
    private void displayResults(MemoryAllocationAlgorithms.AllocationResult result, JPanel panel) {
        panel.removeAll();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBackground(KernelTheme.BG_PANEL);
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setDividerLocation(250);
        mainSplit.setBackground(KernelTheme.BG_PANEL);
        
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        tablesPanel.setBackground(KernelTheme.BG_PANEL);
        
        String[] processColumns = {"Process", "Size", "Allocated To", "Status"};
        String[][] processData = MemoryAllocationAlgorithms.getProcessTableData(result);
        JTable processTable = new JTable(processData, processColumns);
        KernelTheme.styleTable(processTable);
        
        JPanel processPanel = new JPanel(new BorderLayout());
        processPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(KernelTheme.BORDER, 1),
            "Process Allocation",
            0, 0,
            KernelTheme.bodyFont(),
            KernelTheme.TEXT
        ));
        processPanel.setBackground(KernelTheme.BG_PANEL);
        processPanel.add(new JScrollPane(processTable), BorderLayout.CENTER);
        
        String[] blockColumns = {"Block", "Total Size", "Remaining", "Allocated To", "Used"};
        String[][] blockData = MemoryAllocationAlgorithms.getBlockTableData(result);
        JTable blockTable = new JTable(blockData, blockColumns);
        KernelTheme.styleTable(blockTable);
        
        JPanel blockPanel = new JPanel(new BorderLayout());
        blockPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(KernelTheme.BORDER, 1),
            "Memory Block Status",
            0, 0,
            KernelTheme.bodyFont(),
            KernelTheme.TEXT
        ));
        blockPanel.setBackground(KernelTheme.BG_PANEL);
        blockPanel.add(new JScrollPane(blockTable), BorderLayout.CENTER);
        
        tablesPanel.add(processPanel);
        tablesPanel.add(blockPanel);
        
        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setText(result.getSummary());
        summaryArea.setCaretPosition(0);
        KernelTheme.styleTextArea(summaryArea);
        
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(KernelTheme.BORDER, 1),
            "Detailed Summary",
            0, 0,
            KernelTheme.bodyFont(),
            KernelTheme.TEXT
        ));
        summaryPanel.setBackground(KernelTheme.BG_PANEL);
        summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        
        mainSplit.setTopComponent(tablesPanel);
        mainSplit.setBottomComponent(summaryPanel);
        
        panel.add(mainSplit, BorderLayout.CENTER);
        KernelTheme.applyToContainer(panel);
        panel.revalidate();
        panel.repaint();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel4.setBackground(new Color(245, 245, 250));
        jPanel4.setForeground(new java.awt.Color(255, 255, 255));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Contiguous Memory Allocation");

        jButton2.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton2.setText("First Fit");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton3.setText("Main Menu");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton4.setText("Best Fit");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton5.setText("Worst Fit");
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
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(14, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(31, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton5)
                .addGap(29, 29, 29)
                .addComponent(jButton2)
                .addGap(123, 123, 123)
                .addComponent(jButton3)
                .addContainerGap())
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(110, 110, 110)
                    .addComponent(jButton4)
                    .addContainerGap(302, Short.MAX_VALUE)))
        );

        jTabbedPane1.setBackground(new Color(245, 245, 250));
        jTabbedPane1.setForeground(new java.awt.Color(255, 255, 255));
        jTabbedPane1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N

        jPanel7.setBackground(new Color(245, 245, 250));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 667, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 406, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Best Fit", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 667, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 406, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Worst Fit", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 667, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 406, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("First Fit", jPanel3);

        getContentPane().setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        mainPanel.add(jPanel4, BorderLayout.WEST);
        
        mainPanel.add(jTabbedPane1, BorderLayout.CENTER);
        
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        pack();
        setSize(1000, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       jTabbedPane1.setSelectedIndex(2);
       executeFirstFit();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        NavigationHelper.back(this);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
        executeBestFit();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
       jTabbedPane1.setSelectedIndex(1);
       executeWorstFit();
    }//GEN-LAST:event_jButton5ActionPerformed
    
    private void executeFirstFit() {
        int[][] input = new int[2][];
        int[][] pids = new int[1][];
        if (!getUserInput(input, pids)) {
            return;
        }
        MemoryAllocationAlgorithms.AllocationResult result =
            MemoryAllocationAlgorithms.firstFit(input[0], pids[0], input[1]);
        displayResults(result, jPanel3);
    }
    
    private void executeBestFit() {
        int[][] input = new int[2][];
        int[][] pids = new int[1][];
        if (!getUserInput(input, pids)) {
            return;
        }
        MemoryAllocationAlgorithms.AllocationResult result =
            MemoryAllocationAlgorithms.bestFit(input[0], pids[0], input[1]);
        displayResults(result, jPanel7);
    }
    
    private void executeWorstFit() {
        int[][] input = new int[2][];
        int[][] pids = new int[1][];
        if (!getUserInput(input, pids)) {
            return;
        }
        MemoryAllocationAlgorithms.AllocationResult result =
            MemoryAllocationAlgorithms.worstFit(input[0], pids[0], input[1]);
        displayResults(result, jPanel2);
    }

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ContiguosMemory().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
