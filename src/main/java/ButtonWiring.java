import java.awt.Component;
import java.awt.Window;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Ensures menu buttons keep a single working action after UI refactors. */
public final class ButtonWiring {

    private ButtonWiring() {
    }

    public static void bind(JButton button, Runnable action) {
        if (button == null || action == null) {
            return;
        }
        for (java.awt.event.ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
        button.addActionListener(e -> action.run());
        button.setEnabled(true);
        button.setFocusable(true);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
    }

    public static void open(JFrame parent, JFrame child) {
        if (child == null) {
            return;
        }
        child.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        if (parent != null && parent.isShowing()) {
            java.awt.Point loc = parent.getLocationOnScreen();
            child.setLocation(loc.x + 28, loc.y + 28);
        } else if (parent != null) {
            child.setLocationRelativeTo(parent);
        }
        child.setVisible(true);
        child.toFront();
        child.requestFocus();
    }

    public static void openFrom(Component parent, JFrame child) {
        Window window = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        open(window instanceof JFrame frame ? frame : null, child);
    }

    /** Opens a new screen from the main menu — no error popup, just open the window. */
    public static void openScreen(Component parent, Supplier<JFrame> factory) {
        Runnable open = () -> openFrom(parent, factory.get());
        if (SwingUtilities.isEventDispatchThread()) {
            open.run();
        } else {
            SwingUtilities.invokeLater(open);
        }
    }

    @FunctionalInterface
    public interface JFrameFactory {
        JFrame create() throws Exception;
    }
}
