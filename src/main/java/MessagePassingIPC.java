import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class MessagePassingIPC {

    private static final MessagePassingIPC INSTANCE = new MessagePassingIPC();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> mailboxes = new ConcurrentHashMap<>();

    private MessagePassingIPC() {
    }

    public static MessagePassingIPC getInstance() {
        return INSTANCE;
    }

    public void send(String fromProcess, String toProcess, String message) {
        String key = toProcess.trim().toUpperCase();
        mailboxes.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>())
                .add("From P" + fromProcess + ": " + message);
    }

    public String receive(String processId) {
        String key = processId.trim().toUpperCase();
        ConcurrentLinkedQueue<String> queue = mailboxes.get(key);
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }

    public int pendingCount(String processId) {
        ConcurrentLinkedQueue<String> queue = mailboxes.get(processId.trim().toUpperCase());
        return queue == null ? 0 : queue.size();
    }

    public void clear() {
        mailboxes.clear();
    }
}
