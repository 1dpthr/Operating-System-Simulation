import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * SimulationOS — Process Communication (JavaFX).
 * Run: mvn javafx:run
 */
public class ProcessCommunicationFxApp extends Application {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 760;
    private static final int SOCKET_PORT = 9090;

    private TextArea logArea;
    private TextArea shmContentsArea;
    private StackPane contentHost;
    private VBox messagesPanel;
    private VBox sharedPanel;
    private VBox networkPanel;
    private VBox directPanel;
    private VBox indirectPanel;
    private Button messagesNav;
    private Button sharedNav;
    private Button networkNav;
    private Button directNav;
    private Button indirectNav;

    private ComboBox<String> fromCombo;
    private ComboBox<String> toCombo;
    private ComboBox<String> shmProcessCombo;
    private TextField shmDataField;
    private TextField shmSegmentField;
    private TextField shmSizeField;
    private TextArea shmStatusConsole;

    private ComboBox<String> socketServerCombo;
    private ComboBox<String> socketClientCombo;
    private TextField socketPortField;
    private TextField socketHostField;
    private TextField socketMsgField;
    private TextArea socketConsole;

    private ComboBox<String> rmiServerCombo;
    private ComboBox<String> rmiClientCombo;
    private TextField rmiHostField;
    private TextField rmiObjectField;
    private TextArea rmiConsole;

    private volatile ServerSocket activeServerSocket;
    private volatile boolean socketServerRunning;
    private Thread socketServerThread;
    private volatile boolean rmiRunning;

    public static void main(String[] args) {
        launch(args);
    }

    private static volatile boolean fxInitialized = false;

    public static void launchWindow() {
        if (Platform.isFxApplicationThread()) {
            openStage();
            return;
        }
        if (!fxInitialized) {
            try {
                new javafx.embed.swing.JFXPanel();
                Platform.setImplicitExit(false);
                fxInitialized = true;
            } catch (Exception ignored) {
            }
        }
        Platform.runLater(ProcessCommunicationFxApp::openStage);
    }

    private static void openStage() {
        ProcessCommunicationFxApp app = new ProcessCommunicationFxApp();
        app.start(new Stage());
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FxTheme.BG + ";");

        VBox header = new VBox(4);
        header.setPadding(new Insets(14, 18, 8, 18));
        header.getChildren().addAll(
                FxTheme.heading("Process Communication"));

        contentHost = new StackPane();
        contentHost.setPadding(new Insets(0, 14, 0, 0));
        buildPanels();

        HBox body = new HBox(12, buildSidebar(), contentHost);
        HBox.setHgrow(contentHost, Priority.ALWAYS);

        root.setTop(header);
        root.setCenter(body);
        root.setBottom(buildBottom());

        showModule("direct");

        stage.setTitle("SimulationOS — Process Communication");
        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.setOnShown(e -> refreshAllProcessCombos());
        stage.show();

        ProcessPicker.registerRefreshCallback(
                () -> Platform.runLater(this::refreshAllProcessCombos));
        MemorySharingManager.getInstance().addListener(
                () -> Platform.runLater(this::refreshShmDisplay));
        refreshAllProcessCombos();
        appendLog("[SYS] IPC module ready.");
    }

    // ─── UI builders ───────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(8, 8, 8, 14));
        sidebar.setMinWidth(168);

        Label title = FxTheme.label("IPC Modules", true);
        directNav = sidebarButton("Direct IPC");
        indirectNav = sidebarButton("Indirect IPC");
        messagesNav = sidebarButton("Messages");
        sharedNav = sidebarButton("OS Shared Memory");
        networkNav = sidebarButton("Socket / RMI");
        directNav.setOnAction(e -> showModule("direct"));
        indirectNav.setOnAction(e -> showModule("indirect"));
        messagesNav.setOnAction(e -> showModule("messages"));
        sharedNav.setOnAction(e -> showModule("shared"));
        networkNav.setOnAction(e -> showModule("network"));
        sidebar.getChildren().addAll(title, directNav, indirectNav, messagesNav, sharedNav, networkNav);
        return sidebar;
    }

    private Button sidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void buildPanels() {
        OSIPCPanel osIpcHelper = new OSIPCPanel(this::appendLog);
        directPanel = osIpcHelper.buildDirectPanel();
        indirectPanel = osIpcHelper.buildIndirectPanel();
        messagesPanel = buildMessagesPanel();
        sharedPanel = buildSharedMemoryPanel();
        networkPanel = buildNetworkPanel();
        contentHost.getChildren().addAll(directPanel, indirectPanel, messagesPanel, sharedPanel, networkPanel);
    }

    private VBox buildMessagesPanel() {
        VBox root = new VBox(10);
        root.getChildren().addAll(
                FxTheme.heading("Message Queue"));

        VBox card = FxTheme.card();
        fromCombo = processCombo();
        toCombo = processCombo();
        TextField msgField = new TextField();
        msgField.setPromptText("Enter message...");
        msgField.setPrefWidth(280);
        FxTheme.styleField(msgField);

        Button sendBtn = FxTheme.primaryButton("Send Message");
        sendBtn.setOnAction(e -> {
            String from = fromCombo.getValue();
            String to = toCombo.getValue();
            String text = msgField.getText().trim();
            if (from == null || to == null || text.isEmpty()) {
                appendLog("[MSG] Select sender, receiver, and message.");
                return;
            }
            MessageQueueKernel.enqueue(from, to, text);
            appendLog("[MSG] " + from + " → " + to + ": " + text);
            msgField.clear();
        });

        card.getChildren().addAll(
                FxTheme.formRow("From:", fromCombo),
                FxTheme.formRow("To:", toCombo),
                FxTheme.formRow("Message:", msgField),
                sendBtn);

        TableView<MessageQueueKernel.Record> table =
                new TableView<>(MessageQueueKernel.observable());
        table.setPrefHeight(160);
        TableColumn<MessageQueueKernel.Record, String> c1 = new TableColumn<>("From");
        c1.setCellValueFactory(d -> d.getValue().fromProperty());
        TableColumn<MessageQueueKernel.Record, String> c2 = new TableColumn<>("To");
        c2.setCellValueFactory(d -> d.getValue().toProperty());
        TableColumn<MessageQueueKernel.Record, String> c3 = new TableColumn<>("Message");
        c3.setCellValueFactory(d -> d.getValue().messageProperty());
        TableColumn<MessageQueueKernel.Record, String> c4 = new TableColumn<>("Status");
        c4.setCellValueFactory(d -> d.getValue().statusProperty());
        table.getColumns().addAll(c1, c2, c3, c4);

        root.getChildren().addAll(card, FxTheme.label("Message Queue", true), table);
        return root;
    }

    private VBox buildSharedMemoryPanel() {
        VBox root = new VBox(10);
        root.getChildren().addAll(
                FxTheme.heading("Shared Memory Region"));

        HBox body = new HBox(14);
        HBox.setHgrow(body, Priority.ALWAYS);

        VBox card = FxTheme.card();
        card.setPrefWidth(420);
        shmProcessCombo = processCombo();
        shmSegmentField = new TextField("SHM_SEGMENT");
        shmSizeField = new TextField("4096");
        shmDataField = new TextField();
        shmDataField.setPromptText("Data to write...");
        FxTheme.styleField(shmSegmentField);
        FxTheme.styleField(shmSizeField);
        FxTheme.styleField(shmDataField);

        Button createSegBtn = FxTheme.secondaryButton("Create Segment (shmget)");
        Button attachBtn = FxTheme.primaryButton("Attach (shmat)");
        Button detachBtn = FxTheme.secondaryButton("Detach (shmdt)");
        createSegBtn.setOnAction(e -> {
            String seg = shmSegmentField.getText().trim();
            if (seg.isEmpty()) {
                appendLog("[SHM] Enter segment name.");
                return;
            }
            int size;
            try {
                size = Integer.parseInt(shmSizeField.getText().trim());
            } catch (NumberFormatException ex) {
                appendLog("[SHM] Invalid segment size.");
                return;
            }
            int shmid = MemorySharingManager.getInstance().shmget(seg, size);
            appendLog("[SHM] shmget '" + seg + "' → shmid=" + shmid + ", size=" + size);
            refreshShmDisplay();
        });
        attachBtn.setOnAction(e -> {
            Integer pid = selectedPid(shmProcessCombo);
            String seg = shmSegmentField.getText().trim();
            if (pid == null || seg.isEmpty()) {
                appendLog("[SHM] Select process and segment name.");
                return;
            }
            if (MemorySharingManager.getInstance().shmat(seg, pid)) {
                appendLog("[SHM] P" + pid + " attached to segment " + seg + " (shmat).");
                ProcessRegistry.syncViews();
            } else {
                appendLog("[SHM] Attach failed — process not found.");
            }
            refreshShmDisplay();
        });
        detachBtn.setOnAction(e -> {
            Integer pid = selectedPid(shmProcessCombo);
            String seg = shmSegmentField.getText().trim();
            if (pid == null || seg.isEmpty()) {
                return;
            }
            if (MemorySharingManager.getInstance().shmdt(seg, pid)) {
                appendLog("[SHM] P" + pid + " detached from " + seg + " (shmdt).");
            }
            refreshShmDisplay();
        });

        Button writeBtn = FxTheme.primaryButton("Write to SHM");
        Button readBtn = FxTheme.secondaryButton("Read from SHM");
        writeBtn.setOnAction(e -> {
            Integer pid = selectedPid(shmProcessCombo);
            String data = shmDataField.getText().trim();
            String seg = shmSegmentField.getText().trim();
            if (pid == null || data.isEmpty() || seg.isEmpty()) {
                appendLog("[SHM] Select process, segment, and data.");
                return;
            }
            if (MemorySharingManager.getInstance().shmWrite(seg, pid, data)) {
                appendLog("[SHM] P" + pid + " wrote: " + data);
                refreshShmDisplay();
                shmDataField.clear();
            } else {
                appendLog("[SHM] Attach process before writing.");
            }
        });
        readBtn.setOnAction(e -> {
            Integer pid = selectedPid(shmProcessCombo);
            String seg = shmSegmentField.getText().trim();
            if (pid == null || seg.isEmpty()) {
                appendLog("[SHM] Select a process.");
                return;
            }
            String content = MemorySharingManager.getInstance().shmRead(seg, pid);
            if (content != null) {
                shmContentsArea.setText(content);
                appendLog("[SHM] Read segment " + seg);
            } else {
                appendLog("[SHM] Attach process before reading.");
            }
            refreshShmDisplay();
        });

        shmContentsArea = new TextArea();
        shmContentsArea.setEditable(false);
        shmContentsArea.setPrefRowCount(4);
        shmContentsArea.setStyle("-fx-control-inner-background: " + FxTheme.CARD + ";");

        card.getChildren().addAll(
                FxTheme.formRow("Process:", shmProcessCombo),
                FxTheme.formRow("Segment:", shmSegmentField),
                FxTheme.formRow("Size (bytes):", shmSizeField),
                createSegBtn,
                new HBox(10, attachBtn, detachBtn),
                FxTheme.formRow("Write Data:", shmDataField),
                new HBox(10, writeBtn, readBtn),
                FxTheme.label("Shared Memory Contents", true),
                shmContentsArea);

        shmStatusConsole = statusConsole();
        VBox consoleBox = new VBox(6, FxTheme.label("SHM Status", true), shmStatusConsole);
        VBox.setVgrow(shmStatusConsole, Priority.ALWAYS);
        consoleBox.setPrefWidth(320);

        body.getChildren().addAll(card, consoleBox);
        HBox.setHgrow(card, Priority.ALWAYS);
        root.getChildren().add(body);
        refreshShmDisplay();
        return root;
    }

    private VBox buildNetworkPanel() {
        VBox root = new VBox(10);
        root.getChildren().addAll(
                FxTheme.heading("Network IPC (Socket & RMI)"));

        root.getChildren().add(buildSocketSection());
        root.getChildren().add(buildRmiSection());
        return root;
    }

    private HBox buildSocketSection() {
        VBox card = FxTheme.card();
        card.setPrefWidth(420);

        socketServerCombo = processCombo();
        socketClientCombo = processCombo();
        socketPortField = new TextField(String.valueOf(SOCKET_PORT));
        socketHostField = new TextField("127.0.0.1");
        socketMsgField = new TextField();
        socketMsgField.setPromptText("Data to send...");
        FxTheme.styleField(socketPortField);
        FxTheme.styleField(socketHostField);
        FxTheme.styleField(socketMsgField);

        Button startBtn = FxTheme.primaryButton("Start Server");
        Button stopBtn = FxTheme.secondaryButton("Stop Server");
        Button connectBtn = FxTheme.secondaryButton("Connect");
        Button sendBtn = FxTheme.primaryButton("Send Data");
        startBtn.setOnAction(e -> startSocketServerAsync());
        stopBtn.setOnAction(e -> stopSocketServer());
        connectBtn.setOnAction(e -> connectSocketAsync());
        sendBtn.setOnAction(e -> sendSocketAsync());

        card.getChildren().addAll(
                FxTheme.label("Socket IPC", true),
                FxTheme.formRow("Server Process:", socketServerCombo),
                FxTheme.formRow("Port:", socketPortField),
                new HBox(10, startBtn, stopBtn),
                FxTheme.formRow("Client Process:", socketClientCombo),
                FxTheme.formRow("IP:", socketHostField),
                FxTheme.formRow("Data:", socketMsgField),
                new HBox(10, connectBtn, sendBtn));

        socketConsole = statusConsole();
        VBox consoleBox = new VBox(6, FxTheme.label("Socket Status", true), socketConsole);
        consoleBox.setPrefWidth(320);
        updateSocketConsole();

        HBox row = new HBox(14, card, consoleBox);
        HBox.setHgrow(card, Priority.ALWAYS);
        return row;
    }

    private HBox buildRmiSection() {
        VBox card = FxTheme.card();
        card.setPrefWidth(420);

        rmiServerCombo = processCombo();
        rmiClientCombo = processCombo();
        rmiHostField = new TextField("localhost");
        rmiObjectField = new TextField(QueueServiceImpl.BIND_NAME);
        FxTheme.styleField(rmiHostField);
        FxTheme.styleField(rmiObjectField);

        Button startRegBtn = FxTheme.primaryButton("Start Registry");
        Button stopRegBtn = FxTheme.secondaryButton("Stop Registry");
        Button bindBtn = FxTheme.secondaryButton("Bind Object");
        Button lookupBtn = FxTheme.secondaryButton("Lookup Object");
        Button invokeBtn = FxTheme.primaryButton("Fetch Queues");
        startRegBtn.setOnAction(e -> startRmiAsync());
        stopRegBtn.setOnAction(e -> stopRmiRegistry());
        bindBtn.setOnAction(e -> bindRmiObjectAsync());
        lookupBtn.setOnAction(e -> lookupRmiObjectAsync());
        invokeBtn.setOnAction(e -> fetchQueuesAsync());

        card.getChildren().addAll(
                FxTheme.label("RMI IPC", true),
                FxTheme.formRow("Server Process:", rmiServerCombo),
                FxTheme.formRow("Object Name:", rmiObjectField),
                FxTheme.formRow("RMI Host:", rmiHostField),
                new HBox(10, startRegBtn, stopRegBtn),
                FxTheme.formRow("Client Process:", rmiClientCombo),
                new HBox(10, bindBtn, lookupBtn),
                invokeBtn);

        rmiConsole = statusConsole();
        VBox consoleBox = new VBox(6, FxTheme.label("RMI Registry", true), rmiConsole);
        consoleBox.setPrefWidth(320);
        updateRmiConsole();

        HBox row = new HBox(14, card, consoleBox);
        HBox.setHgrow(card, Priority.ALWAYS);
        return row;
    }

    private BorderPane buildBottom() {
        BorderPane bottom = new BorderPane();
        bottom.setPadding(new Insets(8, 14, 12, 14));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(4);
        logArea.setStyle("-fx-control-inner-background: " + FxTheme.PANEL + "; -fx-font-family: monospace;");

        Button clearBtn = FxTheme.secondaryButton("Clear");
        clearBtn.setOnAction(e -> {
            logArea.clear();
            appendLog("[SYS] Log cleared.");
        });

        HBox logHeader = new HBox(10, FxTheme.label("Activity Log", true), new Region(), clearBtn);
        HBox.setHgrow(logHeader.getChildren().get(1), Priority.ALWAYS);
        logHeader.setAlignment(Pos.CENTER_LEFT);

        bottom.setCenter(new VBox(6, logHeader, logArea));

        Button backBtn = FxTheme.secondaryButton("Back");
        backBtn.setOnAction(e -> ((Stage) backBtn.getScene().getWindow()).close());
        bottom.setBottom(new HBox(backBtn));
        return bottom;
    }

    // ─── Network (background threads) ──────────────────────────────

    private void startSocketServerAsync() {
        if (socketServerRunning) {
            appendLog("[SOCKET] Server already running.");
            updateSocketConsole();
            return;
        }
        String serverProc = selectedProcess(socketServerCombo);
        if (serverProc == null) {
            appendLog("[SOCKET] Select server process.");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(socketPortField.getText().trim());
        } catch (NumberFormatException ex) {
            appendLog("[SOCKET] Invalid port number.");
            return;
        }
        final int listenPort = port;
        Thread t = new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(listenPort);
                activeServerSocket = server;
                socketServerRunning = true;
                Platform.runLater(() -> {
                    appendLog("[SOCKET] " + serverProc + " started server on port " + listenPort);
                    updateSocketConsole();
                });
                while (socketServerRunning && !server.isClosed()) {
                    try (Socket client = server.accept();
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(client.getInputStream()))) {
                        String msg = in.readLine();
                        String line = msg == null ? "(empty)" : msg;
                        Platform.runLater(() -> {
                            appendLog("[SOCKET] Received: " + line);
                            appendSocketConsole("[RX] " + line + "\n");
                        });
                    } catch (IOException ex) {
                        if (socketServerRunning) {
                            Platform.runLater(() -> appendLog("[SOCKET] Accept error: " + ex.getMessage()));
                        }
                    }
                }
            } catch (IOException ex) {
                Platform.runLater(() -> appendLog("[SOCKET] Server failed: " + ex.getMessage()));
            } finally {
                socketServerRunning = false;
                activeServerSocket = null;
                Platform.runLater(this::updateSocketConsole);
            }
        }, "FxSocketServer");
        socketServerThread = t;
        t.setDaemon(true);
        t.start();
    }

    private void stopSocketServer() {
        socketServerRunning = false;
        if (activeServerSocket != null) {
            try {
                activeServerSocket.close();
            } catch (IOException ignored) {
            }
            activeServerSocket = null;
        }
        appendLog("[SOCKET] Server stopped.");
        updateSocketConsole();
    }

    private void connectSocketAsync() {
        String clientProc = selectedProcess(socketClientCombo);
        if (clientProc == null) {
            appendLog("[SOCKET] Select client process.");
            return;
        }
        String host = socketHostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(socketPortField.getText().trim());
        } catch (NumberFormatException ex) {
            appendLog("[SOCKET] Invalid port.");
            return;
        }
        final int connectPort = port;
        Thread t = new Thread(() -> {
            try (Socket socket = new Socket(host, connectPort)) {
                Platform.runLater(() -> {
                    appendLog("[SOCKET] " + clientProc + " connected to " + host + ":" + connectPort);
                    appendSocketConsole("[OK] " + clientProc + " connected\n");
                    updateSocketConsole();
                });
            } catch (IOException ex) {
                Platform.runLater(() -> appendLog("[SOCKET] Connect failed: " + ex.getMessage()));
            }
        }, "FxSocketConnect");
        t.setDaemon(true);
        t.start();
    }

    private void sendSocketAsync() {
        String clientProc = selectedProcess(socketClientCombo);
        String host = socketHostField.getText().trim();
        String msg = socketMsgField.getText().trim();
        if (clientProc == null || host.isEmpty() || msg.isEmpty()) {
            appendLog("[SOCKET] Select client, enter IP and data.");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(socketPortField.getText().trim());
        } catch (NumberFormatException ex) {
            appendLog("[SOCKET] Invalid port.");
            return;
        }
        final int sendPort = port;
        Thread t = new Thread(() -> {
            try (Socket socket = new Socket(host, sendPort);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println(msg);
                Platform.runLater(() -> {
                    appendLog("[SOCKET] " + clientProc + " sent: " + msg);
                    appendSocketConsole("[TX] " + clientProc + ": " + msg + "\n");
                    socketMsgField.clear();
                });
            } catch (IOException ex) {
                Platform.runLater(() -> appendLog("[SOCKET] Send failed: " + ex.getMessage()));
            }
        }, "FxSocketClient");
        t.setDaemon(true);
        t.start();
    }

    private void startRmiAsync() {
        String serverProc = selectedProcess(rmiServerCombo);
        if (serverProc == null) {
            appendLog("[RMI] Select server process.");
            return;
        }
        if (rmiRunning) {
            appendLog("[RMI] Registry already running on port " + QueueServiceImpl.RMI_PORT);
            updateRmiConsole();
            return;
        }
        Thread t = new Thread(() -> {
            try {
                QueueServiceImpl service = new QueueServiceImpl();
                Registry registry;
                try {
                    registry = LocateRegistry.createRegistry(QueueServiceImpl.RMI_PORT);
                } catch (Exception ex) {
                    registry = LocateRegistry.getRegistry(QueueServiceImpl.RMI_PORT);
                }
                String rawName = rmiObjectField.getText().trim();
                final String bindName = rawName.isEmpty() ? QueueServiceImpl.BIND_NAME : rawName;
                registry.rebind(bindName, service);
                rmiRunning = true;
                Platform.runLater(() -> {
                    appendLog("[RMI] " + serverProc + " started registry on port "
                            + QueueServiceImpl.RMI_PORT);
                    appendRmiConsole("Registry STARTED — bound as '" + bindName + "'\n");
                    updateRmiConsole();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> appendLog("[RMI] Start failed: " + ex.getMessage()));
            }
        }, "FxRmiStarter");
        t.setDaemon(true);
        t.start();
    }

    private void stopRmiRegistry() {
        rmiRunning = false;
        appendLog("[RMI] Registry stopped (local simulation).");
        appendRmiConsole("Registry STOPPED\n");
        updateRmiConsole();
    }

    private void bindRmiObjectAsync() {
        String serverProc = selectedProcess(rmiServerCombo);
        if (serverProc == null) {
            appendLog("[RMI] Select server process.");
            return;
        }
        Thread t = new Thread(() -> {
            try {
                Registry registry = LocateRegistry.getRegistry(QueueServiceImpl.RMI_PORT);
                String bindName = rmiObjectField.getText().trim();
                registry.rebind(bindName, new QueueServiceImpl());
                rmiRunning = true;
                Platform.runLater(() -> {
                    appendLog("[RMI] " + serverProc + " bound object '" + bindName + "'");
                    appendRmiConsole("Bound '" + bindName + "' by " + serverProc + "\n");
                    updateRmiConsole();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> appendLog("[RMI] Bind failed: " + ex.getMessage()));
            }
        }, "FxRmiBind");
        t.setDaemon(true);
        t.start();
    }

    private void lookupRmiObjectAsync() {
        String clientProc = selectedProcess(rmiClientCombo);
        if (clientProc == null) {
            appendLog("[RMI] Select client process.");
            return;
        }
        String host = rmiHostField.getText().trim();
        String bindName = rmiObjectField.getText().trim();
        Thread t = new Thread(() -> {
            try {
                String url = "rmi://" + host + ":" + QueueServiceImpl.RMI_PORT + "/" + bindName;
                QueueService stub = (QueueService) java.rmi.Naming.lookup(url);
                Platform.runLater(() -> {
                    appendLog("[RMI] " + clientProc + " looked up '" + bindName + "'");
                    appendRmiConsole("[LOOKUP] " + clientProc + " → " + bindName + " OK\n");
                    updateRmiConsole();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> appendLog("[RMI] Lookup failed: " + ex.getMessage()));
            }
        }, "FxRmiLookup");
        t.setDaemon(true);
        t.start();
    }

    private void fetchQueuesAsync() {
        String clientProc = selectedProcess(rmiClientCombo);
        if (clientProc == null) {
            appendLog("[RMI] Select client process.");
            return;
        }
        String host = rmiHostField.getText().trim();
        String bindName = rmiObjectField.getText().trim();
        Thread t = new Thread(() -> {
            try {
                String url = "rmi://" + host + ":" + QueueServiceImpl.RMI_PORT + "/" + bindName;
                QueueService stub = (QueueService) java.rmi.Naming.lookup(url);
                String queues = stub.getQueues();
                Platform.runLater(() -> {
                    appendLog("[RMI] " + clientProc + " invoked getQueues():");
                    appendLog("[RMI]\n" + queues);
                    appendRmiConsole("[INVOKE] getQueues() by " + clientProc + ":\n" + queues + "\n");
                    updateRmiConsole();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> appendLog("[RMI] Fetch failed: " + ex.getMessage()));
            }
        }, "FxRmiFetch");
        t.setDaemon(true);
        t.start();
    }

    // ─── Helpers ───────────────────────────────────────────────────

    private ComboBox<String> processCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefWidth(280);
        FxTheme.styleCombo(combo);
        combo.setOnShowing(e -> IpcProcessOptions.refreshCombo(combo));
        IpcProcessOptions.refreshCombo(combo);
        return combo;
    }

    private void refreshAllProcessCombos() {
        IpcProcessOptions.refreshCombo(fromCombo);
        IpcProcessOptions.refreshCombo(toCombo);
        IpcProcessOptions.refreshCombo(shmProcessCombo);
        IpcProcessOptions.refreshCombo(socketServerCombo);
        IpcProcessOptions.refreshCombo(socketClientCombo);
        IpcProcessOptions.refreshCombo(rmiServerCombo);
        IpcProcessOptions.refreshCombo(rmiClientCombo);
    }

    private String selectedProcess(ComboBox<String> combo) {
        if (combo == null || combo.isDisabled()) {
            return null;
        }
        String val = combo.getValue();
        if (val == null || val.startsWith("(")) {
            return null;
        }
        return val;
    }

    private TextArea statusConsole() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(12);
        area.setStyle("-fx-control-inner-background: " + FxTheme.PANEL + "; -fx-font-family: monospace; -fx-font-size: 11px;");
        return area;
    }

    private Integer selectedPid(ComboBox<String> combo) {
        String proc = selectedProcess(combo);
        if (proc == null) {
            return null;
        }
        return ProcessPicker.parsePid(proc).orElse(null);
    }

    private void refreshShmDisplay() {
        String seg = shmSegmentField == null ? "SHM_SEGMENT" : shmSegmentField.getText().trim();
        if (seg.isEmpty()) {
            seg = "SHM_SEGMENT";
        }
        MemorySharingManager mgr = MemorySharingManager.getInstance();
        var segOpt = mgr.getShm(seg);
        if (shmContentsArea != null) {
            if (segOpt.isPresent()) {
                String content = segOpt.get().content.toString().trim();
                shmContentsArea.setText(content.isEmpty() ? "(empty)" : content);
            } else {
                shmContentsArea.setText("(segment not created — use shmget)");
            }
        }
        if (shmStatusConsole != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== SHARED MEMORY STATUS ===\n\n");
            sb.append("Segment: ").append(seg).append('\n');
            if (segOpt.isEmpty()) {
                sb.append("Status: NOT CREATED\n\n");
                sb.append("Steps:\n");
                sb.append("1. Create segment (shmget)\n");
                sb.append("2. Select your process\n");
                sb.append("3. Attach (shmat)\n");
                sb.append("4. Write / Read data\n");
            } else {
                MemorySharingManager.ShmSegment s = segOpt.get();
                sb.append("Status: ACTIVE\n");
                sb.append("SHMID: ").append(s.shmid).append('\n');
                sb.append("Size: ").append(s.sizeBytes).append(" bytes\n");
                sb.append("Physical: ").append(s.physicalAddress).append('\n');
                sb.append("Attached PIDs: ");
                if (s.virtualByPid.isEmpty()) {
                    sb.append("(none)\n");
                } else {
                    sb.append(s.virtualByPid.keySet()).append('\n');
                    for (var e : s.virtualByPid.entrySet()) {
                        sb.append("  P").append(e.getKey()).append(" → ").append(e.getValue()).append('\n');
                    }
                }
            }
            shmStatusConsole.setText(sb.toString());
        }
    }

    private void updateSocketConsole() {
        if (socketConsole == null) {
            return;
        }
        String server = selectedProcess(socketServerCombo);
        String client = selectedProcess(socketClientCombo);
        String port = socketPortField == null ? String.valueOf(SOCKET_PORT) : socketPortField.getText().trim();
        StringBuilder sb = new StringBuilder();
        sb.append("=== SOCKET STATUS ===\n\n");
        sb.append("Server: ").append(server == null ? "None" : server).append('\n');
        sb.append("Port: ").append(port).append('\n');
        sb.append("Status: ").append(socketServerRunning ? "RUNNING" : "NOT RUNNING").append("\n\n");
        if (!socketServerRunning) {
            sb.append("Steps:\n");
            sb.append("1. Select server process\n");
            sb.append("2. Set port number\n");
            sb.append("3. Start server\n");
            sb.append("4. Select client & Connect\n");
            sb.append("5. Send / receive data\n");
        } else {
            sb.append("Client: ").append(client == null ? "None" : client).append('\n');
            sb.append("Waiting for connections...\n");
        }
        socketConsole.setText(sb.toString());
    }

    private void appendSocketConsole(String line) {
        if (socketConsole != null) {
            socketConsole.appendText(line);
        }
    }

    private void updateRmiConsole() {
        if (rmiConsole == null) {
            return;
        }
        String server = selectedProcess(rmiServerCombo);
        String client = selectedProcess(rmiClientCombo);
        String obj = rmiObjectField == null ? QueueServiceImpl.BIND_NAME : rmiObjectField.getText().trim();
        StringBuilder sb = new StringBuilder();
        sb.append("=== RMI REGISTRY ===\n\n");
        sb.append("Server: ").append(server == null ? "None" : server).append('\n');
        sb.append("Object: ").append(obj.isEmpty() ? QueueServiceImpl.BIND_NAME : obj).append('\n');
        sb.append("Port: ").append(QueueServiceImpl.RMI_PORT).append('\n');
        sb.append("Status: ").append(rmiRunning ? "STARTED" : "NOT STARTED").append("\n\n");
        if (!rmiRunning) {
            sb.append("RMI Service is not running.\n");
        } else {
            sb.append("Client: ").append(client == null ? "None" : client).append('\n');
            sb.append("Registry active on localhost:").append(QueueServiceImpl.RMI_PORT).append('\n');
        }
        rmiConsole.setText(sb.toString());
    }

    private void appendRmiConsole(String line) {
        if (rmiConsole != null) {
            rmiConsole.appendText(line);
        }
    }

    private void showModule(String module) {
        directPanel.setVisible("direct".equals(module));
        indirectPanel.setVisible("indirect".equals(module));
        messagesPanel.setVisible("messages".equals(module));
        sharedPanel.setVisible("shared".equals(module));
        networkPanel.setVisible("network".equals(module));
        FxTheme.styleSidebar(directNav, "direct".equals(module));
        FxTheme.styleSidebar(indirectNav, "indirect".equals(module));
        FxTheme.styleSidebar(messagesNav, "messages".equals(module));
        FxTheme.styleSidebar(sharedNav, "shared".equals(module));
        FxTheme.styleSidebar(networkNav, "network".equals(module));
    }

    private void appendLog(String line) {
        logArea.appendText(line + "\n");
    }

    static final class FxTheme {
        static final String BG = color(KernelTheme.BG);
        static final String PANEL = color(KernelTheme.BG_PANEL);
        static final String CARD = color(KernelTheme.CARD);
        static final String PRIMARY = color(KernelTheme.PRIMARY);
        static final String PRIMARY_HOVER = color(KernelTheme.PRIMARY_HOVER);
        static final String TEXT = color(KernelTheme.TEXT);
        static final String TEXT_MUTED = color(KernelTheme.TEXT_MUTED);
        static final String BORDER = color(KernelTheme.BORDER);

        private static String color(java.awt.Color c) {
            return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
        }

        static Label heading(String t) {
            Label l = new Label(t);
            l.setFont(Font.font("System", FontWeight.BOLD, 18));
            l.setTextFill(Color.web(TEXT));
            l.setStyle("-fx-text-fill: " + TEXT + ";");
            return l;
        }

        static Label subtitle(String t) {
            Label l = new Label(t);
            l.setFont(Font.font("System", FontWeight.NORMAL, 12));
            l.setTextFill(Color.web(TEXT_MUTED));
            l.setStyle("-fx-text-fill: " + TEXT_MUTED + ";");
            l.setWrapText(true);
            return l;
        }

        static Label label(String t, boolean bold) {
            Label l = new Label(t);
            l.setFont(Font.font("System", bold ? FontWeight.BOLD : FontWeight.NORMAL, 13));
            l.setTextFill(Color.web(TEXT));
            l.setStyle("-fx-text-fill: " + TEXT + ";");
            return l;
        }

        static VBox card() {
            VBox v = new VBox(10);
            v.setPadding(new Insets(14));
            v.setStyle("-fx-background-color: " + CARD + "; -fx-border-color: " + BORDER
                    + "; -fx-border-radius: 8; -fx-background-radius: 8;");
            return v;
        }

        static HBox formRow(String label, javafx.scene.control.Control field) {
            Label l = label(label, false);
            l.setMinWidth(100);
            return new HBox(10, l, field);
        }

        static Button primaryButton(String text) {
            Button b = new Button(text);
            String normal = "-fx-background-color: " + PRIMARY + "; -fx-text-fill: black; -fx-font-weight: bold;"
                    + "-fx-background-radius: 6; -fx-padding: 8 16;";
            String hover = "-fx-background-color: " + PRIMARY_HOVER + "; -fx-text-fill: black; -fx-font-weight: bold;"
                    + "-fx-background-radius: 6; -fx-padding: 8 16;";
            b.setStyle(normal);
            b.setOnMouseEntered(e -> b.setStyle(hover));
            b.setOnMouseExited(e -> b.setStyle(normal));
            return b;
        }

        static Button secondaryButton(String text) {
            Button b = new Button(text);
            b.setStyle("-fx-background-color: white; -fx-text-fill: " + TEXT + "; -fx-font-weight: bold;"
                    + "-fx-border-color: " + BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;"
                    + "-fx-padding: 8 16;");
            return b;
        }

        static void styleField(TextField f) {
            f.setStyle("-fx-background-color: white; -fx-text-fill: " + TEXT + "; -fx-border-color: " + BORDER + ";");
        }

        static void styleCombo(ComboBox<?> c) {
            c.setStyle("-fx-background-color: white; -fx-text-fill: " + TEXT + "; -fx-border-color: " + BORDER + ";");
        }

        static void styleSidebar(Button btn, boolean active) {
            if (active) {
                btn.setStyle("-fx-background-color: " + PRIMARY + "; -fx-text-fill: black; -fx-font-weight: bold;"
                        + "-fx-background-radius: 6; -fx-alignment: center-left; -fx-padding: 10 14;");
            } else {
                btn.setStyle("-fx-background-color: white; -fx-text-fill: " + TEXT + ";"
                        + "-fx-border-color: " + BORDER + "; -fx-border-radius: 6; -fx-background-radius: 6;"
                        + "-fx-alignment: center-left; -fx-padding: 10 14;");
            }
        }
    }

    /** Message queue — LinkedList simulation. */
    static final class MessageQueueKernel {
        static final class Record {
            private final javafx.beans.property.SimpleStringProperty from = new javafx.beans.property.SimpleStringProperty();
            private final javafx.beans.property.SimpleStringProperty to = new javafx.beans.property.SimpleStringProperty();
            private final javafx.beans.property.SimpleStringProperty message = new javafx.beans.property.SimpleStringProperty();
            private final javafx.beans.property.SimpleStringProperty status = new javafx.beans.property.SimpleStringProperty("Queued");

            Record(String f, String t, String m) {
                from.set(f);
                to.set(t);
                message.set(m);
            }

            javafx.beans.property.StringProperty fromProperty() {
                return from;
            }

            javafx.beans.property.StringProperty toProperty() {
                return to;
            }

            javafx.beans.property.StringProperty messageProperty() {
                return message;
            }

            javafx.beans.property.StringProperty statusProperty() {
                return status;
            }
        }

        private static final LinkedList<Record> queue = new LinkedList<>();
        private static final javafx.collections.ObservableList<Record> observable =
                javafx.collections.FXCollections.observableArrayList();

        static javafx.collections.ObservableList<Record> observable() {
            return observable;
        }

        static void enqueue(String from, String to, String msg) {
            Record r = new Record(from, to, msg);
            queue.add(r);
            observable.add(r);
            r.status.set("Delivered");
        }
    }
}
