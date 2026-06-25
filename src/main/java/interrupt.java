import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.util.Optional;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;

public class interrupt extends javax.swing.JFrame {
      private JPanel ioCards;
      private CardLayout ioCardLayout;
      private JComboBox<String> interruptCreateCombo;
      private JComboBox<String> interruptReleaseCombo;

     public interrupt(){
         initComponents();
         interruptCreateCombo = ProcessPicker.createCombo(false);
         interruptReleaseCombo = ProcessPicker.createCombo(false);
         fixInterruptTabs();
         fixWindowLayout();
         KernelTheme.applyToWindow(this);
         polishLayout();
         ProcessPicker.registerRefreshCallback(() -> {
             ProcessPicker.refresh(interruptCreateCombo, false);
             ProcessPicker.refresh(interruptReleaseCombo, false);
         });
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(KernelTheme.OS_NAME + " — I/O Management");
         wireIoButtons();
     }

     private void wireIoButtons() {
         ButtonWiring.bind(jButton2, () -> {
             if (ioCardLayout != null) {
                 ioCardLayout.show(ioCards, "in");
             }
         });
         ButtonWiring.bind(jButton1, () -> {
             if (ioCardLayout != null) {
                 ioCardLayout.show(ioCards, "out");
             }
         });
         ButtonWiring.bind(jButton11, () -> NavigationHelper.back(this));
         ButtonWiring.bind(jButton3, () -> jButton3ActionPerformed(
                 new java.awt.event.ActionEvent(jButton3, java.awt.event.ActionEvent.ACTION_PERFORMED, "wire")));
         ButtonWiring.bind(jButton4, () -> jButton4ActionPerformed(
                 new java.awt.event.ActionEvent(jButton4, java.awt.event.ActionEvent.ACTION_PERFORMED, "wire")));
     }

     private DefaultTableModel processModel() {
         return SharedTableModel.getInstance();
     }

     private boolean containsProcessID(String processID) {
         try {
             return ProcessRegistry.getInstance().find(Integer.parseInt(processID.trim())).isPresent();
         } catch (NumberFormatException e) {
             return false;
         }
     }

     private void changeProcessStatus(String processID, String status, String priority, String device) {
         DefaultTableModel model = processModel();
         for (int i = 0; i < model.getRowCount(); i++) {
             if (processID.equals(model.getValueAt(i, 0).toString())) {
                 model.setValueAt(status, i, 4);
                 model.setValueAt(priority, i, 5);
                 syncRegistryState(processID, status, priority, device);
                 return;
             }
         }
     }

     private String selectedDevice() {
         if (jRadioButton2.isSelected()) {
             return "Keyboard";
         }
         if (jRadioButton3.isSelected()) {
             return "Printer";
         }
         return "Mouse";
     }

     private void syncRegistryState(String processID, String status, String priority, String device) {
         try {
             int pid = Integer.parseInt(processID);
             ProcessRegistry.getInstance().find(pid).ifPresent(pcb -> {
                 pcb.setState(switch (status.toLowerCase()) {
                     case "running" -> ProcessControlBlock.ProcessState.RUNNING;
                     case "ready" -> ProcessControlBlock.ProcessState.READY;
                     case "blocked" -> ProcessControlBlock.ProcessState.BLOCKED;
                     case "suspended" -> ProcessControlBlock.ProcessState.SUSPENDED;
                     default -> pcb.getState();
                 });
                 pcb.setPriority(switch (priority.toLowerCase()) {
                     case "high" -> 1;
                     case "low" -> 3;
                     default -> 2;
                 });
                 if (device != null) {
                     pcb.setIoStateInfo("Waiting on " + device);
                 } else if ("ready".equalsIgnoreCase(status)) {
                     pcb.setIoStateInfo("Idle");
                 }
             });
             ProcessRegistry.syncViews();
         } catch (NumberFormatException ignored) {
         }
     }

     private boolean isInterruptedIn(String processID) {
         try {
             return ProcessRegistry.getInstance().find(Integer.parseInt(processID.trim()))
                     .map(pcb -> pcb.getState() == ProcessControlBlock.ProcessState.BLOCKED)
                     .orElse(false);
         } catch (NumberFormatException e) {
             return false;
         }
     }

     private void polishLayout() {
         jButton2.setText("I/O Input");
         jButton1.setText("I/O Output");
         jButton11.setText("Back");
         UiLayout.normalizeMenuButton(jButton2);
         UiLayout.normalizeMenuButton(jButton1);
         UiLayout.normalizeMenuButton(jButton11);
         UiLayout.normalizeCompactButton(jButton3);
         UiLayout.normalizeCompactButton(jButton4);
     }

     private void fixWindowLayout() {
         ioCardLayout = new CardLayout();
         ioCards = new JPanel(ioCardLayout);
         ioCards.setBackground(KernelTheme.BG);
         ioCards.add(jPanel2, "in");
         ioCards.add(jPanel3, "out");

         JPanel sidebar = UiLayout.sidebar("I/O System", jButton2, jButton1, jButton11);
         UiLayout.mountSidebarFrame(this, sidebar, ioCards, 640, 420);
         ioCardLayout.show(ioCards, "in");
         jPanel1.setVisible(false);
     }

     private void fixInterruptTabs() {
         buildInputForm();
         buildOutputForm();
     }

     private void buildInputForm() {
         JPanel card = UiLayout.formCard();

         GridBagConstraints gbc = UiLayout.cardGbc(0);
         gbc.gridwidth = 2;
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.fill = GridBagConstraints.NONE;
         JLabel title = new JLabel("Create I/O Interrupt", JLabel.CENTER);
         title.setFont(KernelTheme.headingFont());
         KernelTheme.styleLabel(title);
         card.add(title, gbc);

         gbc = UiLayout.cardGbc(1);
         gbc.gridwidth = 1;
         gbc.weightx = 0.35;
         JLabel pidLbl = new JLabel("Process ID:");
         KernelTheme.styleLabel(pidLbl);
         card.add(pidLbl, gbc);

         gbc.gridx = 1;
         gbc.weightx = 0.65;
         KernelTheme.styleComboBox(interruptCreateCombo);
         card.add(interruptCreateCombo, gbc);

         jRadioButton1.setText("Mouse");
         jRadioButton2.setText("Keyboard");
         jRadioButton3.setText("Printer");
         ButtonGroup deviceGroup = new ButtonGroup();
         deviceGroup.add(jRadioButton1);
         deviceGroup.add(jRadioButton2);
         deviceGroup.add(jRadioButton3);
         jRadioButton1.setSelected(true);
         for (javax.swing.JRadioButton rb : new javax.swing.JRadioButton[]{jRadioButton1, jRadioButton2, jRadioButton3}) {
             rb.setFont(KernelTheme.bodyFont());
             rb.setForeground(KernelTheme.TEXT);
             rb.setOpaque(false);
             rb.setBackground(KernelTheme.CARD);
         }

         gbc = UiLayout.cardGbc(2);
         gbc.gridwidth = 2;
         gbc.anchor = GridBagConstraints.CENTER;
         JPanel devPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
         devPanel.setOpaque(false);
         devPanel.add(jRadioButton1);
         devPanel.add(jRadioButton2);
         devPanel.add(jRadioButton3);
         card.add(devPanel, gbc);

         gbc = UiLayout.cardGbc(3);
         gbc.gridwidth = 2;
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.insets = new Insets(18, 12, 4, 12);
         KernelTheme.stylePrimaryButton(jButton3, "Create Interrupt");
         UiLayout.normalizeCompactButton(jButton3);
         card.add(jButton3, gbc);

         UiLayout.mountCenteredCard(jPanel2, card);
     }

     private void buildOutputForm() {
         JPanel card = UiLayout.formCard();

         GridBagConstraints gbc = UiLayout.cardGbc(0);
         gbc.gridwidth = 2;
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.fill = GridBagConstraints.NONE;
         JLabel title = new JLabel("Release I/O Interrupt", JLabel.CENTER);
         title.setFont(KernelTheme.headingFont());
         KernelTheme.styleLabel(title);
         card.add(title, gbc);

         gbc = UiLayout.cardGbc(1);
         gbc.gridwidth = 1;
         gbc.weightx = 0.35;
         JLabel pidLbl = new JLabel("Process ID:");
         KernelTheme.styleLabel(pidLbl);
         card.add(pidLbl, gbc);

         gbc.gridx = 1;
         gbc.weightx = 0.65;
         KernelTheme.styleComboBox(interruptReleaseCombo);
         card.add(interruptReleaseCombo, gbc);

         gbc = UiLayout.cardGbc(2);
         gbc.gridx = 0;
         gbc.gridwidth = 2;
         gbc.anchor = GridBagConstraints.CENTER;
         gbc.insets = new Insets(18, 12, 4, 12);
         KernelTheme.stylePrimaryButton(jButton4, "Remove Interrupt");
         UiLayout.normalizeCompactButton(jButton4);
         card.add(jButton4, gbc);

         UiLayout.mountCenteredCard(jPanel3, card);
     }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new Color(245, 245, 250));
        jPanel1.setForeground(new Color(40, 44, 52));

        jButton1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton1.setText("I/O output");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton2.setText("I/O input");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton11.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton11.setText("Main Menu");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel1.setForeground(new Color(40, 44, 52));
        jLabel1.setText("I/O ");

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel2.setForeground(new Color(40, 44, 52));
        jLabel2.setText("Management");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel3.setForeground(new Color(40, 44, 52));
        jLabel3.setText("System");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(jLabel3)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(30, 30, 30)
                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        jTabbedPane1.setBackground(new Color(245, 245, 250));
        jTabbedPane1.setForeground(new Color(40, 44, 52));
        jTabbedPane1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N

        jPanel2.setBackground(new Color(245, 245, 250));

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel4.setForeground(new Color(40, 44, 52));
        jLabel4.setText("ProcessID");

        jTextField1.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N

        jButton3.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton3.setText("I/O Generate");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jRadioButton1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton1.setForeground(new Color(40, 44, 52));
        jRadioButton1.setText("Mouse");

        jRadioButton2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton2.setForeground(new Color(40, 44, 52));
        jRadioButton2.setText("Keyboard");

        jRadioButton3.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jRadioButton3.setForeground(new Color(40, 44, 52));
        jRadioButton3.setText("Printer");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(106, 106, 106)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jRadioButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jRadioButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jRadioButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(118, 118, 118)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(153, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(75, 75, 75)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addGap(38, 38, 38))
        );

        jTabbedPane1.addTab("I/O IN", jPanel2);

        jPanel3.setBackground(new Color(245, 245, 250));

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel5.setForeground(new Color(40, 44, 52));
        jLabel5.setText("ProcessID");

        jTextField2.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N

        jButton4.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton4.setText("I/O Release");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(107, 107, 107)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(121, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(105, 105, 105)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addComponent(jButton4)
                .addContainerGap(156, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("I/O OUT", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {
        NavigationHelper.back(this);
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      if (ioCardLayout != null) {
          ioCardLayout.show(ioCards, "out");
      }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (ioCardLayout != null) {
            ioCardLayout.show(ioCards, "in");
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Optional<Integer> pidOpt = ProcessPicker.getSelectedPid(interruptCreateCombo);
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String processID = String.valueOf(pidOpt.get());
        if (containsProcessID(processID)) {
            String device = selectedDevice();
            changeProcessStatus(processID, "Blocked", "Low", device);
            JOptionPane.showMessageDialog(this,
                    "Interrupt created on " + device + ". Process blocked.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Process P" + processID + " does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Optional<Integer> pidOpt = ProcessPicker.getSelectedPid(interruptReleaseCombo);
        if (pidOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a process.", "Select Process",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String processID = String.valueOf(pidOpt.get());
        if (containsProcessID(processID)) {
            if (isInterruptedIn(processID)) {
                changeProcessStatus(processID, "Ready", "Medium", null);
                JOptionPane.showMessageDialog(this, "Interrupt removed and process is now in ready state");
            } else {
                JOptionPane.showMessageDialog(this, "No interrupt has occurred for process P" + processID);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Process P" + processID + " does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new interrupt().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
