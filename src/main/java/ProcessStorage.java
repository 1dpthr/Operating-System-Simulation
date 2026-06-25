import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Saves and loads process list to processes.txt (same style as page_size.txt). */
public final class ProcessStorage {

    private static final String FILE_NAME = "processes.txt";

    private ProcessStorage() {
    }

    public static File resolveFile() {
        File cwd = new File(System.getProperty("user.dir"));
        File direct = new File(cwd, FILE_NAME);
        if (direct.exists()) {
            return direct;
        }
        File parent = cwd.getParentFile();
        if (parent != null) {
            File sibling = new File(parent, FILE_NAME);
            if (sibling.exists()) {
                return sibling;
            }
        }
        return direct;
    }

    public static boolean save(ProcessRegistry registry) {
        File file = resolveFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# SimulationOS Saved Processes");
            writer.println("# Format: pid|name|owner|arrival|burst|memory_kb|priority|state|parent|children");
            for (ProcessControlBlock pcb : registry.getAll()) {
                writer.println(formatLine(pcb));
            }
            return true;
        } catch (IOException e) {
            System.err.println("ProcessStorage: could not save " + file.getPath());
            return false;
        }
    }

    public static int loadInto(ProcessRegistry registry) {
        File file = resolveFile();
        if (!file.exists()) {
            return 0;
        }
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 9) {
                    rows.add(parts);
                }
            }
        } catch (IOException e) {
            System.err.println("ProcessStorage: could not load " + file.getPath());
            return 0;
        }

        registry.clearForReload();
        PageTableManager.getInstance().resetAll();
        MultiLevelScheduler.clearJobQueue();

        Map<Integer, ProcessControlBlock> loaded = new LinkedHashMap<>();
        for (String[] p : rows) {
            try {
                int pid = Integer.parseInt(p[0].trim());
                String name = unescape(p[1]);
                String owner = unescape(p[2]);
                int arrival = Integer.parseInt(p[3].trim());
                int burst = Integer.parseInt(p[4].trim());
                int memory = Integer.parseInt(p[5].trim());
                int priority = Integer.parseInt(p[6].trim());
                ProcessControlBlock.ProcessState state =
                        ProcessControlBlock.ProcessState.valueOf(p[7].trim().toUpperCase());

                ProcessControlBlock pcb = new ProcessControlBlock(
                        pid, name, owner, arrival, burst, memory, priority, state);
                loaded.put(pid, pcb);
            } catch (Exception ex) {
                System.err.println("ProcessStorage: skipped bad line — " + String.join("|", p));
            }
        }

        for (String[] p : rows) {
            try {
                int pid = Integer.parseInt(p[0].trim());
                ProcessControlBlock pcb = loaded.get(pid);
                if (pcb == null) {
                    continue;
                }
                if (p.length > 9 && !p[8].isBlank()) {
                    int parentId = Integer.parseInt(p[8].trim());
                    pcb.setParentId(parentId);
                    ProcessControlBlock parent = loaded.get(parentId);
                    if (parent != null) {
                        parent.addChild(pid);
                    }
                }
                if (p.length > 9 && !p[9].isBlank()) {
                    for (String child : p[9].split(",")) {
                        if (!child.isBlank()) {
                            pcb.addChild(Integer.parseInt(child.trim()));
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        for (ProcessControlBlock pcb : loaded.values()) {
            registry.registerLoaded(pcb);
        }
        return loaded.size();
    }

    private static String formatLine(ProcessControlBlock pcb) {
        String parent = pcb.getParentId() == null ? "" : String.valueOf(pcb.getParentId());
        StringBuilder children = new StringBuilder();
        for (int i = 0; i < pcb.getChildProcessIds().size(); i++) {
            if (i > 0) {
                children.append(',');
            }
            children.append(pcb.getChildProcessIds().get(i));
        }
        return String.join("|",
                String.valueOf(pcb.getProcessId()),
                escape(pcb.getProcessName()),
                escape(pcb.getOwner()),
                String.valueOf(pcb.getArrivalTime()),
                String.valueOf(pcb.getBurstTime()),
                String.valueOf(pcb.getMemoryRequirementKb()),
                String.valueOf(pcb.getPriority()),
                pcb.getState().name(),
                parent,
                children.toString());
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }

    private static String unescape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                sb.append(value.charAt(++i));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
