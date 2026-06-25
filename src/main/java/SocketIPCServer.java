import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public final class SocketIPCServer {

    public static final int DEFAULT_PORT = 5050;

    private static SocketIPCServer instance;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running;

    private SocketIPCServer() {
    }

    public static synchronized SocketIPCServer getInstance() {
        if (instance == null) {
            instance = new SocketIPCServer();
        }
        return instance;
    }

    public synchronized String start(int port) {
        if (running) {
            return "Socket server already running on port " + port;
        }
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            serverThread = new Thread(this::acceptLoop, "SocketIPCServer");
            serverThread.setDaemon(true);
            serverThread.start();
            return "Socket IPC server started on port " + port;
        } catch (IOException ex) {
            return "Socket server failed: " + ex.getMessage();
        }
    }

    public synchronized void stop() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
            serverSocket = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void acceptLoop() {
        while (running && serverSocket != null && !serverSocket.isClosed()) {
            try (Socket client = serverSocket.accept()) {
                handleClient(client);
            } catch (IOException ex) {
                if (running) {
                    System.err.println("SocketIPCServer: " + ex.getMessage());
                }
            }
        }
    }

    private void handleClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        String line = in.readLine();
        if (line == null || line.isBlank()) {
            out.println("ERR|Empty command");
            return;
        }
        String[] parts = line.split("\\|", -1);
        String cmd = parts[0].trim().toUpperCase();
        switch (cmd) {
            case "MSG" -> {
                if (parts.length < 4) {
                    out.println("ERR|MSG needs: MSG|to|from|text");
                    return;
                }
                MessagePassingIPC.getInstance().send(parts[2], parts[1], parts[3]);
                out.println("OK|Message delivered to P" + parts[1]);
            }
            case "CREATE" -> {
                if (parts.length < 5) {
                    out.println("ERR|CREATE needs: CREATE|name|arrival|burst|owner");
                    return;
                }
                ProcessControlBlock pcb = ProcessRegistry.getInstance().create(
                        parts[1], parts[4], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                PageTableManager.getInstance().allocatePagesForProcess(pcb.getProcessId(), pcb.getMemoryRequirementKb());
                pcb.setAllocatedMemoryPointer(PageTableManager.getInstance().getMemoryPointer(pcb.getProcessId()));
                out.println("OK|Created P" + pcb.getProcessId());
            }
            case "QUEUE" -> out.println("OK|" + String.join(";", ProcessRegistry.getInstance().getQueueSummary()));
            default -> out.println("ERR|Unknown command: " + cmd);
        }
    }

    public static String sendCommand(String host, int port, String command) {
        try (Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(command);
            String response = in.readLine();
            return response == null ? "No response" : response;
        } catch (IOException ex) {
            return "ERR|" + ex.getMessage();
        }
    }
}
