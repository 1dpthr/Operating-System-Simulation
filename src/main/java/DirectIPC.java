import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Direct IPC — processes communicate by name. Each process has a private inbox;
 * send(receiverName, message) delivers directly; receive(senderName) blocks until
 * a message from that sender arrives.
 */
public final class DirectIPC {

    public record Envelope(String sender, String receiver, String body, long timestamp) {
    }

    private static final DirectIPC INSTANCE = new DirectIPC();
    private final ConcurrentHashMap<String, BlockingQueue<Envelope>> inboxes = new ConcurrentHashMap<>();

    private DirectIPC() {
    }

    public static DirectIPC getInstance() {
        return INSTANCE;
    }

    public DirectProcess registerProcess(String processName) {
        inboxes.computeIfAbsent(processName, n -> new LinkedBlockingQueue<>());
        return new DirectProcess(processName, this);
    }

    void send(String senderName, String receiverName, String message) {
        BlockingQueue<Envelope> inbox = inboxes.computeIfAbsent(receiverName, n -> new LinkedBlockingQueue<>());
        inbox.offer(new Envelope(senderName, receiverName, message, System.currentTimeMillis()));
    }

    Envelope receive(String receiverName, String senderName, long timeoutMs) throws InterruptedException {
        BlockingQueue<Envelope> inbox = inboxes.computeIfAbsent(receiverName, n -> new LinkedBlockingQueue<>());
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        while (System.nanoTime() < deadline) {
            Envelope msg = inbox.poll(200, TimeUnit.MILLISECONDS);
            if (msg != null && msg.sender().equals(senderName)) {
                return msg;
            }
            if (msg != null) {
                inbox.offer(msg);
            }
        }
        return null;
    }

    public void clear() {
        inboxes.clear();
    }

    /** Process endpoint for direct send/receive by name. */
    public static final class DirectProcess {
        private final String name;
        private final DirectIPC ipc;

        DirectProcess(String name, DirectIPC ipc) {
            this.name = name;
            this.ipc = ipc;
        }

        public String getName() {
            return name;
        }

        public void send(String receiverName, String message) {
            ipc.send(name, receiverName, message);
        }

        public Envelope receive(String senderName) throws InterruptedException {
            return ipc.receive(name, senderName, 10_000);
        }

        public String receiveBody(String senderName) throws InterruptedException {
            Envelope env = receive(senderName);
            return env == null ? null : env.body();
        }
    }
}
