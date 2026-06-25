import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class noncontigious extends javax.swing.JFrame {

    private static final String DEFAULT_REF_STRING = "7,0,1,2,0,3,0,4,2,3,0,3,2";

    private JButton optimalNavBtn;
    private JButton mruNavBtn;
    private JPanel optimalTabPanel;
    private JPanel optimalTableHost;
    private JTextField optimalRefField;
    private JTextField optimalFrameField;
    private JTextField optimalLenField;
    private JTextField optimalFaultsField;
    private JTextField optimalHitsField;

    private JPanel mruTabPanel;
    private JPanel mruTableHost;
    private JTextField mruRefField;
    private JTextField mruFrameField;
    private JTextField mruLenField;
    private JTextField mruFaultsField;
    private JTextField mruHitsField;

    public noncontigious() {
        initComponents();
        setupOptimalPanel();
        setupMruPanel();
        fixWindowLayout();
        fixPagingPanel();
        fixLruPanel();
        fixFifoPanel();
        setDefaultPagingInputs();
        setDefaultReplacementInputs();
        KernelTheme.applyToWindow(this);
        setTitle(KernelTheme.OS_NAME + " — Paging & Page Replacement");
        jTabbedPane2.setSelectedIndex(0);
        wireSidebarButtons();
    }

    private void wireSidebarButtons() {
        KernelTheme.stylePrimaryButton(jButton4, "Paging");
        KernelTheme.stylePrimaryButton(jButton5, "LRU");
        KernelTheme.stylePrimaryButton(jButton2, "FIFO");
        KernelTheme.styleSecondaryButton(jButton3, "Back");
        ButtonWiring.bind(jButton4, () -> {
            selectTab(0);
            setDefaultPagingInputs();
        });
        ButtonWiring.bind(jButton5, () -> selectTab(1));
        ButtonWiring.bind(jButton2, () -> selectTab(2));
        ButtonWiring.bind(jButton3, () -> NavigationHelper.back(this));
    }

    private void fixWindowLayout() {
        optimalNavBtn = new JButton();
        mruNavBtn = new JButton();
        KernelTheme.stylePrimaryButton(optimalNavBtn, "Optimal");
        KernelTheme.stylePrimaryButton(mruNavBtn, "MRU");
        rebindButton(optimalNavBtn, () -> selectTab(3));
        rebindButton(mruNavBtn, () -> selectTab(4));
        jButton3.setText("Back");

        JPanel sidebar = UiLayout.sidebar("Memory Modules",
                jButton4, jButton5, jButton2, optimalNavBtn, mruNavBtn, jButton3);
        jTabbedPane2.setFont(KernelTheme.bodyFont());
        UiLayout.hideTabBar(jTabbedPane2);
        UiLayout.mountSidebarFrame(this, sidebar, jTabbedPane2, 920, 540);
    }

    private void selectTab(int index) {
        jTabbedPane2.setSelectedIndex(index);
        jTabbedPane2.revalidate();
        jTabbedPane2.repaint();
        toFront();
    }

    private void setupOptimalPanel() {
        optimalTabPanel = new JPanel(new BorderLayout(0, 14));
        optimalTabPanel.setBackground(KernelTheme.BG);
        optimalTabPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        optimalRefField = new JTextField();
        optimalFrameField = new JTextField();
        optimalLenField = new JTextField();
        optimalFaultsField = new JTextField();
        optimalHitsField = new JTextField();
        optimalTableHost = new JPanel(new BorderLayout());
        optimalTableHost.setBackground(KernelTheme.CARD);
        optimalTableHost.setBorder(BorderFactory.createTitledBorder("Optimal Frame Table"));

        JButton runBtn = new JButton();
        JPanel card = buildReplacementCard(
                "Optimal Page Replacement",
                optimalRefField, optimalFrameField, runBtn, "Run Optimal",
                () -> runReplacement(PageReplacementAlgorithms.Algorithm.OPTIMAL,
                        optimalRefField, optimalFrameField,
                        optimalLenField, optimalFaultsField, optimalHitsField, optimalTableHost),
                optimalLenField, optimalFaultsField, optimalHitsField);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        top.add(card, gbc);
        optimalTabPanel.add(top, BorderLayout.NORTH);
        optimalTabPanel.add(new JScrollPane(optimalTableHost), BorderLayout.CENTER);
        jTabbedPane2.addTab("Optimal", optimalTabPanel);
    }

    private void setupMruPanel() {
        mruTabPanel = new JPanel(new BorderLayout(0, 14));
        mruTabPanel.setBackground(KernelTheme.BG);
        mruTabPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        mruRefField = new JTextField();
        mruFrameField = new JTextField();
        mruLenField = new JTextField();
        mruFaultsField = new JTextField();
        mruHitsField = new JTextField();
        mruTableHost = new JPanel(new BorderLayout());
        mruTableHost.setBackground(KernelTheme.CARD);
        mruTableHost.setBorder(BorderFactory.createTitledBorder("MRU Frame Table"));

        JButton runBtn = new JButton();
        JPanel card = buildReplacementCard(
                "MRU Page Replacement (Most Recently Used)",
                mruRefField, mruFrameField, runBtn, "Run MRU",
                () -> runReplacement(PageReplacementAlgorithms.Algorithm.MRU,
                        mruRefField, mruFrameField,
                        mruLenField, mruFaultsField, mruHitsField, mruTableHost),
                mruLenField, mruFaultsField, mruHitsField);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        top.add(card, gbc);
        mruTabPanel.add(top, BorderLayout.NORTH);
        mruTabPanel.add(new JScrollPane(mruTableHost), BorderLayout.CENTER);
        jTabbedPane2.addTab("MRU", mruTabPanel);
    }

    private void fixPagingPanel() {
        JPanel card = UiLayout.formCard();
        card.setLayout(new GridBagLayout());

        UiLayout.addCardTitle(card, 0, "Paging Calculator");

        UiLayout.addAlignedFormRow(card, 1, "Physical Memory:", jTextField6);
        UiLayout.addAlignedFormRow(card, 2, "Logical Memory:", jTextField8);
        UiLayout.addAlignedFormRow(card, 3, "Page Table Entry (bytes):", jTextField7);

        GridBagConstraints gbc = UiLayout.cardGbc(4);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 12, 8, 12);
        KernelTheme.stylePrimaryButton(jButton8, "Calculate");
        KernelTheme.styleSecondaryButton(jButton9, "Reset");
        rebindButton(jButton8, this::calculatePaging);
        rebindButton(jButton9, this::resetPagingFields);
        card.add(UiLayout.centeredButtonRow(jButton8, jButton9), gbc);

        UiLayout.addCardSection(card, 5, "Results");

        UiLayout.addAlignedFormRow(card, 6, "Page Size:", jTextField9);
        jTextField9.setEditable(false);
        UiLayout.addAlignedFormRow(card, 7, "No. of Pages:", jTextField10);
        jTextField10.setEditable(false);
        UiLayout.addAlignedFormRow(card, 8, "No. of Frames:", jTextField11);
        jTextField11.setEditable(false);
        UiLayout.addAlignedFormRow(card, 9, "Page Table Size:", jTextField12);
        jTextField12.setEditable(false);

        UiLayout.mountCenteredCard(jPanel1, card);
    }

    private void fixLruPanel() {
        jPanel2.removeAll();
        jPanel2.setLayout(new BorderLayout(0, 14));
        jPanel2.setBackground(KernelTheme.BG);
        jPanel2.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JPanel card = buildReplacementCard(
                "LRU Page Replacement (Least Recently Used)",
                jTextField2, jTextField1, jButton6, "Run LRU",
                () -> runReplacement(PageReplacementAlgorithms.Algorithm.LRU,
                        jTextField2, jTextField1, jTextField3, jTextField4, jTextField5, jPanel5),
                jTextField3, jTextField4, jTextField5);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        top.add(card, gbc);
        jPanel2.add(top, BorderLayout.NORTH);

        jPanel5.setLayout(new BorderLayout());
        jPanel5.setBackground(KernelTheme.CARD);
        jPanel5.setBorder(BorderFactory.createTitledBorder("LRU Frame Table"));
        jPanel2.add(new JScrollPane(jPanel5), BorderLayout.CENTER);
    }

    private void fixFifoPanel() {
        jPanel6.removeAll();
        jPanel6.setLayout(new BorderLayout(0, 14));
        jPanel6.setBackground(KernelTheme.BG);
        jPanel6.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JPanel card = buildReplacementCard(
                "FIFO Page Replacement",
                jTextField31, jTextField32, jButton7, "Run FIFO",
                () -> runReplacement(PageReplacementAlgorithms.Algorithm.FIFO,
                        jTextField31, jTextField32, jTextField33, jTextField34, jTextField35, jPanel7),
                jTextField33, jTextField34, jTextField35);

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        top.add(card, gbc);
        jPanel6.add(top, BorderLayout.NORTH);

        jPanel7.setLayout(new BorderLayout());
        jPanel7.setBackground(KernelTheme.CARD);
        jPanel7.setBorder(BorderFactory.createTitledBorder("FIFO Frame Table"));
        jPanel6.add(new JScrollPane(jPanel7), BorderLayout.CENTER);
    }

    private JPanel buildReplacementCard(String title, JTextField refField, JTextField frameField,
            JButton runBtn, String runLabel, Runnable runAction,
            JTextField lenField, JTextField faultsField, JTextField hitsField) {
        JPanel card = UiLayout.formCard();
        card.setLayout(new GridBagLayout());

        UiLayout.addCardTitle(card, 0, title);

        refField.setPreferredSize(UiLayout.FORM_FIELD_WIDE);
        refField.setMinimumSize(UiLayout.FORM_FIELD_WIDE);
        refField.setMaximumSize(UiLayout.FORM_FIELD_WIDE);
        UiLayout.addAlignedFormRow(card, 1, "Reference String:", refField);
        UiLayout.addAlignedFormRow(card, 2, "Frame Count:", frameField);

        GridBagConstraints gbc = UiLayout.cardGbc(3);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 12, 8, 12);
        KernelTheme.stylePrimaryButton(runBtn, runLabel);
        rebindButton(runBtn, runAction);
        card.add(runBtn, gbc);
        UiLayout.normalizeCompactButton(runBtn);

        UiLayout.addCardSection(card, 4, "Statistics");

        lenField.setEditable(false);
        faultsField.setEditable(false);
        hitsField.setEditable(false);
        UiLayout.addAlignedFormRow(card, 5, "String Length:", lenField);
        UiLayout.addAlignedFormRow(card, 6, "Page Faults:", faultsField);
        UiLayout.addAlignedFormRow(card, 7, "Page Hits:", hitsField);

        return card;
    }

    private void setDefaultPagingInputs() {
        if (jTextField6.getText().trim().isEmpty()) {
            jTextField6.setText("4 GB");
        }
        if (jTextField8.getText().trim().isEmpty()) {
            jTextField8.setText("8 GB");
        }
        if (jTextField7.getText().trim().isEmpty()) {
            jTextField7.setText("4");
        }
    }

    private void resetPagingFields() {
        jTextField6.setText("4 GB");
        jTextField8.setText("8 GB");
        jTextField7.setText("4");
        jTextField9.setText("");
        jTextField10.setText("");
        jTextField11.setText("");
        jTextField12.setText("");
    }

    private void rebindButton(JButton button, Runnable action) {
        for (java.awt.event.ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }
        button.addActionListener(e -> action.run());
    }

    private void setDefaultReplacementInputs() {
        if (jTextField2.getText().trim().isEmpty()) {
            jTextField2.setText(DEFAULT_REF_STRING);
        }
        if (jTextField1.getText().trim().isEmpty()) {
            jTextField1.setText("3");
        }
        if (jTextField31.getText().trim().isEmpty()) {
            jTextField31.setText(DEFAULT_REF_STRING);
        }
        if (jTextField32.getText().trim().isEmpty()) {
            jTextField32.setText("3");
        }
        if (optimalRefField != null && optimalRefField.getText().trim().isEmpty()) {
            optimalRefField.setText(DEFAULT_REF_STRING);
            optimalFrameField.setText("3");
        }
        if (mruRefField != null && mruRefField.getText().trim().isEmpty()) {
            mruRefField.setText(DEFAULT_REF_STRING);
            mruFrameField.setText("3");
        }
    }

    private int[] parseReferenceString(String refString) {
        String[] references = refString.split(",");
        int[] refArray = new int[references.length];
        for (int i = 0; i < references.length; i++) {
            refArray[i] = Integer.parseInt(references[i].trim());
        }
        return refArray;
    }

    private void runReplacement(PageReplacementAlgorithms.Algorithm algorithm,
            JTextField refField, JTextField frameField,
            JTextField lenField, JTextField faultsField, JTextField hitsField,
            JPanel tableHost) {
        String refString = refField.getText().trim();
        if (refString.isEmpty()) {
            return;
        }
        int frames;
        try {
            frames = Integer.parseInt(frameField.getText().trim());
            if (frames <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            return;
        }
        try {
            int[] refs = parseReferenceString(refString);
            PageReplacementAlgorithms.Result result =
                    PageReplacementAlgorithms.simulate(refs, frames, algorithm);
            lenField.setText(Integer.toString(result.length));
            faultsField.setText(Integer.toString(result.pageFaults));
            hitsField.setText(Integer.toString(result.pageHits));
            showReplacementTable(tableHost, result.tableModel, algorithm.getLabel());
        } catch (NumberFormatException ignored) {
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void showReplacementTable(JPanel host, DefaultTableModel tableModel, String title) {
        JTable table = new JTable(tableModel);
        KernelTheme.styleTable(table);
        host.removeAll();
        host.setLayout(new BorderLayout());
        host.setBorder(BorderFactory.createTitledBorder(title + " Frame Table"));
        host.add(new JScrollPane(table), BorderLayout.CENTER);
        host.revalidate();
        host.repaint();
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jButton8 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jTextField31 = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        jTextField32 = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        jTextField33 = new javax.swing.JTextField();
        jLabel37 = new javax.swing.JLabel();
        jTextField34 = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        jTextField35 = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();

        jLabel2.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel2.setForeground(new Color(40, 44, 52));
        jLabel2.setText("Refrence String");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel4.setBackground(new Color(245, 245, 250));
        jPanel4.setForeground(new Color(245, 245, 250));

        jLabel1.setBackground(new Color(40, 44, 52));
        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel1.setForeground(new Color(40, 44, 52));
        jLabel1.setText("Non-Countiguous Memory Allocation");

        jButton2.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton2.setText("FIFO");
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
        jButton4.setText("Paging");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton5.setText("LRU");
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
                        .addComponent(jLabel1))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addGap(18, 18, 18)
                .addComponent(jButton5)
                .addGap(29, 29, 29)
                .addComponent(jButton2)
                .addGap(94, 94, 94)
                .addComponent(jButton3)
                .addGap(35, 35, 35))
        );

        jTabbedPane2.setBackground(new Color(245, 245, 250));
        jTabbedPane2.setForeground(new Color(40, 44, 52));
        jTabbedPane2.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N

        jPanel1.setBackground(new Color(245, 245, 250));

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel3.setForeground(new Color(40, 44, 52));
        jLabel3.setText("logical Address");

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel7.setForeground(new Color(40, 44, 52));
        jLabel7.setText("Physical Address");

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel8.setForeground(new Color(40, 44, 52));
        jLabel8.setText("Page Table Entry Size");

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel9.setForeground(new Color(40, 44, 52));
        jLabel9.setText("Page  Size");

        jButton8.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton8.setText("Calculate");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel10.setForeground(new Color(40, 44, 52));
        jLabel10.setText("No. of pages");

        jLabel11.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel11.setForeground(new Color(40, 44, 52));
        jLabel11.setText("No. of Frames");

        jLabel12.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        jLabel12.setForeground(new Color(40, 44, 52));
        jLabel12.setText("Size of page table");

        jButton9.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton9.setText("RESET");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addGap(46, 46, 46)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField9, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                            .addComponent(jTextField10)
                            .addComponent(jTextField11, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField12, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(133, 133, 133)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(764, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(41, 41, 41)
                    .addComponent(jLabel7)
                    .addContainerGap(991, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton8)
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addComponent(jButton9)
                .addGap(26, 26, 26))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(59, 59, 59)
                    .addComponent(jLabel7)
                    .addContainerGap(416, Short.MAX_VALUE)))
        );

        jTabbedPane2.addTab("Paging", jPanel1);

        jPanel2.setBackground(new Color(245, 245, 250));

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel4.setForeground(new Color(40, 44, 52));
        jLabel4.setText("Refrence String");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel5.setForeground(new Color(40, 44, 52));
        jLabel5.setText("Frame NO.");

        jButton6.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton6.setText("LRU");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel32.setForeground(new Color(40, 44, 52));
        jLabel32.setText("Len of String");

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel6.setForeground(new Color(40, 44, 52));
        jLabel6.setText("Page Fault");

        jLabel33.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel33.setForeground(new Color(40, 44, 52));
        jLabel33.setText("Page Hit");

        jTable1.setBackground(new Color(245, 245, 250));
        jTable1.setForeground(new Color(40, 44, 52));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 694, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel33)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel32)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jButton6)
                        .addGap(35, 35, 35)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("LRU", jPanel2);

        jPanel6.setBackground(new Color(245, 245, 250));

        jLabel34.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel34.setForeground(new Color(40, 44, 52));
        jLabel34.setText("Refrence String");

        jLabel35.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel35.setForeground(new Color(40, 44, 52));
        jLabel35.setText("Frame NO.");

        jButton7.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jButton7.setText("FIFO");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jLabel36.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel36.setForeground(new Color(40, 44, 52));
        jLabel36.setText("Page Fault");

        jLabel37.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel37.setForeground(new Color(40, 44, 52));
        jLabel37.setText("Len of String");

        jLabel38.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel38.setForeground(new Color(40, 44, 52));
        jLabel38.setText("Page Hit");

        jTable3.setBackground(new Color(245, 245, 250));
        jTable3.setForeground(new Color(40, 44, 52));
        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 729, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(335, Short.MAX_VALUE)))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel35)
                            .addComponent(jLabel34))
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(93, 93, 93)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel38)
                            .addComponent(jLabel36))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(36, 36, 36)
                    .addComponent(jLabel37)
                    .addContainerGap(977, Short.MAX_VALUE)))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(jTextField32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton7)
                .addGap(40, 40, 40)
                .addComponent(jTextField33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(jTextField34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38)
                    .addComponent(jTextField35, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addGap(194, 194, 194)
                    .addComponent(jLabel37)
                    .addContainerGap(271, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1137, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 492, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane2.addTab("FIFO", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 1137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        selectTab(2);
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        NavigationHelper.back(this);
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        selectTab(0);
        setDefaultPagingInputs();
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        selectTab(1);
    }

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
        runReplacement(PageReplacementAlgorithms.Algorithm.LRU,
                jTextField2, jTextField1, jTextField3, jTextField4, jTextField5, jPanel5);
    }

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
        runReplacement(PageReplacementAlgorithms.Algorithm.FIFO,
                jTextField31, jTextField32, jTextField33, jTextField34, jTextField35, jPanel7);
    }

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        calculatePaging();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {
        resetPagingFields();
    }

    private void calculatePaging() {
        try {
            String physicalMemoryStr = jTextField6.getText().trim();
            String logicalMemoryStr = jTextField8.getText().trim();
            String entryStr = jTextField7.getText().trim();

            if (physicalMemoryStr.isEmpty() || logicalMemoryStr.isEmpty() || entryStr.isEmpty()) {
                return;
            }

            int pageTableEntrySize = Integer.parseInt(entryStr);
            if (pageTableEntrySize <= 0) {
                return;
            }

            long physicalMemoryBits = convertToBits(physicalMemoryStr);
            long logicalMemoryBits = convertToBits(logicalMemoryStr);
            int pageSizeBits = KernelConfig.getInstance().getPageSizeBits();
            long pageSizeBytes = 1L << pageSizeBits;

            long numPages = logicalMemoryBits / (pageSizeBytes * 8L);
            long numFrames = physicalMemoryBits / (pageSizeBytes * 8L);

            if (numPages <= 0 || numFrames <= 0) {
                return;
            }

            long sizeOfPageTable = numPages * pageTableEntrySize;

            jTextField9.setText("2^" + pageSizeBits + " bytes (" + pageSizeBytes + " B)");
            jTextField10.setText(String.valueOf(numPages));
            jTextField11.setText(String.valueOf(numFrames));
            jTextField12.setText(sizeOfPageTable + " bytes");

        } catch (NumberFormatException ignored) {
        } catch (IllegalArgumentException ignored) {
        } catch (Exception ignored) {
        }
    }

private long convertToBits(String memoryStr) {
    if (memoryStr == null || memoryStr.isEmpty()) {
        throw new IllegalArgumentException("Memory value is empty.");
    }
    String normalized = memoryStr.trim().replaceAll("\\s+", " ");
    String[] parts = normalized.split(" ");
    if (parts.length < 2) {
        throw new IllegalArgumentException("Invalid memory format: " + memoryStr);
    }
    long size = Long.parseLong(parts[0]);
    String unit = parts[1].toUpperCase();
    switch (unit) {
        case "GB":
            return size * (1L << 30) * 8;
        case "MB":
            return size * (1L << 20) * 8;
        case "KB":
            return size * (1L << 10) * 8;
        default:
            throw new IllegalArgumentException("Unit must be GB, MB, or KB");
    }
}

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new noncontigious().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField31;
    private javax.swing.JTextField jTextField32;
    private javax.swing.JTextField jTextField33;
    private javax.swing.JTextField jTextField34;
    private javax.swing.JTextField jTextField35;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
