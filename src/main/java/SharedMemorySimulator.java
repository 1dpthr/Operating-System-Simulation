
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simulates a shared memory segment with a static in-kernel map.
 */
public final class SharedMemorySimulator {

    public static final String DEFAULT_SEGMENT = "SHM_GLOBAL";

    private static final Map<String, String> segments = new HashMap<>();
    private static final Map<String, Set<String>> attached = new HashMap<>();

    static {
        segments.put(DEFAULT_SEGMENT, "");
    }

    private SharedMemorySimulator() {
    }

    public static synchronized void attach(String segment, String processLabel) {
        attached.computeIfAbsent(segment, k -> new java.util.HashSet<>()).add(processLabel);
    }

    public static synchronized boolean isAttached(String segment, String processLabel) {
        Set<String> set = attached.get(segment);
        return set != null && set.contains(processLabel);
    }

    public static synchronized boolean write(String segment, String processLabel, String data) {
        if (!isAttached(segment, processLabel)) {
            return false;
        }
        String existing = segments.getOrDefault(segment, "");
        String line = "[" + processLabel + "] " + data;
        segments.put(segment, existing.isEmpty() ? line : existing + "\n" + line);
        return true;
    }

    public static synchronized String read(String segment) {
        return segments.getOrDefault(segment, "(empty)");
    }

    public static synchronized String readForProcess(String segment, String processLabel) {
        if (!isAttached(segment, processLabel)) {
            return null;
        }
        return read(segment);
    }

    public static synchronized void clearSegment(String segment) {
        segments.put(segment, "");
    }
}
