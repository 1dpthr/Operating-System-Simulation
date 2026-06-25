import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public final class NavigationHelper {

    private NavigationHelper() {
    }

    /** Close this window and return to the main menu. */
    public static void backToMain(JFrame current) {
        if (current != null) {
            current.dispose();
        }
        new PHH1().setVisible(true);
    }

    /** Close this window (parent screen stays open behind). */
    public static void back(JFrame current) {
        if (current != null) {
            current.dispose();
        }
    }

    public static JButton createBackButton(JFrame owner, Runnable action) {
        JButton btn = new JButton();
        KernelTheme.styleSecondaryButton(btn, "Back");
        UiLayout.normalizeMenuButton(btn);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    public static void addBackBar(JFrame frame, Runnable onBack) {
        if (frame == null) {
            return;
        }
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(6, 12, 10, 12));
        bar.add(createBackButton(frame, onBack));

        var content = frame.getContentPane();
        if (content.getLayout() instanceof BorderLayout) {
            Component south = ((BorderLayout) content.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            if (south == null) {
                content.add(bar, BorderLayout.SOUTH);
            } else {
                JPanel wrap = new JPanel(new BorderLayout());
                wrap.setOpaque(false);
                wrap.add(bar, BorderLayout.NORTH);
                wrap.add(south, BorderLayout.CENTER);
                content.add(wrap, BorderLayout.SOUTH);
            }
        } else {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(KernelTheme.BG);
            root.add(content, BorderLayout.CENTER);
            root.add(bar, BorderLayout.SOUTH);
            frame.setContentPane(root);
        }
        frame.revalidate();
    }
}
