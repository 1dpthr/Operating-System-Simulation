
import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Simulates OS kernel message queue using LinkedList.
 */
public final class MessageQueueKernel {

    public static final class MessageRecord {
        private final StringProperty from = new SimpleStringProperty();
        private final StringProperty to = new SimpleStringProperty();
        private final StringProperty message = new SimpleStringProperty();
        private final StringProperty status = new SimpleStringProperty();

        public MessageRecord(String from, String to, String message, String status) {
            this.from.set(from);
            this.to.set(to);
            this.message.set(message);
            this.status.set(status);
        }

        public StringProperty fromProperty() {
            return from;
        }

        public StringProperty toProperty() {
            return to;
        }

        public StringProperty messageProperty() {
            return message;
        }

        public StringProperty statusProperty() {
            return status;
        }
    }

    private static final MessageQueueKernel INSTANCE = new MessageQueueKernel();
    private final LinkedList<MessageRecord> queue = new LinkedList<>();
    private final ObservableList<MessageRecord> observable = FXCollections.observableArrayList();

    private MessageQueueKernel() {
    }

    public static MessageQueueKernel getInstance() {
        return INSTANCE;
    }

    public ObservableList<MessageRecord> getObservableQueue() {
        return observable;
    }

    public synchronized MessageRecord enqueue(String from, String to, String text) {
        MessageRecord record = new MessageRecord(from, to, text, "Queued");
        queue.addLast(record);
        observable.add(record);
        return record;
    }

    public synchronized void markDelivered(MessageRecord record) {
        if (record != null) {
            record.statusProperty().set("Delivered");
        }
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized List<MessageRecord> snapshot() {
        return List.copyOf(queue);
    }

    public synchronized void clear() {
        queue.clear();
        observable.clear();
    }
}
