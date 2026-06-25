
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Background socket IPC server for JavaFX module (port 9090).
 */
public final class FxSocketServer {

    public static final int PORT = 9090;

    private static FxSocketServer instance;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean running;

    private FxSocketServer() {
    }

    public static synchronized FxSocketServer getInstance() {
        if (instance == null) {
            instance = new FxSocketServer();
        }
        return instance;
    }

    public synchronized String start() {
        if (running) {
            return "Socket server already running on port " + PORT;
        }
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            serverThread = new Thread(this::acceptLoop, "FxSocketServer");
            serverThread.setDaemon(true);
            serverThread.start();
            return "Socket IPC server started on localhost:" + PORT;
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

    public static String sendCommand(String host, String command) {
        try (Socket socket = new Socket(host, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(command);
            String response = in.readLine();
            return response == null ? "OK|No response" : response;
        } catch (IOException ex) {
            return "ERR|" + ex.getMessage();
        }
    }

    private void acceptLoop() {
        while (running && serverSocket != null && !serverSocket.isClosed()) {
            try (Socket client = serverSocket.accept()) {
                handleClient(client);
            } catch (IOException ex) {
                if (running) {
                    System.err.println("FxSocketServer: " + ex.getMessage());
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
            case "PING" -> out.println("OK|PONG");
            case "MSG" -> {
                if (parts.length < 4) {
                    out.println("ERR|MSG needs to|from|text");
                    return;
                }
                MessageQueueKernel.getInstance().enqueue("P" + parts[1], "P" + parts[2], parts[3]);
                out.println("OK|Message queued for P" + parts[2]);
            }
            case "QUEUE" -> {
                int n = MessageQueueKernel.getInstance().size();
                out.println("OK|Message queue size: " + n);
            }
            default -> out.println("ERR|Unknown command: " + cmd);
        }
    }
}
