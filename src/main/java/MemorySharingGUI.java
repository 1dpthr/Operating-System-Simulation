import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Interactive OS Memory Sharing lab: shmget/shmat, mmap, shared libraries (.so).
 */
public class MemorySharingGUI extends JFrame {

    private final MemorySharingDiagramPanel diagram = new MemorySharingDiagramPanel();
    private final JTextArea logArea = new JTextArea(4, 40);

    private final JTextField shmNameField = new JTextField("SHM_GLOBAL");
    private final JTextField shmSizeField = new JTextField("4096");
    private final JComboBox<String> shmProcessCombo = ProcessPicker.createCombo(false);
    private final JTextField shmDataField = new JTextField("Hello from Process A");

    private final JTextField mmapFileField = new JTextField("data.bin");
    private final JTextField mmapSizeField = new JTextField("4096");
    private final JComboBox<String> mmapProcessCombo = ProcessPicker.createCombo(false);
    private final JTextField mmapDataField = new JTextField("mapped write");

    private final JComboBox<String> libCombo = new JComboBox<>(new String[]{"libc.so", "libm.so", "libpthread.so"});
    private final JComboBox<String> libProcessCombo = ProcessPicker.createCombo(false);

    public MemorySharingGUI() {
        super(KernelTheme.OS_NAME + " — Memory Sharing");
        buildUi();
        KernelTheme.applyToWindow(this);
        wireActions();
        MemorySharingManager.getInstance().addListener(() -> diagram.refresh());
        ProcessPicker.registerRefreshCallback(() -> {
            ProcessPicker.refresh(shmProcessCombo, false);
            ProcessPicker.refresh(mmapProcessCombo, false);
            ProcessPicker.refresh(libProcessCombo, false);
        });
        diagram.setMode(MemorySharingManager.SharingMode.SHARED_MEMORY);
        UiLayout.applyWorkspaceWindow(this, 960, 640, 820, 560);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        log("Memory Sharing module ready — diagram updates live on each action.");
        NavigationHelper.addBackBar(this, () -> NavigationHelper.back(this));
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(KernelTheme.BG);
        root.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel header = new JLabel("Memory Sharing");
        header.setFont(KernelTheme.headingFont());
        header.setForeground(KernelTheme.TEXT);
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(KernelTheme.bodyFont());
        tabs.addTab("1. Shared Memory", buildShmControls());
        tabs.addTab("2. mmap (Files)", buildMmapControls());
        tabs.addTab("3. Shared Libraries", buildLibControls());
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 0) {
                diagram.setMode(MemorySharingManager.SharingMode.SHARED_MEMORY);
                diagram.setHighlightSegment(shmNameField.getText());
            } else if (idx == 1) {
                diagram.setMode(MemorySharingManager.SharingMode.MMAP);
                diagram.setHighlightFile(mmapFileField.getText());
            } else {
                diagram.setMode(MemorySharingManager.SharingMode.SHARED_LIBRARY);
                diagram.setHighlightLib((String) libCombo.getSelectedItem());
            }
            diagram.refresh();
        });

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(300, 0));
        left.add(new JScrollPane(tabs), BorderLayout.CENTER);

        JPanel diagramPanel = new JPanel(new BorderLayout());
        diagramPanel.setOpaque(true);
        diagramPanel.setBackground(KernelTheme.CARD);
        diagramPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(KernelTheme.BORDER),
                "Memory Map Diagram",
                0, 0,
                KernelTheme.bodyFont(),
                KernelTheme.TEXT));
        diagramPanel.add(diagram, BorderLayout.CENTER);

        center.add(left, BorderLayout.WEST);
        center.add(diagramPanel, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        root.add(buildLogPanel(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildShmControls() {
        JPanel card = UiLayout.formCard();
        UiLayout.addCardTitle(card, 0, "shmget / shmat / shmdt");
        styleField(shmNameField);
        styleField(shmSizeField);
        styleField(shmDataField);
        styleCombo(shmProcessCombo);
        UiLayout.addAlignedFormRow(card, 1, "Segment Name:", shmNameField);
        UiLayout.addAlignedFormRow(card, 2, "Size (bytes):", shmSizeField);
        UiLayout.addAlignedFormRow(card, 3, "Process:", shmProcessCombo);
        addRow(card, 4, btn("shmget — Create Segment", true, this::doShmget),
                btn("shmat — Attach", false, this::doShmat));
        addRow(card, 5, btn("Write to SHM", true, this::doShmWrite),
                btn("Read from SHM", false, this::doShmRead));
        addRow(card, 6, btn("shmdt — Detach", false, this::doShmdt), null);
        return card;
    }

    private JPanel buildMmapControls() {
        JPanel card = UiLayout.formCard();
        UiLayout.addCardTitle(card, 0, "open + mmap(MAP_SHARED)");
        styleField(mmapFileField);
        styleField(mmapSizeField);
        styleField(mmapDataField);
        styleCombo(mmapProcessCombo);
        UiLayout.addAlignedFormRow(card, 1, "File Name:", mmapFileField);
        UiLayout.addAlignedFormRow(card, 2, "File Size (B):", mmapSizeField);
        UiLayout.addAlignedFormRow(card, 3, "Process:", mmapProcessCombo);
        addRow(card, 4, btn("mmap — Map File", true, this::doMmap), null);
        addRow(card, 5, btn("Write via Map", true, this::doMmapWrite),
                btn("Read via Map", false, this::doMmapRead));
        return card;
    }

    private JPanel buildLibControls() {
        JPanel card = UiLayout.formCard();
        UiLayout.addCardTitle(card, 0, "Shared Library (.so / .dll)");
        KernelTheme.styleComboBox(libCombo);
        styleCombo(libProcessCombo);
        UiLayout.addAlignedFormRow(card, 1, "Library:", libCombo);
        UiLayout.addAlignedFormRow(card, 2, "Process:", libProcessCombo);
        addRow(card, 3, btn("Load Library (dlopen)", true, this::doLoadLib), null);
        return card;
    }

    private JPanel buildLogPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("Activity Log");
        lbl.setFont(KernelTheme.bodyFont());
        p.add(lbl, BorderLayout.NORTH);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        KernelTheme.styleTextArea(logArea);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(0, 72));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void wireActions() {
        shmNameField.addActionListener(e -> diagram.setHighlightSegment(shmNameField.getText()));
        mmapFileField.addActionListener(e -> diagram.setHighlightFile(mmapFileField.getText()));
        libCombo.addActionListener(e -> diagram.setHighlightLib((String) libCombo.getSelectedItem()));
    }

    private void doShmget() {
        try {
            int size = Integer.parseInt(shmSizeField.getText().trim());
            int id = MemorySharingManager.getInstance().shmget(shmNameField.getText().trim(), size);
            diagram.setHighlightSegment(shmNameField.getText());
            log("[shmget] shmid=" + id + " for '" + shmNameField.getText().trim() + "' (" + size + " bytes)");
        } catch (NumberFormatException e) {
            log("[shmget] Invalid size.");
        }
    }

    private void doShmat() {
        ProcessPicker.getSelectedPid(shmProcessCombo).ifPresentOrElse(pid -> {
            String name = shmNameField.getText().trim();
            if (MemorySharingManager.getInstance().shmat(name, pid)) {
                SharedMemoryIPC.getInstance().attach(name, pid);
                diagram.setHighlightSegment(name);
                log("[shmat] P" + pid + " attached → virtual mapping created (see diagram)");
            } else {
                log("[shmat] Failed — process not found.");
            }
        }, () -> log("[shmat] Select a process from dropdown."));
    }

    private void doShmdt() {
        ProcessPicker.getSelectedPid(shmProcessCombo).ifPresentOrElse(pid -> {
            String name = shmNameField.getText().trim();
            if (MemorySharingManager.getInstance().shmdt(name, pid)) {
                log("[shmdt] P" + pid + " detached from '" + name + "'");
            } else {
                log("[shmdt] Process was not attached.");
            }
        }, () -> log("[shmdt] Select a process."));
    }

    private void doShmWrite() {
        ProcessPicker.getSelectedPid(shmProcessCombo).ifPresentOrElse(pid -> {
            String name = shmNameField.getText().trim();
            String data = shmDataField.getText().trim();
            if (MemorySharingManager.getInstance().shmWrite(name, pid, data)) {
                SharedMemoryIPC.getInstance().write(name, pid, data);
                log("[SHM write] P" + pid + " → physical block: \"" + data + "\"");
            } else {
                log("[SHM write] Attach first (shmat).");
            }
        }, () -> log("[SHM write] Select a process."));
    }

    private void doShmRead() {
        ProcessPicker.getSelectedPid(shmProcessCombo).ifPresentOrElse(pid -> {
            String content = MemorySharingManager.getInstance().shmRead(shmNameField.getText().trim(), pid);
            if (content == null) {
                log("[SHM read] Attach first (shmat).");
            } else {
                log("[SHM read] P" + pid + " sees: " + content);
            }
        }, () -> log("[SHM read] Select a process."));
    }

    private void doMmap() {
        ProcessPicker.getSelectedPid(mmapProcessCombo).ifPresentOrElse(pid -> {
            try {
                long size = Long.parseLong(mmapSizeField.getText().trim());
                String file = mmapFileField.getText().trim();
                int fd = MemorySharingManager.getInstance().openAndMap(file, pid, size);
                diagram.setHighlightFile(file);
                log("[mmap] fd=" + fd + " | " + file + " mapped into P" + pid + " (MAP_SHARED)");
            } catch (NumberFormatException e) {
                log("[mmap] Invalid file size.");
            }
        }, () -> log("[mmap] Select a process."));
    }

    private void doMmapWrite() {
        ProcessPicker.getSelectedPid(mmapProcessCombo).ifPresentOrElse(pid -> {
            String file = mmapFileField.getText().trim();
            String data = mmapDataField.getText().trim();
            if (MemorySharingManager.getInstance().mmapWrite(file, pid, data)) {
                log("[mmap write] P" + pid + " wrote to mapped " + file + ": " + data);
            } else {
                log("[mmap write] Map the file first.");
            }
        }, () -> log("[mmap write] Select a process."));
    }

    private void doMmapRead() {
        ProcessPicker.getSelectedPid(mmapProcessCombo).ifPresentOrElse(pid -> {
            String content = MemorySharingManager.getInstance().mmapRead(mmapFileField.getText().trim(), pid);
            if (content == null) {
                log("[mmap read] Map the file first.");
            } else {
                log("[mmap read] P" + pid + ": " + content);
            }
        }, () -> log("[mmap read] Select a process."));
    }

    private void doLoadLib() {
        ProcessPicker.getSelectedPid(libProcessCombo).ifPresentOrElse(pid -> {
            String lib = (String) libCombo.getSelectedItem();
            if (MemorySharingManager.getInstance().loadLibrary(lib, pid)) {
                diagram.setHighlightLib(lib);
                log("[dlopen] P" + pid + " loaded " + lib);
                log("         " + MemorySharingManager.getInstance().getLibrarySummary(lib));
            }
        }, () -> log("[dlopen] Select a process."));
    }

    private void log(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        diagram.refresh();
    }

    private void styleField(JTextField f) {
        KernelTheme.styleTextField(f);
        UiLayout.applyFormFieldSize(f, UiLayout.FORM_FIELD_WIDE);
    }

    private void styleCombo(JComboBox<String> c) {
        KernelTheme.styleComboBox(c);
        UiLayout.applyFormFieldSize(c, UiLayout.FORM_FIELD_WIDE);
    }

    private JButton btn(String text, boolean primary, Runnable action) {
        JButton b = new JButton();
        if (primary) {
            KernelTheme.stylePrimaryButton(b, text);
        } else {
            KernelTheme.styleSecondaryButton(b, text);
        }
        UiLayout.normalizeActionButton(b);
        b.addActionListener(e -> action.run());
        return b;
    }

    private void addRow(JPanel card, int row, JButton a, JButton b) {
        GridBagConstraints gbc = UiLayout.cardGbc(row);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 10, 4, 10);
        if (b == null) {
            card.add(a, gbc);
        } else {
            card.add(UiLayout.centeredButtonRow(a, b), gbc);
        }
    }


}
