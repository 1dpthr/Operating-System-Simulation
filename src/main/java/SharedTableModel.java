import javax.swing.table.DefaultTableModel;

/** Shared process table used by PHH2, interrupt, and ProcessRegistry sync. */
public class SharedTableModel extends DefaultTableModel {

    private static SharedTableModel instance;

    private SharedTableModel() {
        super(new Object[][]{}, new String[]{
            "Process ID", "Process Name", "Arrival Time", "Burst Time", "Status", "Priority"
        });
    }

    public static SharedTableModel getInstance() {
        if (instance == null) {
            instance = new SharedTableModel();
        }
        return instance;
    }

    public void reloadFrom(ProcessRegistry registry) {
        setRowCount(0);
        if (registry == null) {
            return;
        }
        for (ProcessControlBlock pcb : registry.getAll()) {
            addRow(pcb.toTableRow());
        }
    }

    public void reloadFromRegistry() {
        reloadFrom(ProcessRegistry.getInstance());
    }
}
