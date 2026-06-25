public class Job implements Runnable {
    
    private Printer printer;
	private String jobName;  
        MainSyncGui main;
	public Job(Printer printer, String jobName, MainSyncGui gui) {
		this.printer = printer;
		this.jobName = jobName;
                this.main=gui;
	}
	@Override
	public void run() {
                main.readyQueue.add(jobName);
                main.updateGui();
		printer.print(jobName,main);
	}
}
