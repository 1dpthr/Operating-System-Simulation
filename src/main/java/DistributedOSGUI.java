import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.rmi.Naming;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class DistributedOSGUI extends JFrame {

    private final JTextField hostField = new JTextField("localhost");
    private final JComboBox<String> processCombo = ProcessPicker.createCombo(false);
    private final JTextField arrivalField = new JTextField("0");
    private final JTextField burstField = new JTextField("5");
    private final JTextArea logArea = new JTextArea(5, 30);

    private JButton startRmiBtn;
    private JButton startSocketBtn;
    private JButton rmiCreateBtn;
    private JButton socketCreateBtn;
    private JButton fetchQueuesBtn;
    private JButton clearLogBtn;

    public DistributedOSGUI() {
        super(KernelTheme.OS_NAME + " — Distributed OS");
        buildUi();
        KernelTheme.applyToWindow(this);
        wireButtons();
        processCombo.addActionListener(e -> fillFromSelectedProcess());
        ProcessPicker.registerRefreshCallback(() -> ProcessPicker.refresh(processCombo, false));
        UiLayout.applyCompactWindow(this, 400, 520, 360, 460);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        NavigationHelper.addBackBar(this, () -> NavigationHelper.back(this));
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        KernelTheme.stylePanel(root);

        JPanel card = UiLayout.formCard();
        UiLayout.addCardTitle(card, 0, "Distributed Kernel");

        JLabel hint = new JLabel("Server + Client in one panel — start server, then create remote process.",
                SwingConstants.CENTER);
        hint.setFont(KernelTheme.smallFont());
        hint.setForeground(KernelTheme.TEXT_MUTED);
        GridBagConstraints gbc = UiLayout.cardGbc(1);
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 10, 8, 10);
        card.add(hint, gbc);

        UiLayout.addCardSection(card, 2, "Server Mode (this machine)");
        startRmiBtn = actionButton("Start RMI Server", true);
        startSocketBtn = actionButton("Start Socket Server", false);
        addCardRow(card, 3, UiLayout.centeredButtonRow(startRmiBtn, startSocketBtn));

        UiLayout.addCardSection(card, 4, "Client Mode — Remote Process");
        styleField(hostField);
        styleField(arrivalField);
        styleField(burstField);
        arrivalField.setEditable(false);
        burstField.setEditable(false);
        UiLayout.addAlignedFormRow(card, 5, "Server Host:", hostField);
        UiLayout.addStackedFormRow(card, 6, "Select Process:", processCombo);
        UiLayout.addAlignedFormRow(card, 7, "Arrival:", arrivalField);
        UiLayout.addAlignedFormRow(card, 8, "Burst:", burstField);

        rmiCreateBtn = actionButton("Create via RMI", true);
        socketCreateBtn = actionButton("Create via Socket", false);
        addCardRow(card, 9, UiLayout.centeredButtonRow(rmiCreateBtn, socketCreateBtn));

        fetchQueuesBtn = actionButton("Fetch Remote Queues", false);
        addCardRow(card, 10, fetchQueuesBtn);

        UiLayout.addCardSection(card, 11, "Distributed Log");
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        KernelTheme.styleTextArea(logArea);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(UiLayout.CARD_MAX_WIDTH - 24, 100));
        logScroll.setBorder(BorderFactory.createLineBorder(KernelTheme.BORDER, 1, true));

        gbc = UiLayout.cardGbc(12);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 10, 8, 10);
        card.add(logScroll, gbc);

        clearLogBtn = actionButton("Clear Log", false);
        UiLayout.normalizeToolbarButton(clearLogBtn);
        addCardRow(card, 13, clearLogBtn);

        UiLayout.mountCenteredCard(root, card);
        setContentPane(root);
        log("Start server, then create a remote process.");
    }

    private JButton actionButton(String text, boolean primary) {
        JButton btn = new JButton();
        if (primary) {
            KernelTheme.stylePrimaryButton(btn, text);
        } else {
            KernelTheme.styleSecondaryButton(btn, text);
        }
        UiLayout.normalizeActionButton(btn);
        return btn;
    }

    private void styleField(JTextField field) {
        UiLayout.applyFormFieldSize(field, UiLayout.FORM_FIELD);
        KernelTheme.styleTextField(field);
    }

    private void addCardRow(JPanel card, int row, java.awt.Component component) {
        GridBagConstraints gbc = UiLayout.cardGbc(row);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 10, 4, 10);
        card.add(component, gbc);
    }

    private void wireButtons() {
        rebind(startRmiBtn, e -> log(DistributedKernelServer.startServer()));
        rebind(startSocketBtn, e -> log(SocketIPCServer.getInstance().start(SocketIPCServer.DEFAULT_PORT)));
        rebind(rmiCreateBtn, e -> createViaRmi());
        rebind(socketCreateBtn, e -> createViaSocket());
        rebind(fetchQueuesBtn, e -> fetchQueues());
        rebind(clearLogBtn, e -> {
            logArea.setText("");
            log("Log cleared.");
        });
    }

    private void rebind(JButton button, java.awt.event.ActionListener action) {
        for (java.awt.event.ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }
        button.addActionListener(action);
        button.setEnabled(true);
    }

    private void fillFromSelectedProcess() {
        ProcessPicker.getSelectedPid(processCombo).flatMap(
                pid -> ProcessRegistry.getInstance().find(pid)).ifPresent(pcb -> {
            arrivalField.setText(String.valueOf(pcb.getArrivalTime()));
            burstField.setText(String.valueOf(pcb.getBurstTime()));
        });
    }

    private Optional<ProcessControlBlock> selectedProcess() {
        return ProcessPicker.getSelectedPid(processCombo).flatMap(
                pid -> ProcessRegistry.getInstance().find(pid));
    }

    private void createViaRmi() {
        Optional<ProcessControlBlock> pcbOpt = selectedProcess();
        if (pcbOpt.isEmpty()) {
            log("Dropdown se process select karein (pehle Process Management se create karein).");
            return;
        }
        ProcessControlBlock pcb = pcbOpt.get();
        try {
            String host = hostField.getText().trim();
            String url = "rmi://" + host + ":" + DistributedKernelServer.RMI_PORT
                    + "/" + DistributedKernelServer.BIND_NAME;
            RemoteProcessService stub = (RemoteProcessService) Naming.lookup(url);
            String owner = System.getProperty("user.name", "remote-client");
            String result = stub.createRemoteProcess(
                    pcb.getProcessName(),
                    pcb.getArrivalTime(),
                    pcb.getBurstTime(),
                    owner);
            log("RMI: " + result + " (from P" + pcb.getProcessId() + ")");
        } catch (Exception ex) {
            log("RMI error: " + ex.getMessage());
        }
    }

    private void createViaSocket() {
        Optional<ProcessControlBlock> pcbOpt = selectedProcess();
        if (pcbOpt.isEmpty()) {
            log("Dropdown se process select karein (pehle Process Management se create karein).");
            return;
        }
        ProcessControlBlock pcb = pcbOpt.get();
        String host = hostField.getText().trim();
        String cmd = "CREATE|" + pcb.getProcessName() + "|"
                + pcb.getArrivalTime() + "|"
                + pcb.getBurstTime() + "|"
                + System.getProperty("user.name", "remote-client");
        log("Socket: " + SocketIPCServer.sendCommand(host, SocketIPCServer.DEFAULT_PORT, cmd)
                + " (from P" + pcb.getProcessId() + ")");
    }

    private void fetchQueues() {
        try {
            String host = hostField.getText().trim();
            String url = "rmi://" + host + ":" + DistributedKernelServer.RMI_PORT
                    + "/" + DistributedKernelServer.BIND_NAME;
            RemoteProcessService stub = (RemoteProcessService) Naming.lookup(url);
            log("Queues from server:\n" + stub.getQueueSummary());
        } catch (Exception ex) {
            String resp = SocketIPCServer.sendCommand(hostField.getText().trim(), SocketIPCServer.DEFAULT_PORT, "QUEUE");
            log("Queues (socket): " + resp);
        }
    }

    private void log(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
