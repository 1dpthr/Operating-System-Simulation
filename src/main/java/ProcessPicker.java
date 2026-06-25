import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public final class ProcessPicker {

    public static final String NONE = "(none)";
    private static final String EMPTY_HINT = "(no processes yet)";

    private static final List<Runnable> refreshCallbacks = new ArrayList<>();

    private ProcessPicker() {
    }

    public static String format(ProcessControlBlock pcb) {
        return "P" + pcb.getProcessId() + " - " + pcb.getProcessName();
    }

    public static JComboBox<String> createCombo(boolean includeNone) {
        JComboBox<String> combo = new JComboBox<>();
        UiLayout.applyComboBoxSize(combo, UiLayout.COMBO_FIELD);
        refresh(combo, includeNone);
        combo.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                refresh(combo, includeNone);
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
            }
        });
        return combo;
    }

    public static void refresh(JComboBox<String> combo, boolean includeNone) {
        if (combo == null) {
            return;
        }
        String previous = combo.getSelectedItem() == null ? null : combo.getSelectedItem().toString();
        combo.removeAllItems();
        if (includeNone) {
            combo.addItem(NONE);
        }
        List<ProcessControlBlock> active = ProcessRegistry.getInstance().getActiveProcesses();
        if (active.isEmpty()) {
            combo.addItem(EMPTY_HINT);
            combo.setEnabled(false);
            return;
        }
        combo.setEnabled(true);
        for (ProcessControlBlock pcb : active) {
            combo.addItem(format(pcb));
        }
        if (previous != null) {
            combo.setSelectedItem(previous);
        }
    }

    public static Optional<Integer> getSelectedPid(JComboBox<String> combo) {
        if (combo == null || !combo.isEnabled()) {
            return Optional.empty();
        }
        Object sel = combo.getSelectedItem();
        if (sel == null) {
            return Optional.empty();
        }
        return parsePid(sel.toString());
    }

    public static Optional<Integer> parsePid(String text) {
        if (text == null || text.isBlank() || NONE.equals(text) || text.startsWith("(")) {
            return Optional.empty();
        }
        if (text.startsWith("P")) {
            int dash = text.indexOf(" - ");
            String pidPart = dash > 0 ? text.substring(1, dash) : text.substring(1);
            try {
                return Optional.of(Integer.parseInt(pidPart.trim()));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        try {
            return Optional.of(Integer.parseInt(text.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> promptSelect(Component parent, String title) {
        JComboBox<String> combo = createCombo(false);
        if (ProcessRegistry.getInstance().getActiveProcesses().isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Pehle Process Management se process create karein.",
                    "No Processes",
                    JOptionPane.WARNING_MESSAGE);
            return Optional.empty();
        }
        int result = JOptionPane.showConfirmDialog(parent, combo, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return Optional.empty();
        }
        return getSelectedPid(combo);
    }

    public static void registerRefreshCallback(Runnable callback) {
        if (callback != null) {
            refreshCallbacks.add(callback);
        }
    }

    public static void notifyRefresh() {
        for (Runnable callback : new ArrayList<>(refreshCallbacks)) {
            callback.run();
        }
    }
}
