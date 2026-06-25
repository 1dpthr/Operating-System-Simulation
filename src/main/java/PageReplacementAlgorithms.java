import javax.swing.table.DefaultTableModel;

public final class PageReplacementAlgorithms {

    public enum Algorithm {
        FIFO("FIFO"),
        LRU("LRU"),
        OPTIMAL("Optimal"),
        MRU("MRU");

        private final String label;

        Algorithm(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static final class Result {
        public final DefaultTableModel tableModel;
        public final int length;
        public final int pageFaults;
        public final int pageHits;

        public Result(DefaultTableModel tableModel, int length, int pageFaults, int pageHits) {
            this.tableModel = tableModel;
            this.length = length;
            this.pageFaults = pageFaults;
            this.pageHits = pageHits;
        }
    }

    private PageReplacementAlgorithms() {
    }

    public static Result simulate(int[] references, int frameCount, Algorithm algorithm) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be greater than 0.");
        }
        return switch (algorithm) {
            case FIFO -> simulateFifo(references, frameCount);
            case LRU -> simulateLru(references, frameCount);
            case OPTIMAL -> simulateOptimal(references, frameCount);
            case MRU -> simulateMru(references, frameCount);
        };
    }

    private static DefaultTableModel createTableModel(int frameCount) {
        String[] columnNames = new String[frameCount + 1];
        columnNames[0] = "Page";
        for (int i = 1; i <= frameCount; i++) {
            columnNames[i] = "Frame" + i;
        }
        return new DefaultTableModel(columnNames, 0);
    }

    private static void fillRow(Object[] row, int[] frames, int occupied, int highlightPage) {
        for (int j = 0; j < frames.length; j++) {
            if (j < occupied) {
                row[j + 1] = frames[j] == highlightPage ? "*" + frames[j] : frames[j];
            } else {
                row[j + 1] = "";
            }
        }
    }

    private static Result simulateFifo(int[] refs, int frames) {
        DefaultTableModel model = createTableModel(frames);
        int[] memory = new int[frames];
        int occupied = 0;
        int pointer = 0;
        int hits = 0;
        int faults = 0;

        for (int page : refs) {
            Object[] row = new Object[frames + 1];
            row[0] = page;
            int hitIndex = indexOf(memory, occupied, page);
            if (hitIndex >= 0) {
                hits++;
                fillRow(row, memory, occupied, page);
            } else {
                faults++;
                if (occupied < frames) {
                    memory[occupied++] = page;
                } else {
                    memory[pointer] = page;
                    pointer = (pointer + 1) % frames;
                }
                fillRow(row, memory, occupied, -1);
            }
            model.addRow(row);
        }
        return new Result(model, refs.length, faults, hits);
    }

    private static Result simulateLru(int[] refs, int frames) {
        DefaultTableModel model = createTableModel(frames);
        int[] memory = new int[frames];
        int[] lastUsed = new int[frames];
        int occupied = 0;
        int hits = 0;
        int faults = 0;

        for (int i = 0; i < refs.length; i++) {
            int page = refs[i];
            Object[] row = new Object[frames + 1];
            row[0] = page;
            int hitIndex = indexOf(memory, occupied, page);
            if (hitIndex >= 0) {
                hits++;
                lastUsed[hitIndex] = i;
                fillRow(row, memory, occupied, page);
            } else {
                faults++;
                if (occupied < frames) {
                    memory[occupied] = page;
                    lastUsed[occupied] = i;
                    occupied++;
                } else {
                    int victim = 0;
                    for (int j = 1; j < frames; j++) {
                        if (lastUsed[j] < lastUsed[victim]) {
                            victim = j;
                        }
                    }
                    memory[victim] = page;
                    lastUsed[victim] = i;
                }
                fillRow(row, memory, occupied, -1);
            }
            model.addRow(row);
        }
        return new Result(model, refs.length, faults, hits);
    }

    private static Result simulateMru(int[] refs, int frames) {
        DefaultTableModel model = createTableModel(frames);
        int[] memory = new int[frames];
        int[] lastUsed = new int[frames];
        int occupied = 0;
        int hits = 0;
        int faults = 0;

        for (int i = 0; i < refs.length; i++) {
            int page = refs[i];
            Object[] row = new Object[frames + 1];
            row[0] = page;
            int hitIndex = indexOf(memory, occupied, page);
            if (hitIndex >= 0) {
                hits++;
                lastUsed[hitIndex] = i;
                fillRow(row, memory, occupied, page);
            } else {
                faults++;
                if (occupied < frames) {
                    memory[occupied] = page;
                    lastUsed[occupied] = i;
                    occupied++;
                } else {
                    int victim = 0;
                    for (int j = 1; j < frames; j++) {
                        if (lastUsed[j] > lastUsed[victim]) {
                            victim = j;
                        }
                    }
                    memory[victim] = page;
                    lastUsed[victim] = i;
                }
                fillRow(row, memory, occupied, -1);
            }
            model.addRow(row);
        }
        return new Result(model, refs.length, faults, hits);
    }

    private static Result simulateOptimal(int[] refs, int frames) {
        DefaultTableModel model = createTableModel(frames);
        int[] memory = new int[frames];
        int occupied = 0;
        int hits = 0;
        int faults = 0;

        for (int i = 0; i < refs.length; i++) {
            int page = refs[i];
            Object[] row = new Object[frames + 1];
            row[0] = page;
            int hitIndex = indexOf(memory, occupied, page);
            if (hitIndex >= 0) {
                hits++;
                fillRow(row, memory, occupied, page);
            } else {
                faults++;
                if (occupied < frames) {
                    memory[occupied++] = page;
                } else {
                    int victim = 0;
                    int farthest = nextUseDistance(refs, i + 1, memory[0]);
                    for (int j = 1; j < frames; j++) {
                        int dist = nextUseDistance(refs, i + 1, memory[j]);
                        if (dist > farthest) {
                            farthest = dist;
                            victim = j;
                        }
                    }
                    memory[victim] = page;
                }
                fillRow(row, memory, occupied, -1);
            }
            model.addRow(row);
        }
        return new Result(model, refs.length, faults, hits);
    }

    private static int nextUseDistance(int[] refs, int start, int page) {
        for (int i = start; i < refs.length; i++) {
            if (refs[i] == page) {
                return i - start;
            }
        }
        return Integer.MAX_VALUE;
    }

    private static int indexOf(int[] memory, int occupied, int page) {
        for (int i = 0; i < occupied; i++) {
            if (memory[i] == page) {
                return i;
            }
        }
        return -1;
    }
}
