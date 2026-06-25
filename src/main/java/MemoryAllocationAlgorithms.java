import java.util.ArrayList;
import java.util.List;

public class MemoryAllocationAlgorithms {
    
    
    public static class MemoryBlock {
        private int blockId;
        private int size;
        private int remainingSize;
        private boolean isAllocated;
        private int allocatedProcessId;
        
        public MemoryBlock(int blockId, int size) {
            this.blockId = blockId;
            this.size = size;
            this.remainingSize = size;
            this.isAllocated = false;
            this.allocatedProcessId = -1;
        }
        
        public int getBlockId() { return blockId; }
        public int getSize() { return size; }
        public int getRemainingSize() { return remainingSize; }
        public boolean isAllocated() { return isAllocated; }
        public int getAllocatedProcessId() { return allocatedProcessId; }
        
        public void allocate(int processId, int processSize) {
            this.isAllocated = true;
            this.allocatedProcessId = processId;
            this.remainingSize -= processSize;
        }
        
        public void reset() {
            this.isAllocated = false;
            this.allocatedProcessId = -1;
            this.remainingSize = this.size;
        }
        
        @Override
        public String toString() {
            return "Block " + blockId + " [Size: " + size + ", Remaining: " + remainingSize + 
                   ", Allocated: " + isAllocated + ", Process: " + 
                   (allocatedProcessId == -1 ? "None" : "P" + allocatedProcessId) + "]";
        }
    }
    
    
    public static class ProcessAllocation {
        private int processId;
        private int processSize;
        private int allocatedBlockId;
        private boolean isAllocated;
        private String allocationStatus;
        
        public ProcessAllocation(int processId, int processSize) {
            this.processId = processId;
            this.processSize = processSize;
            this.allocatedBlockId = -1;
            this.isAllocated = false;
            this.allocationStatus = "Not Allocated";
        }
        
        public int getProcessId() { return processId; }
        public int getProcessSize() { return processSize; }
        public int getAllocatedBlockId() { return allocatedBlockId; }
        public boolean isAllocated() { return isAllocated; }
        public String getAllocationStatus() { return allocationStatus; }
        
        public void allocate(int blockId) {
            this.allocatedBlockId = blockId;
            this.isAllocated = true;
            this.allocationStatus = "Allocated to Block " + blockId;
        }
        
        public void setNotAllocated(String reason) {
            this.allocationStatus = reason;
        }
        
        @Override
        public String toString() {
            return "Process P" + processId + " [Size: " + processSize + ", " + allocationStatus + "]";
        }
    }
    
    
    public static class AllocationResult {
        private List<MemoryBlock> memoryBlocks;
        private List<ProcessAllocation> processAllocations;
        private String algorithmName;
        private int totalAllocated;
        private int totalNotAllocated;
        private double fragmentationPercentage;
        
        public AllocationResult(String algorithmName) {
            this.algorithmName = algorithmName;
            this.memoryBlocks = new ArrayList<>();
            this.processAllocations = new ArrayList<>();
            this.totalAllocated = 0;
            this.totalNotAllocated = 0;
        }
        
        public List<MemoryBlock> getMemoryBlocks() { return memoryBlocks; }
        public List<ProcessAllocation> getProcessAllocations() { return processAllocations; }
        public String getAlgorithmName() { return algorithmName; }
        public int getTotalAllocated() { return totalAllocated; }
        public int getTotalNotAllocated() { return totalNotAllocated; }
        public double getFragmentationPercentage() { return fragmentationPercentage; }
        
        public void addMemoryBlock(MemoryBlock block) {
            memoryBlocks.add(block);
        }
        
        public void addProcessAllocation(ProcessAllocation allocation) {
            processAllocations.add(allocation);
            if (allocation.isAllocated()) {
                totalAllocated++;
            } else {
                totalNotAllocated++;
            }
        }
        
        public void calculateFragmentation() {
            int totalMemory = 0;
            int usedMemory = 0;
            int wastedMemory = 0;
            
            for (MemoryBlock block : memoryBlocks) {
                totalMemory += block.getSize();
                if (block.isAllocated()) {
                    usedMemory += (block.getSize() - block.getRemainingSize());
                    wastedMemory += block.getRemainingSize();
                }
            }
            
            if (totalMemory > 0) {
                fragmentationPercentage = (wastedMemory * 100.0) / totalMemory;
            }
        }
        
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(algorithmName).append(" - Allocation Summary ===\n\n");
            sb.append("Total Processes: ").append(processAllocations.size()).append("\n");
            sb.append("Successfully Allocated: ").append(totalAllocated).append("\n");
            sb.append("Not Allocated: ").append(totalNotAllocated).append("\n");
            sb.append("Fragmentation: ").append(String.format("%.2f", fragmentationPercentage)).append("%\n\n");
            
            sb.append("--- Process Allocation Details ---\n");
            for (ProcessAllocation pa : processAllocations) {
                sb.append(pa.toString()).append("\n");
            }
            
            sb.append("\n--- Memory Block Status ---\n");
            for (MemoryBlock mb : memoryBlocks) {
                sb.append(mb.toString()).append("\n");
            }
            
            return sb.toString();
        }
    }
    
    
    
    public static AllocationResult firstFit(int[] blockSizes, int[] processSizes) {
        int[] ids = new int[processSizes.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i + 1;
        }
        return firstFit(blockSizes, ids, processSizes);
    }

    public static AllocationResult firstFit(int[] blockSizes, int[] processIds, int[] processSizes) {
        AllocationResult result = new AllocationResult("First Fit");
        
        List<MemoryBlock> blocks = new ArrayList<>();
        for (int i = 0; i < blockSizes.length; i++) {
            blocks.add(new MemoryBlock(i, blockSizes[i]));
        }
        
        for (int i = 0; i < processSizes.length; i++) {
            int pid = processIds[i];
            ProcessAllocation allocation = new ProcessAllocation(pid, processSizes[i]);
            boolean allocated = false;
            
            for (MemoryBlock block : blocks) {
                if (block.getRemainingSize() >= processSizes[i]) {
                    block.allocate(pid, processSizes[i]);
                    allocation.allocate(block.getBlockId());
                    allocated = true;
                    break;
                }
            }
            
            if (!allocated) {
                allocation.setNotAllocated("No suitable block found");
            }
            
            result.addProcessAllocation(allocation);
        }
        
        for (MemoryBlock block : blocks) {
            result.addMemoryBlock(block);
        }
        
        result.calculateFragmentation();
        return result;
    }
    
    
    
    public static AllocationResult bestFit(int[] blockSizes, int[] processSizes) {
        int[] ids = new int[processSizes.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i + 1;
        }
        return bestFit(blockSizes, ids, processSizes);
    }

    public static AllocationResult bestFit(int[] blockSizes, int[] processIds, int[] processSizes) {
        AllocationResult result = new AllocationResult("Best Fit");
        
        List<MemoryBlock> blocks = new ArrayList<>();
        for (int i = 0; i < blockSizes.length; i++) {
            blocks.add(new MemoryBlock(i, blockSizes[i]));
        }
        
        for (int i = 0; i < processSizes.length; i++) {
            int pid = processIds[i];
            ProcessAllocation allocation = new ProcessAllocation(pid, processSizes[i]);
            
            int bestIdx = -1;
            int minWaste = Integer.MAX_VALUE;
            
            for (int j = 0; j < blocks.size(); j++) {
                MemoryBlock block = blocks.get(j);
                if (block.getRemainingSize() >= processSizes[i]) {
                    int waste = block.getRemainingSize() - processSizes[i];
                    if (waste < minWaste) {
                        minWaste = waste;
                        bestIdx = j;
                    }
                }
            }
            
            if (bestIdx != -1) {
                MemoryBlock bestBlock = blocks.get(bestIdx);
                bestBlock.allocate(pid, processSizes[i]);
                allocation.allocate(bestBlock.getBlockId());
            } else {
                allocation.setNotAllocated("No suitable block found");
            }
            
            result.addProcessAllocation(allocation);
        }
        
        for (MemoryBlock block : blocks) {
            result.addMemoryBlock(block);
        }
        
        result.calculateFragmentation();
        return result;
    }
    
    
    
    public static AllocationResult worstFit(int[] blockSizes, int[] processSizes) {
        int[] ids = new int[processSizes.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i + 1;
        }
        return worstFit(blockSizes, ids, processSizes);
    }

    public static AllocationResult worstFit(int[] blockSizes, int[] processIds, int[] processSizes) {
        AllocationResult result = new AllocationResult("Worst Fit");
        
        List<MemoryBlock> blocks = new ArrayList<>();
        for (int i = 0; i < blockSizes.length; i++) {
            blocks.add(new MemoryBlock(i, blockSizes[i]));
        }
        
        for (int i = 0; i < processSizes.length; i++) {
            int pid = processIds[i];
            ProcessAllocation allocation = new ProcessAllocation(pid, processSizes[i]);
            
            int worstIdx = -1;
            int maxSize = -1;
            
            for (int j = 0; j < blocks.size(); j++) {
                MemoryBlock block = blocks.get(j);
                if (block.getRemainingSize() >= processSizes[i]) {
                    if (block.getRemainingSize() > maxSize) {
                        maxSize = block.getRemainingSize();
                        worstIdx = j;
                    }
                }
            }
            
            if (worstIdx != -1) {
                MemoryBlock worstBlock = blocks.get(worstIdx);
                worstBlock.allocate(pid, processSizes[i]);
                allocation.allocate(worstBlock.getBlockId());
            } else {
                allocation.setNotAllocated("No suitable block found");
            }
            
            result.addProcessAllocation(allocation);
        }
        
        for (MemoryBlock block : blocks) {
            result.addMemoryBlock(block);
        }
        
        result.calculateFragmentation();
        return result;
    }
    
    
    
    public static String[][] getProcessTableData(AllocationResult result) {
        List<ProcessAllocation> allocations = result.getProcessAllocations();
        String[][] data = new String[allocations.size()][4];
        
        for (int i = 0; i < allocations.size(); i++) {
            ProcessAllocation pa = allocations.get(i);
            data[i][0] = "P" + pa.getProcessId();
            data[i][1] = String.valueOf(pa.getProcessSize());
            data[i][2] = pa.isAllocated() ? "Block " + pa.getAllocatedBlockId() : "Not Allocated";
            data[i][3] = pa.isAllocated() ? "Yes" : "No";
        }
        
        return data;
    }
    
    
    public static String[][] getBlockTableData(AllocationResult result) {
        List<MemoryBlock> blocks = result.getMemoryBlocks();
        String[][] data = new String[blocks.size()][5];
        
        for (int i = 0; i < blocks.size(); i++) {
            MemoryBlock mb = blocks.get(i);
            data[i][0] = "Block " + mb.getBlockId();
            data[i][1] = String.valueOf(mb.getSize());
            data[i][2] = String.valueOf(mb.getRemainingSize());
            data[i][3] = mb.isAllocated() ? "P" + mb.getAllocatedProcessId() : "Free";
            data[i][4] = String.valueOf(mb.getSize() - mb.getRemainingSize());
        }
        
        return data;
    }
    
    
    public static void main(String[] args) {
        int[] blockSizes = {100, 500, 200, 300, 600};
        int[] processSizes = {212, 417, 112, 426};
        
        System.out.println("Memory Blocks: [100, 500, 200, 300, 600]");
        System.out.println("Processes: [212, 417, 112, 426]\n");
        System.out.println("=".repeat(80));
        
        AllocationResult firstFitResult = firstFit(blockSizes, processSizes);
        System.out.println(firstFitResult.getSummary());
        System.out.println("=".repeat(80));
        
        AllocationResult bestFitResult = bestFit(blockSizes, processSizes);
        System.out.println(bestFitResult.getSummary());
        System.out.println("=".repeat(80));
        
        AllocationResult worstFitResult = worstFit(blockSizes, processSizes);
        System.out.println(worstFitResult.getSummary());
        System.out.println("=".repeat(80));
    }
}
