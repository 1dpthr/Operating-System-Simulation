import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class OSIPCDemo {

    private OSIPCDemo() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("===  OS IPC Demo ===\n");
        demoDirectIPC();
        System.out.println();
        demoIndirectMailbox();
        System.out.println("\n=== All IPC demos completed ===");
    }

    // ─── 1. Direct IPC ─────────────────────────────────────────────

    static void demoDirectIPC() throws Exception {
        System.out.println("--- 1. Direct IPC (ProcessA → ProcessB) ---");
        DirectIPC ipc = DirectIPC.getInstance();
        ipc.clear();

        DirectIPC.DirectProcess processA = ipc.registerProcess("ProcessA");
        DirectIPC.DirectProcess processB = ipc.registerProcess("ProcessB");
        CountDownLatch received = new CountDownLatch(1);
        AtomicReference<String> receivedMsg = new AtomicReference<>();

        Thread processBThread = new Thread(() -> {
            try {
                DirectIPC.Envelope env = processB.receive("ProcessA");
                if (env != null) {
                    receivedMsg.set(env.body());
                    System.out.println("[ProcessB] received from " + env.sender()
                            + ": " + env.body());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                received.countDown();
            }
        }, "ProcessB");

        Thread processAThread = new Thread(() -> {
            try {
                Thread.sleep(300);
                String confirmation = "DATA: Block 0x100 mapped | Status: OK";
                processA.send("ProcessB", confirmation);
                System.out.println("[ProcessA] sent to ProcessB: " + confirmation);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ProcessA");

        processBThread.start();
        processAThread.start();
        processAThread.join();
        received.await(5, TimeUnit.SECONDS);
        processBThread.join(2_000);

        if (receivedMsg.get() != null && receivedMsg.get().contains("DATA: Block 0x100")) {
            System.out.println("[Direct IPC] SUCCESS — message delivered by name.");
        } else {
            System.out.println("[Direct IPC] FAILED — message not received.");
        }
    }

    // ─── 2. Indirect Mailbox IPC ───────────────────────────────────

    static void demoIndirectMailbox() throws Exception {
        System.out.println("--- 2. Indirect IPC");
        OSMailbox mailbox = OSMailbox.getInstance();
        mailbox.clear();

        CountDownLatch notified = new CountDownLatch(1);
        AtomicReference<String> notification = new AtomicReference<>();

        Thread processD = new Thread(() -> {
            try {
                OSMailbox.MailItem item = mailbox.receive("MAIL_BOX");
                if (item != null) {
                    notification.set(item.payload());
                    System.out.println("[ProcessD] read mailbox " + item.mailboxId()
                            + ": " + item.payload());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                notified.countDown();
            }
        }, "ProcessD");

        Thread processC = new Thread(() -> {
            try {
                Thread.sleep(400);
                String msg = "MSG_RECEIVED: Data payload from ProcessC";
                mailbox.send("MAIL_BOX", msg);
                System.out.println("[ProcessC] put in MAIL_BOX: " + msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ProcessC");

        processD.start();
        processC.start();
        processC.join();
        notified.await(5, TimeUnit.SECONDS);
        processD.join(2_000);

        if (notification.get() != null && notification.get().contains("MSG_RECEIVED")) {
            System.out.println("[Indirect IPC] SUCCESS — notification read from mailbox.");
        } else {
            System.out.println("[Indirect IPC] FAILED — mailbox empty.");
        }
    }

}
