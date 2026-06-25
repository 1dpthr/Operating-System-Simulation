import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Optional;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.border.EmptyBorder;

public class ProcessCommunicationGUI extends JFrame {

    private static final String CARD_MESSAGES = "messages";
    private static final String CARD_SHARED = "shared";
    private static final String CARD_NETWORK = "network";

    private final JComboBox<String> fromCombo = ProcessPicker.createCombo(false);
    private final JComboBox<String> toCombo = ProcessPicker.createCombo(false);
    private final JTextField messageField = new JTextField();
    private final JComboBox<String> receiveCombo = ProcessPicker.createCombo(false);
    private final JTextField segmentField = new JTextField();
    private final JComboBox<String> shmProcessCombo = ProcessPicker.createCombo(false);
    private final JTextField sharedDataField = new JTextField();
    private final JTextField socketHostField = new JTextField("localhost");
    private final JTextField socketMsgField = new JTextField();
    private final JTextField rmiHostField = new JTextField("localhost");
    private final JTextArea logArea = new JTextArea(5, 40);

    private JButton messagesNavBtn;
    private JButton sharedNavBtn;
    private JButton networkNavBtn;
    private JButton sendMsgBtn;
    private JButton recvBtn;
    private JButton writeBtn;
    private JButton readBtn;
    private JButton attachBtn;
    private JButton startSocketBtn;
    private JButton sendSocketBtn;
    private JButton startRmiBtn;
    private JButton fetchRmiBtn;
    private JButton clearLogBtn;

    private JLabel sectionTitle;
    private JLabel sectionHint;
    private JPanel cardHost;
    private CardLayout cardLayout;

    public ProcessCommunicationGUI() {
        super(KernelTheme.OS_NAME + " — Process Communication");
        buildUi();
        KernelTheme.applyToWindow(this);
        wireButtons();
        ProcessPicker.registerRefreshCallback(() -> {
            ProcessPicker.refresh(fromCombo, false);
            ProcessPicker.refresh(toCombo, false);
            ProcessPicker.refresh(receiveCombo, false);
            ProcessPicker.refresh(shmProcessCombo, false);
        });
        showSection(CARD_MESSAGES);
        UiLayout.applyWorkspaceWindow(this, 700, 520, 600, 460);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        NavigationHelper.addBackBar(this, () -> NavigationHelper.back(this));
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(KernelTheme.BG);
        root.add(buildTopBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 0, 10, 0));
        body.add(buildSidebar(), BorderLayout.WEST);
        body.add(buildMainPanel(), BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        root.add(buildLogPanel(), BorderLayout.SOUTH);
        setContentPane(root);
        log("IPC module ready.");
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(KernelTheme.PRIMARY);
        bar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Process Communication");
        title.setFont(KernelTheme.headingFont());
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.WEST);
        return bar;
    }

    private JPanel buildSidebar() {
        messagesNavBtn = navButton("Messages");
        sharedNavBtn = navButton("Shared Memory");
        networkNavBtn = navButton("Socket / RMI");
        return UiLayout.sidebar("IPC Modules", messagesNavBtn, sharedNavBtn, networkNavBtn);
    }

    private JButton navButton(String label) {
        JButton btn = new JButton();
        KernelTheme.styleSecondaryButton(btn, label);
        UiLayout.normalizeMenuButton(btn);
        return btn;
    }

    private JPanel buildMainPanel() {
        JPanel main = new JPanel(new BorderLayout(0, 8));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(0, 14, 0, 14));

        JPanel intro = new JPanel(new GridBagLayout());
        intro.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        sectionTitle = new JLabel("Message Passing");
        sectionTitle.setFont(KernelTheme.headingFont());
        sectionTitle.setForeground(KernelTheme.TEXT);
        intro.add(sectionTitle, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        sectionHint = new JLabel("Send and receive messages between process mailboxes.");
        sectionHint.setFont(KernelTheme.smallFont());
        sectionHint.setForeground(KernelTheme.TEXT_MUTED);
        intro.add(sectionHint, gbc);
        main.add(intro, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardHost = new JPanel(cardLayout);
        cardHost.setOpaque(false);
        cardHost.add(wrapSection(buildMessageCard()), CARD_MESSAGES);
        cardHost.add(wrapSection(buildSharedMemoryCard()), CARD_SHARED);
        cardHost.add(wrapSection(buildNetworkCard()), CARD_NETWORK);
        main.add(cardHost, BorderLayout.CENTER);
        return main;
    }

    private JPanel wrapSection(JPanel card) {
        JPanel host = new JPanel(new GridBagLayout());
        host.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        card.setMaximumSize(new Dimension(UiLayout.scaledCardMaxWidth(), Integer.MAX_VALUE));
        host.add(card, gbc);
        return host;
    }

    private JPanel buildMessageCard() {
        JPanel card = UiLayout.formCard();
        UiLayout.addCardTitle(card, 0, "Mailbox");

        styleCombo(fromCombo);
        styleCombo(toCombo);
        styleWideField(messageField);
        styleCombo(receiveCombo);

        addInlinePidRow(card, 1, "From:", fromCombo, "To:", toCombo);
        UiLayout.addAlignedFormRow(card, 2, "Message:", messageField);

        sendMsgBtn = actionButton("Send Message", true);
        addCardActions(card, 3, sendMsgBtn);

        UiLayout.addCardSection(card, 4, "Receive Inbox");
        UiLayout.addAlignedFormRow(card, 5, "My Process:", receiveCombo);
        recvBtn = actionButton("Receive Message", false);
        addCardActions(card, 6, recvBtn);
        return card;
    }

    private JPanel buildSharedMemoryCard() {
        JPanel card = UiLayout.formCard();
        UiLayout.addCardTitle(card, 0, "Shared Memory Segment (shmat / shmdt)");

        styleWideField(segmentField);
        styleCombo(shmProcessCombo);
        styleWideField(sharedDataField);
        segmentField.setText("SHM_GLOBAL");
        segmentField.setToolTipText("Shared segment name — processes must attach first");
        shmProcessCombo.setToolTipText("Process attached to this segment");
        sharedDataField.setToolTipText("Data written to shared memory");

        UiLayout.addAlignedFormRow(card, 1, "Segment Name:", segmentField);
        UiLayout.addAlignedFormRow(card, 2, "Process:", shmProcessCombo);
        attachBtn = actionButton("Attach to Segment", false);
        addCardActions(card, 3, attachBtn);
        UiLayout.addAlignedFormRow(card, 4, "Write Data:", sharedDataField);

        writeBtn = actionButton("Write (SHM)", true);
        readBtn = actionButton("Read (SHM)", false);
        addCardActions(card, 5, writeBtn, readBtn);
        return card;
    }

    private JPanel buildNetworkCard() {
        JPanel stack = new JPanel();
        stack.setLayout(new javax.swing.BoxLayout(stack, javax.swing.BoxLayout.Y_AXIS));
        stack.setOpaque(false);

        JPanel socketCard = UiLayout.formCard();
        UiLayout.addCardTitle(socketCard, 0, "Socket IPC");
        styleWideField(socketHostField);
        styleWideField(socketMsgField);
        socketHostField.setToolTipText("Server hostname or IP");
        UiLayout.addAlignedFormRow(socketCard, 1, "Server Host:", socketHostField);
        UiLayout.addAlignedFormRow(socketCard, 2, "Socket Message:", socketMsgField);
        startSocketBtn = actionButton("Start Server", false);
        sendSocketBtn = actionButton("Send via Socket", true);
        addCardActions(socketCard, 3, startSocketBtn, sendSocketBtn);

        JPanel rmiCard = UiLayout.formCard();
        UiLayout.addCardTitle(rmiCard, 0, "RMI IPC");
        styleWideField(rmiHostField);
        UiLayout.addAlignedFormRow(rmiCard, 1, "RMI Host:", rmiHostField);
        startRmiBtn = actionButton("Start RMI", false);
        fetchRmiBtn = actionButton("Fetch Queues", true);
        addCardActions(rmiCard, 2, startRmiBtn, fetchRmiBtn);

        stack.add(socketCard);
        stack.add(javax.swing.Box.createVerticalStrut(12));
        stack.add(rmiCard);
        return stack;
    }

    private void addInlinePidRow(JPanel card, int row,
            String label1, JComboBox<String> field1, String label2, JComboBox<String> field2) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rowPanel.setOpaque(false);
        rowPanel.add(fieldLabel(label1));
        rowPanel.add(field1);
        rowPanel.add(fieldLabel(label2));
        rowPanel.add(field2);

        GridBagConstraints gbc = UiLayout.cardGbc(row);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(4, 10, 4, 10);
        card.add(rowPanel, gbc);
    }

    private void styleCombo(JComboBox<String> combo) {
        UiLayout.applyFormFieldSize(combo, new Dimension(160, 28));
        KernelTheme.styleComboBox(combo);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(KernelTheme.bodyFont());
        lbl.setForeground(KernelTheme.TEXT);
        return lbl;
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

    private void addCardActions(JPanel card, int row, JButton... buttons) {
        GridBagConstraints gbc = UiLayout.cardGbc(row);
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 10, 6, 10);
        if (buttons.length == 1) {
            card.add(buttons[0], gbc);
        } else {
            card.add(UiLayout.centeredButtonRow(buttons), gbc);
        }
    }

    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 14, 12, 14));

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);
        JLabel logLbl = new JLabel("Activity Log");
        logLbl.setFont(KernelTheme.bodyFont());
        logLbl.setForeground(KernelTheme.TEXT);
        logHeader.add(logLbl, BorderLayout.WEST);

        clearLogBtn = new JButton();
        KernelTheme.styleSecondaryButton(clearLogBtn, "Clear");
        UiLayout.normalizeToolbarButton(clearLogBtn);
        logHeader.add(clearLogBtn, BorderLayout.EAST);
        panel.add(logHeader, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        KernelTheme.styleTextArea(logArea);
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(0, 88));
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(KernelTheme.BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void stylePidField(JTextField field) {
        UiLayout.applyFormFieldSize(field, new Dimension(72, 28));
        KernelTheme.styleTextField(field);
    }

    private void styleWideField(JTextField field) {
        UiLayout.applyFormFieldSize(field, UiLayout.FORM_FIELD_WIDE);
        KernelTheme.styleTextField(field);
    }

    private void showSection(String card) {
        cardLayout.show(cardHost, card);
        highlightNav(card);
        switch (card) {
            case CARD_MESSAGES -> {
                sectionTitle.setText("Message Passing");
                sectionHint.setText("Send and receive messages between process mailboxes.");
            }
            case CARD_SHARED -> {
                sectionTitle.setText("Shared Memory");
                sectionHint.setText("Processes attach to a segment, then read/write the same memory region.");
            }
            case CARD_NETWORK -> {
                sectionTitle.setText("Network IPC");
                sectionHint.setText("Socket and RMI communication with the kernel server.");
            }
            default -> {
            }
        }
    }

    private void highlightNav(String active) {
        styleNav(messagesNavBtn, CARD_MESSAGES.equals(active));
        styleNav(sharedNavBtn, CARD_SHARED.equals(active));
        styleNav(networkNavBtn, CARD_NETWORK.equals(active));
    }

    private void styleNav(JButton btn, boolean active) {
        if (active) {
            KernelTheme.stylePrimaryButton(btn, btn.getText());
        } else {
            KernelTheme.styleSecondaryButton(btn, btn.getText());
        }
        UiLayout.normalizeMenuButton(btn);
    }

    private void wireButtons() {
        rebind(messagesNavBtn, e -> showSection(CARD_MESSAGES));
        rebind(sharedNavBtn, e -> showSection(CARD_SHARED));
        rebind(networkNavBtn, e -> showSection(CARD_NETWORK));
        rebind(sendMsgBtn, e -> sendMessage());
        rebind(recvBtn, e -> receiveMessage());
        rebind(writeBtn, e -> writeShared());
        rebind(readBtn, e -> readShared());
        rebind(attachBtn, e -> attachShared());
        rebind(startSocketBtn, e -> log(SocketIPCServer.getInstance().start(SocketIPCServer.DEFAULT_PORT)));
        rebind(sendSocketBtn, e -> sendSocket());
        rebind(startRmiBtn, e -> log(DistributedKernelServer.startServer()));
        rebind(fetchRmiBtn, e -> fetchRmiQueues());
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

    private void sendMessage() {
        Optional<Integer> fromOpt = ProcessPicker.getSelectedPid(fromCombo);
        Optional<Integer> toOpt = ProcessPicker.getSelectedPid(toCombo);
        String msg = messageField.getText().trim();
        if (fromOpt.isEmpty() || toOpt.isEmpty() || msg.isEmpty()) {
            log("[MSG] From, To, aur Message select/fill karein.");
            return;
        }
        String from = String.valueOf(fromOpt.get());
        int toPid = toOpt.get();
        ProcessRegistry.getInstance().find(toPid).ifPresentOrElse(
                pcb -> {
                    MessagePassingIPC.getInstance().send(from, String.valueOf(toPid), msg);
                    pcb.setIoStateInfo("IPC: message from P" + from);
                    log("[MSG] P" + from + " → P" + toPid + ": " + msg);
                    messageField.setText("");
                },
                () -> log("[MSG] Target process P" + toPid + " not found."));
    }

    private void receiveMessage() {
        ProcessPicker.getSelectedPid(receiveCombo).ifPresentOrElse(pid -> {
            String pidText = String.valueOf(pid);
            String msg = MessagePassingIPC.getInstance().receive(pidText);
            if (msg == null) {
                log("[MSG] P" + pid + ": mailbox empty ("
                        + MessagePassingIPC.getInstance().pendingCount(pidText) + " pending)");
            } else {
                ProcessRegistry.getInstance().find(pid).ifPresent(
                        pcb -> pcb.setIoStateInfo("IPC: received message"));
                log("[MSG] P" + pid + " received: " + msg);
            }
        }, () -> log("[MSG] Dropdown se process select karein."));
    }

    private void attachShared() {
        String seg = segmentField.getText().trim();
        if (seg.isEmpty()) {
            log("[SHM] Segment name enter karein.");
            return;
        }
        ProcessPicker.getSelectedPid(shmProcessCombo).ifPresentOrElse(pid -> {
            if (!ProcessRegistry.getInstance().find(pid).isPresent()) {
                log("[SHM] Process P" + pid + " not found.");
                return;
            }
            if (SharedMemoryIPC.getInstance().attach(seg, pid)) {
                log("[SHM] P" + pid + " attached to '" + seg + "'. Attached: "
                        + SharedMemoryIPC.getInstance().getAttachedSummary(seg));
            } else {
                log("[SHM] Attach failed for P" + pid + ".");
            }
        }, () -> log("[SHM] Dropdown se process select karein."));
    }

    private void writeShared() {
        String seg = segmentField.getText().trim();
        String data = sharedDataField.getText().trim();
        if (seg.isEmpty() || data.isEmpty()) {
            log("[SHM] Segment aur data enter karein.");
            return;
        }
        ProcessPicker.getSelectedPid(shmProcessCombo).ifPresentOrElse(pid -> {
            if (SharedMemoryIPC.getInstance().write(seg, pid, data)) {
                log("[SHM] P" + pid + " wrote to '" + seg + "': " + data);
                sharedDataField.setText("");
            } else {
                log("[SHM] P" + pid + " must attach to '" + seg + "' before writing.");
            }
        }, () -> log("[SHM] Dropdown se process select karein."));
    }

    private void readShared() {
        String seg = segmentField.getText().trim();
        if (seg.isEmpty()) {
            log("[SHM] Segment name enter karein.");
            return;
        }
        ProcessPicker.getSelectedPid(shmProcessCombo).ifPresentOrElse(pid -> {
            String content = SharedMemoryIPC.getInstance().read(seg, pid);
            if (content == null) {
                log("[SHM] P" + pid + " must attach to '" + seg + "' before reading.");
            } else {
                log("[SHM] " + seg + " (read by P" + pid + ") = " + content);
            }
        }, () -> log("[SHM] Dropdown se process select karein."));
    }

    private void sendSocket() {
        String host = socketHostField.getText().trim();
        Optional<Integer> fromOpt = ProcessPicker.getSelectedPid(fromCombo);
        Optional<Integer> toOpt = ProcessPicker.getSelectedPid(toCombo);
        String text = socketMsgField.getText().trim();
        if (host.isEmpty() || fromOpt.isEmpty() || toOpt.isEmpty() || text.isEmpty()) {
            return;
        }
        String from = String.valueOf(fromOpt.get());
        String to = String.valueOf(toOpt.get());
        String cmd = "MSG|" + to + "|" + from + "|" + text;
        log("[SOCKET] " + SocketIPCServer.sendCommand(host, SocketIPCServer.DEFAULT_PORT, cmd));
    }

    private void fetchRmiQueues() {
        try {
            String host = rmiHostField.getText().trim();
            String url = "rmi://" + host + ":" + DistributedKernelServer.RMI_PORT
                    + "/" + DistributedKernelServer.BIND_NAME;
            RemoteProcessService stub = (RemoteProcessService) java.rmi.Naming.lookup(url);
            log("[RMI] Queues:\n" + stub.getQueueSummary());
        } catch (Exception ex) {
            log("[RMI] Error: " + ex.getMessage());
        }
    }

    private void log(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
