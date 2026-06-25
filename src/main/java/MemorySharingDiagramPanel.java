import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JPanel;

/**
 * Interactive diagram: physical RAM / file / library mapped into process virtual spaces.
 */
public class MemorySharingDiagramPanel extends JPanel {

    private MemorySharingManager.SharingMode mode = MemorySharingManager.SharingMode.SHARED_MEMORY;
    private String highlightSegment = "SHM_GLOBAL";
    private String highlightFile = "data.bin";
    private String highlightLib = "libc.so";

    public MemorySharingDiagramPanel() {
        setOpaque(true);
        setBackground(KernelTheme.BG_PANEL);
        setPreferredSize(new Dimension(520, 320));
        setMinimumSize(new Dimension(400, 260));
    }

    public void setMode(MemorySharingManager.SharingMode mode) {
        this.mode = mode;
        repaint();
    }

    public void setHighlightSegment(String name) {
        this.highlightSegment = name == null ? "" : name.trim();
    }

    public void setHighlightFile(String name) {
        this.highlightFile = name == null ? "" : name.trim();
    }

    public void setHighlightLib(String name) {
        this.highlightLib = name == null ? "" : name.trim();
    }

    public void refresh() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(KernelTheme.bodyFont());

        switch (mode) {
            case SHARED_MEMORY -> paintSharedMemory(g2);
            case MMAP -> paintMmap(g2);
            case SHARED_LIBRARY -> paintSharedLibrary(g2);
        }
        g2.dispose();
    }

    private void paintSharedMemory(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        drawTitle(g2, "Shared Memory — shmget / shmat", 12, 22);

        MemorySharingManager mgr = MemorySharingManager.getInstance();
        var segOpt = mgr.getShm(highlightSegment);
        int physX = w / 2 - 90;
        int physY = h / 2 - 20;
        int physW = 180;
        int physH = 70;

        if (segOpt.isPresent()) {
            MemorySharingManager.ShmSegment seg = segOpt.get();
            drawBox(g2, physX, physY, physW, physH, KernelTheme.PRIMARY,
                    "Physical RAM Block",
                    "shmid=" + seg.shmid + " | " + seg.sizeBytes + " B",
                    seg.physicalAddress);

            String preview = seg.content.length() > 0
                    ? truncate(seg.content.toString().replace('\n', ' '), 28)
                    : "(empty — write via shmat)";
            drawSmallLabel(g2, physX, physY + physH + 6, physW, preview, KernelTheme.TEXT_MUTED);

            List<Integer> pids = List.copyOf(seg.virtualByPid.keySet());
            int leftX = 30;
            int rightX = w - 170;
            int procY = 50;
            int procW = 140;
            int procH = 52;

            if (pids.isEmpty()) {
                drawBox(g2, w / 2 - 100, 48, 200, 44, KernelTheme.BORDER,
                        "No process attached", "Use shmat() to map segment", "");
            } else {
                for (int i = 0; i < pids.size(); i++) {
                    int pid = pids.get(i);
                    int px = (i % 2 == 0) ? leftX : rightX;
                    int py = procY + (i / 2) * 72;
                    String vaddr = seg.virtualByPid.get(pid);
                    String pname = ProcessRegistry.getInstance().find(pid)
                            .map(ProcessControlBlock::getProcessName).orElse("?");
                    drawBox(g2, px, py, procW, procH, KernelTheme.SECONDARY,
                            "Process P" + pid, pname, "Virtual " + vaddr);
                    drawArrow(g2, px + procW / 2, py + procH, physX + physW / 2, physY, KernelTheme.ACCENT);
                }
            }
        } else {
            drawBox(g2, w / 2 - 110, h / 2 - 30, 220, 60, KernelTheme.BORDER,
                    "No SHM segment yet", "Click shmget to create", "");
        }

        drawLegend(g2, new String[]{
                "One physical block → many virtual mappings (fast IPC)",
                "Linux: shmget() → shmat() → read/write → shmdt()"
        }, 12, h - 36);
    }

    private void paintMmap(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        drawTitle(g2, "Memory-Mapped File — mmap(MAP_SHARED)", 12, 22);

        var files = MemorySharingManager.getInstance().getMappedFiles();
        MemorySharingManager.MappedFile file = files.stream()
                .filter(f -> f.filename.equalsIgnoreCase(highlightFile))
                .findFirst().orElse(files.isEmpty() ? null : files.get(0));

        int diskX = 24;
        int diskY = h / 2 - 35;
        int ramX = w / 2 - 85;
        int ramY = h / 2 - 35;
        int procX = w - 164;
        int procY = h / 2 - 35;
        int boxW = 130;
        int boxH = 70;

        if (file != null) {
            drawBox(g2, diskX, diskY, boxW, boxH, new Color(120, 100, 80),
                    "File (disk)", file.filename, "fd=" + file.fd + " | " + file.fileSize + " B");
            drawBox(g2, ramX, ramY, boxW, boxH, KernelTheme.PRIMARY,
                    "Page in RAM", file.mapAddress, "MAP_SHARED");
            drawArrow(g2, diskX + boxW, diskY + boxH / 2, ramX, ramY + boxH / 2, KernelTheme.TEXT);

            if (file.virtualByPid.isEmpty()) {
                drawBox(g2, procX, procY, boxW, boxH, KernelTheme.BORDER,
                        "Process", "Not mapped", "mmap(...)");
            } else {
                int i = 0;
                for (var e : file.virtualByPid.entrySet()) {
                    int pid = e.getKey();
                    int py = procY + i * 78;
                    drawBox(g2, procX, py, boxW, boxH, KernelTheme.SECONDARY,
                            "Process P" + pid, "Virtual " + e.getValue(), "array-like access");
                    drawArrow(g2, ramX + boxW, ramY + boxH / 2, procX, py + boxH / 2, KernelTheme.ACCENT);
                    i++;
                }
            }
            String preview = file.content.length() > 0
                    ? truncate(file.content.toString().replace('\n', ' '), 32) : "(no writes)";
            drawSmallLabel(g2, ramX, ramY + boxH + 8, boxW + 40, "Content: " + preview, KernelTheme.TEXT_MUTED);
        } else {
            drawBox(g2, w / 2 - 100, h / 2 - 25, 200, 50, KernelTheme.BORDER,
                    "No file mapped", "open + mmap to begin", "");
        }

        drawLegend(g2, new String[]{
                "File ↔ RAM — skip repeated read()/write() syscalls",
                "Linux: open() → mmap(PROT_READ|PROT_WRITE, MAP_SHARED, fd, 0)"
        }, 12, h - 36);
    }

    private void paintSharedLibrary(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        drawTitle(g2, "Shared Library — one .so code segment, many processes", 12, 22);

        var libOpt = MemorySharingManager.getInstance().getLibrary(highlightLib);
        int codeX = w / 2 - 100;
        int codeY = 55;
        int codeW = 200;
        int codeH = 58;

        if (libOpt.isPresent()) {
            MemorySharingManager.SharedLibrary lib = libOpt.get();
            drawBox(g2, codeX, codeY, codeW, codeH, KernelTheme.PRIMARY,
                    lib.name + " CODE", "1 physical copy", lib.physicalCodeAddress);

            if (lib.attachedPids.isEmpty()) {
                drawBox(g2, w / 2 - 90, codeY + codeH + 40, 180, 44, KernelTheme.BORDER,
                        "No process loaded lib", "dlopen / Load Library", "");
            } else {
                int i = 0;
                int cols = Math.min(3, lib.attachedPids.size());
                int startX = w / 2 - (cols * 130) / 2;
                for (int pid : lib.attachedPids) {
                    int col = i % cols;
                    int row = i / cols;
                    int px = startX + col * 138;
                    int py = codeY + codeH + 36 + row * 90;
                    String dataAddr = lib.privateDataByPid.getOrDefault(pid, "private DATA");
                    drawBox(g2, px, py, 124, 48, KernelTheme.SECONDARY,
                            "P" + pid + " (data)", "Private globals", truncate(dataAddr, 22));
                    drawArrow(g2, px + 62, py, codeX + codeW / 2, codeY + codeH, KernelTheme.SUCCESS);
                    i++;
                }
            }
            drawSmallLabel(g2, codeX - 20, codeY + codeH + 12, codeW + 40,
                    lib.attachedPids.size() + " process(es) share this code — each has own data/BSS",
                    KernelTheme.TEXT_MUTED);
        } else {
            drawBox(g2, codeX, codeY, codeW, codeH, KernelTheme.BORDER, highlightLib, "Not loaded", "");
        }

        drawLegend(g2, new String[]{
                "libc.so loaded once in RAM — all processes share read-only code",
                "Global variables / heap per process stay private (copy-on-write for data)"
        }, 12, h - 36);
    }

    private void drawTitle(Graphics2D g2, String text, int x, int y) {
        g2.setColor(KernelTheme.TEXT);
        g2.setFont(KernelTheme.headingFont().deriveFont(Font.BOLD, 14f));
        g2.drawString(text, x, y);
        g2.setFont(KernelTheme.bodyFont());
    }

    private void drawBox(Graphics2D g2, int x, int y, int w, int h, Color border,
            String line1, String line2, String line3) {
        g2.setColor(KernelTheme.CARD);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setColor(KernelTheme.TEXT);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(line1, x + 8, y + 18);
        g2.setColor(KernelTheme.TEXT_MUTED);
        g2.setFont(KernelTheme.smallFont());
        if (line2 != null && !line2.isEmpty()) {
            g2.drawString(line2, x + 8, y + 34);
        }
        if (line3 != null && !line3.isEmpty()) {
            g2.drawString(line3, x + 8, y + 48);
        }
        g2.setFont(KernelTheme.bodyFont());
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int ax = 8;
        g2.drawLine(x2, y2,
                (int) (x2 - ax * Math.cos(angle - 0.4)),
                (int) (y2 - ax * Math.sin(angle - 0.4)));
        g2.drawLine(x2, y2,
                (int) (x2 - ax * Math.cos(angle + 0.4)),
                (int) (y2 - ax * Math.sin(angle + 0.4)));
    }

    private void drawSmallLabel(Graphics2D g2, int x, int y, int maxW, String text, Color c) {
        g2.setColor(c);
        g2.setFont(KernelTheme.smallFont());
        g2.drawString(truncate(text, maxW / 6), x, y + 12);
        g2.setFont(KernelTheme.bodyFont());
    }

    private void drawLegend(Graphics2D g2, String[] lines, int x, int y) {
        g2.setFont(KernelTheme.smallFont());
        g2.setColor(KernelTheme.TEXT_MUTED);
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], x, y + i * 14);
        }
        g2.setFont(KernelTheme.bodyFont());
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
