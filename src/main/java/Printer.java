import java.util.concurrent.Semaphore;

class Printer {

    private static final int MAX_PERMIT = 1;
    private final Semaphore semaphore = new Semaphore(MAX_PERMIT, true);
    MainSyncGui main;
    

    public void print(String jobName, MainSyncGui main) {
        this.main = main;
        try {

            semaphore.acquire();

            main.runningProcess = jobName;
            main.readyQueue.poll();
            main.updateGui();

            Thread.sleep(4000);

            main.finishedQueue.add(jobName);
            main.updateGui();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    
    
    

}

}
