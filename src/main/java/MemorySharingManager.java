import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kernel model for OS memory sharing: shmget/shmat, mmap, and shared libraries.
 */
public final class MemorySharingManager {

    public enum SharingMode {
        SHARED_MEMORY, MMAP, SHARED_LIBRARY
    }

    public static final class ShmSegment {
        public final int shmid;
        public final String name;
        public final int sizeBytes;
        public final String physicalAddress;
        public final StringBuilder content = new StringBuilder();
        public final Map<Integer, String> virtualByPid = new LinkedHashMap<>();

        ShmSegment(int shmid, String name, int sizeBytes) {
            this.shmid = shmid;
            this.name = name;
            this.sizeBytes = sizeBytes;
            this.physicalAddress = String.format("PHYS@0x%X", 0xA000 + shmid * 0x1000);
        }
    }

    public static final class MappedFile {
        public final int fd;
        public final String filename;
        public final long fileSize;
        public final String mapAddress;
        public final StringBuilder content = new StringBuilder();
        public final Map<Integer, String> virtualByPid = new LinkedHashMap<>();

        MappedFile(int fd, String filename, long fileSize) {
            this.fd = fd;
            this.filename = filename;
            this.fileSize = fileSize;
            this.mapAddress = String.format("MAP@0x%X", 0xB000 + fd * 0x800);
        }
    }

    public static final class SharedLibrary {
        public final String name;
        public final String physicalCodeAddress;
        public final Set<Integer> attachedPids = ConcurrentHashMap.newKeySet();
        public final Map<Integer, String> privateDataByPid = new LinkedHashMap<>();

        SharedLibrary(String name) {
            this.name = name;
            this.physicalCodeAddress = String.format("CODE@0x%X", name.hashCode() & 0xFFFFF | 0x400000);
        }
    }

    public interface Listener {
        void onMemorySharingChanged();
    }

    private static final MemorySharingManager INSTANCE = new MemorySharingManager();
    private final AtomicInteger shmidGen = new AtomicInteger(1000);
    private final AtomicInteger fdGen = new AtomicInteger(3);
    private final Map<String, ShmSegment> shmByName = new LinkedHashMap<>();
    private final Map<String, MappedFile> filesByName = new LinkedHashMap<>();
    private final Map<String, SharedLibrary> libraries = new LinkedHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    private MemorySharingManager() {
        libraries.put("libc.so", new SharedLibrary("libc.so"));
    }

    public static MemorySharingManager getInstance() {
        return INSTANCE;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private void notifyChanged() {
        for (Listener l : new ArrayList<>(listeners)) {
            l.onMemorySharingChanged();
        }
    }

    // --- Shared Memory (shmget / shmat / shmdt) ---

    public int shmget(String name, int sizeBytes) {
        String key = normalize(name);
        if (key.isEmpty()) {
            return -1;
        }
        ShmSegment seg = shmByName.get(key);
        if (seg == null) {
            int id = shmidGen.incrementAndGet();
            seg = new ShmSegment(id, key, Math.max(sizeBytes, 4096));
            shmByName.put(key, seg);
            notifyChanged();
            return id;
        }
        return seg.shmid;
    }

    public boolean shmat(String name, int processId) {
        if (!ProcessRegistry.getInstance().find(processId).isPresent()) {
            return false;
        }
        String key = normalize(name);
        ShmSegment seg = shmByName.computeIfAbsent(key, k -> {
            int id = shmidGen.incrementAndGet();
            return new ShmSegment(id, k, 4096);
        });
        seg.virtualByPid.put(processId, virtualAddress(processId, seg.shmid));
        ProcessRegistry.getInstance().find(processId).ifPresent(
                pcb -> pcb.setIoStateInfo("SHM shmat: " + key + " → " + seg.virtualByPid.get(processId)));
        notifyChanged();
        return true;
    }

    public boolean shmdt(String name, int processId) {
        ShmSegment seg = shmByName.get(normalize(name));
        if (seg == null) {
            return false;
        }
        boolean removed = seg.virtualByPid.remove(processId) != null;
        if (removed) {
            notifyChanged();
        }
        return removed;
    }

    public boolean shmWrite(String name, int processId, String data) {
        ShmSegment seg = shmByName.get(normalize(name));
        if (seg == null || !seg.virtualByPid.containsKey(processId)) {
            return false;
        }
        seg.content.append("[P").append(processId).append("] ").append(data).append("\n");
        ProcessRegistry.getInstance().find(processId).ifPresent(
                pcb -> pcb.setIoStateInfo("SHM write → " + seg.physicalAddress));
        notifyChanged();
        return true;
    }

    public String shmRead(String name, int processId) {
        ShmSegment seg = shmByName.get(normalize(name));
        if (seg == null || !seg.virtualByPid.containsKey(processId)) {
            return null;
        }
        return seg.content.length() == 0 ? "(empty segment)" : seg.content.toString().trim();
    }

    public List<ShmSegment> getShmSegments() {
        return new ArrayList<>(shmByName.values());
    }

    public Optional<ShmSegment> getShm(String name) {
        return Optional.ofNullable(shmByName.get(normalize(name)));
    }

    public Set<Integer> getShmAttachedPids(String name) {
        ShmSegment seg = shmByName.get(normalize(name));
        if (seg == null) {
            return Collections.emptySet();
        }
        return Set.copyOf(seg.virtualByPid.keySet());
    }

    // --- Memory-mapped files (mmap) ---

    public int openAndMap(String filename, int processId, long fileSize) {
        if (!ProcessRegistry.getInstance().find(processId).isPresent()) {
            return -1;
        }
        String key = normalize(filename);
        MappedFile file = filesByName.computeIfAbsent(key, k -> {
            int fd = fdGen.incrementAndGet();
            return new MappedFile(fd, k, fileSize);
        });
        file.virtualByPid.put(processId, String.format("0x7F%05X", 0x20000 + processId * 0x100));
        ProcessRegistry.getInstance().find(processId).ifPresent(
                pcb -> pcb.setIoStateInfo("mmap: " + key + " MAP_SHARED"));
        notifyChanged();
        return file.fd;
    }

    public boolean mmapWrite(String filename, int processId, String data) {
        MappedFile file = filesByName.get(normalize(filename));
        if (file == null || !file.virtualByPid.containsKey(processId)) {
            return false;
        }
        file.content.append("[P").append(processId).append("@map] ").append(data).append("\n");
        notifyChanged();
        return true;
    }

    public String mmapRead(String filename, int processId) {
        MappedFile file = filesByName.get(normalize(filename));
        if (file == null || !file.virtualByPid.containsKey(processId)) {
            return null;
        }
        return file.content.length() == 0 ? "(file mapped, no writes yet)" : file.content.toString().trim();
    }

    public List<MappedFile> getMappedFiles() {
        return new ArrayList<>(filesByName.values());
    }

    // --- Shared libraries (.so) ---

    public boolean loadLibrary(String libName, int processId) {
        if (!ProcessRegistry.getInstance().find(processId).isPresent()) {
            return false;
        }
        String raw = normalize(libName);
        final String libKey = raw.isEmpty() ? "libc.so" : raw;
        SharedLibrary lib = libraries.computeIfAbsent(libKey, SharedLibrary::new);
        lib.attachedPids.add(processId);
        lib.privateDataByPid.put(processId, String.format("DATA@0x%X (private globals)", 0x600000 + processId * 0x200));
        ProcessRegistry.getInstance().find(processId).ifPresent(
                pcb -> pcb.setIoStateInfo("dlopen: " + libKey + " (shared code segment)"));
        notifyChanged();
        return true;
    }

    public List<SharedLibrary> getLibraries() {
        return new ArrayList<>(libraries.values());
    }

    public Optional<SharedLibrary> getLibrary(String name) {
        return Optional.ofNullable(libraries.get(normalize(name)));
    }

    public String getLibrarySummary(String libName) {
        SharedLibrary lib = libraries.get(normalize(libName));
        if (lib == null) {
            return "Library not loaded.";
        }
        return lib.name + " | 1 physical code copy @ " + lib.physicalCodeAddress
                + " | " + lib.attachedPids.size() + " process(es) share code";
    }

    private static String virtualAddress(int pid, int shmid) {
        return String.format("0x7F%05X", 0x10000 + pid * 0x100 + (shmid & 0xFF));
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }
}
