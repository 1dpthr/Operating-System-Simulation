import java.util.Map;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public final class OSIPCPanel {

    private static final String[] OS_PROCESSES = {
            "ProcessA", "ProcessB", "ProcessC", "ProcessD"
    };

    private final Consumer<String> log;
    private final DirectIPC directIpc = DirectIPC.getInstance();
    private final OSMailbox mailbox = OSMailbox.getInstance();
    private TextArea directConsole;
    private TextArea mailboxConsole;

    public OSIPCPanel(Consumer<String> log) {
        this.log = log;
        directIpc.registerProcess("ProcessA");
        directIpc.registerProcess("ProcessB");
    }

    public VBox buildDirectPanel() {
        VBox root = new VBox(14);
        root.getChildren().addAll(
                ProcessCommunicationFxApp.FxTheme.heading("Direct IPC"));

        VBox content = new VBox(16);
        content.setPadding(new Insets(4, 4, 12, 4));
        content.getChildren().add(buildDirectSection());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }

    public VBox buildIndirectPanel() {
        VBox root = new VBox(14);
        root.getChildren().addAll(
                ProcessCommunicationFxApp.FxTheme.heading("Indirect IPC"));

        VBox content = new VBox(16);
        content.setPadding(new Insets(4, 4, 12, 4));
        content.getChildren().add(buildIndirectSection());

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.getChildren().add(scroll);
        return root;
    }



    // ─── Direct IPC ────────────────────────────────────────────────

    private VBox buildDirectSection() {
        VBox card = ProcessCommunicationFxApp.FxTheme.card();

        ComboBox<String> senderBox = osProcessCombo("ProcessA");
        ComboBox<String> receiverBox = osProcessCombo("ProcessB");
        TextField msgField = styledField("DATA: Block 0x100 mapped | Status: OK");
        directConsole = miniConsole();

        Button sendBtn = ProcessCommunicationFxApp.FxTheme.primaryButton("Send (Direct)");
        sendBtn.setOnAction(e -> {
            String sender = senderBox.getValue();
            String receiver = receiverBox.getValue();
            String text = msgField.getText().trim();
            if (sender == null || receiver == null || text.isEmpty()) {
                appendDirect("[Direct] Sender, receiver, and message required.");
                return;
            }
            directIpc.registerProcess(sender).send(receiver, text);
            appendDirect("[" + sender + "] → " + receiver + ": " + text);
            log.accept("[Direct IPC] " + sender + " sent to " + receiver);
        });

        ComboBox<String> listenAsBox = osProcessCombo("ProcessB");
        ComboBox<String> fromBox = osProcessCombo("ProcessA");
        Button recvBtn = ProcessCommunicationFxApp.FxTheme.secondaryButton("Receive (wait 5s)");
        recvBtn.setOnAction(e -> {
            String me = listenAsBox.getValue();
            String from = fromBox.getValue();
            if (me == null || from == null) {
                appendDirect("[Direct] Select listener and expected sender.");
                return;
            }
            recvBtn.setDisable(true);
            DirectIPC.DirectProcess proc = directIpc.registerProcess(me);
            Thread t = new Thread(() -> {
                try {
                    DirectIPC.Envelope env = proc.receive(from);
                    Platform.runLater(() -> {
                        if (env != null) {
                            appendDirect("[" + me + "] received from " + env.sender() + ": " + env.body());
                            log.accept("[Direct IPC] " + me + " received message.");
                        } else {
                            appendDirect("[" + me + "] no message from " + from + " (timeout).");
                        }
                        recvBtn.setDisable(false);
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> recvBtn.setDisable(false));
                }
            }, "DirectIPC-Receive");
            t.setDaemon(true);
            t.start();
        });

        card.getChildren().addAll(
                ProcessCommunicationFxApp.FxTheme.label("Direct IPC Settings", true),
                ProcessCommunicationFxApp.FxTheme.formRow("Sender:", senderBox),
                ProcessCommunicationFxApp.FxTheme.formRow("Receiver:", receiverBox),
                ProcessCommunicationFxApp.FxTheme.formRow("Message:", msgField),
                sendBtn,
                new HBox(10, listenAsBox, fromBox, recvBtn),
                ProcessCommunicationFxApp.FxTheme.label("Direct IPC Log", true),
                directConsole);
        return wrapSection(card);
    }

    // ─── Indirect IPC (Mailbox) ─────────────────────────────────────

    private VBox buildIndirectSection() {
        VBox card = ProcessCommunicationFxApp.FxTheme.card();

        TextField mailboxField = styledField("MAIL_BOX");
        TextField mailMsgField = styledField("MSG_RECEIVED: Data payload from ProcessC");
        Label pendingLbl = ProcessCommunicationFxApp.FxTheme.label("Pending in mailbox: 0", false);
        mailboxConsole = miniConsole();

        Button sendMailBtn = ProcessCommunicationFxApp.FxTheme.primaryButton(
                "Send to Mailbox");
        sendMailBtn.setOnAction(e -> {
            String box = mailboxField.getText().trim();
            String text = mailMsgField.getText().trim();
            if (box.isEmpty() || text.isEmpty()) {
                appendMailbox("[Mailbox] Mailbox ID and message required.");
                return;
            }
            mailbox.send(box, text);
            pendingLbl.setText("Pending in mailbox: " + mailbox.pendingCount(box));
            appendMailbox("Sent → " + box + ": " + text);
            log.accept("[Indirect IPC] Message sent to " + box);
        });

        Button recvMailBtn = ProcessCommunicationFxApp.FxTheme.secondaryButton(
                "Receive from Mailbox");
        recvMailBtn.setOnAction(e -> {
            String box = mailboxField.getText().trim();
            if (box.isEmpty()) {
                return;
            }
            recvMailBtn.setDisable(true);
            Thread t = new Thread(() -> {
                try {
                    OSMailbox.MailItem item = mailbox.receive(box, 8_000);
                    Platform.runLater(() -> {
                        if (item != null) {
                            appendMailbox("Received ← " + item.mailboxId()
                                    + ": " + item.payload());
                            log.accept("[Indirect IPC] Notification received.");
                        } else {
                            appendMailbox("Mailbox empty (timeout).");
                        }
                        pendingLbl.setText("Pending in mailbox: " + mailbox.pendingCount(box));
                        recvMailBtn.setDisable(false);
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> recvMailBtn.setDisable(false));
                }
            }, "Mailbox-Receive");
            t.setDaemon(true);
            t.start();
        });

        card.getChildren().addAll(
                ProcessCommunicationFxApp.FxTheme.label("Indirect IPC Settings", true),
                ProcessCommunicationFxApp.FxTheme.formRow("Mailbox ID:", mailboxField),
                ProcessCommunicationFxApp.FxTheme.formRow("Message:", mailMsgField),
                pendingLbl,
                sendMailBtn,
                recvMailBtn,
                ProcessCommunicationFxApp.FxTheme.label("Mailbox Log", true),
                mailboxConsole);
        return wrapSection(card);
    }

    private void runMailboxDemo(TextField mailboxField, Label pendingLbl) {
        String box = mailboxField.getText().trim().isEmpty() ? "MAIL_BOX" : mailboxField.getText().trim();
        appendMailbox("--- Running Mailbox demo (" + box + ") ---");
        Thread reader = new Thread(() -> {
            try {
                OSMailbox.MailItem item = mailbox.receive(box);
                if (item != null) {
                    Platform.runLater(() -> appendMailbox("[ProcessD] demo: " + item.payload()));
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, "Demo-ProcessD");
        reader.setDaemon(true);
        reader.start();
        Thread writer = new Thread(() -> {
            try {
                Thread.sleep(500);
                String msg = "MSG_RECEIVED: Data payload from ProcessC";
                mailbox.send(box, msg);
                Platform.runLater(() -> {
                    appendMailbox("[ProcessC] demo → " + box + ": " + msg);
                    pendingLbl.setText("Pending in mailbox: " + mailbox.pendingCount(box));
                    log.accept("[Indirect IPC] Mailbox demo completed.");
                });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, "Demo-ProcessC");
        writer.setDaemon(true);
        writer.start();
    }



    // ─── Helpers ───────────────────────────────────────────────────

    private VBox wrapSection(VBox card) {
        VBox box = new VBox(card);
        VBox.setVgrow(card, Priority.NEVER);
        return box;
    }

    private ComboBox<String> osProcessCombo(String defaultVal) {
        ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(OS_PROCESSES));
        combo.setValue(defaultVal);
        combo.setPrefWidth(220);
        ProcessCommunicationFxApp.FxTheme.styleCombo(combo);
        return combo;
    }

    private TextField styledField(String text) {
        TextField field = new TextField(text);
        field.setPrefWidth(320);
        ProcessCommunicationFxApp.FxTheme.styleField(field);
        return field;
    }

    private TextArea miniConsole() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(5);
        area.setStyle("-fx-control-inner-background: " + ProcessCommunicationFxApp.FxTheme.PANEL
                + "; -fx-text-fill: " + ProcessCommunicationFxApp.FxTheme.TEXT
                + "; -fx-font-family: monospace; -fx-font-size: 11px;");
        return area;
    }

    private void appendDirect(String line) {
        directConsole.appendText(line + "\n");
    }

    private void appendMailbox(String line) {
        mailboxConsole.appendText(line + "\n");
    }


}
