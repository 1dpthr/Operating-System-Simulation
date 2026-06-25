import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public final class KernelConfig {

    private static KernelConfig instance;

    private int pageSizeBits = 12;
    private int cpuCount = 1;
    private int timeQuantum = 4;
    private String configFilePath = "page_size.txt";

    private KernelConfig() {
        loadFromFile();
    }

    public static synchronized KernelConfig getInstance() {
        if (instance == null) {
            instance = new KernelConfig();
        }
        return instance;
    }

    public void loadFromFile() {
        File file = resolveConfigFile();
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("page_size_bits=")) {
                    pageSizeBits = Integer.parseInt(line.substring("page_size_bits=".length()).trim());
                } else if (line.startsWith("cpu_count=")) {
                    cpuCount = Integer.parseInt(line.substring("cpu_count=".length()).trim());
                } else if (line.startsWith("time_quantum=")) {
                    timeQuantum = Integer.parseInt(line.substring("time_quantum=".length()).trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("KernelConfig: could not load " + file.getPath() + " — using defaults.");
        }
    }

    public boolean saveToFile() {
        File file = resolveConfigFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# SimulationOS Kernel Configuration");
            writer.println("page_size_bits=" + pageSizeBits);
            writer.println("cpu_count=" + cpuCount);
            writer.println("time_quantum=" + timeQuantum);
            return true;
        } catch (IOException e) {
            System.err.println("KernelConfig: could not save " + file.getPath());
            return false;
        }
    }

    private File resolveConfigFile() {
        File cwd = new File(System.getProperty("user.dir"));
        File direct = new File(cwd, configFilePath);
        if (direct.exists()) {
            return direct;
        }
        File parent = new File(cwd.getParentFile(), configFilePath);
        if (parent.exists()) {
            return parent;
        }
        return direct;
    }

    public int getPageSizeBits() {
        return pageSizeBits;
    }

    public void setPageSizeBits(int pageSizeBits) {
        this.pageSizeBits = pageSizeBits;
    }

    public int getPageSizeBytes() {
        return 1 << pageSizeBits;
    }

    public String getPageSizeDisplay() {
        return "2^" + pageSizeBits + " bytes (" + getPageSizeBytes() + " B)";
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }

    public void setTimeQuantum(int timeQuantum) {
        this.timeQuantum = timeQuantum;
    }
}
