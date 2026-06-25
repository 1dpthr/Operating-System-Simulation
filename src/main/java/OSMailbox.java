import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Indirect IPC — mailbox pattern for OS Simulation. Processes do not address each other;
 * they send/receive via named mailboxes (e.g. MATCH_BOX).
 */
public final class OSMailbox {

    public record MailItem(String mailboxId, String payload, long timestamp) {
    }

    private static final OSMailbox INSTANCE = new OSMailbox();
    private final ConcurrentHashMap<String, LinkedBlockingQueue<MailItem>> boxes = new ConcurrentHashMap<>();

    private OSMailbox() {
    }

    public static OSMailbox getInstance() {
        return INSTANCE;
    }

    public void send(String mailboxId, String message) {
        String id = normalize(mailboxId);
        boxes.computeIfAbsent(id, k -> new LinkedBlockingQueue<>())
                .offer(new MailItem(id, message, System.currentTimeMillis()));
    }

    public MailItem receive(String mailboxId) throws InterruptedException {
        return receive(mailboxId, 10_000);
    }

    public MailItem receive(String mailboxId, long timeoutMs) throws InterruptedException {
        LinkedBlockingQueue<MailItem> box = boxes.computeIfAbsent(normalize(mailboxId),
                k -> new LinkedBlockingQueue<>());
        return box.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public int pendingCount(String mailboxId) {
        LinkedBlockingQueue<MailItem> box = boxes.get(normalize(mailboxId));
        return box == null ? 0 : box.size();
    }

    public void clear() {
        boxes.clear();
    }

    private static String normalize(String mailboxId) {
        return mailboxId == null ? "" : mailboxId.trim().toUpperCase();
    }
}
