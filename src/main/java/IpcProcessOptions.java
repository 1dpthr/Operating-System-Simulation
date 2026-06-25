import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ComboBox;

public final class IpcProcessOptions {

    private static final String EMPTY_HINT = "(no processes — create in Process Management)";

    private IpcProcessOptions() {
    }

    /** Same labels as Process Management dropdowns — all created processes in the registry. */
    public static List<String> loadProcessLabels() {
        List<String> labels = new ArrayList<>();
        for (ProcessControlBlock pcb : ProcessRegistry.getInstance().getAll()) {
            labels.add(ProcessPicker.format(pcb));
        }
        return labels;
    }

    public static String pidFromLabel(String label) {
        Optional<Integer> pid = ProcessPicker.parsePid(label);
        return pid.map(id -> "P" + id).orElse("");
    }

    public static void refreshCombo(ComboBox<String> combo) {
        if (combo == null) {
            return;
        }
        String previous = combo.getValue();
        List<String> labels = loadProcessLabels();
        combo.getItems().setAll(labels);
        if (labels.isEmpty()) {
            combo.getItems().add(EMPTY_HINT);
            combo.setValue(EMPTY_HINT);
            combo.setDisable(true);
            return;
        }
        combo.setDisable(false);
        if (previous != null && combo.getItems().contains(previous)) {
            combo.setValue(previous);
        } else {
            combo.setValue(combo.getItems().get(0));
        }
    }
}
