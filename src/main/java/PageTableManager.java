import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PageTableManager {

    private static PageTableManager instance;
    private final Map<Integer, List<Integer>> pageTables = new LinkedHashMap<>();
    private int nextFrame = 0;

    private PageTableManager() {
    }

    public static synchronized PageTableManager getInstance() {
        if (instance == null) {
            instance = new PageTableManager();
        }
        return instance;
    }

    public int allocatePagesForProcess(int processId, int memoryKb) {
        int pageSizeBytes = KernelConfig.getInstance().getPageSizeBytes();
        int pageCount = Math.max(1, (int) Math.ceil((memoryKb * 1024.0) / pageSizeBytes));
        List<Integer> pages = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            pages.add(nextFrame++);
        }
        pageTables.put(processId, pages);
        return pageCount;
    }

    public void resetAll() {
        pageTables.clear();
        nextFrame = 0;
    }

    public void freeProcess(int processId) {
        pageTables.remove(processId);
    }

    public int getPageCount(int processId) {
        List<Integer> pages = pageTables.get(processId);
        return pages == null ? 0 : pages.size();
    }

    public String getMemoryPointer(int processId) {
        List<Integer> pages = pageTables.get(processId);
        if (pages == null || pages.isEmpty()) {
            return "NULL";
        }
        return "PAGE_TBL@" + processId + " [" + pages.size() + " pg, frames " + pages + "]";
    }

    public String getPageTableSummary(int processId) {
        int bits = KernelConfig.getInstance().getPageSizeBits();
        int count = getPageCount(processId);
        if (count == 0) {
            return "No pages";
        }
        return count + " pages @ 2^" + bits + " B (" + KernelConfig.getInstance().getPageSizeBytes() + " B/page)";
    }
}
